package at.mentor.bouldclockapp.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import at.mentor.bouldclockapp.core.model.SensorKind
import at.mentor.bouldclockapp.core.model.SyncState
import at.mentor.bouldclockapp.data.db.entity.SensorChunkEntity
import java.io.File
import java.util.UUID

/**
 * Schreibt Beschleunigung, Drehrate und Luftdruck waehrend der Session mit.
 *
 * Heute wertet das niemand aus - und genau deshalb passiert es jetzt. Ohne
 * gelabelte Aufzeichnungen aus echten Sessions gibt es spaeter weder die
 * automatische Versuchserkennung noch die Fallhoehe, und nachtraeglich lassen
 * sich Sensordaten nicht herstellen. Jeder Abend ohne Aufzeichnung ist
 * endgueltig weg.
 *
 * Laeuft auf einem eigenen Thread, damit das Schreiben die Oberflaeche nicht
 * bremst, und schreibt gzip-komprimiert je Sensor eine Datei.
 */
class RawSensorRecorder(private val context: Context) {

    private val sensorManager: SensorManager? =
        context.getSystemService(SensorManager::class.java)

    private var thread: HandlerThread? = null
    private var listener: SensorEventListener? = null
    private var recordings: List<Recording> = emptyList()

    /** Zeitpunkt des Systemstarts - Sensorzeitstempel zaehlen ab da. */
    private var bootAtMillis: Long = 0L

    val isRecording: Boolean get() = recordings.isNotEmpty()

    fun start(sessionId: String, startedAtMillis: Long) {
        if (isRecording) return
        val manager = sensorManager ?: return

        bootAtMillis = startedAtMillis - SystemClock.elapsedRealtime()

        sessionDirectory(sessionId).mkdirs()
        recordings = TRACKED.mapNotNull { tracked ->
            val sensor = manager.getDefaultSensor(tracked.androidType) ?: return@mapNotNull null
            val relativePath = SensorChunkEntity.relativePath(sessionId, tracked.kind)
            val file = File(context.filesDir, relativePath)
            Recording(
                tracked = tracked,
                sensor = sensor,
                file = file,
                relativePath = relativePath,
                writer = SensorChunkWriter(
                    sink = file.outputStream(),
                    sensor = tracked.kind,
                    valuesPerSample = tracked.valuesPerSample,
                    startedAtMillis = startedAtMillis,
                ),
                startedAtMillis = startedAtMillis,
            )
        }
        if (recordings.isEmpty()) return

        val handlerThread = HandlerThread("bouldclock-sensors").also { it.start() }
        val handler = Handler(handlerThread.looper)
        val eventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val recording = recordings.firstOrNull { it.sensor.type == event.sensor.type }
                    ?: return
                val timestampMs = bootAtMillis + event.timestamp / 1_000_000L
                val values = FloatArray(recording.tracked.valuesPerSample) {
                    event.values.getOrElse(it) { 0f }
                }
                runCatching { recording.writer.append(timestampMs, values) }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        recordings.forEach { recording ->
            manager.registerListener(
                eventListener,
                recording.sensor,
                recording.tracked.samplingDelay,
                handler,
            )
        }

        thread = handlerThread
        listener = eventListener
    }

    /**
     * Schreibt das Gepufferte aller Sensoren in die Dateien.
     *
     * Begrenzt den Verlust bei einem Absturz auf die Zeit seit dem letzten
     * Aufruf, statt auf die ganze Session.
     */
    fun flush() {
        recordings.forEach { runCatching { it.writer.flush() } }
    }

    /** Beendet die Aufzeichnung und liefert die Metadaten fuer die Datenbank. */
    fun stop(sessionId: String, endedAtMillis: Long): List<SensorChunkEntity> {
        val manager = sensorManager
        listener?.let { manager?.unregisterListener(it) }
        listener = null
        thread?.quitSafely()
        thread = null

        val finished = recordings.map { recording ->
            runCatching { recording.writer.close() }
            SensorChunkEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                sensor = recording.tracked.kind,
                relativePath = recording.relativePath,
                startedAt = recording.startedAtMillis,
                endedAt = endedAtMillis,
                sampleRateHz = recording.tracked.nominalHz,
                sampleCount = recording.writer.sampleCount,
                sizeBytes = recording.file.length(),
                syncState = SyncState.PENDING,
            )
        }
        recordings = emptyList()
        // Leere Dateien sind nur Muell - der Sensor hat nichts geliefert.
        return finished.filter { it.sampleCount > 0 }
    }

    private fun sessionDirectory(sessionId: String) =
        File(File(context.filesDir, SensorChunkEntity.DIR), sessionId)

    private class Recording(
        val tracked: TrackedSensor,
        val sensor: Sensor,
        val file: File,
        val relativePath: String,
        val writer: SensorChunkWriter,
        val startedAtMillis: Long,
    )

    private class TrackedSensor(
        val kind: SensorKind,
        val androidType: Int,
        val valuesPerSample: Int,
        val samplingDelay: Int,
        val nominalHz: Int,
    )

    private companion object {
        /**
         * Beschleunigung und Drehrate bei etwa 50 Hz - fein genug fuer Zuege und
         * Aufschlaege, grob genug, um in zwei Stunden nicht zweistellige
         * Megabyte zu erzeugen. Der Luftdruck aendert sich traege, da reicht
         * deutlich weniger.
         */
        val TRACKED = listOf(
            TrackedSensor(
                SensorKind.ACCELEROMETER,
                Sensor.TYPE_ACCELEROMETER,
                valuesPerSample = 3,
                samplingDelay = SensorManager.SENSOR_DELAY_GAME,
                nominalHz = 50,
            ),
            // Der Gyro lieferte bei gleicher Anforderung 1,7-mal so viele Punkte
            // wie die Beschleunigung (68.293 gegen 39.382 in einer echten
            // Session). Fuers Erkennen von Landungen ist die Beschleunigung das
            // Hauptsignal; der Gyro liefert die Orientierung und darf groeber sein.
            TrackedSensor(
                SensorKind.GYROSCOPE,
                Sensor.TYPE_GYROSCOPE,
                valuesPerSample = 3,
                samplingDelay = SensorManager.SENSOR_DELAY_UI,
                nominalHz = 16,
            ),
            TrackedSensor(
                SensorKind.PRESSURE,
                Sensor.TYPE_PRESSURE,
                valuesPerSample = 1,
                samplingDelay = SensorManager.SENSOR_DELAY_NORMAL,
                nominalHz = 5,
            ),
        )
    }
}

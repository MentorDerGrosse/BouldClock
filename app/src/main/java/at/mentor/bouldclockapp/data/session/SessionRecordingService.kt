package at.mentor.bouldclockapp.data.session

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import at.mentor.bouldclockapp.R
import at.mentor.bouldclockapp.data.db.BouldClockDatabase
import at.mentor.bouldclockapp.data.health.ExerciseRecorder
import at.mentor.bouldclockapp.data.sensor.RawSensorRecorder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Haelt Puls- und Sensoraufzeichnung am Leben, solange eine Session laeuft.
 *
 * Muss ein Vordergrunddienst sein: sobald der Bildschirm ausgeht - und das tut
 * er auf einer Uhr nach Sekunden - wuerde das System einen normalen Dienst
 * einschlaefern und die Messung mitten in der Session abwuergen.
 */
class SessionRecordingService : LifecycleService() {

    private val database by lazy { BouldClockDatabase.get(applicationContext) }
    private val rawRecorder by lazy { RawSensorRecorder(applicationContext) }
    private val exerciseRecorder by lazy {
        ExerciseRecorder(
            context = applicationContext,
            hrSampleDao = database.hrSampleDao(),
            metricSampleDao = database.metricSampleDao(),
            // Anzeigen, sobald die Uhr etwas meldet - nicht erst beim naechsten
            // Wegschreiben. Der Takt der Schleife unten gehoert der Datenbank.
            onLive = ::publishLive,
        )
    }

    private var sessionId: String? = null
    private var startedAtMillis: Long = 0L
    private var pump: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> intent.getStringExtra(EXTRA_SESSION_ID)?.let(::beginRecording)
            ACTION_STOP -> endRecording(intent.getStringExtra(EXTRA_SESSION_ID))
        }
        return START_NOT_STICKY
    }

    private fun beginRecording(sessionId: String) {
        if (this.sessionId == sessionId) return
        this.sessionId = sessionId
        this.startedAtMillis = System.currentTimeMillis()

        startInForeground()
        rawRecorder.start(sessionId, startedAtMillis)

        pump = lifecycleScope.launch {
            exerciseRecorder.start(sessionId)
            while (isActive) {
                delay(FLUSH_INTERVAL_MS)
                rawRecorder.flush()
                exerciseRecorder.flush()
            }
        }
    }

    /** Traegt den aktuellen Stand in die Anzeige. Laeuft im Rueckruf der Uhr. */
    private fun publishLive() {
        LiveMetrics.update(
            bpm = exerciseRecorder.latestBpm,
            heartRate = exerciseRecorder.heartRateState,
            kcal = exerciseRecorder.latestKcal,
            elevationGainMeters = exerciseRecorder.latestElevationGain,
        )
    }

    /**
     * Beendet die Aufzeichnung und meldet, sobald alles geschrieben ist.
     *
     * [requestedId] kommt aus dem Intent und wird auch dann gemeldet, wenn gar
     * nicht aufgezeichnet wurde - sonst wartet der Aufrufer auf ein Signal, das
     * nie kommt.
     */
    private fun endRecording(requestedId: String?) {
        val sessionId = this.sessionId ?: run {
            requestedId?.let { RecordingHandoff.markWritten(it) }
            stopSelf()
            return
        }
        this.sessionId = null
        pump?.cancel()
        pump = null

        val chunks = rawRecorder.stop(sessionId, System.currentTimeMillis())

        // Nicht im lifecycleScope: der stirbt mit stopSelf(), und die letzten
        // Messwerte waeren weg.
        lifecycleScope.launch {
            exerciseRecorder.stop()
            chunks.forEach { database.sensorChunkDao().upsert(it) }
        }.invokeOnCompletion {
            LiveMetrics.clear()
            // Erst jetzt stehen die Dateizeilen - vorher findet die Auswertung
            // den Luftdruckverlauf nicht.
            RecordingHandoff.markWritten(sessionId)
            stopSelf()
        }
    }

    override fun onDestroy() {
        LiveMetrics.clear()
        super.onDestroy()
    }

    private fun startInForeground() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.recording_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.splash_icon)
            .setContentTitle(getString(R.string.recording_notification_title))
            .setOngoing(true)
            .build()

        // Direkt statt ueber ServiceCompat: minSdk ist 30, die Ueberladung mit
        // Diensttyp gibt es seit 29.
        startForeground(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH,
        )
    }

    companion object {
        private const val ACTION_START = "at.mentor.bouldclockapp.START_RECORDING"
        private const val ACTION_STOP = "at.mentor.bouldclockapp.STOP_RECORDING"
        private const val EXTRA_SESSION_ID = "sessionId"
        private const val CHANNEL_ID = "session_recording"
        private const val NOTIFICATION_ID = 1

        /** Wie oft der Puffer in die Datenbank wandert. */
        private const val FLUSH_INTERVAL_MS = 10_000L

        /** Wie lange das Sessionende auf die letzten Zeilen wartet. */
        private const val STOP_TIMEOUT_MS = 5_000L

        fun start(context: Context, sessionId: String) {
            context.startForegroundService(
                Intent(context, SessionRecordingService::class.java).apply {
                    action = ACTION_START
                    putExtra(EXTRA_SESSION_ID, sessionId)
                },
            )
        }

        /**
         * Beendet die Aufzeichnung und wartet, bis alles geschrieben ist.
         *
         * Das Warten ist der Punkt: direkt danach wird die Kletterhoehe aus dem
         * Luftdruckverlauf gerechnet, und der ist erst vollstaendig, wenn der
         * Dienst seine Dateien geschlossen und die Zeilen abgelegt hat.
         *
         * Mit Frist, damit ein haengender Dienst nicht das Sessionende blockiert
         * - dann fehlt eben die Hoehe, so wie vorher immer.
         */
        suspend fun stopAndAwait(context: Context, sessionId: String) {
            RecordingHandoff.expect()
            context.startService(
                Intent(context, SessionRecordingService::class.java).apply {
                    action = ACTION_STOP
                    putExtra(EXTRA_SESSION_ID, sessionId)
                },
            )
            withTimeoutOrNull(STOP_TIMEOUT_MS) {
                RecordingHandoff.writtenSessionId.first { it == sessionId }
            }
        }
    }
}

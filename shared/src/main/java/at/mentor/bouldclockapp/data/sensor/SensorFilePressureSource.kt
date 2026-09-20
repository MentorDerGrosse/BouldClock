package at.mentor.bouldclockapp.data.sensor

import android.content.Context
import at.mentor.bouldclockapp.core.metrics.PressurePoint
import at.mentor.bouldclockapp.core.metrics.PressureTraceSource
import at.mentor.bouldclockapp.core.model.SensorKind
import at.mentor.bouldclockapp.data.db.dao.SensorChunkDao
import java.io.File

/**
 * Liest den Luftdruckverlauf aus der aufgezeichneten Sensordatei.
 *
 * Bei 5 Hz sind das wenige zehn Kilobyte je Session - am Sessionende komplett zu
 * lesen ist billiger als jede Teilabfrage. Die grossen Dateien mit Beschleunigung
 * und Drehrate bleiben unangetastet.
 */
class SensorFilePressureSource(
    private val context: Context,
    private val sensorChunkDao: SensorChunkDao,
) : PressureTraceSource {

    override suspend fun trace(sessionId: String): List<PressurePoint> {
        val chunk = sensorChunkDao.bySession(sessionId)
            .firstOrNull { it.sensor == SensorKind.PRESSURE }
            ?: return emptyList()

        val file = File(context.filesDir, chunk.relativePath)
        if (!file.exists()) return emptyList()

        // Eine unlesbare Datei darf die Zusammenfassung nicht verhindern - dann
        // fehlt eben die Hoehe.
        return runCatching {
            file.inputStream().use { stream ->
                SensorChunkReader.read(stream).samples.mapNotNull { sample ->
                    sample.values.firstOrNull()?.let {
                        PressurePoint(sample.timestampMs, it.toDouble())
                    }
                }
            }
        }.getOrDefault(emptyList())
    }
}

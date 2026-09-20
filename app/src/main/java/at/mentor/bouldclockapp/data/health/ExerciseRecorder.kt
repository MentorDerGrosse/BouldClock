package at.mentor.bouldclockapp.data.health

import android.content.Context
import at.mentor.bouldclockapp.core.diagnostics.Diagnostics
import android.os.SystemClock
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.CumulativeDataPoint
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.HeartRateAccuracy
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.endExercise
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.startExercise
import at.mentor.bouldclockapp.core.model.SessionMetric
import at.mentor.bouldclockapp.data.db.dao.MetricSampleDao
import at.mentor.bouldclockapp.data.db.dao.HrSampleDao
import at.mentor.bouldclockapp.data.db.entity.MetricSampleEntity
import at.mentor.bouldclockapp.data.db.entity.HrSampleEntity
import java.time.Instant
import kotlin.math.roundToInt

/**
 * Zeichnet Puls, Kalorien und Kletterhoehe ueber Health Services auf.
 *
 * Hiess frueher HeartRateRecorder - der Name stimmte schon nicht mehr, als die
 * Kalorien dazukamen.
 *
 * Gepuffert und in Bloecken geschrieben: bei 1 Hz waere ein Datenbankzugriff je
 * Messwert zwei Stunden lang purer Verschleiss.
 *
 * Kalorien und Hoehe werden als Verlauf mitgeschrieben, nicht als Endsumme. Nur
 * so laesst sich spaeter auch der Anteil *an der Wand* ausrechnen - die
 * Differenz zwischen Versuchsbeginn und -ende.
 */
class ExerciseRecorder(
    private val context: Context,
    private val hrSampleDao: HrSampleDao,
    private val metricSampleDao: MetricSampleDao,
) {

    private val exerciseClient = HealthServices.getClient(context).exerciseClient

    private val buffer = Any()
    private val pendingHr = mutableListOf<HrSampleEntity>()
    private val pendingMetrics = mutableListOf<MetricSampleEntity>()

    private var sessionId: String? = null
    private var callback: ExerciseUpdateCallback? = null

    /** Letzter gemeldeter Kalorienstand - fuer die Live-Anzeige. */
    @Volatile
    var latestKcal: Double? = null
        private set

    /** Bisher in dieser Session geklettene Hoehe in Metern - fuer die Live-Anzeige. */
    @Volatile
    var latestElevationGain: Double? = null
        private set

    /** Letzter brauchbarer Pulswert - fuer die Live-Anzeige. */
    @Volatile
    var latestBpm: Int? = null
        private set

    /**
     * Startet die Messung. Gibt `false` zurueck, wenn die Uhr nicht mitspielt
     * oder die Berechtigung fehlt - die Session laeuft dann eben ohne Puls
     * weiter, statt gar nicht.
     */
    suspend fun start(sessionId: String): Boolean {
        if (this.sessionId != null) return true

        val bootAt = Instant.ofEpochMilli(System.currentTimeMillis() - SystemClock.elapsedRealtime())

        return runCatching {
            val capabilities = exerciseClient.getCapabilities()
            val exerciseType = PREFERRED_TYPES.firstOrNull {
                it in capabilities.supportedExerciseTypes
            }
            if (exerciseType == null) {
                Diagnostics.log(context, TAG, "Kein passender Uebungstyp: ${capabilities.supportedExerciseTypes}")
                return false
            }

            val supported = capabilities.getExerciseTypeCapabilities(exerciseType).supportedDataTypes
            val dataTypes = WANTED_DATA_TYPES.filter { it in supported }.toSet()
            Diagnostics.log(context, TAG, "Typ=$exerciseType, moeglich=$dataTypes, unterstuetzt=$supported")
            if (dataTypes.isEmpty()) return false

            val listener = updateCallback(sessionId, bootAt)
            exerciseClient.setUpdateCallback(listener)
            exerciseClient.startExercise(
                ExerciseConfig.Builder(exerciseType)
                    .setDataTypes(dataTypes)
                    // Wir pausieren selbst - eine Automatik, die beim Sitzen
                    // anhaelt, wuerde mitten in jeder Pause zuschlagen.
                    .setIsAutoPauseAndResumeEnabled(false)
                    .setIsGpsEnabled(false)
                    .build(),
            )

            this.sessionId = sessionId
            this.callback = listener
            Diagnostics.log(context, TAG, "Messung gestartet")
            true
        }.onFailure { Diagnostics.log(context, TAG, "Messung nicht gestartet", it) }.getOrDefault(false)
    }

    private fun updateCallback(sessionId: String, bootAt: Instant) = object : ExerciseUpdateCallback {
        override fun onRegistered() {
            Diagnostics.log(context, TAG, "Rueckruf angemeldet")
        }

        override fun onRegistrationFailed(throwable: Throwable) {
            Diagnostics.log(context, TAG, "Rueckruf abgelehnt", throwable)
        }
        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) = Unit
        override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {
            Diagnostics.log(context, TAG, "Verfuegbarkeit $dataType -> $availability")
        }

        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            val metrics = update.latestMetrics

            val heartRates = metrics.getData(DataType.HEART_RATE_BPM).map { point ->
                val status = (point.accuracy as? HeartRateAccuracy)?.sensorStatus?.id ?: 0
                val bpm = point.value.roundToInt()
                if (status >= USABLE_ACCURACY) latestBpm = bpm
                HrSampleEntity(
                    sessionId = sessionId,
                    timestampMs = point.getTimeInstant(bootAt).toEpochMilli(),
                    bpm = bpm,
                    accuracy = status,
                )
            }

            val calories = metrics.getData(DataType.CALORIES_TOTAL)?.also {
                latestKcal = it.total.toDouble()
            }?.toSample(sessionId, SessionMetric.CALORIES)

            val elevation = metrics.getData(DataType.ELEVATION_GAIN_TOTAL)?.also {
                latestElevationGain = it.total.toDouble()
            }?.toSample(sessionId, SessionMetric.ELEVATION_GAIN)

            synchronized(buffer) {
                pendingHr += heartRates
                pendingMetrics += listOfNotNull(calories, elevation)
            }
        }
    }

    /** Schreibt den Puffer weg. Regelmaessig aufrufen, nicht nur am Ende. */
    suspend fun flush() {
        val (heartRates, metricSamples) = synchronized(buffer) {
            val hr = pendingHr.toList()
            val metrics = pendingMetrics.toList()
            pendingHr.clear()
            pendingMetrics.clear()
            hr to metrics
        }
        if (heartRates.isNotEmpty()) hrSampleDao.insertAll(heartRates)
        if (metricSamples.isNotEmpty()) metricSampleDao.insertAll(metricSamples)
    }

    suspend fun stop() {
        if (sessionId == null) return
        runCatching { exerciseClient.endExercise() }
        callback?.let { runCatching { exerciseClient.clearUpdateCallbackAsync(it) } }
        flush()
        sessionId = null
        callback = null
        latestBpm = null
        latestKcal = null
        latestElevationGain = null
    }

    private fun CumulativeDataPoint<Double>.toSample(
        sessionId: String,
        metric: SessionMetric,
    ) = MetricSampleEntity(
        sessionId = sessionId,
        metric = metric,
        timestampMs = end.toEpochMilli(),
        value = total,
    )

    private companion object {
        const val TAG = "BouldClockExercise"

        /** Ab "niedrig" gilt ein Wert als brauchbar, siehe SessionMetrics. */
        const val USABLE_ACCURACY = 3

        /** Bouldern bevorzugt; sonst ein allgemeines Training, damit ueberhaupt gemessen wird. */
        val PREFERRED_TYPES = listOf(ExerciseType.ROCK_CLIMBING, ExerciseType.WORKOUT)

        val WANTED_DATA_TYPES = listOf(
            DataType.HEART_RATE_BPM,
            DataType.CALORIES_TOTAL,
            // Die Uhr liefert die Kletterhoehe direkt - kein Rechnen mit
            // Luftdruckdifferenzen und keine Hallenhoehe noetig.
            DataType.ELEVATION_GAIN_TOTAL,
        )
    }
}

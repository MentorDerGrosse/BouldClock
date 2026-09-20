package at.mentor.bouldclockapp.data.health

import android.content.Context
import at.mentor.bouldclockapp.core.diagnostics.Diagnostics
import android.os.SystemClock
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.Availability
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
import at.mentor.bouldclockapp.data.db.dao.CalorieSampleDao
import at.mentor.bouldclockapp.data.db.dao.HrSampleDao
import at.mentor.bouldclockapp.data.db.entity.CalorieSampleEntity
import at.mentor.bouldclockapp.data.db.entity.HrSampleEntity
import java.time.Instant
import kotlin.math.roundToInt

/**
 * Zeichnet Puls und Kalorienverlauf ueber Health Services auf.
 *
 * Gepuffert und in Bloecken geschrieben: bei 1 Hz waere ein Datenbankzugriff je
 * Messwert zwei Stunden lang purer Verschleiss.
 *
 * Der Kalorienstand wird als Verlauf mitgeschrieben, nicht als Endsumme. Nur so
 * laesst sich spaeter auch der Anteil *an der Wand* ausrechnen - die Differenz
 * zwischen Versuchsbeginn und -ende.
 */
class HeartRateRecorder(
    private val context: Context,
    private val hrSampleDao: HrSampleDao,
    private val calorieSampleDao: CalorieSampleDao,
) {

    private val exerciseClient = HealthServices.getClient(context).exerciseClient

    private val buffer = Any()
    private val pendingHr = mutableListOf<HrSampleEntity>()
    private val pendingCalories = mutableListOf<CalorieSampleEntity>()

    private var sessionId: String? = null
    private var callback: ExerciseUpdateCallback? = null

    /** Letzter gemeldeter Kalorienstand - fuer die Live-Anzeige. */
    @Volatile
    var latestKcal: Double? = null
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

            val calories = metrics.getData(DataType.CALORIES_TOTAL)?.let { point ->
                latestKcal = point.total.toDouble()
                CalorieSampleEntity(
                    sessionId = sessionId,
                    timestampMs = point.end.toEpochMilli(),
                    kcalTotal = point.total.toDouble(),
                )
            }

            synchronized(buffer) {
                pendingHr += heartRates
                calories?.let { pendingCalories += it }
            }
        }
    }

    /** Schreibt den Puffer weg. Regelmaessig aufrufen, nicht nur am Ende. */
    suspend fun flush() {
        val (heartRates, calories) = synchronized(buffer) {
            val hr = pendingHr.toList()
            val kcal = pendingCalories.toList()
            pendingHr.clear()
            pendingCalories.clear()
            hr to kcal
        }
        if (heartRates.isNotEmpty()) hrSampleDao.insertAll(heartRates)
        if (calories.isNotEmpty()) calorieSampleDao.insertAll(calories)
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
    }

    private companion object {
        const val TAG = "BouldClockHR"

        /** Ab "niedrig" gilt ein Wert als brauchbar, siehe SessionMetrics. */
        const val USABLE_ACCURACY = 3

        /** Bouldern bevorzugt; sonst ein allgemeines Training, damit ueberhaupt gemessen wird. */
        val PREFERRED_TYPES = listOf(ExerciseType.ROCK_CLIMBING, ExerciseType.WORKOUT)

        val WANTED_DATA_TYPES = listOf(DataType.HEART_RATE_BPM, DataType.CALORIES_TOTAL)
    }
}

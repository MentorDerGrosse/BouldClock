package at.mentor.bouldclockapp.data.health

import android.content.Context
import at.mentor.bouldclockapp.core.diagnostics.Diagnostics
import android.os.SystemClock
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.CumulativeDataPoint
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.HeartRateAccuracy
import java.util.Locale
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

    /**
     * Wird bei jeder Meldung der Uhr aufgerufen, also etwa sekuendlich.
     *
     * Getrennt vom Wegschreiben: in die Datenbank gehen die Werte gebuendelt
     * alle zehn Sekunden, angezeigt werden muessen sie sofort. Frueher hing
     * beides am selben Takt, und der Puls auf der Uhr sprang nur alle zehn
     * Sekunden - beim Start dauerte der erste Wert entsprechend lang.
     */
    private val onLive: () -> Unit = {},
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

    /**
     * Die beiden Kalorienquellen der Uhr, getrennt mitgezaehlt.
     *
     * Health Services bietet denselben Wert zweimal an: als laufenden
     * Zaehlerstand ([DataType.CALORIES_TOTAL]) und als Einzelmeldungen je
     * Zeitabschnitt ([DataType.CALORIES]). Am 21.09.2026 meldete der
     * Zaehlerstand ueber eine ganze Klettersession 34 Mal **0,0**, waehrend die
     * Verfuegbarkeit durchgehend AVAILABLE war - bei kurzen Sessions mit wachem
     * Bildschirm lieferte er dagegen Werte.
     *
     * Deshalb zaehlen wir beides mit und schreiben beides ins Protokoll. Was
     * gespeichert wird, entscheidet [usableKcal].
     */
    @Volatile
    private var platformKcal: Double = 0.0

    @Volatile
    private var intervalKcal: Double = 0.0

    @Volatile
    private var totalReports: Int = 0

    @Volatile
    private var intervalReports: Int = 0

    /**
     * Zustand und angerechnete Dauer der Uebung.
     *
     * Der Verdacht: Health Services rechnet Kalorien nur an, solange es die
     * Uebung als aktiv zaehlt. Steht die angerechnete Dauer still, waehrend die
     * Session laeuft, erklaert das die Nullen - und dann hilft kein anderer
     * Datentyp, sondern nur ein anderer Umgang mit dem Zustand.
     */
    @Volatile
    private var exerciseState: String? = null

    @Volatile
    private var activeSeconds: Long = 0L

    /** Wie oft eine Dauerangabe ueberhaupt kam - "fehlt" ist etwas anderes als "null". */
    @Volatile
    private var checkpoints: Int = 0

    /** Wie viele Meldungen die Uhr insgesamt geschickt hat. */
    @Volatile
    private var updates: Int = 0

    /** Bisher in dieser Session geklettene Hoehe in Metern - fuer die Live-Anzeige. */
    @Volatile
    var latestElevationGain: Double? = null
        private set

    /** Letzter brauchbarer Pulswert - fuer die Live-Anzeige. */
    @Volatile
    var latestBpm: Int? = null
        private set

    /** Warum gerade kein Puls dasteht - fuer die Live-Anzeige. */
    @Volatile
    var heartRateState: HeartRateState = HeartRateState.STARTING
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
            if (dataType != DataType.HEART_RATE_BPM) return

            heartRateState = when (availability) {
                DataTypeAvailability.AVAILABLE -> HeartRateState.MEASURING
                DataTypeAvailability.ACQUIRING -> HeartRateState.STARTING
                DataTypeAvailability.UNAVAILABLE_DEVICE_OFF_BODY -> HeartRateState.OFF_BODY
                DataTypeAvailability.UNAVAILABLE -> HeartRateState.UNAVAILABLE
                // UNKNOWN und alles Neue: nichts behaupten, beim Alten bleiben.
                else -> heartRateState
            }
            // Ohne Hautkontakt ist der letzte Wert nur noch eine Erinnerung.
            if (heartRateState != HeartRateState.MEASURING) latestBpm = null
            onLive()
        }

        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            noteExerciseState(update)
            val metrics = update.latestMetrics

            val heartRates = metrics.getData(DataType.HEART_RATE_BPM).map { point ->
                val status = (point.accuracy as? HeartRateAccuracy)?.sensorStatus?.id ?: 0
                val bpm = point.value.roundToInt()
                if (status >= USABLE_ACCURACY) {
                    latestBpm = bpm
                    heartRateState = HeartRateState.MEASURING
                }
                HrSampleEntity(
                    sessionId = sessionId,
                    timestampMs = point.getTimeInstant(bootAt).toEpochMilli(),
                    bpm = bpm,
                    accuracy = status,
                )
            }

            metrics.getData(DataType.CALORIES).let { intervals ->
                if (intervals.isNotEmpty()) {
                    intervalKcal += intervals.sumOf { it.value }
                    intervalReports += intervals.size
                }
            }

            val cumulative = metrics.getData(DataType.CALORIES_TOTAL)?.also {
                platformKcal = it.total.toDouble()
                totalReports++
            }
            latestKcal = usableKcal()

            val calories = cumulative?.let {
                MetricSampleEntity(
                    sessionId = sessionId,
                    metric = SessionMetric.CALORIES,
                    timestampMs = it.end.toEpochMilli(),
                    value = usableKcal(),
                )
            }

            val elevation = metrics.getData(DataType.ELEVATION_GAIN_TOTAL)?.also {
                latestElevationGain = it.total.toDouble()
            }?.toSample(sessionId, SessionMetric.ELEVATION_GAIN)

            synchronized(buffer) {
                pendingHr += heartRates
                pendingMetrics += listOfNotNull(calories, elevation)
            }

            // Anzeigen, sobald es da ist. Geschrieben wird weiterhin gebuendelt.
            onLive()
        }
    }

    /** Haelt Zustand und angerechnete Dauer fest; Wechsel landen im Protokoll. */
    private fun noteExerciseState(update: ExerciseUpdate) {
        updates++
        update.activeDurationCheckpoint?.let {
            activeSeconds = it.activeDuration.seconds
            checkpoints++
        }

        val state = update.exerciseStateInfo.toString()
        if (state != exerciseState) {
            exerciseState = state
            Diagnostics.log(context, TAG, "Uebungszustand -> $state")
        }
    }

    /**
     * Der Kalorienwert, den wir speichern.
     *
     * Der Zaehlerstand der Plattform, solange er sich bewegt - sonst die Summe
     * ihrer Einzelmeldungen. Beides sind Zahlen der Uhr, keine gerechneten;
     * welche gegriffen hat, steht im Protokoll.
     */
    private fun usableKcal(): Double =
        platformKcal.takeIf { it > 0.0 } ?: intervalKcal

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

        logCalories()
    }

    /**
     * Schreibt beide Kalorienquellen ins Protokoll.
     *
     * Zum Nachsehen, welche der beiden ueberhaupt zaehlt - siehe [platformKcal].
     * Sobald das geklaert ist, kann diese Zeile wieder raus.
     */
    private fun logCalories() {
        if (sessionId == null) return
        Diagnostics.log(
            context,
            TAG,
            String.format(
                Locale.ROOT,
                "Kalorien: Zaehler=%.2f (%d), Intervallsumme=%.2f (%d), gespeichert=%.2f, " +
                    "angerechnet=%d s (%d Angaben), Meldungen=%d, Zustand=%s",
                platformKcal, totalReports, intervalKcal, intervalReports, usableKcal(),
                activeSeconds, checkpoints, updates, exerciseState,
            ),
        )
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
        heartRateState = HeartRateState.STARTING
        platformKcal = 0.0
        intervalKcal = 0.0
        totalReports = 0
        intervalReports = 0
        exerciseState = null
        activeSeconds = 0L
        checkpoints = 0
        updates = 0
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
            // Dieselbe Groesse als Einzelmeldungen - siehe platformKcal.
            DataType.CALORIES,
            // Die Uhr liefert die Kletterhoehe direkt - kein Rechnen mit
            // Luftdruckdifferenzen und keine Hallenhoehe noetig.
            DataType.ELEVATION_GAIN_TOTAL,
        )
    }
}

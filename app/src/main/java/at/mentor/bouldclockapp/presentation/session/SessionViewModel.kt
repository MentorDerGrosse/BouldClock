package at.mentor.bouldclockapp.presentation.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import at.mentor.bouldclockapp.core.metrics.SessionMetrics
import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.BiologicalSex
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.ProfileRanges
import at.mentor.bouldclockapp.core.model.RestDurations
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.core.session.SessionPhase
import at.mentor.bouldclockapp.data.db.BouldClockDatabase
import at.mentor.bouldclockapp.data.db.entity.RecordMeta
import at.mentor.bouldclockapp.data.sensor.SensorFilePressureSource
import at.mentor.bouldclockapp.data.sync.SessionSyncRepository
import at.mentor.bouldclockapp.data.sync.SessionSyncSender
import at.mentor.bouldclockapp.data.db.entity.UserProfileEntity
import at.mentor.bouldclockapp.data.session.FinishedSession
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import at.mentor.bouldclockapp.core.metrics.RestingHeartRate
import at.mentor.bouldclockapp.data.health.HeartRateMeasurer
import at.mentor.bouldclockapp.data.health.HeartRateState
import kotlinx.coroutines.Job
import kotlinx.coroutines.withTimeoutOrNull
import at.mentor.bouldclockapp.data.session.LiveMetrics
import at.mentor.bouldclockapp.data.session.SessionController
import at.mentor.bouldclockapp.data.session.SessionRecordingService
import at.mentor.bouldclockapp.data.settings.AppSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Was der Bildschirm zeigen soll.
 *
 * [Restoring] ist ein eigener Zustand und nicht einfach "keine Session":
 * beim Start wird erst die Datenbank gelesen. Wer in diesem Moment schon den
 * Startbildschirm sieht und eine Sessionart antippt, legt eine zweite Session
 * an, waehrend die erste noch offen ist.
 */
/**
 * Zustand des Nutzerprofils.
 *
 * [Loading] ist ein eigener Zustand: waere er es nicht, blitzte beim Start die
 * Profilabfrage auf, obwohl laengst ein Profil existiert.
 */
sealed interface ProfileState {
    data object Loading : ProfileState
    data object Missing : ProfileState
    data class Present(val profile: UserProfileEntity) : ProfileState
}

sealed interface SessionUiState {
    data object Restoring : SessionUiState
    data object NoSession : SessionUiState

    /** Pausenlaenge waehlen, bevor eine benutzerdefinierte Session startet. */
    data class ChoosingRest(val restTargetMs: Long) : SessionUiState

    data class Running(val phase: SessionPhase, val type: SessionType) : SessionUiState

    /** Zusammenfassung nach dem Beenden. */
    data class Summary(val finished: FinishedSession) : SessionUiState
}

class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BouldClockDatabase.get(application)
    private val settings = AppSettings(application)
    private val controller = SessionController(
        sessionDao = db.sessionDao(),
        attemptDao = db.attemptDao(),
        hrSampleDao = db.hrSampleDao(),
        metricSampleDao = db.metricSampleDao(),
        summaryDao = db.sessionSummaryDao(),
        userProfileDao = db.userProfileDao(),
        pressureTraceSource = SensorFilePressureSource(application, db.sensorChunkDao()),
        // Bei jeder Gradabfrage frisch gelesen - eine Umstellung greift sofort.
        preferredGradeSystem = { settings.gradeSystem.first() },
    )

    private val profileDao = db.userProfileDao()
    private val measurer = HeartRateMeasurer(application)

    val profileState: StateFlow<ProfileState> = profileDao.observe()
        .map { profile -> profile?.let(ProfileState::Present) ?: ProfileState.Missing }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ProfileState.Loading)

    /** Laufende Messwerte fuer die Live-Seite. Kommen vom Aufzeichnungsdienst. */
    val liveBpm: StateFlow<Int?> = LiveMetrics.bpm
    val liveHeartRateState: StateFlow<HeartRateState> = LiveMetrics.heartRate
    val liveKcal: StateFlow<Double?> = LiveMetrics.kcal
    val liveClimbHeightMeters: StateFlow<Double?> = LiveMetrics.elevationGainMeters

    /** Versuche der laufenden Session - endlich die Verwendung fuer observeBySession. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val liveAttemptCount: StateFlow<Int> = controller.session
        .flatMapLatest { session ->
            if (session == null) {
                flowOf(0)
            } else {
                db.attemptDao().observeBySession(session.id).map { it.size }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    /** Die letzten beendeten Sessions fuer das Fortschritt-Fenster. */
    val recentSummaries: StateFlow<List<SessionSummaryEntity>> =
        db.sessionSummaryDao().observeRecent(RECENT_SESSIONS)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val syncSender = SessionSyncSender(application, SessionSyncRepository(db, application.filesDir))

    private val restored = MutableStateFlow(false)
    private val choosingRest = MutableStateFlow<Long?>(null)
    private val finished = MutableStateFlow<FinishedSession?>(null)

    /** Anzeigeskala der Grade. Einstellbar, bevor eine Session laeuft. */
    val gradeSystem: StateFlow<GradeSystem> = settings.gradeSystem
        .stateIn(viewModelScope, SharingStarted.Eagerly, GradeSystem.FONT)

    val uiState: StateFlow<SessionUiState> = combine(
        restored,
        controller.session,
        controller.phase,
        choosingRest,
        finished,
    ) { restored, session, phase, pendingRest, finishedSession ->
        when {
            !restored -> SessionUiState.Restoring
            finishedSession != null -> SessionUiState.Summary(finishedSession)
            session != null -> SessionUiState.Running(phase, session.type)
            pendingRest != null -> SessionUiState.ChoosingRest(pendingRest)
            else -> SessionUiState.NoSession
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SessionUiState.Restoring)

    private val phase: StateFlow<SessionPhase> = controller.phase

    init {
        viewModelScope.launch {
            // Nach einem Absturz laeuft die Session weiter - dann muss auch die
            // Aufzeichnung wieder anspringen, sonst fehlt der Rest des Abends.
            controller.resumeUnfinished()?.let { startRecording(it.id) }
            restored.value = true

            // Hoehen nachtragen, die frueher wegen der falschen Reihenfolge
            // nie gerechnet wurden. Die Luftdruckdateien liegen noch da.
            controller.backfillAnalysis().forEach { syncSender.sendSession(it) }

            // Beim Start nachholen, was beim letzten Mal nicht durchging - etwa
            // weil das Handy in der Halle nicht in Reichweite war.
            syncSender.syncPending()

            // Das Profil legt meist die Uhr an, bearbeitet wird es am Handy.
            // Ohne diesen Anstoss saehe das Handy es nie und boete beim ersten
            // Oeffnen leere Voreinstellungen an.
            syncSender.sendProfile()
        }

    }

    // --- Ruhepuls ---

    /**
     * Einmal beim ersten Start nachfragen, dann nie wieder von selbst.
     *
     * Der Ruhepuls geht doppelt in die Kalorien ein und ist monatelang gueltig -
     * es lohnt also zu fragen, aber nur einmal. Wer ueberspringt, bekommt eine
     * Schaetzung statt einer Messung, sonst nichts.
     */
    val needsRestingHr: StateFlow<Boolean> = combine(
        profileDao.observe(),
        settings.restingHrAsked,
    ) { profile, asked ->
        profile != null && profile.restingHrBpm == null && !asked
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun skipRestingHr() {
        viewModelScope.launch { settings.markRestingHrAsked() }
    }

    private val _restingHr = MutableStateFlow<RestingHrState>(RestingHrState.Idle)
    val restingHr: StateFlow<RestingHrState> = _restingHr

    private var measuring: Job? = null

    /**
     * Misst den Ruhepuls und legt ihn ins Profil.
     *
     * Zwei Minuten, dann der Median der niedrigsten Werte - siehe
     * [RestingHeartRate]. Waehrenddessen laeuft der optische Sensor durchgehend,
     * deshalb nur auf ausdrueckliche Anforderung und nicht nebenher.
     */
    fun measureRestingHr() {
        if (measuring?.isActive == true) return
        val started = System.currentTimeMillis()
        val samples = mutableListOf<Int>()

        measuring = viewModelScope.launch {
            _restingHr.value = RestingHrState.Measuring(null, HeartRateState.STARTING, RestingHeartRate.MEASURE_SECONDS, 0)

            withTimeoutOrNull(RestingHeartRate.MEASURE_SECONDS * 1000L) {
                measurer.measure().collect { measurement ->
                    measurement.bpm?.let(samples::add)
                    val elapsed = ((System.currentTimeMillis() - started) / 1000L).toInt()
                    _restingHr.value = RestingHrState.Measuring(
                        bpm = measurement.bpm,
                        sensor = measurement.state,
                        remainingSeconds = (RestingHeartRate.MEASURE_SECONDS - elapsed).coerceAtLeast(0),
                        samples = samples.size,
                    )
                }
            }

            val resting = RestingHeartRate.fromSamples(samples)
            if (resting != null) saveRestingHr(resting)
            // Auch ein Fehlschlag zaehlt als gefragt - sonst kommt die Frage
            // bei jedem Start wieder.
            settings.markRestingHrAsked()
            _restingHr.value = RestingHrState.Done(resting)
        }
    }

    fun dismissRestingHr() {
        measuring?.cancel()
        measuring = null
        _restingHr.value = RestingHrState.Idle
    }

    private suspend fun saveRestingHr(bpm: Int) {
        val profile = profileDao.get() ?: return
        val timestamp = System.currentTimeMillis()
        profileDao.upsert(profile.copy(restingHrBpm = bpm, meta = profile.meta.touched(timestamp)))
        syncSender.sendProfile()
    }


    /**
     * Legt das Profil an.
     *
     * Gespeichert wird das Geburtsjahr, eingegeben das Alter - ein Alter in der
     * Datenbank waere naechstes Jahr still falsch.
     */
    fun saveProfile(weightKg: Int, ageYears: Int, sex: BiologicalSex) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            profileDao.upsert(
                UserProfileEntity(
                    weightKg = ProfileRanges.clampWeight(weightKg),
                    birthYear = LocalDate.now().year - ProfileRanges.clampAge(ageYears),
                    sex = sex,
                    meta = RecordMeta.now(now),
                ),
            )
            syncSender.sendProfile()
        }
    }

    /** Schaltet durch die Skalen mit fester Leiter - derzeit Font und V-Scale. */
    fun toggleGradeSystem() {
        viewModelScope.launch {
            val options = GradeSystem.entries.filter { it.hasFixedLadder }
            val next = options[(options.indexOf(gradeSystem.value) + 1).mod(options.size)]
            settings.setGradeSystem(next)
        }
    }

    /**
     * Sessionart gewaehlt. [SessionType.CUSTOM] startet nicht sofort, sondern
     * fragt zuerst die Pausenlaenge ab.
     */
    fun chooseSession(type: SessionType) {
        if (type == SessionType.CUSTOM) {
            viewModelScope.launch {
                choosingRest.value = settings.restTargetMs(type).first()
            }
        } else {
            viewModelScope.launch { startRecording(controller.start(type = type).id) }
        }
    }

    fun setCustomRest(restTargetMs: Long) {
        choosingRest.value = RestDurations.clamp(restTargetMs)
    }

    fun confirmCustomSession() {
        val restTargetMs = choosingRest.value ?: return
        viewModelScope.launch {
            // Merken, damit die naechste eigene Session dort wieder anfaengt.
            settings.setRestTargetMs(SessionType.CUSTOM, restTargetMs)
            startRecording(controller.start(type = SessionType.CUSTOM, restTargetMs = restTargetMs).id)
            choosingRest.value = null
        }
    }

    /** Gradauswahl mitfuehren - geschrieben wird erst beim Bestaetigen. */
    fun previewGrade(gradeValue: Int) = controller.previewGrade(gradeValue)

    /** Winkelauswahl mitfuehren - geschrieben wird erst beim Bestaetigen. */
    fun previewAngle(degrees: Int) = controller.previewAngle(degrees)

    /** Grad bestaetigen und dabei ausdruecklich einen neuen Boulder beginnen. */
    fun confirmGradeAsNewBoulder() {
        viewModelScope.launch { controller.confirmGrade(forceNewBoulder = true) }
    }

    /** Der eine Knopf. */
    fun trigger() {
        viewModelScope.launch { controller.trigger() }
    }

    /** Dieselbe Taste erneut nimmt das Ergebnis wieder zurueck. */
    fun logOutcome(outcome: AttemptOutcome) {
        val resting = phase.value as? SessionPhase.Resting ?: return
        val next = if (resting.loggedOutcome == outcome) null else outcome
        viewModelScope.launch { controller.logOutcome(resting.lastAttemptId, next) }
    }

    /**
     * Beendet die Session.
     *
     * Reihenfolge ist hier alles: **erst** die Aufzeichnung sauber beenden und
     * abwarten, **dann** auswerten. Andersherum sucht die Auswertung den
     * Luftdruckverlauf, den der Dienst noch nicht abgelegt hat - und die
     * Kletterhoehe blieb still leer, bei jeder Session.
     */
    fun finishSession() {
        viewModelScope.launch {
            sessionId()?.let { SessionRecordingService.stopAndAwait(getApplication(), it) }
            finished.value = controller.finish()
            syncSender.syncPending()
        }
    }

    private fun sessionId(): String? = controller.session.value?.id

    private fun startRecording(sessionId: String) {
        SessionRecordingService.start(getApplication(), sessionId)
    }

    fun dismissSummary() {
        finished.value = null
    }

    private companion object {
        /** Kleiner Nachlauf, damit das Sample bei +60 s sicher geschrieben ist. */

        /** So viele Sessions zeigt die Uhr - alles Weitere gehoert aufs Handy. */
        const val RECENT_SESSIONS = 10
    }
}



/** Stand der Ruhepulsmessung. */
sealed interface RestingHrState {
    data object Idle : RestingHrState

    data class Measuring(
        val bpm: Int?,
        val sensor: HeartRateState,
        val remainingSeconds: Int,
        val samples: Int,
    ) : RestingHrState

    /** [bpm] ist null, wenn zu wenige Werte zusammenkamen. */
    data class Done(val bpm: Int?) : RestingHrState
}

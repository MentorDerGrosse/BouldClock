package at.mentor.bouldclockapp.presentation.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import at.mentor.bouldclockapp.core.metrics.SessionMetrics
import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.RestDurations
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.core.session.SessionPhase
import at.mentor.bouldclockapp.data.db.BouldClockDatabase
import at.mentor.bouldclockapp.data.session.FinishedSession
import at.mentor.bouldclockapp.data.session.SessionController
import at.mentor.bouldclockapp.data.settings.AppSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Was der Bildschirm zeigen soll.
 *
 * [Restoring] ist ein eigener Zustand und nicht einfach "keine Session":
 * beim Start wird erst die Datenbank gelesen. Wer in diesem Moment schon den
 * Startbildschirm sieht und eine Sessionart antippt, legt eine zweite Session
 * an, waehrend die erste noch offen ist.
 */
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
        summaryDao = db.sessionSummaryDao(),
        // Bei jeder Gradabfrage frisch gelesen - eine Umstellung greift sofort.
        preferredGradeSystem = { settings.gradeSystem.first() },
    )

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
            controller.resumeUnfinished()
            restored.value = true
        }

        viewModelScope.launch {
            controller.phase.collectLatest { phase ->
                val pending = phase.pendingHrr60() ?: return@collectLatest
                // Frist ab dem Absteigen, nicht ab dem Phasenwechsel: die
                // Gradabfrage liegt dazwischen und darf das Fenster nicht schieben.
                val wait = pending.endedAt + SessionMetrics.HRR_WINDOW_MS +
                    HRR_SETTLE_SLACK_MS - System.currentTimeMillis()
                if (wait > 0) delay(wait)
                controller.settleHrr60(pending.attemptId)
            }
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
            viewModelScope.launch { controller.start(type = type) }
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
            controller.start(type = SessionType.CUSTOM, restTargetMs = restTargetMs)
            choosingRest.value = null
        }
    }

    /** Gradauswahl mitfuehren - geschrieben wird erst beim Bestaetigen. */
    fun previewGrade(gradeValue: Int) = controller.previewGrade(gradeValue)

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

    fun finishSession() {
        viewModelScope.launch { finished.value = controller.finish() }
    }

    fun dismissSummary() {
        finished.value = null
    }

    private companion object {
        /** Kleiner Nachlauf, damit das Sample bei +60 s sicher geschrieben ist. */
        const val HRR_SETTLE_SLACK_MS = 2_000L
    }
}

/** Versuch, dessen HRR60 noch aussteht. */
private data class PendingHrr(val attemptId: String, val endedAt: Long)

private fun SessionPhase.pendingHrr60(): PendingHrr? = when (this) {
    is SessionPhase.Grading -> PendingHrr(attemptId, endedAt)
    is SessionPhase.Resting -> PendingHrr(lastAttemptId, since)
    else -> null
}

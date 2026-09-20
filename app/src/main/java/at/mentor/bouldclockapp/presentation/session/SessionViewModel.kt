package at.mentor.bouldclockapp.presentation.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import at.mentor.bouldclockapp.core.metrics.SessionMetrics
import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.core.session.SessionPhase
import at.mentor.bouldclockapp.data.db.BouldClockDatabase
import at.mentor.bouldclockapp.data.db.entity.SessionEntity
import at.mentor.bouldclockapp.data.session.SessionController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BouldClockDatabase.get(application)
    private val controller = SessionController(
        sessionDao = db.sessionDao(),
        attemptDao = db.attemptDao(),
        hrSampleDao = db.hrSampleDao(),
        summaryDao = db.sessionSummaryDao(),
    )

    val phase: StateFlow<SessionPhase> = controller.phase
    val session: StateFlow<SessionEntity?> = controller.session

    init {
        // Nach einem Absturz dort weitermachen, wo es aufgehoert hat - ohne zu fragen,
        // ob eine Session laeuft. Das steht in der Datenbank.
        viewModelScope.launch { controller.resumeUnfinished() }

        // Traegt HRR60 nach, sobald das Messfenster durch ist. collectLatest bricht
        // das Warten ab, wenn vorher ein neuer Versuch startet - dann uebernimmt
        // der Controller die Berechnung selbst, mit der echten Pausenlaenge.
        viewModelScope.launch {
            controller.phase.collectLatest { phase ->
                if (phase is SessionPhase.Resting) {
                    delay(SessionMetrics.HRR_WINDOW_MS + HRR_SETTLE_SLACK_MS)
                    controller.settleHrr60(phase.lastAttemptId)
                }
            }
        }
    }

    fun startSession(type: SessionType = SessionType.FREE) {
        viewModelScope.launch { controller.start(type = type) }
    }

    /** Der eine Knopf. */
    fun trigger() {
        viewModelScope.launch { controller.trigger() }
    }

    fun logOutcome(outcome: AttemptOutcome) {
        val resting = phase.value as? SessionPhase.Resting ?: return
        viewModelScope.launch { controller.logOutcome(resting.lastAttemptId, outcome) }
    }

    fun finishSession() {
        viewModelScope.launch { controller.finish() }
    }

    private companion object {
        /** Kleiner Nachlauf, damit das Sample bei +60 s sicher geschrieben ist. */
        const val HRR_SETTLE_SLACK_MS = 2_000L
    }
}

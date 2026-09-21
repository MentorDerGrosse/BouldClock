package at.mentor.bouldclockapp.mobile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.BiologicalSex
import at.mentor.bouldclockapp.core.model.ProfileRanges
import at.mentor.bouldclockapp.data.db.BouldClockDatabase
import at.mentor.bouldclockapp.data.db.entity.AttemptEntity
import at.mentor.bouldclockapp.data.db.entity.RecordMeta
import at.mentor.bouldclockapp.data.db.entity.SessionEntity
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import at.mentor.bouldclockapp.data.db.entity.UserProfileEntity
import at.mentor.bouldclockapp.data.sync.SessionSyncRepository
import at.mentor.bouldclockapp.data.sync.SessionSyncSender
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Eine Session mit allem, was der Detailbildschirm braucht. */
data class SessionDetail(
    val session: SessionEntity,
    val summary: SessionSummaryEntity?,
    val attempts: List<AttemptEntity>,
)

/**
 * Die Handy-App.
 *
 * Jede Aenderung wird erst lokal gespeichert und dann zur Uhr geschickt. Geht
 * das Verschicken schief, bleibt die Aenderung trotzdem da - die Session steht
 * dann weiter auf `PENDING` und geht beim naechsten Mal mit.
 */
class MobileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BouldClockDatabase.get(application)
    private val repository = SessionSyncRepository(db)
    private val sender = SessionSyncSender(application, repository)

    val summaries: StateFlow<List<SessionSummaryEntity>> =
        db.sessionSummaryDao().observeRecent(RECENT_LIMIT)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val profile: StateFlow<UserProfileEntity?> = db.userProfileDao().observe()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val openSessionId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val detail: StateFlow<SessionDetail?> = openSessionId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                combine(
                    db.sessionDao().observe(id),
                    db.sessionSummaryDao().observeBySession(id),
                    db.attemptDao().observeBySession(id),
                ) { session, summary, attempts ->
                    session?.let { SessionDetail(it, summary, attempts) }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun openSession(sessionId: String?) {
        openSessionId.value = sessionId
    }

    // --- Bearbeiten ---

    fun setRpe(sessionId: String, rpe: Int?) = edit(sessionId) { session ->
        db.sessionDao().upsert(session.copy(rpe = rpe, meta = session.meta.touched(now())))
    }

    fun setNote(sessionId: String, note: String?) = edit(sessionId) { session ->
        db.sessionDao().upsert(
            session.copy(note = note?.takeIf { it.isNotBlank() }, meta = session.meta.touched(now())),
        )
    }

    fun setAttemptOutcome(sessionId: String, attemptId: String, outcome: AttemptOutcome?) =
        edit(sessionId) {
            db.attemptDao().byId(attemptId)?.let { attempt ->
                db.attemptDao().upsert(
                    attempt.copy(outcome = outcome, meta = attempt.meta.touched(now())),
                )
            }
        }

    fun setAttemptGrade(sessionId: String, attemptId: String, gradeValue: Int?) =
        edit(sessionId) {
            db.attemptDao().byId(attemptId)?.let { attempt ->
                db.attemptDao().upsert(
                    attempt.copy(gradeValue = gradeValue, meta = attempt.meta.touched(now())),
                )
            }
        }

    /** Weich geloescht: ein hart entfernter Versuch kaeme bei der Uhr nie an. */
    fun deleteAttempt(sessionId: String, attemptId: String) = edit(sessionId) {
        db.attemptDao().softDelete(attemptId, now())
    }

    /**
     * Speichert lokal, rechnet die Zusammenfassung neu und schickt die Session
     * zur Uhr. Die Reihenfolge ist wichtig - erst sicher ablegen, dann senden.
     */
    private fun edit(sessionId: String, change: suspend (SessionEntity) -> Unit) {
        viewModelScope.launch {
            val session = db.sessionDao().byId(sessionId) ?: return@launch
            change(session)
            repository.refreshSummary(sessionId)
            sender.sendSession(sessionId)
        }
    }

    // --- Profil ---

    fun saveProfile(weightKg: Int, ageYears: Int, sex: BiologicalSex) {
        viewModelScope.launch {
            val existing = db.userProfileDao().get()
            val timestamp = now()
            db.userProfileDao().upsert(
                UserProfileEntity(
                    weightKg = ProfileRanges.clampWeight(weightKg),
                    birthYear = LocalDate.now().year - ProfileRanges.clampAge(ageYears),
                    sex = sex,
                    meta = existing?.meta?.touched(timestamp) ?: RecordMeta.now(timestamp),
                ),
            )
            sender.sendProfile()
        }
    }

    private fun now() = System.currentTimeMillis()

    private companion object {
        const val RECENT_LIMIT = 200
    }
}

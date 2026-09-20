package at.mentor.bouldclockapp.data.session

import at.mentor.bouldclockapp.core.metrics.AttemptFact
import at.mentor.bouldclockapp.core.metrics.AttemptRun
import at.mentor.bouldclockapp.core.metrics.SessionMetrics
import at.mentor.bouldclockapp.core.metrics.groupRuns
import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.core.model.SessionState
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.core.session.SessionPhase
import at.mentor.bouldclockapp.data.db.buildSessionSummary
import at.mentor.bouldclockapp.data.db.dao.AttemptDao
import at.mentor.bouldclockapp.data.db.dao.HrSampleDao
import at.mentor.bouldclockapp.data.db.dao.SessionDao
import at.mentor.bouldclockapp.data.db.dao.SessionSummaryDao
import at.mentor.bouldclockapp.data.db.entity.AttemptEntity
import at.mentor.bouldclockapp.data.db.entity.RecordMeta
import at.mentor.bouldclockapp.data.db.entity.SessionEntity
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * Die Zustandsmaschine der laufenden Session.
 *
 * Bedient wird sie ueber genau eine Aktion, [trigger]:
 *
 *     Ready --> Climbing --> Resting --> Climbing --> Resting --> ...
 *
 * Aus [SessionPhase.Resting] fuehrt kein automatischer Weg heraus. Wenn die
 * Soll-Pause ablaeuft, passiert hier nichts - das ist Sache der Oberflaeche
 * (vibrieren, Anzeige auf Ueberzeit umstellen). Der naechste Versuch beginnt
 * ausschliesslich durch [trigger].
 *
 * Jeder Uebergang schreibt sofort in die Datenbank. Die Uhr kann jederzeit
 * sterben; was nur im Speicher steht, ist dann weg.
 */
class SessionController(
    private val sessionDao: SessionDao,
    private val attemptDao: AttemptDao,
    private val hrSampleDao: HrSampleDao,
    private val summaryDao: SessionSummaryDao,
    /**
     * Bevorzugte Anzeigeskala. Wird bei jeder Gradabfrage frisch gelesen, damit
     * eine Umstellung sofort greift. Steht bewusst vor [clock], damit der
     * abschliessende Lambda-Parameter die Uhr bleibt.
     */
    private val preferredGradeSystem: suspend () -> GradeSystem = { GradeSystem.FONT },
    private val clock: () -> Long = System::currentTimeMillis,
) {

    private val _session = MutableStateFlow<SessionEntity?>(null)
    val session: StateFlow<SessionEntity?> = _session.asStateFlow()

    private val _phase = MutableStateFlow<SessionPhase>(SessionPhase.Ready)
    val phase: StateFlow<SessionPhase> = _phase.asStateFlow()

    /** Serialisiert die Uebergaenge - ein Knopf, der prellt, darf nichts zerreissen. */
    private val mutex = Mutex()
    private var lastTriggerAt = 0L

    suspend fun start(
        gymId: String? = null,
        type: SessionType = SessionType.FREE,
        restTargetMs: Long = type.defaultRestMs,
    ): SessionEntity = mutex.withLock {
        val now = clock()
        val session = SessionEntity(
            id = UUID.randomUUID().toString(),
            gymId = gymId,
            type = type,
            state = SessionState.ACTIVE,
            startedAt = now,
            restTargetMs = restTargetMs,
            meta = RecordMeta.now(now),
        )
        // Vor dem Anlegen aufraeumen: die neue ID existiert noch nicht, also
        // trifft das jede bestehende offene Session.
        sessionDao.abandonOpenExcept(session.id, now)
        sessionDao.upsert(session)
        _session.value = session
        _phase.value = SessionPhase.Ready
        session
    }

    /**
     * Nimmt eine Session wieder auf, die nie sauber beendet wurde.
     *
     * Die Phase wird aus den Daten rekonstruiert, nicht geraten: laeuft noch ein
     * Versuch, war man an der Wand; sonst pausiert man seit dem Ende des letzten.
     */
    suspend fun resumeUnfinished(): SessionEntity? = mutex.withLock {
        val session = sessionDao.findUnfinished() ?: return@withLock null
        // Nur die neueste offene Session wird fortgesetzt. Alles aeltere, was je
        // offen geblieben ist, wird hier verworfen - sonst kaeme es spaeter
        // nacheinander wieder hoch.
        sessionDao.abandonOpenExcept(session.id, clock())
        _session.value = session
        _phase.value = restorePhase(session)
        session
    }

    private suspend fun restorePhase(session: SessionEntity): SessionPhase {
        attemptDao.running(session.id)?.let { running ->
            return SessionPhase.Climbing(running.id, running.startedAt)
        }
        val last = attemptDao.lastFinished(session.id)
        val endedAt = last?.endedAt
        return if (last != null && endedAt != null) {
            SessionPhase.Resting(endedAt, session.restTargetMs, last.id, last.outcome)
        } else {
            SessionPhase.Ready
        }
    }

    /**
     * Der eine Knopf.
     *
     * Rueckgabe sagt, ob tatsaechlich geschaltet wurde - ein zu schnell
     * wiederholter Druck wird verworfen, statt einen Versuch mit Laenge null
     * anzulegen.
     */
    suspend fun trigger(): Boolean = mutex.withLock {
        val session = _session.value ?: return@withLock false
        val now = clock()
        if (now - lastTriggerAt < MIN_TRIGGER_INTERVAL_MS) return@withLock false
        lastTriggerAt = now

        when (val phase = _phase.value) {
            SessionPhase.Ready, is SessionPhase.Resting -> beginAttempt(session, now)
            is SessionPhase.Climbing -> endAttempt(session, phase, now)
            is SessionPhase.Grading -> applyGrade(session, phase, now)
        }
        true
    }

    private suspend fun beginAttempt(session: SessionEntity, now: Long) {
        // Pause des vorigen Versuchs abschliessen. Erst jetzt steht ihre Laenge
        // fest, und damit auch, ob HRR60 ueberhaupt gueltig sein kann.
        attemptDao.lastFinished(session.id)?.let { previous ->
            val endedAt = previous.endedAt
            if (endedAt != null && previous.restAfterMs == null) {
                attemptDao.upsert(
                    previous.copy(
                        restAfterMs = now - endedAt,
                        meta = previous.meta.touched(now),
                    ),
                )
                settleHrr60(previous.id)
            }
        }

        val attempt = AttemptEntity(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            ordinal = attemptDao.nextOrdinal(session.id),
            startedAt = now,
            meta = RecordMeta.now(now),
        )
        attemptDao.upsert(attempt)
        _phase.value = SessionPhase.Climbing(attempt.id, now)
    }

    private suspend fun endAttempt(session: SessionEntity, phase: SessionPhase.Climbing, now: Long) {
        val attempt = attemptDao.byId(phase.attemptId)

        // Fehlstart: in der Tasche ausgeloest, zu frueh getippt, doch nicht
        // losgeklettert. Spurlos verwerfen und zurueck in den vorigen Zustand -
        // eine laufende Pause laeuft dadurch ungestoert weiter, weil sie ab dem
        // Absteigen des *vorigen* Versuchs zaehlt.
        if (attempt != null && now - attempt.startedAt < MIN_ATTEMPT_MS) {
            attemptDao.delete(attempt.id)
            _phase.value = restorePhase(session)
            return
        }

        if (attempt != null) {
            attemptDao.upsert(
                attempt.copy(
                    endedAt = now,
                    hrEnd = nearestHr(session.id, now),
                    hrAvg = hrSampleDao.avgBetween(
                        session.id, attempt.startedAt, now, SessionMetrics.MIN_HR_ACCURACY,
                    ),
                    hrMax = hrSampleDao.maxBetween(
                        session.id, attempt.startedAt, now, SessionMetrics.MIN_HR_ACCURACY,
                    ),
                    meta = attempt.meta.touched(now),
                ),
            )
        }
        // Weiter zur Gradabfrage, nicht direkt in die Pause: hier steht man noch
        // unter dem Boulder. Das Ergebnis wird spaeter abgefragt - dafuer ist die
        // ganze Pause Zeit.
        // Stufe aus der Vorgeschichte, Skala aus den Einstellungen: die Stufe ist
        // eine Schwierigkeit, die Skala nur ihre Schreibweise.
        _phase.value = SessionPhase.Grading(
            attemptId = phase.attemptId,
            endedAt = now,
            gradeValue = attemptDao.lastGradeValue() ?: Grades.DEFAULT_VALUE,
            gradeSystem = preferredGradeSystem(),
        )
    }

    /**
     * Gradauswahl im Auswahlfenster mitfuehren.
     *
     * Schreibt noch nichts - der Wert wandert erst beim Bestaetigen in die
     * Datenbank. Beim Drehen am Kranz waeren das sonst dutzende Schreibvorgaenge
     * fuer einen einzigen Wert.
     */
    fun previewGrade(gradeValue: Int) {
        _phase.update { current ->
            if (current is SessionPhase.Grading) {
                current.copy(gradeValue = Grades.clamp(gradeValue))
            } else {
                current
            }
        }
    }

    fun previewGradeSystem(gradeSystem: GradeSystem) {
        _phase.update { current ->
            if (current is SessionPhase.Grading) current.copy(gradeSystem = gradeSystem) else current
        }
    }

    /** Grad bestaetigen und in die Pause wechseln. */
    suspend fun confirmGrade() = mutex.withLock {
        val session = _session.value ?: return@withLock
        val grading = _phase.value as? SessionPhase.Grading ?: return@withLock
        applyGrade(session, grading, clock())
    }

    private suspend fun applyGrade(
        session: SessionEntity,
        grading: SessionPhase.Grading,
        now: Long,
    ) {
        attemptDao.byId(grading.attemptId)?.let { attempt ->
            attemptDao.upsert(
                attempt.copy(
                    gradeValue = grading.gradeValue,
                    gradeSystem = grading.gradeSystem,
                    meta = attempt.meta.touched(now),
                ),
            )
        }
        _phase.value = SessionPhase.Resting(
            // Ab dem Absteigen, nicht ab dem Bestaetigen. Die Sekunden in der
            // Gradabfrage sind bereits Erholung und muessen mitzaehlen - sonst
            // weicht die Anzeige von restAfterMs und HRR60 ab.
            since = grading.endedAt,
            targetMs = session.restTargetMs,
            lastAttemptId = grading.attemptId,
        )
    }

    /**
     * Traegt die Herzfrequenz-Erholung nach.
     *
     * Idempotent und aus zwei Richtungen aufgerufen: 60 s nach dem Absteigen
     * durch einen Timer, und erneut beim naechsten [trigger], sobald die
     * tatsaechliche Pausenlaenge feststeht.
     */
    suspend fun settleHrr60(attemptId: String) {
        val attempt = attemptDao.byId(attemptId) ?: return
        val endedAt = attempt.endedAt ?: return
        val hrEnd = attempt.hrEnd ?: nearestHr(attempt.sessionId, endedAt)
        val hrAfter = nearestHr(attempt.sessionId, endedAt + SessionMetrics.HRR_WINDOW_MS)
        attemptDao.upsert(
            attempt.copy(
                hrEnd = hrEnd,
                hrAfter60s = hrAfter,
                hrr60 = SessionMetrics.hrr60(hrEnd, hrAfter, attempt.restAfterMs),
                meta = attempt.meta.touched(clock()),
            ),
        )
    }

    /**
     * Ergebnis nachtragen - waehrend der Pause, ohne den Ausloeser zu blockieren.
     *
     * [outcome] darf `null` sein: damit nimmt man einen Fehlgriff zurueck. Mit
     * Kalk an den Fingern trifft man die falsche Taste, und ein Protokoll, das
     * sich nicht korrigieren laesst, wird stillschweigend unehrlich.
     */
    suspend fun logOutcome(
        attemptId: String,
        outcome: AttemptOutcome?,
        gradeValue: Int? = null,
        gradeSystem: GradeSystem? = null,
        topMoveReached: Int? = null,
    ) = mutex.withLock {
        val attempt = attemptDao.byId(attemptId) ?: return@withLock
        attemptDao.upsert(
            attempt.copy(
                outcome = outcome,
                gradeValue = gradeValue ?: attempt.gradeValue,
                gradeSystem = gradeSystem ?: attempt.gradeSystem,
                topMoveReached = topMoveReached ?: attempt.topMoveReached,
                meta = attempt.meta.touched(clock()),
            ),
        )
        val phase = _phase.value
        if (phase is SessionPhase.Resting && phase.lastAttemptId == attemptId) {
            _phase.value = phase.copy(loggedOutcome = outcome)
        }
    }

    /** Soll-Pause mitten in der Session aendern - wirkt sofort auf die laufende Pause. */
    suspend fun setRestTarget(restTargetMs: Long) = mutex.withLock {
        val session = _session.value ?: return@withLock
        val now = clock()
        val updated = session.copy(restTargetMs = restTargetMs, meta = session.meta.touched(now))
        sessionDao.upsert(updated)
        _session.value = updated
        (_phase.value as? SessionPhase.Resting)?.let { resting ->
            _phase.value = resting.copy(targetMs = restTargetMs)
        }
    }

    /** Beendet die Session und liefert ihre Zusammenfassung. */
    suspend fun finish(): FinishedSession? = mutex.withLock {
        val session = _session.value ?: return@withLock null
        val now = clock()

        // Ein noch laufender Versuch wird beendet, nicht verschluckt.
        (_phase.value as? SessionPhase.Climbing)?.let { endAttempt(session, it, now) }
        (_phase.value as? SessionPhase.Grading)?.let { applyGrade(session, it, now) }
        (_phase.value as? SessionPhase.Resting)?.let { settleHrr60(it.lastAttemptId) }

        val finished = session.copy(
            state = SessionState.FINISHED,
            endedAt = now,
            meta = session.meta.touched(now),
        )
        sessionDao.upsert(finished)

        val summary = buildSessionSummary(
            session = finished,
            aggregate = attemptDao.aggregate(finished.id),
            hrAvg = hrSampleDao.avgBetween(
                finished.id, finished.startedAt, now, SessionMetrics.MIN_HR_ACCURACY,
            ),
            hrMax = hrSampleDao.maxBetween(
                finished.id, finished.startedAt, now, SessionMetrics.MIN_HR_ACCURACY,
            ),
            now = now,
        )
        summaryDao.upsert(summary)

        val runs = groupRuns(
            attemptDao.finishedBySession(finished.id).map { attempt ->
                AttemptFact(
                    gradeValue = attempt.gradeValue,
                    isSend = attempt.outcome?.isSend == true,
                    workMs = (attempt.endedAt ?: attempt.startedAt) - attempt.startedAt,
                    hrMax = attempt.hrMax,
                )
            },
        )

        _session.value = null
        _phase.value = SessionPhase.Ready
        FinishedSession(summary, runs)
    }

    private suspend fun nearestHr(sessionId: String, at: Long): Int? = hrSampleDao.nearest(
        sessionId = sessionId,
        at = at,
        toleranceMs = SessionMetrics.HR_MATCH_TOLERANCE_MS,
        minAccuracy = SessionMetrics.MIN_HR_ACCURACY,
    )

    private companion object {
        /** Prellschutz. Zwei Versuche innerhalb einer halben Sekunde gibt es nicht. */
        const val MIN_TRIGGER_INTERVAL_MS = 400L

        /** Kuerzer als das war kein Versuch, sondern ein Fehlgriff am Bildschirm. */
        const val MIN_ATTEMPT_MS = 3_000L
    }
}

/** Ergebnis einer beendeten Session, wie es der Zusammenfassungsbildschirm braucht. */
data class FinishedSession(
    val summary: SessionSummaryEntity,
    val runs: List<AttemptRun>,
)

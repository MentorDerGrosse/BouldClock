package at.mentor.bouldclockapp.session

import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.core.model.SessionState
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.core.session.RestProgress
import at.mentor.bouldclockapp.core.session.SessionPhase
import at.mentor.bouldclockapp.core.session.TriggerAction
import at.mentor.bouldclockapp.core.session.nextAction
import at.mentor.bouldclockapp.data.db.entity.RecordMeta
import at.mentor.bouldclockapp.data.db.entity.SessionEntity
import at.mentor.bouldclockapp.data.session.SessionController
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionControllerTest {

    private lateinit var sessionDao: FakeSessionDao
    private lateinit var attemptDao: FakeAttemptDao
    private lateinit var hrDao: FakeHrSampleDao
    private lateinit var summaryDao: FakeSummaryDao
    private lateinit var controller: SessionController

    /** Steuerbare Uhr - sonst liesse sich "die Pause ist abgelaufen" nicht pruefen. */
    private var now = 1_000_000L

    private val gradingMs = 2_000L

    @Before
    fun setUp() {
        sessionDao = FakeSessionDao()
        attemptDao = FakeAttemptDao()
        hrDao = FakeHrSampleDao()
        summaryDao = FakeSummaryDao()
        controller = SessionController(sessionDao, attemptDao, hrDao, summaryDao) { now }
    }

    private fun advance(ms: Long) {
        now += ms
    }

    private suspend fun press() = controller.trigger()

    /** Vollstaendiger Versuch: starten, klettern, absteigen, Grad bestaetigen. */
    private suspend fun doAttempt(climbMs: Long = 40_000L) {
        press()
        advance(climbMs)
        press()
        advance(gradingMs)
        press()
    }

    /** Legt eine offene Session an, wie sie auf einem Geraet liegen bleibt. */
    private suspend fun leaveOrphan(id: String, startedAt: Long) {
        sessionDao.upsert(
            SessionEntity(
                id = id,
                type = SessionType.FREE,
                state = SessionState.ACTIVE,
                startedAt = startedAt,
                restTargetMs = SessionType.FREE.defaultRestMs,
                meta = RecordMeta.now(startedAt),
            ),
        )
    }

    @Test
    fun `erster Druck startet den ersten Versuch`() = runTest {
        controller.start(type = SessionType.LIMIT)
        assertEquals(SessionPhase.Ready, controller.phase.value)
        assertEquals(TriggerAction.START_FIRST, controller.phase.value.nextAction)

        advance(5_000)
        press()

        val phase = controller.phase.value
        assertTrue(phase is SessionPhase.Climbing)
        assertEquals(1, attemptDao.attempts.values.single().ordinal)
        assertEquals(TriggerAction.END_ATTEMPT, phase.nextAction)
    }

    @Test
    fun `nach dem Absteigen wird der Grad abgefragt`() = runTest {
        controller.start(type = SessionType.LIMIT)
        advance(5_000); press()
        advance(40_000); press()

        val phase = controller.phase.value
        assertTrue(phase is SessionPhase.Grading)
        assertEquals(TriggerAction.CONFIRM_GRADE, phase.nextAction)
        // Ohne Vorgeschichte der Standardvorschlag.
        assertEquals(Grades.DEFAULT_VALUE, (phase as SessionPhase.Grading).gradeValue)

        val attempt = attemptDao.attempts.values.single()
        assertEquals(40_000L, attempt.endedAt!! - attempt.startedAt)
        // Das Ergebnis wird hier noch nicht abgefragt - dafuer ist die Pause da.
        assertNull(attempt.outcome)
    }

    /**
     * Die Pause zaehlt ab dem Absteigen, nicht ab dem Bestaetigen. Sonst wuerde
     * die Zeit in der Gradabfrage unterschlagen und die Anzeige wiche von
     * restAfterMs und HRR60 ab.
     */
    @Test
    fun `die Pause laeuft ab dem Absteigen`() = runTest {
        controller.start(type = SessionType.LIMIT)
        advance(5_000); press()
        advance(40_000); press()
        val descendedAt = now

        controller.previewGrade(Grades.parse("7A")!!)
        advance(6_000)
        press()

        val resting = controller.phase.value as SessionPhase.Resting
        assertEquals(descendedAt, resting.since)
        assertEquals(6_000L, RestProgress.of(resting, now).elapsedMs)
        assertEquals(Grades.parse("7A"), attemptDao.attempts.values.single().gradeValue)
    }

    @Test
    fun `der zuletzt verwendete Grad wird vorgeschlagen`() = runTest {
        controller.start()
        press()
        advance(30_000); press()
        controller.previewGrade(Grades.parse("6C+")!!)
        advance(gradingMs); press()

        advance(200_000)
        press()
        advance(30_000); press()

        val grading = controller.phase.value as SessionPhase.Grading
        assertEquals(Grades.parse("6C+"), grading.gradeValue)
        assertEquals(GradeSystem.FONT, grading.gradeSystem)
    }

    /**
     * Die Skala kommt aus den Einstellungen, die Stufe aus der Vorgeschichte.
     * Eine Umstellung darf frueher eingetragene Grade nicht umdeuten - dieselbe
     * Stufe, nur anders geschrieben.
     */
    @Test
    fun `die Gradabfrage nutzt die eingestellte Skala`() = runTest {
        val vScale = SessionController(
            sessionDao, attemptDao, hrDao, summaryDao,
            preferredGradeSystem = { GradeSystem.V_SCALE },
        ) { now }
        vScale.start()
        vScale.trigger()
        advance(30_000)
        vScale.trigger()
        vScale.previewGrade(Grades.parse("7A")!!)
        advance(gradingMs)
        vScale.trigger()

        val attempt = attemptDao.attempts.values.single()
        assertEquals(Grades.parse("V6"), attempt.gradeValue)
        assertEquals(GradeSystem.V_SCALE, attempt.gradeSystem)
        // Dieselbe Stufe, in Font geschrieben.
        assertEquals("7A", Grades.label(attempt.gradeValue!!, GradeSystem.FONT))
    }

    /**
     * Der Kern der Anforderung: der Pausen-Timer ist ein Signal, kein Uebergang.
     */
    @Test
    fun `nach Ablauf der Pause bleibt die Phase Resting`() = runTest {
        controller.start(type = SessionType.LIMIT)
        advance(5_000)
        doAttempt()

        val resting = controller.phase.value as SessionPhase.Resting
        advance(SessionType.LIMIT.defaultRestMs + 4 * 60_000L)

        assertEquals(resting, controller.phase.value)
        assertEquals(1, attemptDao.attempts.size)

        val progress = RestProgress.of(resting, now)
        assertTrue(progress.isOvertime)
        assertTrue(progress.remainingMs < 0)
        assertEquals(gradingMs + 4 * 60_000L, progress.overtimeMs)
        assertEquals(1f, progress.fraction)
    }

    @Test
    fun `erst der naechste Druck startet den naechsten Versuch`() = runTest {
        controller.start(type = SessionType.LIMIT)
        advance(5_000)
        doAttempt()
        advance(SessionType.LIMIT.defaultRestMs + 4 * 60_000L)

        press()

        assertTrue(controller.phase.value is SessionPhase.Climbing)
        assertEquals(listOf(1, 2), attemptDao.attempts.values.map { it.ordinal }.sorted())
    }

    @Test
    fun `die tatsaechliche Pausenlaenge wird am vorigen Versuch festgehalten`() = runTest {
        controller.start()
        advance(5_000)
        doAttempt(climbMs = 30_000L)
        advance(220_000)
        press()

        val first = attemptDao.attempts.values.first { it.ordinal == 1 }
        assertEquals(gradingMs + 220_000L, first.restAfterMs)
    }

    @Test
    fun `ein prellender Knopf legt keinen Versuch mit Laenge null an`() = runTest {
        controller.start()
        advance(5_000)

        assertTrue(press())
        advance(50)
        assertFalse(press())

        assertTrue(controller.phase.value is SessionPhase.Climbing)
        assertEquals(1, attemptDao.attempts.size)
    }

    @Test
    fun `HRR60 wird erhoben wenn die Pause lang genug war`() = runTest {
        controller.start()
        advance(5_000); press()
        advance(30_000)
        val sessionId = controller.session.value!!.id
        hrDao.add(sessionId, at = now, bpm = 168)
        press()
        hrDao.add(sessionId, at = now + 60_000L, bpm = 132)
        advance(gradingMs); press()

        advance(180_000)
        press()

        val first = attemptDao.attempts.values.first { it.ordinal == 1 }
        assertEquals(168, first.hrEnd)
        assertEquals(132, first.hrAfter60s)
        assertEquals(36, first.hrr60)
    }

    @Test
    fun `HRR60 faellt weg wenn die naechste Runde zu frueh startet`() = runTest {
        controller.start(type = SessionType.VOLUME)
        advance(5_000); press()
        advance(30_000)
        val sessionId = controller.session.value!!.id
        hrDao.add(sessionId, at = now, bpm = 168)
        press()
        hrDao.add(sessionId, at = now + 60_000L, bpm = 132)
        advance(gradingMs); press()

        advance(40_000)
        press()

        val first = attemptDao.attempts.values.first { it.ordinal == 1 }
        assertEquals(gradingMs + 40_000L, first.restAfterMs)
        assertNull(first.hrr60)
    }

    @Test
    fun `Wiederaufnahme nach Absturz stellt die laufende Pause wieder her`() = runTest {
        controller.start()
        advance(5_000); press()
        advance(30_000); press()
        val descendedAt = now
        advance(gradingMs); press()

        val restarted = SessionController(sessionDao, attemptDao, hrDao, summaryDao) { now }
        advance(90_000)
        assertTrue(restarted.resumeUnfinished() != null)

        val phase = restarted.phase.value
        assertTrue(phase is SessionPhase.Resting)
        assertEquals(descendedAt, (phase as SessionPhase.Resting).since)
        assertTrue(RestProgress.of(phase, now).elapsedMs >= 90_000L)
    }

    @Test
    fun `Wiederaufnahme mitten im Versuch bleibt an der Wand`() = runTest {
        controller.start()
        advance(5_000); press()

        val restarted = SessionController(sessionDao, attemptDao, hrDao, summaryDao) { now }
        advance(20_000)
        restarted.resumeUnfinished()

        assertTrue(restarted.phase.value is SessionPhase.Climbing)
    }

    @Test
    fun `ein protokolliertes Ergebnis erscheint sofort in der Phase`() = runTest {
        controller.start()
        advance(5_000)
        doAttempt(climbMs = 30_000L)

        val resting = controller.phase.value as SessionPhase.Resting
        assertNull(resting.loggedOutcome)

        controller.logOutcome(resting.lastAttemptId, AttemptOutcome.TOP)

        assertEquals(
            AttemptOutcome.TOP,
            (controller.phase.value as SessionPhase.Resting).loggedOutcome,
        )
        assertEquals(AttemptOutcome.TOP, attemptDao.attempts.values.single().outcome)
    }

    @Test
    fun `ein Fehlgriff laesst sich zuruecknehmen`() = runTest {
        controller.start()
        advance(5_000)
        doAttempt(climbMs = 30_000L)
        val attemptId = (controller.phase.value as SessionPhase.Resting).lastAttemptId

        controller.logOutcome(attemptId, AttemptOutcome.FLASH)
        controller.logOutcome(attemptId, null)

        assertNull((controller.phase.value as SessionPhase.Resting).loggedOutcome)
        assertNull(attemptDao.attempts.values.single().outcome)
    }

    @Test
    fun `das Ergebnis ueberlebt einen Absturz`() = runTest {
        controller.start()
        advance(5_000)
        doAttempt(climbMs = 30_000L)
        controller.logOutcome(
            (controller.phase.value as SessionPhase.Resting).lastAttemptId,
            AttemptOutcome.FLASH,
        )

        val restarted = SessionController(sessionDao, attemptDao, hrDao, summaryDao) { now }
        restarted.resumeUnfinished()

        assertEquals(
            AttemptOutcome.FLASH,
            (restarted.phase.value as SessionPhase.Resting).loggedOutcome,
        )
    }

    @Test
    fun `eine neue Session verwirft eine noch offene`() = runTest {
        val first = controller.start()
        advance(5_000); press()
        advance(30_000)

        val second = controller.start(type = SessionType.LIMIT)

        assertEquals(SessionState.ABANDONED, sessionDao.sessions.getValue(first.id).state)
        assertEquals(SessionState.ACTIVE, sessionDao.sessions.getValue(second.id).state)
    }

    @Test
    fun `nach dem Beenden kommt keine alte Session zurueck`() = runTest {
        leaveOrphan("alt", now - 3_600_000L)

        controller.start()
        advance(5_000)
        doAttempt(climbMs = 30_000L)
        advance(10_000)
        controller.finish()

        val restarted = SessionController(sessionDao, attemptDao, hrDao, summaryDao) { now }
        assertNull(restarted.resumeUnfinished())
        assertEquals(SessionPhase.Ready, restarted.phase.value)
        assertEquals(SessionState.ABANDONED, sessionDao.sessions.getValue("alt").state)
    }

    @Test
    fun `die Wiederaufnahme nimmt die neueste offene und verwirft den Rest`() = runTest {
        leaveOrphan("ganz_alt", now - 7_200_000L)
        leaveOrphan("alt", now - 3_600_000L)
        leaveOrphan("neu", now - 60_000L)

        val resumed = controller.resumeUnfinished()

        assertEquals("neu", resumed?.id)
        assertEquals(SessionState.ACTIVE, sessionDao.sessions.getValue("neu").state)
        assertEquals(SessionState.ABANDONED, sessionDao.sessions.getValue("alt").state)
        assertEquals(SessionState.ABANDONED, sessionDao.sessions.getValue("ganz_alt").state)
    }

    /** Beenden mitten in der Gradabfrage darf den Grad nicht verschlucken. */
    @Test
    fun `Beenden aus der Gradabfrage heraus schreibt den Grad noch`() = runTest {
        controller.start()
        advance(5_000); press()
        advance(30_000); press()
        controller.previewGrade(Grades.parse("7B")!!)

        controller.finish()

        val attempt = attemptDao.attempts.values.single()
        assertEquals(Grades.parse("7B"), attempt.gradeValue)
        assertEquals(SessionPhase.Ready, controller.phase.value)
    }

    @Test
    fun `Beenden schliesst einen laufenden Versuch ab und schreibt die Zusammenfassung`() = runTest {
        controller.start()
        advance(5_000); press()
        advance(30_000)
        controller.logOutcome(
            (controller.phase.value as SessionPhase.Climbing).attemptId,
            AttemptOutcome.TOP,
            gradeValue = 11,
        )
        advance(10_000)
        controller.finish()

        val summary = summaryDao.summaries.values.single()
        assertEquals(1, summary.attemptCount)
        assertEquals(1, summary.sendCount)
        assertEquals(40_000L, summary.workMs)
        assertTrue(attemptDao.attempts.values.all { it.endedAt != null })
        assertEquals(SessionPhase.Ready, controller.phase.value)
    }
}

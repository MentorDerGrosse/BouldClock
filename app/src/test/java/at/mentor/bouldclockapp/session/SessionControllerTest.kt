package at.mentor.bouldclockapp.session

import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.core.session.RestProgress
import at.mentor.bouldclockapp.core.session.SessionPhase
import at.mentor.bouldclockapp.core.session.TriggerAction
import at.mentor.bouldclockapp.core.session.nextAction
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
    fun `zweiter Druck beendet den Versuch und startet die Pause`() = runTest {
        controller.start(type = SessionType.LIMIT)
        advance(5_000); press()
        advance(40_000); press()

        val phase = controller.phase.value
        assertTrue(phase is SessionPhase.Resting)
        assertEquals(SessionType.LIMIT.defaultRestMs, (phase as SessionPhase.Resting).targetMs)

        val attempt = attemptDao.attempts.values.single()
        assertEquals(40_000L, attempt.endedAt!! - attempt.startedAt)
        // Das Ergebnis wird bewusst noch nicht gesetzt - dafuer ist die Pause da.
        assertNull(attempt.outcome)
    }

    /**
     * Der Kern der Anforderung: der Pausen-Timer ist ein Signal, kein Uebergang.
     */
    @Test
    fun `nach Ablauf der Pause bleibt die Phase Resting`() = runTest {
        controller.start(type = SessionType.LIMIT)
        advance(5_000); press()
        advance(40_000); press()

        val resting = controller.phase.value as SessionPhase.Resting

        // Weit ueber die Soll-Pause hinaus sitzen bleiben.
        advance(SessionType.LIMIT.defaultRestMs + 4 * 60_000L)

        assertEquals(resting, controller.phase.value)
        assertEquals(1, attemptDao.attempts.size)

        val progress = RestProgress.of(resting, now)
        assertTrue(progress.isOvertime)
        assertTrue(progress.remainingMs < 0)
        assertEquals(4 * 60_000L, progress.overtimeMs)
        assertEquals(1f, progress.fraction)
    }

    @Test
    fun `erst der naechste Druck startet den naechsten Versuch`() = runTest {
        controller.start(type = SessionType.LIMIT)
        advance(5_000); press()
        advance(40_000); press()
        advance(SessionType.LIMIT.defaultRestMs + 4 * 60_000L)

        press()

        assertTrue(controller.phase.value is SessionPhase.Climbing)
        assertEquals(listOf(1, 2), attemptDao.attempts.values.map { it.ordinal }.sorted())
    }

    @Test
    fun `die tatsaechliche Pausenlaenge wird am vorigen Versuch festgehalten`() = runTest {
        controller.start()
        advance(5_000); press()
        advance(30_000); press()
        advance(222_000)
        press()

        val first = attemptDao.attempts.values.first { it.ordinal == 1 }
        assertEquals(222_000L, first.restAfterMs)
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
        hrDao.add(controller.session.value!!.id, at = now, bpm = 168)
        press()
        hrDao.add(controller.session.value!!.id, at = now + 60_000L, bpm = 132)

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
        hrDao.add(controller.session.value!!.id, at = now, bpm = 168)
        press()
        hrDao.add(controller.session.value!!.id, at = now + 60_000L, bpm = 132)

        advance(45_000)
        press()

        val first = attemptDao.attempts.values.first { it.ordinal == 1 }
        assertEquals(45_000L, first.restAfterMs)
        assertNull(first.hrr60)
    }

    @Test
    fun `Wiederaufnahme nach Absturz stellt die laufende Pause wieder her`() = runTest {
        controller.start()
        advance(5_000); press()
        advance(30_000); press()
        val endedAt = now

        // Uhr stirbt, App startet neu.
        val restarted = SessionController(sessionDao, attemptDao, hrDao, summaryDao) { now }
        advance(90_000)
        assertTrue(restarted.resumeUnfinished() != null)

        val phase = restarted.phase.value
        assertTrue(phase is SessionPhase.Resting)
        assertEquals(endedAt, (phase as SessionPhase.Resting).since)
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
        assertEquals(11, summary.hardestSendValue)
        assertEquals(40_000L, summary.workMs)
        assertTrue(attemptDao.attempts.values.all { it.endedAt != null })
        assertEquals(SessionPhase.Ready, controller.phase.value)
    }
}

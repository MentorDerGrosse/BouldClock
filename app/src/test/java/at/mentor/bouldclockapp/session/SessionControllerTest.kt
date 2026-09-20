package at.mentor.bouldclockapp.session

import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.BoardAngles
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.core.model.SessionMetric
import at.mentor.bouldclockapp.core.model.SessionState
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.core.metrics.Barometry
import at.mentor.bouldclockapp.core.metrics.PressurePoint
import at.mentor.bouldclockapp.core.metrics.PressureTraceSource
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
    private lateinit var metricDao: FakeMetricSampleDao
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
        metricDao = FakeMetricSampleDao()
        summaryDao = FakeSummaryDao()
        controller = newController()
    }

    /** Baut einen Controller mit den Doubles - eine Stelle statt sechs. */
    private fun newController(
        pressure: PressureTraceSource = PressureTraceSource.None,
        gradeSystem: GradeSystem = GradeSystem.FONT,
    ) = SessionController(
        sessionDao, attemptDao, hrDao, metricDao, summaryDao,
        pressureTraceSource = pressure,
        preferredGradeSystem = { gradeSystem },
    ) { now }

    /**
     * Luftdruckverlauf, der zu jedem beendeten Versuch einen Aufstieg von
     * [meters] enthaelt - erst am Boden stehen, dann hinauf.
     */
    private fun climbTrace(meters: Double) = PressureTraceSource { sessionId ->
        attemptDao.attempts.values
            .filter { it.sessionId == sessionId && it.endedAt != null }
            .flatMap { attempt ->
                val drop = meters / Barometry.METERS_PER_HPA
                val end = attempt.endedAt!!
                (0 until 40).map { i ->
                    val anteil = ((i - 8).coerceAtLeast(0).toDouble() / 24.0).coerceIn(0.0, 1.0)
                    PressurePoint(
                        timestampMs = attempt.startedAt + (end - attempt.startedAt) * i / 40,
                        hpa = 1005.70 - drop * anteil,
                    )
                }
            }
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
        val vScale = newController(gradeSystem = GradeSystem.V_SCALE)
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

        val restarted = newController()
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

        val restarted = newController()
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

        val restarted = newController()
        restarted.resumeUnfinished()

        assertEquals(
            AttemptOutcome.FLASH,
            (restarted.phase.value as SessionPhase.Resting).loggedOutcome,
        )
    }

    /**
     * In der Tasche ausgeloest oder zu frueh getippt. Solche Versuche sollen
     * spurlos verschwinden statt die Statistik mit Null-Sekunden-Burns zu fuellen.
     */
    @Test
    fun `ein Fehlstart unter drei Sekunden wird verworfen`() = runTest {
        controller.start()
        advance(5_000); press()
        advance(1_000); press()

        assertEquals(SessionPhase.Ready, controller.phase.value)
        assertTrue(attemptDao.attempts.isEmpty())
    }

    @Test
    fun `ein Fehlstart waehrend der Pause laesst den Timer unberuehrt`() = runTest {
        controller.start()
        advance(5_000)
        doAttempt(climbMs = 30_000L)
        val resting = controller.phase.value as SessionPhase.Resting

        advance(20_000); press()
        advance(1_000); press()

        assertEquals(resting, controller.phase.value)
        assertEquals(1, attemptDao.attempts.size)
    }

    /**
     * Gemeldeter Fall: statt des langen Drucks zum Beenden wird kurz getippt,
     * also ein Versuch gestartet. Beim anschliessenden Beenden darf kein leerer
     * Versuch uebrigbleiben.
     */
    @Test
    fun `ein versehentlich gestarteter Versuch verschwindet beim Beenden`() = runTest {
        controller.start()
        advance(5_000)
        doAttempt(climbMs = 30_000L)
        advance(60_000)

        press()
        advance(1_000)
        val finished = controller.finish()!!

        assertEquals(1, attemptDao.attempts.size)
        assertEquals(1, finished.summary.attemptCount)
    }

    @Test
    fun `genau drei Sekunden zaehlen noch als Versuch`() = runTest {
        controller.start()
        advance(5_000); press()
        advance(3_000); press()

        assertTrue(controller.phase.value is SessionPhase.Grading)
        assertEquals(1, attemptDao.attempts.size)
    }

    @Test
    fun `die Zusammenfassung gruppiert die Versuche zu Bouldern`() = runTest {
        controller.start()
        advance(5_000)

        suspend fun burn(grade: String, outcome: AttemptOutcome?) {
            press()
            advance(20_000)
            press()
            controller.previewGrade(Grades.parse(grade)!!)
            advance(gradingMs)
            press()
            outcome?.let {
                controller.logOutcome(
                    (controller.phase.value as SessionPhase.Resting).lastAttemptId, it,
                )
            }
            advance(90_000)
        }

        burn("7A", null)
        burn("7A", AttemptOutcome.TOP)
        burn("6B", AttemptOutcome.FLASH)
        burn("7A", null)

        val finished = controller.finish()!!
        assertEquals(3, finished.runs.size)
        assertEquals(2, finished.runs[0].attempts)
        assertTrue(finished.runs[0].isSent)
        assertTrue(finished.runs[1].isFlash)
        assertFalse(finished.runs[2].isSent)
        assertEquals(4, finished.summary.attemptCount)
        assertEquals(2, finished.summary.sendCount)
    }

    // --- Boulder-Grenze ---

    /** Vollstaendiger Versuch mit ausgewaehltem Grad und Ergebnis. */
    private suspend fun burn(grade: String, outcome: AttemptOutcome?, newBoulder: Boolean = false) {
        press()
        advance(20_000)
        press()
        controller.previewGrade(Grades.parse(grade)!!)
        advance(gradingMs)
        if (newBoulder) controller.confirmGrade(forceNewBoulder = true) else press()
        outcome?.let {
            controller.logOutcome((controller.phase.value as SessionPhase.Resting).lastAttemptId, it)
        }
        advance(90_000)
    }

    private fun attempt(ordinal: Int) = attemptDao.attempts.values.first { it.ordinal == ordinal }

    @Test
    fun `nach einem Top beginnt der naechste Versuch einen neuen Boulder`() = runTest {
        controller.start()
        burn("7A", AttemptOutcome.TOP)
        burn("7A", null)

        assertTrue(attempt(1).startsNewBoulder)
        assertTrue(attempt(2).startsNewBoulder)
    }

    /** Projektieren: Sturz, wieder derselbe Grad - das ist weiterhin derselbe Boulder. */
    @Test
    fun `nach einem Sturz mit gleichem Grad bleibt es derselbe Boulder`() = runTest {
        controller.start()
        burn("7A", AttemptOutcome.FAIL)
        burn("7A", AttemptOutcome.FAIL)
        burn("7A", null)

        assertTrue(attempt(1).startsNewBoulder)
        assertFalse(attempt(2).startsNewBoulder)
        assertFalse(attempt(3).startsNewBoulder)
    }

    @Test
    fun `ein anderer Grad beginnt von selbst einen neuen Boulder`() = runTest {
        controller.start()
        burn("7A", AttemptOutcome.FAIL)
        burn("6B", null)

        assertTrue(attempt(2).startsNewBoulder)
    }

    /** Der mehrdeutige Fall, den der Nutzer entscheidet. */
    @Test
    fun `Neu erzwingt einen neuen Boulder trotz gleichem Grad`() = runTest {
        controller.start()
        burn("7A", AttemptOutcome.FAIL)
        burn("7A", null, newBoulder = true)

        assertTrue(attempt(2).startsNewBoulder)
    }

    @Test
    fun `zwei Boulder mit gleichem Grad erscheinen getrennt in der Zusammenfassung`() = runTest {
        controller.start()
        burn("7A", AttemptOutcome.FAIL)
        burn("7A", AttemptOutcome.TOP)
        burn("7A", AttemptOutcome.FAIL)
        burn("7A", null)

        val finished = controller.finish()!!
        assertEquals(2, finished.runs.size)
        assertEquals(2, finished.runs[0].attempts)
        assertTrue(finished.runs[0].isSent)
        assertEquals(2, finished.runs[1].attempts)
        assertFalse(finished.runs[1].isSent)
    }

    // --- Auswertung ---

    @Test
    fun `die Kletterhoehe wird je Versuch aus dem Luftdruck nachgetragen`() = runTest {
        val messend = newController(pressure = climbTrace(meters = 4.0))
        messend.start()
        advance(5_000)
        messend.trigger(); advance(20_000); messend.trigger()
        advance(gradingMs); messend.trigger()
        advance(90_000)
        messend.trigger(); advance(20_000); messend.trigger()
        advance(gradingMs); messend.trigger()
        advance(10_000)

        val finished = messend.finish()!!

        assertEquals(4.0, attempt(1).climbHeightMeters!!, 0.4)
        assertEquals(8.0, finished.summary.climbHeightMeters!!, 0.8)
        assertEquals(4.0, finished.summary.maxClimbHeightMeters!!, 0.4)
    }

    /** Ohne aufgezeichneten Luftdruck entsteht die Zusammenfassung trotzdem. */
    @Test
    fun `ohne Luftdruckverlauf bleibt die Hoehe leer`() = runTest {
        controller.start()
        advance(5_000)
        doAttempt(climbMs = 20_000L)
        advance(10_000)

        val finished = controller.finish()!!

        assertNull(attempt(1).climbHeightMeters)
        assertNull(finished.summary.climbHeightMeters)
        assertEquals(1, finished.summary.attemptCount)
    }

    /**
     * Der Unterschied, auf den es ankommt: der Gesamtwert enthaelt die Erholung,
     * der Wandanteil nicht.
     */
    @Test
    fun `Kalorien an der Wand zaehlen nur die Zeit an der Wand`() = runTest {
        val sessionId = controller.start().id
        advance(5_000)
        doAttempt(climbMs = 20_000L)
        advance(60_000)
        doAttempt(climbMs = 20_000L)
        advance(30_000)

        val versuche = attemptDao.attempts.values.sortedBy { it.ordinal }
        metricDao.add(sessionId, SessionMetric.CALORIES, versuche[0].startedAt, 10.0)
        metricDao.add(sessionId, SessionMetric.CALORIES, versuche[0].endedAt!!, 14.0)
        metricDao.add(sessionId, SessionMetric.CALORIES, versuche[1].startedAt, 20.0)
        metricDao.add(sessionId, SessionMetric.CALORIES, versuche[1].endedAt!!, 23.0)
        metricDao.add(sessionId, SessionMetric.CALORIES, now, 30.0)

        val finished = controller.finish()!!

        assertEquals(30.0, finished.summary.caloriesTotal!!, 0.01)
        assertEquals(7.0, finished.summary.caloriesOnWall!!, 0.01)
    }

    @Test
    fun `ohne Kalorienmessung bleiben beide Werte leer`() = runTest {
        controller.start()
        advance(5_000)
        doAttempt(climbMs = 20_000L)

        val finished = controller.finish()!!

        assertNull(finished.summary.caloriesTotal)
        assertNull(finished.summary.caloriesOnWall)
    }

    // --- Board ---

    /** Am Board kommt der Winkel vor dem Grad: ohne ihn sagt der Grad nichts aus. */
    @Test
    fun `der Board-Modus fragt vor dem Grad den Winkel ab`() = runTest {
        controller.start(type = SessionType.KILTERBOARD)
        press()
        advance(20_000)
        press()

        val phase = controller.phase.value
        assertTrue(phase is SessionPhase.ChoosingAngle)
        assertEquals(BoardAngles.DEFAULT, (phase as SessionPhase.ChoosingAngle).angleDegrees)

        controller.previewAngle(45)
        advance(gradingMs)
        press()

        assertTrue(controller.phase.value is SessionPhase.Grading)
        assertEquals(45, attemptDao.attempts.values.single().boardAngleDegrees)
    }

    @Test
    fun `der zuletzt eingestellte Winkel wird vorgeschlagen`() = runTest {
        controller.start(type = SessionType.KILTERBOARD)
        press(); advance(20_000); press()
        controller.previewAngle(25)
        advance(gradingMs); press()
        advance(gradingMs); press()
        advance(90_000)

        press(); advance(20_000); press()

        assertEquals(25, (controller.phase.value as SessionPhase.ChoosingAngle).angleDegrees)
    }

    @Test
    fun `Beenden aus der Winkelabfrage heraus schreibt den Winkel noch`() = runTest {
        controller.start(type = SessionType.KILTERBOARD)
        press(); advance(20_000); press()
        controller.previewAngle(50)

        controller.finish()

        assertEquals(50, attemptDao.attempts.values.single().boardAngleDegrees)
        assertEquals(SessionPhase.Ready, controller.phase.value)
    }

    @Test
    fun `der Winkel landet in der Zusammenfassung`() = runTest {
        controller.start(type = SessionType.KILTERBOARD)
        press(); advance(20_000); press()
        controller.previewAngle(35)
        advance(gradingMs); press()
        controller.previewGrade(Grades.parse("7A")!!)
        advance(gradingMs); press()
        advance(10_000)

        val finished = controller.finish()!!
        assertEquals(35, finished.runs.single().boardAngleDegrees)
    }

    // --- Wettkampf ---

    /** Zwei Tipper je Versuch statt drei: keine Gradabfrage dazwischen. */
    @Test
    fun `der Wettkampfmodus ueberspringt die Gradabfrage`() = runTest {
        controller.start(type = SessionType.COMPETITION)
        press()
        advance(30_000)
        press()

        assertTrue(controller.phase.value is SessionPhase.Resting)
        assertNull(attemptDao.attempts.values.single().gradeValue)
    }

    @Test
    fun `im Wettkampf zaehlt Weiterdruecken als Sturz`() = runTest {
        controller.start(type = SessionType.COMPETITION)
        press(); advance(30_000); press()
        advance(60_000); press()

        assertEquals(AttemptOutcome.FAIL, attempt(1).outcome)
    }

    @Test
    fun `im Wettkampf bleibt ein getipptes Ergebnis erhalten`() = runTest {
        controller.start(type = SessionType.COMPETITION)
        press(); advance(30_000); press()
        controller.logOutcome(
            (controller.phase.value as SessionPhase.Resting).lastAttemptId,
            AttemptOutcome.ZONE,
        )
        advance(60_000); press()

        assertEquals(AttemptOutcome.ZONE, attempt(1).outcome)
    }

    @Test
    fun `Beenden traegt im Wettkampf das offene Ergebnis nach`() = runTest {
        controller.start(type = SessionType.COMPETITION)
        press(); advance(30_000); press()
        advance(10_000)
        controller.finish()

        assertEquals(AttemptOutcome.FAIL, attempt(1).outcome)
    }

    /** Im Alltag wird nichts erfunden: ein nicht protokollierter Versuch bleibt offen. */
    @Test
    fun `ausserhalb des Wettkampfs bleibt ein offenes Ergebnis offen`() = runTest {
        controller.start()
        advance(5_000)
        doAttempt(climbMs = 30_000L)
        advance(90_000)
        press()

        assertNull(attempt(1).outcome)
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

        val restarted = newController()
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

package at.mentor.bouldclockapp.core

import at.mentor.bouldclockapp.core.metrics.ReadinessLevel
import at.mentor.bouldclockapp.core.metrics.SessionFact
import at.mentor.bouldclockapp.core.metrics.readiness
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadinessTest {

    private val zone: ZoneId = ZoneId.of("Europe/Vienna")
    private val today: LocalDate = LocalDate.parse("2026-09-23")

    private fun session(date: String, attempts: Int = 20) = SessionFact(
        startedAt = LocalDate.parse(date).atTime(18, 0).atZone(zone).toInstant().toEpochMilli(),
        attemptCount = attempts,
        sendCount = attempts / 3,
        climbHeightMeters = 20.0,
        caloriesTotal = 250.0,
        workMs = 600_000L,
        totalMs = 3_600_000L,
        hardestSendValue = 11,
    )

    @Test
    fun `ohne Sessions gibt es kein Urteil`() {
        assertEquals(ReadinessLevel.UNKNOWN, readiness(emptyList(), null, today, zone).level)
    }

    @Test
    fun `heute schon geklettert heisst locker machen`() {
        val result = readiness(listOf(session("2026-09-23")), null, today, zone)
        assertEquals(ReadinessLevel.TIRED, result.level)
        assertTrue(result.reasons.any { it.contains("Heute schon") })
    }

    @Test
    fun `drei Tage Pause ist ein guter Tag`() {
        val result = readiness(listOf(session("2026-09-20")), null, today, zone)
        assertEquals(ReadinessLevel.RESTED, result.level)
    }

    /** Viel Volumen in der Woche zieht die Einschaetzung herunter. */
    @Test
    fun `eine harte Woche daempft`() {
        val viel = listOf(
            session("2026-09-17", attempts = 20),
            session("2026-09-18", attempts = 60),
            session("2026-09-20", attempts = 60),
        ) + (1..8).map { session("2026-0${if (it < 7) 8 else 9}-0$it", attempts = 15) }

        val result = readiness(viel, lastRpe = null, today = today, zone = zone)
        assertTrue(result.reasons.any { it.contains("über deinem Schnitt") })
    }

    @Test
    fun `eine sehr harte Session gestern zaehlt nach`() {
        val result = readiness(listOf(session("2026-09-22")), lastRpe = 9, today = today, zone = zone)
        assertTrue(result.reasons.any { it.contains("RPE 9") })
        assertEquals(ReadinessLevel.TIRED, result.level)
    }

    /** Die Begruendung ist der Inhalt - ohne sie waere es ein Orakel. */
    @Test
    fun `es gibt immer eine Begruendung`() {
        listOf(emptyList(), listOf(session("2026-09-20")), listOf(session("2026-09-23")))
            .forEach { facts ->
                assertTrue(readiness(facts, null, today, zone).reasons.isNotEmpty())
            }
    }
}

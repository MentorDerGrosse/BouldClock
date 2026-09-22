package at.mentor.bouldclockapp.core

import at.mentor.bouldclockapp.core.metrics.SessionMetrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionMetricsTest {

    @Test
    fun `hrr60 ist die Differenz ueber das Fenster`() {
        assertEquals(32, SessionMetrics.hrr60(hrEnd = 168, hrAfter60s = 136, restAfterMs = 180_000))
    }

    @Test
    fun `hrr60 gilt nicht wenn der naechste Versuch zu frueh startet`() {
        assertNull(SessionMetrics.hrr60(hrEnd = 168, hrAfter60s = 136, restAfterMs = 45_000))
    }

    @Test
    fun `hrr60 gilt bei offener Pause ohne Folgeversuch`() {
        assertEquals(20, SessionMetrics.hrr60(hrEnd = 160, hrAfter60s = 140, restAfterMs = null))
    }

    @Test
    fun `hrr60 ohne Pulswerte ist null`() {
        assertNull(SessionMetrics.hrr60(hrEnd = null, hrAfter60s = 140, restAfterMs = null))
        assertNull(SessionMetrics.hrr60(hrEnd = 160, hrAfter60s = null, restAfterMs = null))
    }

    @Test
    fun `timeSplit rechnet die Pausenzeit als Rest`() {
        // 2 h Session, 11 min an der Wand, 5 min Auto-Pause.
        val split = SessionMetrics.timeSplit(
            totalMs = 2 * 60 * 60_000L,
            workMs = 11 * 60_000L,
            pausedMs = 5 * 60_000L,
        )
        assertEquals(104 * 60_000L, split.restMs)
        assertTrue(split.restPerWork!! > 9.0)
    }

    @Test
    fun `timeSplit erzeugt keine negativen Dauern`() {
        val split = SessionMetrics.timeSplit(totalMs = 1_000L, workMs = 5_000L, pausedMs = 5_000L)
        assertEquals(1_000L, split.workMs)
        assertEquals(0L, split.pausedMs)
        assertEquals(0L, split.restMs)
    }
}

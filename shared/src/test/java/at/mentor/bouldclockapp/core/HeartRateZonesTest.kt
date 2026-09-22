package at.mentor.bouldclockapp.core

import at.mentor.bouldclockapp.core.metrics.HeartBeat
import at.mentor.bouldclockapp.core.metrics.HeartRateZone
import at.mentor.bouldclockapp.core.metrics.lowerBpm
import at.mentor.bouldclockapp.core.metrics.timeInZones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HeartRateZonesTest {

    private val resting = 60
    private val max = 190   // Pulsreserve 130

    @Test
    fun `Zonen folgen der Pulsreserve`() {
        assertEquals(HeartRateZone.REST, HeartRateZone.of(100, resting, max))      // 31 %
        assertEquals(HeartRateZone.RECOVERY, HeartRateZone.of(130, resting, max))  // 54 %
        assertEquals(HeartRateZone.BASE, HeartRateZone.of(142, resting, max))      // 63 %
        assertEquals(HeartRateZone.TEMPO, HeartRateZone.of(155, resting, max))     // 73 %
        assertEquals(HeartRateZone.THRESHOLD, HeartRateZone.of(168, resting, max)) // 83 %
        assertEquals(HeartRateZone.MAXIMAL, HeartRateZone.of(185, resting, max))   // 96 %
    }

    @Test
    fun `die Grenze in Schlaegen laesst sich beschriften`() {
        assertEquals(125, HeartRateZone.RECOVERY.lowerBpm(resting, max))
        assertEquals(177, HeartRateZone.MAXIMAL.lowerBpm(resting, max))
    }

    @Test
    fun `ohne sinnvolle Spanne faellt alles in Ruhe`() {
        assertEquals(HeartRateZone.REST, HeartRateZone.of(150, 190, 190))
    }

    @Test
    fun `Zeit wird gemessen, nicht gezaehlt`() {
        val t0 = 1_000_000L
        val beats = (0..59).map { HeartBeat(t0 + it * 1000L, 155) }   // eine Minute Tempo
        val zones = timeInZones(beats, resting, max)

        assertEquals(59_000L, zones.getValue(HeartRateZone.TEMPO))
        assertEquals(setOf(HeartRateZone.TEMPO), zones.keys)
    }

    /** Eine Aufzeichnungsluecke darf nicht als Belastung zaehlen. */
    @Test
    fun `Luecken werden gekappt`() {
        val t0 = 1_000_000L
        val beats = listOf(HeartBeat(t0, 155), HeartBeat(t0 + 600_000L, 155))
        val zones = timeInZones(beats, resting, max)

        assertTrue(zones.getValue(HeartRateZone.TEMPO) <= 5_000L)
    }
}

package at.mentor.bouldclockapp.core

import at.mentor.bouldclockapp.core.model.Landmarks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LandmarksTest {

    @Test
    fun `unter dem kleinsten Bauwerk gibt es noch keinen Vergleich`() {
        val comparison = Landmarks.compare(2.0)
        assertNull(comparison.reached)
        assertEquals("Kletterwand", comparison.next?.name)
        assertEquals(2.5, comparison.missingMeters, 0.001)
    }

    @Test
    fun `das groesste passende Bauwerk gewinnt`() {
        // 300 m: der Donauturm passt, der Eiffelturm noch nicht.
        val comparison = Landmarks.compare(300.0)
        assertEquals("Donauturm", comparison.reached?.name)
        assertEquals("Eiffelturm", comparison.next?.name)
        assertEquals(300.0 / 252.0, comparison.times, 0.001)
    }

    @Test
    fun `ueber dem hoechsten Berg gibt es kein naechstes Ziel`() {
        val comparison = Landmarks.compare(10_000.0)
        assertEquals("Mount Everest", comparison.reached?.name)
        assertNull(comparison.next)
        assertEquals(1f, comparison.progressToNext, 0.001f)
    }

    @Test
    fun `die Leiter steigt lueckenlos an`() {
        val heights = Landmarks.ALL.map { it.heightMeters }
        assertEquals(heights.sorted(), heights)
        assertEquals(heights.distinct().size, heights.size)
    }

    @Test
    fun `Fortschritt zum naechsten Ziel liegt zwischen null und eins`() {
        listOf(0.0, 3.0, 137.0, 5000.0).forEach { meters ->
            val progress = Landmarks.compare(meters).progressToNext
            assertTrue("$meters -> $progress", progress in 0f..1f)
        }
        assertNotNull(Landmarks.compare(0.0).next)
    }
}

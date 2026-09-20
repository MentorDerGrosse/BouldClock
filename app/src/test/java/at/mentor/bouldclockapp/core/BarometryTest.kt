package at.mentor.bouldclockapp.core

import at.mentor.bouldclockapp.core.metrics.Barometry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.abs

class BarometryTest {

    /**
     * Ein realistischer Versuch bei 5 Hz: erst ein paar Sekunden vor der Wand
     * stehen, dann hinaufklettern, dann oben sein. Mit dem auf der Uhr
     * gemessenen Rauschen von 0,01 hPa.
     */
    private fun climb(meters: Double, noise: Double = 0.01): List<Double> {
        val base = 1005.70
        val drop = meters / Barometry.METERS_PER_HPA
        val amBoden = List(20) { base }
        val aufstieg = (1..40).map { base - drop * it / 40.0 }
        val oben = List(10) { base - drop }
        return (amBoden + aufstieg + oben).mapIndexed { index, hpa ->
            hpa + if (index % 2 == 0) noise else -noise
        }
    }

    @Test
    fun `ein vier Meter hoher Boulder wird erkannt`() {
        val height = Barometry.climbHeightMeters(climb(4.0))!!
        assertEquals(4.0, height, 0.3)
    }

    @Test
    fun `ein hoher Boulder wird ebenso erkannt`() {
        val height = Barometry.climbHeightMeters(climb(6.0))!!
        assertEquals(6.0, height, 0.3)
    }

    /** Die Uhr liegt still - daraus darf keine Kletterhoehe werden. */
    @Test
    fun `Rauschen allein ergibt keine Hoehe`() {
        val still = (0..69).map { 1005.70 + if (it % 2 == 0) 0.01 else -0.01 }
        assertNull(Barometry.climbHeightMeters(still))
    }

    /**
     * Der Grund fuer die Mittelung ueber drei Messpunkte: ein einzelner
     * Ausreisser darf die Hoehe nicht erfinden.
     */
    @Test
    fun `ein einzelner Ausreisser verfaelscht die Hoehe kaum`() {
        val sauber = Barometry.climbHeightMeters(climb(4.0))!!
        val mitAusreisser = climb(4.0).toMutableList().also { it[35] = it[35] - 0.30 }
        val gestoert = Barometry.climbHeightMeters(mitAusreisser)!!
        assertEquals(sauber, gestoert, 0.9)
    }

    @Test
    fun `zu wenige Messpunkte ergeben keine Hoehe`() {
        assertNull(Barometry.climbHeightMeters(listOf(1005.7, 1005.6, 1005.5)))
        assertNull(Barometry.climbHeightMeters(emptyList()))
    }

    @Test
    fun `unplausible Spruenge werden verworfen`() {
        val sprung = (0..29).map { if (it < 15) 1005.70 else 1002.00 }
        val meters = (1005.70 - 1002.00) * Barometry.METERS_PER_HPA
        assert(meters > Barometry.MAX_PLAUSIBLE_METERS)
        assertNull(Barometry.climbHeightMeters(sprung))
    }

    @Test
    fun `die Umrechnung entspricht der Faustregel`() {
        // 1 hPa sind grob 8 Meter.
        assert(abs(Barometry.METERS_PER_HPA - 8.3) < 0.5)
    }
}

package at.mentor.bouldclockapp.core

import at.mentor.bouldclockapp.core.metrics.RestingHeartRate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RestingHeartRateTest {

    @Test
    fun `zu wenige Werte ergeben keinen Ruhepuls`() {
        assertNull(RestingHeartRate.fromSamples(List(10) { 60 }))
    }

    @Test
    fun `bei ruhiger Messung kommt der ruhige Wert heraus`() {
        assertEquals(58, RestingHeartRate.fromSamples(List(120) { 58 }))
    }

    /**
     * Der Grund fuer den Median statt des Minimums: ein einzelner Ausrutscher
     * duerfte den Wert nicht bestimmen - er ginge doppelt in die Kalorien ein.
     */
    @Test
    fun `ein einzelner Ausreisser nach unten zieht nicht`() {
        val mitAusreisser = List(119) { 58 } + listOf(31)
        assertEquals(58, RestingHeartRate.fromSamples(mitAusreisser))
    }

    @Test
    fun `die niedrige Phase zaehlt, nicht der Durchschnitt`() {
        // Erst noch aufgeregt, dann ruhig - gesucht ist die ruhige Phase.
        val beruhigend = List(60) { 90 } + List(60) { 52 }
        assertEquals(52, RestingHeartRate.fromSamples(beruhigend))
    }
}

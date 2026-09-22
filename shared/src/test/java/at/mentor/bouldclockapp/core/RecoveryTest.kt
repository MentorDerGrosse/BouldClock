package at.mentor.bouldclockapp.core

import at.mentor.bouldclockapp.core.metrics.HeartBeat
import at.mentor.bouldclockapp.core.metrics.Recovery
import at.mentor.bouldclockapp.core.metrics.WallBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecoveryTest {

    private val t0 = 1_758_000_000_000L

    /** Puls faellt gleichmaessig von [from] auf [to] ueber [seconds]. */
    private fun beats(from: Int, to: Int, seconds: Int, start: Long = t0) =
        (0..seconds).map { i ->
            HeartBeat(start + i * 1000L, from + (to - from) * i / seconds)
        }

    private fun attempt(startSec: Int, endSec: Int) =
        WallBlock(t0 + startSec * 1000L, t0 + endSec * 1000L, isAttempt = true)

    private fun moveTest(startSec: Int, endSec: Int) =
        WallBlock(t0 + startSec * 1000L, t0 + endSec * 1000L, isAttempt = false)

    @Test
    fun `nach ungestoerter Pause wird gerechnet`() {
        val block = attempt(0, 20)
        val result = Recovery.after(block, listOf(block), beats(150, 110, 200))

        // 0,2 Schlaege je Sekunde: bei 20 s noch 146, sechzig Sekunden spaeter 134.
        assertEquals(146, result!!.hrEnd)
        assertEquals(134, result.hrAfter60s)
        assertEquals(12, result.drop)
    }

    /** Der Kern der neuen Regel: eine Zugprobe unterbricht die Erholung. */
    @Test
    fun `eine Zugprobe im Fenster verhindert den Wert`() {
        val block = attempt(0, 20)
        val zug = moveTest(40, 55)

        assertNull(Recovery.after(block, listOf(block, zug), beats(150, 110, 200)))
    }

    @Test
    fun `ein neuer Versuch im Fenster verhindert den Wert`() {
        val block = attempt(0, 20)
        val naechster = attempt(60, 80)

        assertNull(Recovery.after(block, listOf(block, naechster), beats(150, 110, 200)))
    }

    /** Genau ausserhalb des Fensters ist in Ordnung. */
    @Test
    fun `ein Versuch nach dem Fenster stoert nicht`() {
        val block = attempt(0, 20)
        val naechster = attempt(81, 100)

        assertEquals(12, Recovery.after(block, listOf(block, naechster), beats(150, 110, 200))?.drop)
    }

    @Test
    fun `fuer eine Zugprobe selbst gibt es keinen Wert`() {
        val zug = moveTest(0, 20)
        assertNull(Recovery.after(zug, listOf(zug), beats(150, 110, 200)))
    }

    @Test
    fun `ohne Pulswert am Fensterende kein Wert`() {
        val block = attempt(0, 20)
        // Aufzeichnung bricht nach 30 s ab.
        assertNull(Recovery.after(block, listOf(block), beats(150, 140, 30)))
    }

    @Test
    fun `forAll liefert nur die messbaren Versuche`() {
        val a = attempt(0, 20)
        val zug = moveTest(40, 55)
        val b = attempt(200, 220)
        val alle = listOf(a, zug, b)

        val result = Recovery.forAll(alle, beats(150, 110, 400))

        // a ist durch die Zugprobe gestoert, zug ist kein Versuch, b ist sauber.
        assertEquals(setOf(b), result.keys)
    }
}

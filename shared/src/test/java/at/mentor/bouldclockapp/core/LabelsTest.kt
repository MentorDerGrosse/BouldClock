package at.mentor.bouldclockapp.core

import at.mentor.bouldclockapp.core.text.attemptLabel
import org.junit.Assert.assertEquals
import org.junit.Test

class LabelsTest {

    @Test
    fun `die Einzahl heisst Versuch`() {
        assertEquals("1 Versuch", attemptLabel(1))
        assertEquals("2 Versuche", attemptLabel(2))
        assertEquals("0 Versuche", attemptLabel(0))
    }
}

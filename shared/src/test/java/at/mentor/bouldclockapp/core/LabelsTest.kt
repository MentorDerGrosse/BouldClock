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

/** Die Einzahl ohne Zahl davor - fuer Kacheln, in denen die Zahl schon oben steht. */
class LabelNounTest {

    @org.junit.Test
    fun `Einzahl und Mehrzahl ohne Zahl`() {
        org.junit.Assert.assertEquals("Versuch", at.mentor.bouldclockapp.core.text.attemptNoun(1))
        org.junit.Assert.assertEquals("Versuche", at.mentor.bouldclockapp.core.text.attemptNoun(0))
        org.junit.Assert.assertEquals("Versuche", at.mentor.bouldclockapp.core.text.attemptNoun(2))
        org.junit.Assert.assertEquals("Session", at.mentor.bouldclockapp.core.text.sessionNoun(1))
        org.junit.Assert.assertEquals("Sessions", at.mentor.bouldclockapp.core.text.sessionNoun(3))
        org.junit.Assert.assertEquals("Top", at.mentor.bouldclockapp.core.text.topNoun(1))
        org.junit.Assert.assertEquals("Tops", at.mentor.bouldclockapp.core.text.topNoun(2))
    }

    @org.junit.Test
    fun `mit Zahl bleibt es wie bisher`() {
        org.junit.Assert.assertEquals("1 Versuch", at.mentor.bouldclockapp.core.text.attemptLabel(1))
        org.junit.Assert.assertEquals("7 Versuche", at.mentor.bouldclockapp.core.text.attemptLabel(7))
        org.junit.Assert.assertEquals("0 Versuche", at.mentor.bouldclockapp.core.text.attemptLabel(0))
    }
}

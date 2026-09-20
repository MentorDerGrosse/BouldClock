package at.mentor.bouldclockapp.core

import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.core.model.RestDurations
import at.mentor.bouldclockapp.core.model.SessionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GradesTest {

    /**
     * Der Test, der das Erweitern absichert: wer eine Stufe anhaengt, muss das in
     * jeder Skala tun. Eine Leiter mit abweichender Laenge wuerde Grade still
     * gegeneinander verschieben - der Fehler faellt sonst erst in der Statistik auf.
     */
    @Test
    fun `alle Skalen beschriften dieselbe Leiter`() {
        val lengths = GradeSystem.entries
            .filter { it.hasFixedLadder }
            .map { Grades.labelsOf(it).size }
            .distinct()
        assertEquals(1, lengths.size)
        assertEquals(lengths.single() - 1, Grades.MAX_VALUE)
    }

    @Test
    fun `Font und V beschreiben dieselbe Stufe`() {
        val font7a = Grades.parse("7A")!!
        assertEquals(font7a, Grades.parse("V6"))
        assertEquals("7A", Grades.label(font7a, GradeSystem.FONT))
        assertEquals("V6", Grades.label(font7a, GradeSystem.V_SCALE))
    }

    @Test
    fun `Kleinschreibung wird gelesen`() {
        assertEquals(Grades.parse("7A"), Grades.parse("7a"))
        assertEquals(Grades.parse("6C+"), Grades.parse("6c+"))
        assertEquals(Grades.parse("V6"), Grades.parse("v6"))
    }

    @Test
    fun `die Leiter reicht vom Einstieg bis zum haertesten Boulder`() {
        assertEquals(0, Grades.parse("3"))
        assertEquals(Grades.MAX_VALUE, Grades.parse("9A"))
        assertEquals(Grades.MAX_VALUE, Grades.parse("V17"))
        assertEquals("VB", Grades.label(0, GradeSystem.V_SCALE))
    }

    @Test
    fun `Routengrade gehoeren nicht auf die Boulderleiter`() {
        // 9b+ ist franzoesisches Seilklettern, eine andere Disziplin.
        // Waere es hier auffindbar, wuerde die App Unvergleichbares vergleichen.
        assertNull(Grades.parse("9b+"))
        assertNull(Grades.parse("5.13a"))
    }

    @Test
    fun `Grade sind sortierbar`() {
        assertTrue(Grades.parse("6C")!! < Grades.parse("7A")!!)
        assertTrue(Grades.parse("V4")!! < Grades.parse("V10")!!)
    }

    @Test
    fun `der Drehkranz zeigt in der V-Skala keine Dopplungen`() {
        val labels = Grades.pickerValues(GradeSystem.V_SCALE)
            .map { Grades.label(it, GradeSystem.V_SCALE) }
        assertEquals(labels.distinct(), labels)
        assertEquals(Grades.MAX_VALUE + 1, Grades.pickerValues(GradeSystem.FONT).size)
    }

    @Test
    fun `Hallenfarben fallen auf Font zurueck statt zu knallen`() {
        assertNotNull(Grades.label(5, GradeSystem.COLOR))
        assertEquals("?", Grades.label(999, GradeSystem.FONT))
    }

    @Test
    fun `clamp haelt Werte auf der Leiter`() {
        assertEquals(Grades.MAX_VALUE, Grades.clamp(500))
        assertEquals(Grades.MIN_VALUE, Grades.clamp(-3))
    }
}

class RestDurationsTest {

    @Test
    fun `Optionen sind aufsteigend und eindeutig`() {
        assertEquals(RestDurations.OPTIONS.sorted(), RestDurations.OPTIONS)
        assertEquals(RestDurations.OPTIONS.distinct(), RestDurations.OPTIONS)
    }

    @Test
    fun `Formatierung ist mmss`() {
        assertEquals("0:30", RestDurations.format(30_000L))
        assertEquals("3:00", RestDurations.format(180_000L))
        assertEquals("15:00", RestDurations.format(900_000L))
    }

    @Test
    fun `gespeicherte Werte rasten auf den naechsten Eintrag ein`() {
        assertEquals(180_000L, RestDurations.snap(175_000L))
        assertEquals(RestDurations.MIN_MS, RestDurations.snap(1L))
        assertEquals(RestDurations.MAX_MS, RestDurations.snap(99 * 60_000L))
    }

    @Test
    fun `jeder Sessiontyp hat eine waehlbare Voreinstellung`() {
        SessionType.entries.forEach { type ->
            assertTrue(type.displayName.isNotBlank())
            assertEquals(type.defaultRestMs, RestDurations.snap(type.defaultRestMs))
        }
    }

    /** Limit-Training lebt von langen Pausen, Volumen von kurzen. */
    @Test
    fun `die Voreinstellungen trennen die Trainingsformen`() {
        assertTrue(SessionType.LIMIT.defaultRestMs > SessionType.FREE.defaultRestMs)
        assertTrue(SessionType.VOLUME.defaultRestMs < SessionType.FREE.defaultRestMs)
    }
}

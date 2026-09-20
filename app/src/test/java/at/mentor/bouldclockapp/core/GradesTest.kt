package at.mentor.bouldclockapp.core

import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.core.metrics.SessionMetrics
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

    /**
     * Der Grund fuer die Untergrenze: kuerzer als das Messfenster darf die
     * Soll-Pause nicht sein, sonst faellt der naechste Versuch hinein und HRR60
     * waere nicht mehr erhebbar.
     */
    @Test
    fun `die Untergrenze deckt das HRR60-Fenster ab`() {
        assertTrue(RestDurations.MIN_MS >= SessionMetrics.HRR_WINDOW_MS)
    }

    @Test
    fun `Minuten und Sekunden ergeben die Dauer`() {
        assertEquals(60_000L, RestDurations.of(1, 0))
        assertEquals(210_000L, RestDurations.of(3, 30))
        assertEquals("3:30", RestDurations.format(RestDurations.of(3, 30)))
        assertEquals("1:00", RestDurations.format(RestDurations.MIN_MS))
    }

    @Test
    fun `clamp haelt die Dauer im waehlbaren Bereich`() {
        assertEquals(RestDurations.MIN_MS, RestDurations.clamp(0L))
        assertEquals(RestDurations.MIN_MS, RestDurations.clamp(30_000L))
        assertEquals(RestDurations.MAX_MS, RestDurations.clamp(99 * 60_000L))
    }

    @Test
    fun `Zerlegen und Zusammensetzen ergibt denselben Wert`() {
        RestDurations.MINUTES.forEach { minutes ->
            RestDurations.SECONDS.forEach { seconds ->
                val ms = RestDurations.of(minutes, seconds)
                assertEquals(minutes, RestDurations.minutesOf(ms))
                assertEquals(seconds, RestDurations.secondsOf(ms))
            }
        }
    }

    /** Gespeicherte Werte muessen nicht auf dem Viertelminuten-Raster liegen. */
    @Test
    fun `krumme Sekunden rasten auf das naechste Viertel ein`() {
        assertEquals(30, RestDurations.secondsOf(RestDurations.of(2, 0) + 28_000L))
        assertEquals(0, RestDurations.secondsOf(RestDurations.of(2, 0) + 4_000L))
        assertTrue(RestDurations.secondIndexOf(RestDurations.of(2, 45)) in RestDurations.SECONDS.indices)
    }

    @Test
    fun `der Trainingshinweis trennt kurze von langen Pausen`() {
        assertEquals("Kraftausdauer", RestDurations.trainingHint(RestDurations.of(1, 0)))
        assertEquals("Maximalkraft", RestDurations.trainingHint(RestDurations.of(5, 0)))
    }

    @Test
    fun `jeder Sessiontyp hat eine waehlbare Voreinstellung`() {
        SessionType.entries.forEach { type ->
            assertTrue(type.displayName.isNotBlank())
            assertEquals(type.defaultRestMs, RestDurations.clamp(type.defaultRestMs))
        }
    }

    /** Limit-Training lebt von langen Pausen, Volumen von kurzen. */
    @Test
    fun `die Voreinstellungen trennen die Trainingsformen`() {
        assertTrue(SessionType.LIMIT.defaultRestMs > SessionType.FREE.defaultRestMs)
        assertTrue(SessionType.VOLUME.defaultRestMs < SessionType.FREE.defaultRestMs)
    }
}

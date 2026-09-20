package at.mentor.bouldclockapp.core

import at.mentor.bouldclockapp.core.metrics.AttemptFact
import at.mentor.bouldclockapp.core.metrics.groupRuns
import at.mentor.bouldclockapp.core.model.Grades
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AttemptRunsTest {

    private fun fact(grade: String?, send: Boolean = false, hrMax: Int? = null) = AttemptFact(
        gradeValue = grade?.let { Grades.parse(it) },
        isSend = send,
        workMs = 20_000L,
        hrMax = hrMax,
    )

    /** Das Beispiel aus der Anforderung: 7A sechsmal, 6B einmal, 7A dreimal. */
    @Test
    fun `aufeinanderfolgende Versuche desselben Grades werden zu einem Boulder`() {
        val runs = groupRuns(
            listOf(
                fact("7A"), fact("7A"), fact("7A"), fact("7A"), fact("7A"), fact("7A", send = true),
                fact("6B", send = true),
                fact("7A"), fact("7A"), fact("7A"),
            ),
        )

        assertEquals(3, runs.size)

        assertEquals(Grades.parse("7A"), runs[0].gradeValue)
        assertEquals(6, runs[0].attempts)
        assertTrue(runs[0].isSent)
        assertFalse(runs[0].isFlash)

        assertEquals(1, runs[1].attempts)
        assertTrue(runs[1].isFlash)

        assertEquals(3, runs[2].attempts)
        assertFalse(runs[2].isSent)
    }

    /** Derselbe Grad nach einem Wechsel ist ein anderer Boulder, keine Fortsetzung. */
    @Test
    fun `ein Gradwechsel trennt auch bei Rueckkehr`() {
        val runs = groupRuns(listOf(fact("6A"), fact("6B"), fact("6A")))
        assertEquals(3, runs.size)
        assertEquals(runs[0].gradeValue, runs[2].gradeValue)
    }

    @Test
    fun `Versuche ohne Grad bilden eigene Gruppen`() {
        val runs = groupRuns(listOf(fact(null), fact(null), fact("6A")))
        assertEquals(2, runs.size)
        assertNull(runs[0].gradeValue)
        assertEquals(2, runs[0].attempts)
    }

    @Test
    fun `Wandzeit und Maximalpuls werden ueber die Gruppe zusammengezogen`() {
        val runs = groupRuns(
            listOf(fact("6A", hrMax = 150), fact("6A", hrMax = 171), fact("6A", hrMax = null)),
        )
        assertEquals(1, runs.size)
        assertEquals(60_000L, runs.single().workMs)
        assertEquals(171, runs.single().hrMax)
    }

    @Test
    fun `ohne Versuche gibt es keine Gruppen`() {
        assertTrue(groupRuns(emptyList()).isEmpty())
    }
}

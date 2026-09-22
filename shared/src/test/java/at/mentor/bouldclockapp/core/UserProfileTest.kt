package at.mentor.bouldclockapp.core

import at.mentor.bouldclockapp.core.model.BiologicalSex
import at.mentor.bouldclockapp.core.model.ProfileRanges
import at.mentor.bouldclockapp.data.db.entity.RecordMeta
import at.mentor.bouldclockapp.data.db.entity.UserProfileEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class UserProfileTest {

    private fun profile(birthYear: Int) = UserProfileEntity(
        weightKg = 72,
        birthYear = birthYear,
        sex = BiologicalSex.MALE,
        meta = RecordMeta.now(0L),
    )

    /**
     * Der Grund, warum das Geburtsjahr gespeichert wird und nicht das Alter:
     * ein gespeichertes Alter waere naechstes Jahr still falsch.
     */
    @Test
    fun `das Alter altert von selbst mit`() {
        val today = LocalDate.of(2026, 9, 21)
        val person = profile(birthYear = today.year - 34)

        assertEquals(34, person.ageInYears(today))
        assertEquals(35, person.ageInYears(today.plusYears(1)))
        assertEquals(44, person.ageInYears(today.plusYears(10)))
    }

    @Test
    fun `Gewicht und Alter bleiben im waehlbaren Bereich`() {
        assertEquals(ProfileRanges.WEIGHT_KG.first(), ProfileRanges.clampWeight(0))
        assertEquals(ProfileRanges.WEIGHT_KG.last(), ProfileRanges.clampWeight(500))
        assertEquals(72, ProfileRanges.clampWeight(72))

        assertEquals(ProfileRanges.AGE_YEARS.first(), ProfileRanges.clampAge(3))
        assertEquals(ProfileRanges.AGE_YEARS.last(), ProfileRanges.clampAge(140))
    }

    @Test
    fun `die Voreinstellungen liegen im Bereich`() {
        assertTrue(ProfileRanges.DEFAULT_WEIGHT_KG in ProfileRanges.WEIGHT_KG)
        assertTrue(ProfileRanges.DEFAULT_AGE_YEARS in ProfileRanges.AGE_YEARS)
    }

    /**
     * Feste ID statt UUID: legen Uhr und Handy das Profil unabhaengig an,
     * schreiben beide dieselbe Zeile - und beim Abgleich gewinnt das juengere
     * updatedAt, ohne dass eine Seite die andere fragen muss.
     */
    @Test
    fun `es gibt genau ein Profil`() {
        assertEquals(UserProfileEntity.SINGLETON_ID, profile(1990).id)
    }

    @Test
    fun `keine Angabe beim Geschlecht ist moeglich`() {
        assertTrue(BiologicalSex.UNSPECIFIED in BiologicalSex.entries)
        BiologicalSex.entries.forEach { assertTrue(it.displayName.isNotBlank()) }
    }
}

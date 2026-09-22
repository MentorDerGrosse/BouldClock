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

/**
 * Das Profil muss die Koerperdaten ueber die Uebertragung retten.
 *
 * Ohne sie waere die erste Synchronisierung nach dem Messen ein stiller
 * Datenverlust - und Ruhepuls und Groesse sind genau die Werte, die die
 * Kalorienrechnung individuell machen.
 */
class ProfilePayloadBodyTest {

    private val profile = at.mentor.bouldclockapp.data.db.entity.UserProfileEntity(
        weightKg = 73,
        birthYear = 2005,
        sex = at.mentor.bouldclockapp.core.model.BiologicalSex.MALE,
        heightCm = 180,
        restingHrBpm = 54,
        maxHrBpm = 191,
        meta = at.mentor.bouldclockapp.data.db.entity.RecordMeta.now(1_000L),
    )

    @org.junit.Test
    fun `Groesse Ruhepuls und Maximalpuls ueberstehen die Uebertragung`() {
        val raw = at.mentor.bouldclockapp.data.sync.ProfilePayload(profile).toJson()
        val restored = at.mentor.bouldclockapp.data.sync.ProfilePayload.fromJson(raw).profile

        org.junit.Assert.assertEquals(180, restored.heightCm)
        org.junit.Assert.assertEquals(54, restored.restingHrBpm)
        org.junit.Assert.assertEquals(191, restored.maxHrBpm)
        org.junit.Assert.assertEquals(73, restored.weightKg)
    }

    @org.junit.Test
    fun `ein altes Paket ohne Koerperdaten bleibt lesbar`() {
        val alt = org.json.JSONObject(
            at.mentor.bouldclockapp.data.sync.ProfilePayload(profile).toJson(),
        ).apply {
            remove("heightCm"); remove("restingHrBpm"); remove("maxHrBpm")
        }.toString()

        val restored = at.mentor.bouldclockapp.data.sync.ProfilePayload.fromJson(alt).profile

        org.junit.Assert.assertNull(restored.heightCm)
        org.junit.Assert.assertNull(restored.restingHrBpm)
        org.junit.Assert.assertEquals(73, restored.weightKg)
    }
}

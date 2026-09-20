package at.mentor.bouldclockapp.data.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import at.mentor.bouldclockapp.core.model.BiologicalSex
import java.time.LocalDate

/**
 * Koerperdaten fuer die Kalorienberechnung.
 *
 * In der Datenbank und nicht in den Einstellungen: die Skala darf sich zwischen
 * Uhr und Handy unterscheiden, das Profil nicht. Es braucht deshalb die
 * Sync-Spalten aus [RecordMeta].
 *
 * Feste ID statt UUID - es gibt genau ein Profil. Legen Uhr und Handy es
 * unabhaengig voneinander an, schreiben beide dieselbe Zeile, und beim Abgleich
 * gewinnt schlicht das juengere `updatedAt`. Keine Seite muss die andere fragen,
 * keine Seite blockiert, wenn die andere nicht erreichbar ist.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = SINGLETON_ID,

    val weightKg: Int,

    /**
     * Geburtsjahr, nicht Alter.
     *
     * Ein gespeichertes Alter waere naechstes Jahr falsch, ohne dass es jemand
     * merkt. Eingegeben wird trotzdem das Alter - umgerechnet wird beim Speichern.
     */
    val birthYear: Int,

    val sex: BiologicalSex,

    @Embedded val meta: RecordMeta,
) {
    fun ageInYears(today: LocalDate = LocalDate.now()): Int = today.year - birthYear

    companion object {
        const val SINGLETON_ID: String = "self"
    }
}

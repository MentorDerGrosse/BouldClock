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

    /**
     * Koerpergroesse in Zentimetern.
     *
     * Fuer den Grundumsatz nach Mifflin-St Jeor. Optional, weil bestehende
     * Profile sie nicht haben - fehlt sie, faellt die Rechnung auf die
     * MET-Naeherung zurueck, die nur das Gewicht braucht.
     */
    val heightCm: Int? = null,

    /**
     * Gemessener Ruhepuls.
     *
     * Der wichtigste einzelne Wert fuer die Kalorienrechnung: er geht doppelt
     * ein - einmal in die Pulsreserve, einmal in die daraus geschaetzte
     * Sauerstoffaufnahme. Selbst gemessen statt von der Plattform uebernommen,
     * damit dieselbe Zahl spaeter auch auf einer Garmin entsteht.
     */
    val restingHrBpm: Int? = null,

    /**
     * Hoechster je beobachteter Puls.
     *
     * Wird automatisch mitgefuehrt, wenn eine Session darueber hinausgeht. Nach
     * ein paar harten Abenden ist das genauer als jede Altersformel.
     */
    val maxHrBpm: Int? = null,

    @Embedded val meta: RecordMeta,
) {
    fun ageInYears(today: LocalDate = LocalDate.now()): Int = today.year - birthYear

    companion object {
        const val SINGLETON_ID: String = "self"
    }
}

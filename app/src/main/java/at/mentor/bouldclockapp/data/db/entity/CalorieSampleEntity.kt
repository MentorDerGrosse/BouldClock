package at.mentor.bouldclockapp.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * Kumulativer Kalorienstand, so wie Health Services ihn meldet.
 *
 * Als Verlauf und nicht als einzelne Summe: daraus laesst sich beides ableiten,
 * was gebraucht wird - der Gesamtwert der Session (letzter Punkt) und der Anteil
 * an der Wand (Differenz zwischen Versuchsbeginn und -ende). Wuerde nur die
 * Summe gespeichert, waere der Wandanteil spaeter nicht mehr rekonstruierbar.
 *
 * Kein [RecordMeta] - Massendaten werden nicht einzeln synchronisiert, sondern
 * am Stueck mit der Session.
 */
@Entity(
    tableName = "calorie_sample",
    primaryKeys = ["sessionId", "timestampMs"],
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CalorieSampleEntity(
    val sessionId: String,
    val timestampMs: Long,

    /** Kilokalorien seit Beginn der Aufzeichnung, monoton steigend. */
    val kcalTotal: Double,
)

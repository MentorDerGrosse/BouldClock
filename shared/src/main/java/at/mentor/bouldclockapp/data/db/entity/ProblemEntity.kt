package at.mentor.bouldclockapp.data.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import at.mentor.bouldclockapp.core.model.WallAngle

/**
 * Ein konkreter Boulder an der Wand.
 *
 * In Stufe 1 optional: das Ein-Tap-Logging schreibt nur Grad und Ergebnis, ohne
 * den Boulder zu identifizieren ([AttemptEntity.problemId] ist nullable).
 * Die Tabelle steht trotzdem schon, weil ein nachtraeglich eingefuehrter
 * Fremdschluessel eine Migration kostet - eine nullable Spalte jetzt kostet nichts.
 *
 * Sie ist die Voraussetzung fuer den Projektverlauf ueber Sessions hinweg
 * ("Versuch 47, letztes Mal bis Zug 5").
 */
@Entity(
    tableName = "problem",
    foreignKeys = [
        ForeignKey(
            entity = GymEntity::class,
            parentColumns = ["id"],
            childColumns = ["gymId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("gymId")],
)
data class ProblemEntity(
    @PrimaryKey val id: String,
    val gymId: String,

    /** Wie er in der Halle heisst: "Blau 14", "Gelber Sloper". */
    val label: String,
    val colorHex: String? = null,

    /** Stufe auf der kanonischen Leiter, siehe [at.mentor.bouldclockapp.core.model.Grades]. */
    val gradeValue: Int? = null,
    val wallAngle: WallAngle? = null,

    /** Erster erfolgreicher Durchstieg - trennt Projekt von erledigt. */
    val firstSentAt: Long? = null,

    /** Abgeschraubt. Zaehlt danach nicht mehr in offene Projekte. */
    val retiredAt: Long? = null,

    @Embedded val meta: RecordMeta,
)

package at.mentor.bouldclockapp.data.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import at.mentor.bouldclockapp.core.model.GradeSystem

/**
 * Halle.
 *
 * Eigene Tabelle schon in Stufe 1, weil Grade hallenabhaengig sind: ein 6C in
 * Halle A ist kein 6C in Halle B, und nach dem Umschrauben verschiebt sich alles.
 * Ohne diese Dimension zeigt die Historie spaeter "Fortschritt", der in Wahrheit
 * nur ein Hallenwechsel war.
 */
@Entity(tableName = "gym")
data class GymEntity(
    @PrimaryKey val id: String,
    val name: String,
    val gradeSystem: GradeSystem = GradeSystem.FONT,

    /** Stammhalle - wird beim Sessionstart vorausgewaehlt. */
    val isDefault: Boolean = false,

    @Embedded val meta: RecordMeta,
)

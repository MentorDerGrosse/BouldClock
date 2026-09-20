package at.mentor.bouldclockapp.data.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.GradeSystem

/**
 * Ein Versuch ("Burn") - die zentrale Einheit der App.
 *
 * Laufender Versuch: [endedAt] und [outcome] sind null. Es kann pro Session
 * hoechstens einen davon geben.
 *
 * Grad wird hier denormalisiert gespeichert und nicht aus [problemId] gezogen.
 * Das ist Absicht: geloggt wird, was der Nutzer in diesem Moment eingeschaetzt hat.
 * Wenn der Boulder spaeter umbewertet oder erst nachtraeglich zugeordnet wird,
 * bleibt die Historie trotzdem ehrlich.
 */
@Entity(
    tableName = "attempt",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ProblemEntity::class,
            parentColumns = ["id"],
            childColumns = ["problemId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["sessionId", "ordinal"], unique = true),
        Index("problemId"),
        Index("startedAt"),
    ],
)
data class AttemptEntity(
    @PrimaryKey val id: String,
    val sessionId: String,

    /** Optional - Ein-Tap-Logging kennt den Boulder nicht. */
    val problemId: String? = null,

    /** Fortlaufend ab 1 innerhalb der Session. */
    val ordinal: Int,

    /**
     * Beginnt mit diesem Versuch ein neuer Boulder?
     *
     * Ersetzt das Raten anhand gleicher Grade. Gesetzt wird es automatisch -
     * nach einem Top faengt zwangslaeufig ein neuer Boulder an, ebenso bei einem
     * anderen Grad - und nur im mehrdeutigen Fall (Sturz, danach derselbe Grad)
     * entscheidet der Nutzer.
     */
    val startsNewBoulder: Boolean = false,

    val startedAt: Long,

    /** null solange der Versuch laeuft. */
    val endedAt: Long? = null,
    val outcome: AttemptOutcome? = null,

    /** Stufe auf der kanonischen Leiter, siehe [at.mentor.bouldclockapp.core.model.Grades]. */
    val gradeValue: Int? = null,

    /** Nur fuer die Anzeige - in welcher Skala wurde eingegeben. */
    val gradeSystem: GradeSystem? = null,

    /** Bis zu welchem Zug gekommen. Fuettert den Projektverlauf. */
    val topMoveReached: Int? = null,

    /**
     * Neigung des Boards in Grad, nur im Board-Modus gesetzt.
     *
     * Am Versuch und nicht an der Session: an einem Abend wird umgestellt, und
     * ohne den Winkel ist ein Boardgrad nichts wert.
     */
    val boardAngleDegrees: Int? = null,

    // --- Pulsauswertung, gefuellt beim Beenden bzw. 60 s danach ---

    val hrAvg: Int? = null,
    val hrMax: Int? = null,

    /** Puls beim Absteigen. */
    val hrEnd: Int? = null,

    /** Puls 60 s nach dem Absteigen. */
    val hrAfter60s: Int? = null,

    /**
     * Herzfrequenz-Erholung: [hrEnd] minus [hrAfter60s].
     *
     * Nur gesetzt, wenn die Pause tatsaechlich >= 60 s war - sonst misst man den
     * naechsten Versuch mit und die Zahl luegt. Der Verlauf dieser Werte ueber die
     * Session ist das Ermuedungssignal, ueber Monate der Fitnesstrend.
     */
    val hrr60: Int? = null,

    /** Pause bis zum naechsten Versuch. Wird beim Start des naechsten gesetzt. */
    val restAfterMs: Long? = null,

    @Embedded val meta: RecordMeta,
)

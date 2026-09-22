package at.mentor.bouldclockapp.data.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import at.mentor.bouldclockapp.core.model.SessionState
import at.mentor.bouldclockapp.core.model.SessionType

/**
 * Eine Bouldersession.
 *
 * Wird beim Start sofort geschrieben, nicht erst beim Stop. Die Uhr kann jederzeit
 * sterben (Akku leer, Service gekillt, Sturz auf die Matte) - eine Session, die nur
 * im ViewModel liegt, ist dann weg. [state] `ACTIVE`/`PAUSED` ohne [endedAt] ist
 * genau das Signal, an dem die Wiederherstellung beim App-Start ansetzt.
 */
@Entity(
    tableName = "session",
    foreignKeys = [
        ForeignKey(
            entity = GymEntity::class,
            parentColumns = ["id"],
            childColumns = ["gymId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("gymId"), Index("startedAt"), Index("state")],
)
data class SessionEntity(
    @PrimaryKey val id: String,

    /** Nullable: draussen oder "Halle noch nicht angelegt". */
    val gymId: String? = null,

    /**
     * Sessions sind nur innerhalb desselben Typs vergleichbar - eine Volumensession
     * gegen eine Limit-Session zu stellen ist statistischer Unsinn. Deshalb ist der
     * Typ keine Notiz, sondern eine Dimension.
     */
    val type: SessionType = SessionType.FREE,
    val state: SessionState = SessionState.ACTIVE,

    val startedAt: Long,
    val endedAt: Long? = null,

    /** Aufsummierte Auto-Pause (Handy in der Tasche, Kaffee). Kein Rest zwischen Burns. */
    val pausedMs: Long = 0L,

    /**
     * Eingestellte Soll-Pause zwischen zwei Versuchen.
     *
     * Wird mitgespeichert statt nur als Einstellung gehalten: erst der Vergleich
     * von Soll und Ist macht die Pausendisziplin sichtbar - "du wolltest 3 min,
     * gewartet hast du im Schnitt 1:20".
     */
    val restTargetMs: Long,

    /** Subjektive Anstrengung 1..10, ein Tap nach der Session. */
    val rpe: Int? = null,
    val note: String? = null,

    @Embedded val meta: RecordMeta,
)

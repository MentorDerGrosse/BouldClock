package at.mentor.bouldclockapp.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import at.mentor.bouldclockapp.core.model.SessionType

/**
 * Vorberechnete Kennzahlen einer abgeschlossenen Session.
 *
 * Bewusst eine echte Tabelle und keine View: die Historie auf der Uhr soll beim
 * Oeffnen zehn Zeilen lesen und nichts aggregieren. Geschrieben wird einmal beim
 * Sessionende (und neu, wenn jemand nachtraeglich Versuche korrigiert).
 *
 * Das ist die Tabelle, aus der jeder Vergleich kommt - "24 Versuche, dein Schnitt
 * sind 31" waehrend der Session, das Delta nach dem Stop, der HRR60-Trend ueber Monate.
 */
@Entity(
    tableName = "session_summary",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("startedAt"), Index("gymId"), Index("type")],
)
data class SessionSummaryEntity(
    @PrimaryKey val sessionId: String,

    // Dimensionen dupliziert, damit die Historie ohne Join filtern kann.
    val gymId: String?,
    val type: SessionType,
    val startedAt: Long,

    /** Bruttodauer inkl. Pausen. */
    val totalMs: Long,

    /** Summe aller Versuchsdauern - "Time on Wall". */
    val workMs: Long,

    /** totalMs - workMs - pausedMs. Die Zahl, die alle ueberrascht. */
    val restMs: Long,
    val pausedMs: Long,

    val attemptCount: Int,
    val sendCount: Int,
    val flashCount: Int,

    val hardestSendValue: Int? = null,

    val hrAvg: Int? = null,
    val hrMax: Int? = null,

    /** Mittlere Herzfrequenz-Erholung ueber alle auswertbaren Versuche. */
    val hrr60Avg: Int? = null,

    val rpe: Int? = null,
    val computedAt: Long,
) {
    /** Pause je Sekunde an der Wand. 1:5 ist gesund, 1:1 ist versehentliches Ausdauertraining. */
    val restPerWork: Double? get() = if (workMs > 0L) restMs.toDouble() / workMs else null

    val sendRate: Double? get() = if (attemptCount > 0) sendCount.toDouble() / attemptCount else null
}

package at.mentor.bouldclockapp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import at.mentor.bouldclockapp.data.db.entity.AttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttemptDao {

    @Upsert
    suspend fun upsert(attempt: AttemptEntity)

    @Query("SELECT * FROM attempt WHERE id = :id")
    suspend fun byId(id: String): AttemptEntity?

    @Query(
        "SELECT * FROM attempt WHERE sessionId = :sessionId AND deletedAt IS NULL ORDER BY ordinal",
    )
    fun observeBySession(sessionId: String): Flow<List<AttemptEntity>>

    /** Der gerade laufende Versuch. Hoechstens einer pro Session. */
    @Query(
        """
        SELECT * FROM attempt
        WHERE sessionId = :sessionId AND endedAt IS NULL AND deletedAt IS NULL
        ORDER BY ordinal DESC LIMIT 1
        """,
    )
    suspend fun running(sessionId: String): AttemptEntity?

    /**
     * Bewusst ohne `deletedAt IS NULL`: der Unique-Index auf (sessionId, ordinal)
     * gilt auch fuer soft-geloeschte Zeilen. Nummern werden nie wiederverwendet.
     */
    @Query("SELECT COALESCE(MAX(ordinal), 0) + 1 FROM attempt WHERE sessionId = :sessionId")
    suspend fun nextOrdinal(sessionId: String): Int

    @Query(
        """
        SELECT * FROM attempt
        WHERE sessionId = :sessionId AND endedAt IS NOT NULL AND deletedAt IS NULL
        ORDER BY ordinal DESC LIMIT 1
        """,
    )
    suspend fun lastFinished(sessionId: String): AttemptEntity?

    /** Der beendete Versuch direkt vor :beforeAttemptId - fuer die Boulder-Grenze. */
    @Query(
        """
        SELECT * FROM attempt
        WHERE sessionId = :sessionId
          AND deletedAt IS NULL
          AND endedAt IS NOT NULL
          AND ordinal < (SELECT ordinal FROM attempt WHERE id = :beforeAttemptId)
        ORDER BY ordinal DESC LIMIT 1
        """,
    )
    suspend fun previousFinished(sessionId: String, beforeAttemptId: String): AttemptEntity?

    /** Alle Versuche einer Session, auch unbeendete - fuer die Uebertragung. */
    @Query("SELECT * FROM attempt WHERE sessionId = :sessionId ORDER BY ordinal")
    suspend fun allBySession(sessionId: String): List<AttemptEntity>

    /**
     * Beendete **Versuche** in zeitlicher Reihenfolge.
     *
     * Ohne Zugproben: die zaehlen weder in die Statistik noch in die
     * Boulder-Gruppierung, und eine Hoehe haben sie auch nicht - man startet
     * dabei nicht vom Boden.
     */
    @Query(
        """
        SELECT * FROM attempt
        WHERE sessionId = :sessionId AND deletedAt IS NULL AND endedAt IS NOT NULL
          AND kind = 'ATTEMPT'
        ORDER BY ordinal
        """,
    )
    suspend fun finishedBySession(sessionId: String): List<AttemptEntity>

    /**
     * Alle beendeten Bloecke an der Wand, Zugproben eingeschlossen.
     *
     * Fuer Energie und Erholung: koerperlich ist eine Zugprobe Arbeit, und sie
     * unterbricht eine Pause genauso wie ein Versuch.
     */
    @Query(
        """
        SELECT * FROM attempt
        WHERE sessionId = :sessionId AND deletedAt IS NULL AND endedAt IS NOT NULL
        ORDER BY ordinal
        """,
    )
    suspend fun finishedBlocks(sessionId: String): List<AttemptEntity>

    /**
     * Endgueltig loeschen.
     *
     * Nur fuer Versuche, die es nie gab - ein Fehlstart, der nach Sekunden
     * wieder beendet wird. Ein Soft Delete waere hier falsch: er hinterliesse
     * eine Luecke in der Nummerierung fuer etwas, das nie passiert ist.
     */
    @Query("DELETE FROM attempt WHERE id = :id")
    suspend fun delete(id: String)

    /** Projektverlauf: alle Versuche an einem Boulder ueber Sessions hinweg. */
    @Query(
        "SELECT * FROM attempt WHERE problemId = :problemId AND deletedAt IS NULL ORDER BY startedAt",
    )
    fun observeByProblem(problemId: String): Flow<List<AttemptEntity>>

    /**
     * Der zuletzt eingetragene Grad, ueber Sessions hinweg.
     *
     * Vorschlagswert der Gradabfrage. Aus der Datenbank statt aus dem Speicher,
     * damit er einen App-Neustart ueberlebt - wer gestern 6C probiert hat,
     * faengt heute selten bei 3 an.
     *
     * Nur die Stufe, nicht die Skala: die Stufe ist eine Schwierigkeit und gilt
     * skalenunabhaengig, die Skala ist reine Anzeigevorliebe und steht in den
     * Einstellungen.
     */
    @Query(
        """
        SELECT gradeValue FROM attempt
        WHERE gradeValue IS NOT NULL AND deletedAt IS NULL
        ORDER BY startedAt DESC LIMIT 1
        """,
    )
    suspend fun lastGradeValue(): Int?

    /** Zuletzt eingestellter Boardwinkel - Vorschlag fuer den naechsten Versuch. */
    @Query(
        """
        SELECT boardAngleDegrees FROM attempt
        WHERE boardAngleDegrees IS NOT NULL AND deletedAt IS NULL
        ORDER BY startedAt DESC LIMIT 1
        """,
    )
    suspend fun lastBoardAngle(): Int?

    @Query("UPDATE attempt SET deletedAt = :now, updatedAt = :now, syncState = 'PENDING' WHERE id = :id")
    suspend fun softDelete(id: String, now: Long)

    /**
     * Aggregat fuer die Zusammenfassung - eine Abfrage statt N Zeilen in den Speicher.
     *
     * Ein Versuch ohne Ergebnis zaehlt mit: nach dem Ausloeser beendet man den
     * Burn, das Ergebnis tippt man erst in der Pause - und manchmal gar nicht.
     * Stattgefunden hat er trotzdem. Als Send zaehlt nur, was ausdruecklich
     * protokolliert wurde.
     */
    @Query(
        """
        SELECT
            COUNT(*)                                                      AS attemptCount,
            COALESCE(SUM(CASE WHEN outcome IN ('FLASH','TOP') THEN 1 ELSE 0 END), 0) AS sendCount,
            COALESCE(SUM(CASE WHEN outcome = 'FLASH' THEN 1 ELSE 0 END), 0)         AS flashCount,
            COALESCE(SUM(endedAt - startedAt), 0)                         AS workMs,
            MAX(CASE WHEN outcome IN ('FLASH','TOP') THEN gradeValue END) AS hardestSendValue,
            AVG(hrr60)                                                    AS hrr60Avg,
            SUM(climbHeightMeters)                                        AS climbHeightMeters,
            MAX(climbHeightMeters)                                        AS maxClimbHeightMeters
        FROM attempt
        WHERE sessionId = :sessionId
          AND deletedAt IS NULL
          AND endedAt IS NOT NULL
          AND kind = 'ATTEMPT'
        """,
    )
    suspend fun aggregate(sessionId: String): AttemptAggregate

    /**
     * Gradverteilung ueber alle Sessions - die Gradpyramide.
     *
     * Der Join auf `session` ist noetig, weil ein Versuch nichts davon weiss,
     * dass seine Session geloescht wurde: der Soft Delete setzt `deletedAt` nur
     * an der Session. Ohne den Join taucht ein geloeschter Abend in der
     * Pyramide weiter auf.
     */
    @Query(
        """
        SELECT
            a.gradeValue                                                    AS gradeValue,
            COUNT(*)                                                        AS attemptCount,
            COALESCE(SUM(CASE WHEN a.outcome IN ('FLASH','TOP') THEN 1 ELSE 0 END), 0) AS sendCount,
            COALESCE(SUM(CASE WHEN a.outcome = 'FLASH' THEN 1 ELSE 0 END), 0)          AS flashCount
        FROM attempt a
        JOIN session s ON s.id = a.sessionId
        WHERE a.deletedAt IS NULL
          AND a.endedAt IS NOT NULL
          AND a.gradeValue IS NOT NULL
          AND a.kind = 'ATTEMPT'
          AND s.deletedAt IS NULL
          AND s.state = 'FINISHED'
          AND a.startedAt >= :since
        GROUP BY a.gradeValue
        ORDER BY a.gradeValue
        """,
    )
    fun observeGradeHistogram(since: Long): Flow<List<GradeBucket>>

    /** Ordnet einen Versuch einem Boulder zu - das Zusammenfuehren am Handy. */
    @Query(
        """
        UPDATE attempt SET problemId = :problemId, updatedAt = :now, syncState = 'PENDING'
        WHERE id = :attemptId
        """,
    )
    suspend fun setProblem(attemptId: String, problemId: String?, now: Long)

    /** Wie oft an einem Boulder gearbeitet wurde, ueber alle Sessions. */
    @Query(
        """
        SELECT
            a.problemId                                                     AS problemId,
            COUNT(*)                                                        AS attemptCount,
            COALESCE(SUM(CASE WHEN a.outcome IN ('FLASH','TOP') THEN 1 ELSE 0 END), 0) AS sendCount,
            MIN(a.startedAt)                                                AS firstAt,
            MAX(a.startedAt)                                                AS lastAt
        FROM attempt a
        JOIN session s ON s.id = a.sessionId
        WHERE a.problemId IS NOT NULL
          AND a.deletedAt IS NULL
          AND s.deletedAt IS NULL
        GROUP BY a.problemId
        """,
    )
    fun observeProblemTallies(): Flow<List<ProblemTally>>
}

/** Eine Stufe der Gradpyramide. */
data class GradeBucket(
    val gradeValue: Int,
    val attemptCount: Int,
    val sendCount: Int,
    val flashCount: Int,
)

/** Versuchsbilanz eines Boulders ueber alle Sessions. */
data class ProblemTally(
    val problemId: String,
    val attemptCount: Int,
    val sendCount: Int,
    val firstAt: Long,
    val lastAt: Long,
)

/** Projektion von [AttemptDao.aggregate]. */
data class AttemptAggregate(
    val attemptCount: Int,
    val sendCount: Int,
    val flashCount: Int,
    val workMs: Long,
    val hardestSendValue: Int?,
    val hrr60Avg: Double?,
    val climbHeightMeters: Double?,
    val maxClimbHeightMeters: Double?,
)

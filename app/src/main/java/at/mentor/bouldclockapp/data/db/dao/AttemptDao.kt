package at.mentor.bouldclockapp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import at.mentor.bouldclockapp.core.model.GradeSystem
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
     */
    @Query(
        """
        SELECT gradeValue AS gradeValue, gradeSystem AS gradeSystem FROM attempt
        WHERE gradeValue IS NOT NULL AND deletedAt IS NULL
        ORDER BY startedAt DESC LIMIT 1
        """,
    )
    suspend fun lastGrade(): LastGrade?

    @Query("UPDATE attempt SET deletedAt = :now, updatedAt = :now, syncState = 'PENDING' WHERE id = :id")
    suspend fun softDelete(id: String, now: Long)

    /**
     * Aggregat fuer die Zusammenfassung - eine Abfrage statt N Zeilen in den Speicher.
     *
     * ABORTED bleibt draussen: ein abgebrochener Versuch ist keine Belastung.
     * Ein Versuch ohne Ergebnis zaehlt dagegen mit: nach dem Ausloeser beendet man
     * den Burn, das Ergebnis tippt man erst in der Pause - und manchmal gar nicht.
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
            AVG(hrr60)                                                    AS hrr60Avg
        FROM attempt
        WHERE sessionId = :sessionId
          AND deletedAt IS NULL
          AND endedAt IS NOT NULL
          AND (outcome IS NULL OR outcome <> 'ABORTED')
        """,
    )
    suspend fun aggregate(sessionId: String): AttemptAggregate
}

/** Projektion von [AttemptDao.lastGrade]. */
data class LastGrade(
    val gradeValue: Int,
    val gradeSystem: GradeSystem?,
)

/** Projektion von [AttemptDao.aggregate]. */
data class AttemptAggregate(
    val attemptCount: Int,
    val sendCount: Int,
    val flashCount: Int,
    val workMs: Long,
    val hardestSendValue: Int?,
    val hrr60Avg: Double?,
)

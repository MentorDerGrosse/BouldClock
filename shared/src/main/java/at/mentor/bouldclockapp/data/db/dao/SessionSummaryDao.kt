package at.mentor.bouldclockapp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionSummaryDao {

    @Upsert
    suspend fun upsert(summary: SessionSummaryEntity)

    @Query("SELECT * FROM session_summary WHERE sessionId = :sessionId")
    suspend fun bySession(sessionId: String): SessionSummaryEntity?

    @Query("SELECT * FROM session_summary WHERE sessionId = :sessionId")
    fun observeBySession(sessionId: String): Flow<SessionSummaryEntity?>

    /** Die Historie auf der Uhr: zehn Zeilen lesen, nichts rechnen. */
    @Query("SELECT * FROM session_summary ORDER BY startedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<SessionSummaryEntity>>

    /**
     * Alles fuer die Auswertung am Handy.
     *
     * Ohne Grenze und ohne Aggregat in SQL: ein paar hundert Zeilen, und das
     * Zusammenfassen nach Woche, Monat und Jahr passiert in
     * [at.mentor.bouldclockapp.core.metrics.bucket] - dort ist es ohne
     * Datenbank testbar und kennt die Zeitzone des Geraets.
     */
    @Query("SELECT * FROM session_summary ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<SessionSummaryEntity>>

    /**
     * Entfernt die Zusammenfassung einer Session.
     *
     * Sie ist ein Zwischenspeicher, kein Rohdatum - und sie ist die Quelle
     * saemtlicher Summen. Faellt sie weg, verschwindet die geloeschte Session
     * ueberall: aus der Historie, den Hoehenmetern, den Diagrammen.
     */
    @Query("DELETE FROM session_summary WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: String)

    /** Referenzwerte fuer "dein Schnitt sind 31" - nur vergleichbare Sessions. */
    @Query(
        """
        SELECT
            COUNT(*)              AS sessionCount,
            AVG(attemptCount)     AS avgAttempts,
            AVG(sendCount)        AS avgSends,
            AVG(workMs)           AS avgWorkMs,
            AVG(restMs)           AS avgRestMs,
            AVG(hrr60Avg)         AS avgHrr60,
            MAX(hardestSendValue) AS bestSendValue
        FROM session_summary
        WHERE type = :type
          AND (:gymId IS NULL OR gymId = :gymId)
          AND startedAt >= :since
          AND sessionId <> :excludeSessionId
        """,
    )
    suspend fun baseline(
        type: SessionType,
        gymId: String?,
        since: Long,
        excludeSessionId: String,
    ): SessionBaseline

    @Query(
        """
        SELECT startedAt, hrr60Avg FROM session_summary
        WHERE hrr60Avg IS NOT NULL AND startedAt >= :since
        ORDER BY startedAt
        """,
    )
    fun observeHrr60Trend(since: Long): Flow<List<Hrr60Point>>
}

/** Vergleichswerte aus vergangenen Sessions. Alles nullable - am Anfang gibt es keine. */
data class SessionBaseline(
    val sessionCount: Int,
    val avgAttempts: Double?,
    val avgSends: Double?,
    val avgWorkMs: Double?,
    val avgRestMs: Double?,
    val avgHrr60: Double?,
    val bestSendValue: Int?,
)

/** Ein Punkt im Fitnesstrend. */
data class Hrr60Point(
    val startedAt: Long,
    val hrr60Avg: Int,
)

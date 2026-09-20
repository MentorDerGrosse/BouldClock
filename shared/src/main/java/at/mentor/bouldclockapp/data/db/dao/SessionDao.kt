package at.mentor.bouldclockapp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.data.db.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Enum-Werte stehen hier als String-Literale in den Queries. Das ist konsistent
 * zur Speicherung (Room persistiert Enums als Konstantenname) und damit ohnehin
 * Teil des Datenbankvertrags - siehe [at.mentor.bouldclockapp.core.model.SessionState].
 */
@Dao
interface SessionDao {

    @Upsert
    suspend fun upsert(session: SessionEntity)

    @Query("SELECT * FROM session WHERE id = :id")
    suspend fun byId(id: String): SessionEntity?

    @Query("SELECT * FROM session WHERE id = :id")
    fun observe(id: String): Flow<SessionEntity?>

    /**
     * Absturzwiederherstellung: eine Session, die nie sauber beendet wurde.
     * Beim App-Start aufrufen und "Weitermachen oder verwerfen?" anbieten.
     */
    @Query(
        """
        SELECT * FROM session
        WHERE state IN ('ACTIVE', 'PAUSED') AND deletedAt IS NULL
        ORDER BY startedAt DESC LIMIT 1
        """,
    )
    suspend fun findUnfinished(): SessionEntity?

    /**
     * Setzt jede offene Session ausser [keepId] auf ABANDONED.
     *
     * Sichert die Regel "hoechstens eine offene Session". Ohne sie sammeln sich
     * ACTIVE-Zeilen an, und nach dem Beenden der aktuellen holt
     * [findUnfinished] die naechstaeltere wieder hervor - die Session wirkt dann,
     * als liesse sie sich nicht beenden.
     *
     * Verworfen, nicht geloescht: die Versuche darin bleiben erhalten.
     */
    @Query(
        """
        UPDATE session
        SET state = 'ABANDONED', updatedAt = :now, syncState = 'PENDING'
        WHERE state IN ('ACTIVE', 'PAUSED') AND deletedAt IS NULL AND id <> :keepId
        """,
    )
    suspend fun abandonOpenExcept(keepId: String, now: Long): Int

    @Query(
        """
        SELECT * FROM session
        WHERE deletedAt IS NULL AND state = 'FINISHED'
        ORDER BY startedAt DESC LIMIT :limit
        """,
    )
    fun observeRecent(limit: Int): Flow<List<SessionEntity>>

    /** Beendete Sessions, die noch nicht uebertragen wurden. */
    @Query(
        """
        SELECT * FROM session
        WHERE state = 'FINISHED' AND deletedAt IS NULL AND syncState = 'PENDING'
        ORDER BY startedAt
        """,
    )
    suspend fun pendingSync(): List<SessionEntity>

    /**
     * Die letzte vergleichbare Session: gleicher Typ, gleiche Halle. Basis fuer
     * das Delta in der Zusammenfassung.
     */
    @Query(
        """
        SELECT * FROM session
        WHERE deletedAt IS NULL
          AND state = 'FINISHED'
          AND type = :type
          AND (:gymId IS NULL OR gymId = :gymId)
          AND startedAt < :before
        ORDER BY startedAt DESC LIMIT 1
        """,
    )
    suspend fun previousComparable(type: SessionType, gymId: String?, before: Long): SessionEntity?
}

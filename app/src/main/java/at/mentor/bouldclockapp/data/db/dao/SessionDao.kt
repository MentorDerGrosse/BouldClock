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

    @Query(
        """
        SELECT * FROM session
        WHERE deletedAt IS NULL AND state = 'FINISHED'
        ORDER BY startedAt DESC LIMIT :limit
        """,
    )
    fun observeRecent(limit: Int): Flow<List<SessionEntity>>

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

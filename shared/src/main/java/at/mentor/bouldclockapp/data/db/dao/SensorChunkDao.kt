package at.mentor.bouldclockapp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import at.mentor.bouldclockapp.data.db.entity.SensorChunkEntity

@Dao
interface SensorChunkDao {

    @Upsert
    suspend fun upsert(chunk: SensorChunkEntity)

    @Query("SELECT * FROM sensor_chunk WHERE sessionId = :sessionId ORDER BY startedAt")
    suspend fun bySession(sessionId: String): List<SensorChunkEntity>

    /**
     * Sessions, von denen noch Dateien ausstehen.
     *
     * Eigene Warteschlange, weil eine Session laengst uebertragen sein kann,
     * waehrend ihre Dateien noch fehlen - etwa weil die Gegenstelle beim
     * Beenden nicht erreichbar war.
     */
    @Query("SELECT DISTINCT sessionId FROM sensor_chunk WHERE syncState = 'PENDING'")
    suspend fun sessionsWithPendingFiles(): List<String>

    @Query("SELECT * FROM sensor_chunk WHERE relativePath = :relativePath LIMIT 1")
    suspend fun byRelativePath(relativePath: String): SensorChunkEntity?

    /** Kandidaten fuers Aufraeumen: schon aufs Handy uebertragen. */
    @Query("SELECT * FROM sensor_chunk WHERE syncState = 'SYNCED' ORDER BY startedAt LIMIT :limit")
    suspend fun synced(limit: Int): List<SensorChunkEntity>

    @Query("DELETE FROM sensor_chunk WHERE id = :id")
    suspend fun delete(id: String)
}

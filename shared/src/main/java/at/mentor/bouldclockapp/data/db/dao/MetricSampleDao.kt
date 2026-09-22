package at.mentor.bouldclockapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import at.mentor.bouldclockapp.core.model.SessionMetric
import at.mentor.bouldclockapp.data.db.entity.MetricSampleEntity

@Dao
interface MetricSampleDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(samples: List<MetricSampleEntity>)

    /** Stand am Ende der Session - der hoechste gemeldete Wert. */
    @Query("SELECT MAX(value) FROM metric_sample WHERE sessionId = :sessionId AND metric = :metric")
    suspend fun total(sessionId: String, metric: SessionMetric): Double?

    /**
     * Stand zum Zeitpunkt :at. Zwei davon ergeben den Verbrauch eines Versuchs -
     * so entsteht "Kalorien an der Wand".
     */
    @Query(
        """
        SELECT value FROM metric_sample
        WHERE sessionId = :sessionId AND metric = :metric AND timestampMs <= :at
        ORDER BY timestampMs DESC LIMIT 1
        """,
    )
    suspend fun latestAt(sessionId: String, metric: SessionMetric, at: Long): Double?

    /** Alle Werte einer Session - fuer die Uebertragung ans Handy. */
    @Query("SELECT * FROM metric_sample WHERE sessionId = :sessionId ORDER BY timestampMs")
    suspend fun bySession(sessionId: String): List<MetricSampleEntity>

    @Query("DELETE FROM metric_sample WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: String)
}

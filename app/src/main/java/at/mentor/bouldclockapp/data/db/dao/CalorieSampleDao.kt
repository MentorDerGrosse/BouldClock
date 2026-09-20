package at.mentor.bouldclockapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import at.mentor.bouldclockapp.data.db.entity.CalorieSampleEntity

@Dao
interface CalorieSampleDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(samples: List<CalorieSampleEntity>)

    /** Gesamtverbrauch der Session - der letzte gemeldete Stand. */
    @Query("SELECT MAX(kcalTotal) FROM calorie_sample WHERE sessionId = :sessionId")
    suspend fun total(sessionId: String): Double?

    /** Stand zum Zeitpunkt :at. Zwei davon ergeben den Verbrauch eines Versuchs. */
    @Query(
        """
        SELECT kcalTotal FROM calorie_sample
        WHERE sessionId = :sessionId AND timestampMs <= :at
        ORDER BY timestampMs DESC LIMIT 1
        """,
    )
    suspend fun latestAt(sessionId: String, at: Long): Double?

    @Query("DELETE FROM calorie_sample WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: String)
}

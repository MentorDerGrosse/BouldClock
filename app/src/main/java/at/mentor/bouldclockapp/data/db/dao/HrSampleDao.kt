package at.mentor.bouldclockapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import at.mentor.bouldclockapp.data.db.entity.HrSampleEntity

@Dao
interface HrSampleDao {

    /** IGNORE statt REPLACE: doppelte Zeitstempel nach einem Neustart sollen nichts ueberschreiben. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(samples: List<HrSampleEntity>)

    @Query(
        """
        SELECT * FROM hr_sample
        WHERE sessionId = :sessionId AND timestampMs BETWEEN :from AND :to
        ORDER BY timestampMs
        """,
    )
    suspend fun between(sessionId: String, from: Long, to: Long): List<HrSampleEntity>

    /**
     * Puls moeglichst nah an :at, innerhalb von :toleranceMs.
     *
     * Kein exakter Treffer, weil der Sensor Luecken hat - waehrend eines Zuges
     * liefert PPG am Handgelenk regelmaessig gar nichts Brauchbares.
     * [minAccuracy] filtert die unbrauchbaren Samples raus.
     */
    @Query(
        """
        SELECT bpm FROM hr_sample
        WHERE sessionId = :sessionId
          AND accuracy >= :minAccuracy
          AND ABS(timestampMs - :at) <= :toleranceMs
        ORDER BY ABS(timestampMs - :at)
        LIMIT 1
        """,
    )
    suspend fun nearest(sessionId: String, at: Long, toleranceMs: Long, minAccuracy: Int): Int?

    @Query(
        """
        SELECT CAST(ROUND(AVG(bpm)) AS INTEGER) FROM hr_sample
        WHERE sessionId = :sessionId
          AND accuracy >= :minAccuracy
          AND timestampMs BETWEEN :from AND :to
        """,
    )
    suspend fun avgBetween(sessionId: String, from: Long, to: Long, minAccuracy: Int): Int?

    @Query(
        """
        SELECT MAX(bpm) FROM hr_sample
        WHERE sessionId = :sessionId
          AND accuracy >= :minAccuracy
          AND timestampMs BETWEEN :from AND :to
        """,
    )
    suspend fun maxBetween(sessionId: String, from: Long, to: Long, minAccuracy: Int): Int?

    @Query("DELETE FROM hr_sample WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: String)
}

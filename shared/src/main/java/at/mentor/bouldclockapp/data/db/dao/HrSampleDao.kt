package at.mentor.bouldclockapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import at.mentor.bouldclockapp.data.db.entity.HrSampleEntity
import kotlinx.coroutines.flow.Flow

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

    /** Alle Werte einer Session - fuer die Uebertragung ans Handy. */
    @Query("SELECT * FROM hr_sample WHERE sessionId = :sessionId ORDER BY timestampMs")
    suspend fun bySession(sessionId: String): List<HrSampleEntity>

    /** Alle Werte einer Session, beobachtbar - fuer den Verlauf in der Detailansicht. */
    @Query(
        """
        SELECT * FROM hr_sample
        WHERE sessionId = :sessionId AND accuracy >= :minAccuracy
        ORDER BY timestampMs
        """,
    )
    fun observeBySession(sessionId: String, minAccuracy: Int): Flow<List<HrSampleEntity>>

    /**
     * Kennzahlen je Session in einer Abfrage.
     *
     * Fuer Verlaeufe ueber Wochen und Monate: die Zusammenfassung kennt Schnitt
     * und Maximum, aber kein Minimum - und alle Rohwerte zu laden waere bei
     * dreitausend Messpunkten je Session nicht vertretbar.
     */
    @Query(
        """
        SELECT h.sessionId                        AS sessionId,
               MIN(h.bpm)                         AS minBpm,
               MAX(h.bpm)                         AS maxBpm,
               CAST(ROUND(AVG(h.bpm)) AS INTEGER) AS avgBpm,
               COUNT(*)                           AS samples
        FROM hr_sample h
        JOIN session s ON s.id = h.sessionId
        WHERE h.accuracy >= :minAccuracy AND s.deletedAt IS NULL
        GROUP BY h.sessionId
        """,
    )
    fun observeSessionRanges(minAccuracy: Int): Flow<List<HrSessionRange>>

    /**
     * Zeit je Pulszone, in Sekunden, je Session.
     *
     * Gezaehlt statt gemessen: der Puls kommt sekuendlich und lueckenlos, ein
     * Messwert ist also eine Sekunde. Das spart es, dreitausend Zeilen je
     * Session in den Speicher zu holen, nur um Abstaende zu addieren.
     * Die Grenzen kommen von aussen, weil sie am Profil haengen.
     */
    @Query(
        """
        SELECT h.sessionId AS sessionId,
               SUM(CASE WHEN h.bpm <  :z1 THEN 1 ELSE 0 END) AS restSeconds,
               SUM(CASE WHEN h.bpm >= :z1 AND h.bpm < :z2 THEN 1 ELSE 0 END) AS recoverySeconds,
               SUM(CASE WHEN h.bpm >= :z2 AND h.bpm < :z3 THEN 1 ELSE 0 END) AS baseSeconds,
               SUM(CASE WHEN h.bpm >= :z3 AND h.bpm < :z4 THEN 1 ELSE 0 END) AS tempoSeconds,
               SUM(CASE WHEN h.bpm >= :z4 AND h.bpm < :z5 THEN 1 ELSE 0 END) AS thresholdSeconds,
               SUM(CASE WHEN h.bpm >= :z5 THEN 1 ELSE 0 END) AS maximalSeconds
        FROM hr_sample h
        JOIN session s ON s.id = h.sessionId
        WHERE h.accuracy >= :minAccuracy AND s.deletedAt IS NULL
        GROUP BY h.sessionId
        """,
    )
    fun observeZoneSeconds(
        minAccuracy: Int,
        z1: Int,
        z2: Int,
        z3: Int,
        z4: Int,
        z5: Int,
    ): Flow<List<HrZoneSeconds>>

    @Query("DELETE FROM hr_sample WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: String)
}

/** Puls einer Session auf drei Zahlen reduziert. */
data class HrSessionRange(
    val sessionId: String,
    val minBpm: Int,
    val maxBpm: Int,
    val avgBpm: Int,
    val samples: Int,
)

/** Sekunden je Pulszone, in der Reihenfolge von HeartRateZone. */
data class HrZoneSeconds(
    val sessionId: String,
    val restSeconds: Int,
    val recoverySeconds: Int,
    val baseSeconds: Int,
    val tempoSeconds: Int,
    val thresholdSeconds: Int,
    val maximalSeconds: Int,
) {
    val total: Int
        get() = restSeconds + recoverySeconds + baseSeconds +
            tempoSeconds + thresholdSeconds + maximalSeconds
}

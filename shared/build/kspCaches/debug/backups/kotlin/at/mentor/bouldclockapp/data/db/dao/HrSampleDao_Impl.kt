package at.mentor.bouldclockapp.`data`.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import at.mentor.bouldclockapp.`data`.db.entity.HrSampleEntity
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class HrSampleDao_Impl(
  __db: RoomDatabase,
) : HrSampleDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfHrSampleEntity: EntityInsertAdapter<HrSampleEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfHrSampleEntity = object : EntityInsertAdapter<HrSampleEntity>() {
      protected override fun createQuery(): String = "INSERT OR IGNORE INTO `hr_sample` (`sessionId`,`timestampMs`,`bpm`,`accuracy`) VALUES (?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: HrSampleEntity) {
        statement.bindText(1, entity.sessionId)
        statement.bindLong(2, entity.timestampMs)
        statement.bindLong(3, entity.bpm.toLong())
        statement.bindLong(4, entity.accuracy.toLong())
      }
    }
  }

  public override suspend fun insertAll(samples: List<HrSampleEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfHrSampleEntity.insert(_connection, samples)
  }

  public override suspend fun between(
    sessionId: String,
    from: Long,
    to: Long,
  ): List<HrSampleEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM hr_sample
        |        WHERE sessionId = ? AND timestampMs BETWEEN ? AND ?
        |        ORDER BY timestampMs
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, from)
        _argIndex = 3
        _stmt.bindLong(_argIndex, to)
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfTimestampMs: Int = getColumnIndexOrThrow(_stmt, "timestampMs")
        val _columnIndexOfBpm: Int = getColumnIndexOrThrow(_stmt, "bpm")
        val _columnIndexOfAccuracy: Int = getColumnIndexOrThrow(_stmt, "accuracy")
        val _result: MutableList<HrSampleEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HrSampleEntity
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpTimestampMs: Long
          _tmpTimestampMs = _stmt.getLong(_columnIndexOfTimestampMs)
          val _tmpBpm: Int
          _tmpBpm = _stmt.getLong(_columnIndexOfBpm).toInt()
          val _tmpAccuracy: Int
          _tmpAccuracy = _stmt.getLong(_columnIndexOfAccuracy).toInt()
          _item = HrSampleEntity(_tmpSessionId,_tmpTimestampMs,_tmpBpm,_tmpAccuracy)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun nearest(
    sessionId: String,
    at: Long,
    toleranceMs: Long,
    minAccuracy: Int,
  ): Int? {
    val _sql: String = """
        |
        |        SELECT bpm FROM hr_sample
        |        WHERE sessionId = ?
        |          AND accuracy >= ?
        |          AND ABS(timestampMs - ?) <= ?
        |        ORDER BY ABS(timestampMs - ?)
        |        LIMIT 1
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, minAccuracy.toLong())
        _argIndex = 3
        _stmt.bindLong(_argIndex, at)
        _argIndex = 4
        _stmt.bindLong(_argIndex, toleranceMs)
        _argIndex = 5
        _stmt.bindLong(_argIndex, at)
        val _result: Int?
        if (_stmt.step()) {
          if (_stmt.isNull(0)) {
            _result = null
          } else {
            _result = _stmt.getLong(0).toInt()
          }
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun avgBetween(
    sessionId: String,
    from: Long,
    to: Long,
    minAccuracy: Int,
  ): Int? {
    val _sql: String = """
        |
        |        SELECT CAST(ROUND(AVG(bpm)) AS INTEGER) FROM hr_sample
        |        WHERE sessionId = ?
        |          AND accuracy >= ?
        |          AND timestampMs BETWEEN ? AND ?
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, minAccuracy.toLong())
        _argIndex = 3
        _stmt.bindLong(_argIndex, from)
        _argIndex = 4
        _stmt.bindLong(_argIndex, to)
        val _result: Int?
        if (_stmt.step()) {
          val _tmp: Int?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(0).toInt()
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun maxBetween(
    sessionId: String,
    from: Long,
    to: Long,
    minAccuracy: Int,
  ): Int? {
    val _sql: String = """
        |
        |        SELECT MAX(bpm) FROM hr_sample
        |        WHERE sessionId = ?
        |          AND accuracy >= ?
        |          AND timestampMs BETWEEN ? AND ?
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, minAccuracy.toLong())
        _argIndex = 3
        _stmt.bindLong(_argIndex, from)
        _argIndex = 4
        _stmt.bindLong(_argIndex, to)
        val _result: Int?
        if (_stmt.step()) {
          val _tmp: Int?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(0).toInt()
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun bySession(sessionId: String): List<HrSampleEntity> {
    val _sql: String = "SELECT * FROM hr_sample WHERE sessionId = ? ORDER BY timestampMs"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfTimestampMs: Int = getColumnIndexOrThrow(_stmt, "timestampMs")
        val _columnIndexOfBpm: Int = getColumnIndexOrThrow(_stmt, "bpm")
        val _columnIndexOfAccuracy: Int = getColumnIndexOrThrow(_stmt, "accuracy")
        val _result: MutableList<HrSampleEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HrSampleEntity
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpTimestampMs: Long
          _tmpTimestampMs = _stmt.getLong(_columnIndexOfTimestampMs)
          val _tmpBpm: Int
          _tmpBpm = _stmt.getLong(_columnIndexOfBpm).toInt()
          val _tmpAccuracy: Int
          _tmpAccuracy = _stmt.getLong(_columnIndexOfAccuracy).toInt()
          _item = HrSampleEntity(_tmpSessionId,_tmpTimestampMs,_tmpBpm,_tmpAccuracy)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeBySession(sessionId: String, minAccuracy: Int): Flow<List<HrSampleEntity>> {
    val _sql: String = """
        |
        |        SELECT * FROM hr_sample
        |        WHERE sessionId = ? AND accuracy >= ?
        |        ORDER BY timestampMs
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("hr_sample")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, minAccuracy.toLong())
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfTimestampMs: Int = getColumnIndexOrThrow(_stmt, "timestampMs")
        val _columnIndexOfBpm: Int = getColumnIndexOrThrow(_stmt, "bpm")
        val _columnIndexOfAccuracy: Int = getColumnIndexOrThrow(_stmt, "accuracy")
        val _result: MutableList<HrSampleEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HrSampleEntity
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpTimestampMs: Long
          _tmpTimestampMs = _stmt.getLong(_columnIndexOfTimestampMs)
          val _tmpBpm: Int
          _tmpBpm = _stmt.getLong(_columnIndexOfBpm).toInt()
          val _tmpAccuracy: Int
          _tmpAccuracy = _stmt.getLong(_columnIndexOfAccuracy).toInt()
          _item = HrSampleEntity(_tmpSessionId,_tmpTimestampMs,_tmpBpm,_tmpAccuracy)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeSessionRanges(minAccuracy: Int): Flow<List<HrSessionRange>> {
    val _sql: String = """
        |
        |        SELECT h.sessionId                        AS sessionId,
        |               MIN(h.bpm)                         AS minBpm,
        |               MAX(h.bpm)                         AS maxBpm,
        |               CAST(ROUND(AVG(h.bpm)) AS INTEGER) AS avgBpm,
        |               COUNT(*)                           AS samples
        |        FROM hr_sample h
        |        JOIN session s ON s.id = h.sessionId
        |        WHERE h.accuracy >= ? AND s.deletedAt IS NULL
        |        GROUP BY h.sessionId
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("hr_sample", "session")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, minAccuracy.toLong())
        val _columnIndexOfSessionId: Int = 0
        val _columnIndexOfMinBpm: Int = 1
        val _columnIndexOfMaxBpm: Int = 2
        val _columnIndexOfAvgBpm: Int = 3
        val _columnIndexOfSamples: Int = 4
        val _result: MutableList<HrSessionRange> = mutableListOf()
        while (_stmt.step()) {
          val _item: HrSessionRange
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpMinBpm: Int
          _tmpMinBpm = _stmt.getLong(_columnIndexOfMinBpm).toInt()
          val _tmpMaxBpm: Int
          _tmpMaxBpm = _stmt.getLong(_columnIndexOfMaxBpm).toInt()
          val _tmpAvgBpm: Int
          _tmpAvgBpm = _stmt.getLong(_columnIndexOfAvgBpm).toInt()
          val _tmpSamples: Int
          _tmpSamples = _stmt.getLong(_columnIndexOfSamples).toInt()
          _item = HrSessionRange(_tmpSessionId,_tmpMinBpm,_tmpMaxBpm,_tmpAvgBpm,_tmpSamples)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeZoneSeconds(
    minAccuracy: Int,
    z1: Int,
    z2: Int,
    z3: Int,
    z4: Int,
    z5: Int,
  ): Flow<List<HrZoneSeconds>> {
    val _sql: String = """
        |
        |        SELECT h.sessionId AS sessionId,
        |               SUM(CASE WHEN h.bpm <  ? THEN 1 ELSE 0 END) AS restSeconds,
        |               SUM(CASE WHEN h.bpm >= ? AND h.bpm < ? THEN 1 ELSE 0 END) AS recoverySeconds,
        |               SUM(CASE WHEN h.bpm >= ? AND h.bpm < ? THEN 1 ELSE 0 END) AS baseSeconds,
        |               SUM(CASE WHEN h.bpm >= ? AND h.bpm < ? THEN 1 ELSE 0 END) AS tempoSeconds,
        |               SUM(CASE WHEN h.bpm >= ? AND h.bpm < ? THEN 1 ELSE 0 END) AS thresholdSeconds,
        |               SUM(CASE WHEN h.bpm >= ? THEN 1 ELSE 0 END) AS maximalSeconds
        |        FROM hr_sample h
        |        JOIN session s ON s.id = h.sessionId
        |        WHERE h.accuracy >= ? AND s.deletedAt IS NULL
        |        GROUP BY h.sessionId
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("hr_sample", "session")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, z1.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, z1.toLong())
        _argIndex = 3
        _stmt.bindLong(_argIndex, z2.toLong())
        _argIndex = 4
        _stmt.bindLong(_argIndex, z2.toLong())
        _argIndex = 5
        _stmt.bindLong(_argIndex, z3.toLong())
        _argIndex = 6
        _stmt.bindLong(_argIndex, z3.toLong())
        _argIndex = 7
        _stmt.bindLong(_argIndex, z4.toLong())
        _argIndex = 8
        _stmt.bindLong(_argIndex, z4.toLong())
        _argIndex = 9
        _stmt.bindLong(_argIndex, z5.toLong())
        _argIndex = 10
        _stmt.bindLong(_argIndex, z5.toLong())
        _argIndex = 11
        _stmt.bindLong(_argIndex, minAccuracy.toLong())
        val _columnIndexOfSessionId: Int = 0
        val _columnIndexOfRestSeconds: Int = 1
        val _columnIndexOfRecoverySeconds: Int = 2
        val _columnIndexOfBaseSeconds: Int = 3
        val _columnIndexOfTempoSeconds: Int = 4
        val _columnIndexOfThresholdSeconds: Int = 5
        val _columnIndexOfMaximalSeconds: Int = 6
        val _result: MutableList<HrZoneSeconds> = mutableListOf()
        while (_stmt.step()) {
          val _item: HrZoneSeconds
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpRestSeconds: Int
          _tmpRestSeconds = _stmt.getLong(_columnIndexOfRestSeconds).toInt()
          val _tmpRecoverySeconds: Int
          _tmpRecoverySeconds = _stmt.getLong(_columnIndexOfRecoverySeconds).toInt()
          val _tmpBaseSeconds: Int
          _tmpBaseSeconds = _stmt.getLong(_columnIndexOfBaseSeconds).toInt()
          val _tmpTempoSeconds: Int
          _tmpTempoSeconds = _stmt.getLong(_columnIndexOfTempoSeconds).toInt()
          val _tmpThresholdSeconds: Int
          _tmpThresholdSeconds = _stmt.getLong(_columnIndexOfThresholdSeconds).toInt()
          val _tmpMaximalSeconds: Int
          _tmpMaximalSeconds = _stmt.getLong(_columnIndexOfMaximalSeconds).toInt()
          _item = HrZoneSeconds(_tmpSessionId,_tmpRestSeconds,_tmpRecoverySeconds,_tmpBaseSeconds,_tmpTempoSeconds,_tmpThresholdSeconds,_tmpMaximalSeconds)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteForSession(sessionId: String) {
    val _sql: String = "DELETE FROM hr_sample WHERE sessionId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}

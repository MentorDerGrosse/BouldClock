package at.mentor.bouldclockapp.`data`.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
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

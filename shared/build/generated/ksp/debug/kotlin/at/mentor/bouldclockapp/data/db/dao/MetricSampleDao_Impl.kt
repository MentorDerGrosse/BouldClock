package at.mentor.bouldclockapp.`data`.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import at.mentor.bouldclockapp.`data`.db.entity.MetricSampleEntity
import at.mentor.bouldclockapp.core.model.SessionMetric
import javax.`annotation`.processing.Generated
import kotlin.Double
import kotlin.IllegalArgumentException
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
public class MetricSampleDao_Impl(
  __db: RoomDatabase,
) : MetricSampleDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfMetricSampleEntity: EntityInsertAdapter<MetricSampleEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfMetricSampleEntity = object : EntityInsertAdapter<MetricSampleEntity>() {
      protected override fun createQuery(): String = "INSERT OR IGNORE INTO `metric_sample` (`sessionId`,`metric`,`timestampMs`,`value`) VALUES (?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MetricSampleEntity) {
        statement.bindText(1, entity.sessionId)
        statement.bindText(2, __SessionMetric_enumToString(entity.metric))
        statement.bindLong(3, entity.timestampMs)
        statement.bindDouble(4, entity.value)
      }
    }
  }

  public override suspend fun insertAll(samples: List<MetricSampleEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfMetricSampleEntity.insert(_connection, samples)
  }

  public override suspend fun total(sessionId: String, metric: SessionMetric): Double? {
    val _sql: String = "SELECT MAX(value) FROM metric_sample WHERE sessionId = ? AND metric = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        _argIndex = 2
        _stmt.bindText(_argIndex, __SessionMetric_enumToString(metric))
        val _result: Double?
        if (_stmt.step()) {
          val _tmp: Double?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getDouble(0)
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

  public override suspend fun latestAt(
    sessionId: String,
    metric: SessionMetric,
    at: Long,
  ): Double? {
    val _sql: String = """
        |
        |        SELECT value FROM metric_sample
        |        WHERE sessionId = ? AND metric = ? AND timestampMs <= ?
        |        ORDER BY timestampMs DESC LIMIT 1
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        _argIndex = 2
        _stmt.bindText(_argIndex, __SessionMetric_enumToString(metric))
        _argIndex = 3
        _stmt.bindLong(_argIndex, at)
        val _result: Double?
        if (_stmt.step()) {
          if (_stmt.isNull(0)) {
            _result = null
          } else {
            _result = _stmt.getDouble(0)
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

  public override suspend fun bySession(sessionId: String): List<MetricSampleEntity> {
    val _sql: String = "SELECT * FROM metric_sample WHERE sessionId = ? ORDER BY timestampMs"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfMetric: Int = getColumnIndexOrThrow(_stmt, "metric")
        val _columnIndexOfTimestampMs: Int = getColumnIndexOrThrow(_stmt, "timestampMs")
        val _columnIndexOfValue: Int = getColumnIndexOrThrow(_stmt, "value")
        val _result: MutableList<MetricSampleEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MetricSampleEntity
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpMetric: SessionMetric
          _tmpMetric = __SessionMetric_stringToEnum(_stmt.getText(_columnIndexOfMetric))
          val _tmpTimestampMs: Long
          _tmpTimestampMs = _stmt.getLong(_columnIndexOfTimestampMs)
          val _tmpValue: Double
          _tmpValue = _stmt.getDouble(_columnIndexOfValue)
          _item = MetricSampleEntity(_tmpSessionId,_tmpMetric,_tmpTimestampMs,_tmpValue)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteForSession(sessionId: String) {
    val _sql: String = "DELETE FROM metric_sample WHERE sessionId = ?"
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

  private fun __SessionMetric_enumToString(_value: SessionMetric): String = when (_value) {
    SessionMetric.CALORIES -> "CALORIES"
    SessionMetric.ELEVATION_GAIN -> "ELEVATION_GAIN"
  }

  private fun __SessionMetric_stringToEnum(_value: String): SessionMetric = when (_value) {
    "CALORIES" -> SessionMetric.CALORIES
    "ELEVATION_GAIN" -> SessionMetric.ELEVATION_GAIN
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}

package at.mentor.bouldclockapp.`data`.db.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import at.mentor.bouldclockapp.`data`.db.entity.SensorChunkEntity
import at.mentor.bouldclockapp.core.model.SensorKind
import at.mentor.bouldclockapp.core.model.SyncState
import javax.`annotation`.processing.Generated
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
public class SensorChunkDao_Impl(
  __db: RoomDatabase,
) : SensorChunkDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfSensorChunkEntity: EntityUpsertAdapter<SensorChunkEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfSensorChunkEntity = EntityUpsertAdapter<SensorChunkEntity>(object : EntityInsertAdapter<SensorChunkEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `sensor_chunk` (`id`,`sessionId`,`sensor`,`relativePath`,`startedAt`,`endedAt`,`sampleRateHz`,`sampleCount`,`sizeBytes`,`syncState`) VALUES (?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SensorChunkEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.sessionId)
        statement.bindText(3, __SensorKind_enumToString(entity.sensor))
        statement.bindText(4, entity.relativePath)
        statement.bindLong(5, entity.startedAt)
        statement.bindLong(6, entity.endedAt)
        statement.bindLong(7, entity.sampleRateHz.toLong())
        statement.bindLong(8, entity.sampleCount.toLong())
        statement.bindLong(9, entity.sizeBytes)
        statement.bindText(10, __SyncState_enumToString(entity.syncState))
      }
    }, object : EntityDeleteOrUpdateAdapter<SensorChunkEntity>() {
      protected override fun createQuery(): String = "UPDATE `sensor_chunk` SET `id` = ?,`sessionId` = ?,`sensor` = ?,`relativePath` = ?,`startedAt` = ?,`endedAt` = ?,`sampleRateHz` = ?,`sampleCount` = ?,`sizeBytes` = ?,`syncState` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SensorChunkEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.sessionId)
        statement.bindText(3, __SensorKind_enumToString(entity.sensor))
        statement.bindText(4, entity.relativePath)
        statement.bindLong(5, entity.startedAt)
        statement.bindLong(6, entity.endedAt)
        statement.bindLong(7, entity.sampleRateHz.toLong())
        statement.bindLong(8, entity.sampleCount.toLong())
        statement.bindLong(9, entity.sizeBytes)
        statement.bindText(10, __SyncState_enumToString(entity.syncState))
        statement.bindText(11, entity.id)
      }
    })
  }

  public override suspend fun upsert(chunk: SensorChunkEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfSensorChunkEntity.upsert(_connection, chunk)
  }

  public override suspend fun bySession(sessionId: String): List<SensorChunkEntity> {
    val _sql: String = "SELECT * FROM sensor_chunk WHERE sessionId = ? ORDER BY startedAt"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfSensor: Int = getColumnIndexOrThrow(_stmt, "sensor")
        val _columnIndexOfRelativePath: Int = getColumnIndexOrThrow(_stmt, "relativePath")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfSampleRateHz: Int = getColumnIndexOrThrow(_stmt, "sampleRateHz")
        val _columnIndexOfSampleCount: Int = getColumnIndexOrThrow(_stmt, "sampleCount")
        val _columnIndexOfSizeBytes: Int = getColumnIndexOrThrow(_stmt, "sizeBytes")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: MutableList<SensorChunkEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SensorChunkEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpSensor: SensorKind
          _tmpSensor = __SensorKind_stringToEnum(_stmt.getText(_columnIndexOfSensor))
          val _tmpRelativePath: String
          _tmpRelativePath = _stmt.getText(_columnIndexOfRelativePath)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long
          _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          val _tmpSampleRateHz: Int
          _tmpSampleRateHz = _stmt.getLong(_columnIndexOfSampleRateHz).toInt()
          val _tmpSampleCount: Int
          _tmpSampleCount = _stmt.getLong(_columnIndexOfSampleCount).toInt()
          val _tmpSizeBytes: Long
          _tmpSizeBytes = _stmt.getLong(_columnIndexOfSizeBytes)
          val _tmpSyncState: SyncState
          _tmpSyncState = __SyncState_stringToEnum(_stmt.getText(_columnIndexOfSyncState))
          _item = SensorChunkEntity(_tmpId,_tmpSessionId,_tmpSensor,_tmpRelativePath,_tmpStartedAt,_tmpEndedAt,_tmpSampleRateHz,_tmpSampleCount,_tmpSizeBytes,_tmpSyncState)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun synced(limit: Int): List<SensorChunkEntity> {
    val _sql: String = "SELECT * FROM sensor_chunk WHERE syncState = 'SYNCED' ORDER BY startedAt LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfSensor: Int = getColumnIndexOrThrow(_stmt, "sensor")
        val _columnIndexOfRelativePath: Int = getColumnIndexOrThrow(_stmt, "relativePath")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfSampleRateHz: Int = getColumnIndexOrThrow(_stmt, "sampleRateHz")
        val _columnIndexOfSampleCount: Int = getColumnIndexOrThrow(_stmt, "sampleCount")
        val _columnIndexOfSizeBytes: Int = getColumnIndexOrThrow(_stmt, "sizeBytes")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: MutableList<SensorChunkEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SensorChunkEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpSensor: SensorKind
          _tmpSensor = __SensorKind_stringToEnum(_stmt.getText(_columnIndexOfSensor))
          val _tmpRelativePath: String
          _tmpRelativePath = _stmt.getText(_columnIndexOfRelativePath)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long
          _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          val _tmpSampleRateHz: Int
          _tmpSampleRateHz = _stmt.getLong(_columnIndexOfSampleRateHz).toInt()
          val _tmpSampleCount: Int
          _tmpSampleCount = _stmt.getLong(_columnIndexOfSampleCount).toInt()
          val _tmpSizeBytes: Long
          _tmpSizeBytes = _stmt.getLong(_columnIndexOfSizeBytes)
          val _tmpSyncState: SyncState
          _tmpSyncState = __SyncState_stringToEnum(_stmt.getText(_columnIndexOfSyncState))
          _item = SensorChunkEntity(_tmpId,_tmpSessionId,_tmpSensor,_tmpRelativePath,_tmpStartedAt,_tmpEndedAt,_tmpSampleRateHz,_tmpSampleCount,_tmpSizeBytes,_tmpSyncState)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: String) {
    val _sql: String = "DELETE FROM sensor_chunk WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __SensorKind_enumToString(_value: SensorKind): String = when (_value) {
    SensorKind.ACCELEROMETER -> "ACCELEROMETER"
    SensorKind.GYROSCOPE -> "GYROSCOPE"
    SensorKind.PRESSURE -> "PRESSURE"
  }

  private fun __SyncState_enumToString(_value: SyncState): String = when (_value) {
    SyncState.PENDING -> "PENDING"
    SyncState.SYNCED -> "SYNCED"
  }

  private fun __SensorKind_stringToEnum(_value: String): SensorKind = when (_value) {
    "ACCELEROMETER" -> SensorKind.ACCELEROMETER
    "GYROSCOPE" -> SensorKind.GYROSCOPE
    "PRESSURE" -> SensorKind.PRESSURE
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  private fun __SyncState_stringToEnum(_value: String): SyncState = when (_value) {
    "PENDING" -> SyncState.PENDING
    "SYNCED" -> SyncState.SYNCED
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}

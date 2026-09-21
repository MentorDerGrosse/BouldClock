package at.mentor.bouldclockapp.`data`.db.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import at.mentor.bouldclockapp.`data`.db.entity.SessionSummaryEntity
import at.mentor.bouldclockapp.core.model.SessionType
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
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SessionSummaryDao_Impl(
  __db: RoomDatabase,
) : SessionSummaryDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfSessionSummaryEntity: EntityUpsertAdapter<SessionSummaryEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfSessionSummaryEntity = EntityUpsertAdapter<SessionSummaryEntity>(object : EntityInsertAdapter<SessionSummaryEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `session_summary` (`sessionId`,`gymId`,`type`,`startedAt`,`totalMs`,`workMs`,`restMs`,`pausedMs`,`attemptCount`,`sendCount`,`flashCount`,`hardestSendValue`,`hrAvg`,`hrMax`,`hrr60Avg`,`caloriesTotal`,`caloriesOnWall`,`climbHeightMeters`,`maxClimbHeightMeters`,`rpe`,`computedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SessionSummaryEntity) {
        statement.bindText(1, entity.sessionId)
        val _tmpGymId: String? = entity.gymId
        if (_tmpGymId == null) {
          statement.bindNull(2)
        } else {
          statement.bindText(2, _tmpGymId)
        }
        statement.bindText(3, __SessionType_enumToString(entity.type))
        statement.bindLong(4, entity.startedAt)
        statement.bindLong(5, entity.totalMs)
        statement.bindLong(6, entity.workMs)
        statement.bindLong(7, entity.restMs)
        statement.bindLong(8, entity.pausedMs)
        statement.bindLong(9, entity.attemptCount.toLong())
        statement.bindLong(10, entity.sendCount.toLong())
        statement.bindLong(11, entity.flashCount.toLong())
        val _tmpHardestSendValue: Int? = entity.hardestSendValue
        if (_tmpHardestSendValue == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmpHardestSendValue.toLong())
        }
        val _tmpHrAvg: Int? = entity.hrAvg
        if (_tmpHrAvg == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmpHrAvg.toLong())
        }
        val _tmpHrMax: Int? = entity.hrMax
        if (_tmpHrMax == null) {
          statement.bindNull(14)
        } else {
          statement.bindLong(14, _tmpHrMax.toLong())
        }
        val _tmpHrr60Avg: Int? = entity.hrr60Avg
        if (_tmpHrr60Avg == null) {
          statement.bindNull(15)
        } else {
          statement.bindLong(15, _tmpHrr60Avg.toLong())
        }
        val _tmpCaloriesTotal: Double? = entity.caloriesTotal
        if (_tmpCaloriesTotal == null) {
          statement.bindNull(16)
        } else {
          statement.bindDouble(16, _tmpCaloriesTotal)
        }
        val _tmpCaloriesOnWall: Double? = entity.caloriesOnWall
        if (_tmpCaloriesOnWall == null) {
          statement.bindNull(17)
        } else {
          statement.bindDouble(17, _tmpCaloriesOnWall)
        }
        val _tmpClimbHeightMeters: Double? = entity.climbHeightMeters
        if (_tmpClimbHeightMeters == null) {
          statement.bindNull(18)
        } else {
          statement.bindDouble(18, _tmpClimbHeightMeters)
        }
        val _tmpMaxClimbHeightMeters: Double? = entity.maxClimbHeightMeters
        if (_tmpMaxClimbHeightMeters == null) {
          statement.bindNull(19)
        } else {
          statement.bindDouble(19, _tmpMaxClimbHeightMeters)
        }
        val _tmpRpe: Int? = entity.rpe
        if (_tmpRpe == null) {
          statement.bindNull(20)
        } else {
          statement.bindLong(20, _tmpRpe.toLong())
        }
        statement.bindLong(21, entity.computedAt)
      }
    }, object : EntityDeleteOrUpdateAdapter<SessionSummaryEntity>() {
      protected override fun createQuery(): String = "UPDATE `session_summary` SET `sessionId` = ?,`gymId` = ?,`type` = ?,`startedAt` = ?,`totalMs` = ?,`workMs` = ?,`restMs` = ?,`pausedMs` = ?,`attemptCount` = ?,`sendCount` = ?,`flashCount` = ?,`hardestSendValue` = ?,`hrAvg` = ?,`hrMax` = ?,`hrr60Avg` = ?,`caloriesTotal` = ?,`caloriesOnWall` = ?,`climbHeightMeters` = ?,`maxClimbHeightMeters` = ?,`rpe` = ?,`computedAt` = ? WHERE `sessionId` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SessionSummaryEntity) {
        statement.bindText(1, entity.sessionId)
        val _tmpGymId: String? = entity.gymId
        if (_tmpGymId == null) {
          statement.bindNull(2)
        } else {
          statement.bindText(2, _tmpGymId)
        }
        statement.bindText(3, __SessionType_enumToString(entity.type))
        statement.bindLong(4, entity.startedAt)
        statement.bindLong(5, entity.totalMs)
        statement.bindLong(6, entity.workMs)
        statement.bindLong(7, entity.restMs)
        statement.bindLong(8, entity.pausedMs)
        statement.bindLong(9, entity.attemptCount.toLong())
        statement.bindLong(10, entity.sendCount.toLong())
        statement.bindLong(11, entity.flashCount.toLong())
        val _tmpHardestSendValue: Int? = entity.hardestSendValue
        if (_tmpHardestSendValue == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmpHardestSendValue.toLong())
        }
        val _tmpHrAvg: Int? = entity.hrAvg
        if (_tmpHrAvg == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmpHrAvg.toLong())
        }
        val _tmpHrMax: Int? = entity.hrMax
        if (_tmpHrMax == null) {
          statement.bindNull(14)
        } else {
          statement.bindLong(14, _tmpHrMax.toLong())
        }
        val _tmpHrr60Avg: Int? = entity.hrr60Avg
        if (_tmpHrr60Avg == null) {
          statement.bindNull(15)
        } else {
          statement.bindLong(15, _tmpHrr60Avg.toLong())
        }
        val _tmpCaloriesTotal: Double? = entity.caloriesTotal
        if (_tmpCaloriesTotal == null) {
          statement.bindNull(16)
        } else {
          statement.bindDouble(16, _tmpCaloriesTotal)
        }
        val _tmpCaloriesOnWall: Double? = entity.caloriesOnWall
        if (_tmpCaloriesOnWall == null) {
          statement.bindNull(17)
        } else {
          statement.bindDouble(17, _tmpCaloriesOnWall)
        }
        val _tmpClimbHeightMeters: Double? = entity.climbHeightMeters
        if (_tmpClimbHeightMeters == null) {
          statement.bindNull(18)
        } else {
          statement.bindDouble(18, _tmpClimbHeightMeters)
        }
        val _tmpMaxClimbHeightMeters: Double? = entity.maxClimbHeightMeters
        if (_tmpMaxClimbHeightMeters == null) {
          statement.bindNull(19)
        } else {
          statement.bindDouble(19, _tmpMaxClimbHeightMeters)
        }
        val _tmpRpe: Int? = entity.rpe
        if (_tmpRpe == null) {
          statement.bindNull(20)
        } else {
          statement.bindLong(20, _tmpRpe.toLong())
        }
        statement.bindLong(21, entity.computedAt)
        statement.bindText(22, entity.sessionId)
      }
    })
  }

  public override suspend fun upsert(summary: SessionSummaryEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfSessionSummaryEntity.upsert(_connection, summary)
  }

  public override suspend fun bySession(sessionId: String): SessionSummaryEntity? {
    val _sql: String = "SELECT * FROM session_summary WHERE sessionId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfGymId: Int = getColumnIndexOrThrow(_stmt, "gymId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfTotalMs: Int = getColumnIndexOrThrow(_stmt, "totalMs")
        val _columnIndexOfWorkMs: Int = getColumnIndexOrThrow(_stmt, "workMs")
        val _columnIndexOfRestMs: Int = getColumnIndexOrThrow(_stmt, "restMs")
        val _columnIndexOfPausedMs: Int = getColumnIndexOrThrow(_stmt, "pausedMs")
        val _columnIndexOfAttemptCount: Int = getColumnIndexOrThrow(_stmt, "attemptCount")
        val _columnIndexOfSendCount: Int = getColumnIndexOrThrow(_stmt, "sendCount")
        val _columnIndexOfFlashCount: Int = getColumnIndexOrThrow(_stmt, "flashCount")
        val _columnIndexOfHardestSendValue: Int = getColumnIndexOrThrow(_stmt, "hardestSendValue")
        val _columnIndexOfHrAvg: Int = getColumnIndexOrThrow(_stmt, "hrAvg")
        val _columnIndexOfHrMax: Int = getColumnIndexOrThrow(_stmt, "hrMax")
        val _columnIndexOfHrr60Avg: Int = getColumnIndexOrThrow(_stmt, "hrr60Avg")
        val _columnIndexOfCaloriesTotal: Int = getColumnIndexOrThrow(_stmt, "caloriesTotal")
        val _columnIndexOfCaloriesOnWall: Int = getColumnIndexOrThrow(_stmt, "caloriesOnWall")
        val _columnIndexOfClimbHeightMeters: Int = getColumnIndexOrThrow(_stmt, "climbHeightMeters")
        val _columnIndexOfMaxClimbHeightMeters: Int = getColumnIndexOrThrow(_stmt, "maxClimbHeightMeters")
        val _columnIndexOfRpe: Int = getColumnIndexOrThrow(_stmt, "rpe")
        val _columnIndexOfComputedAt: Int = getColumnIndexOrThrow(_stmt, "computedAt")
        val _result: SessionSummaryEntity?
        if (_stmt.step()) {
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpGymId: String?
          if (_stmt.isNull(_columnIndexOfGymId)) {
            _tmpGymId = null
          } else {
            _tmpGymId = _stmt.getText(_columnIndexOfGymId)
          }
          val _tmpType: SessionType
          _tmpType = __SessionType_stringToEnum(_stmt.getText(_columnIndexOfType))
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpTotalMs: Long
          _tmpTotalMs = _stmt.getLong(_columnIndexOfTotalMs)
          val _tmpWorkMs: Long
          _tmpWorkMs = _stmt.getLong(_columnIndexOfWorkMs)
          val _tmpRestMs: Long
          _tmpRestMs = _stmt.getLong(_columnIndexOfRestMs)
          val _tmpPausedMs: Long
          _tmpPausedMs = _stmt.getLong(_columnIndexOfPausedMs)
          val _tmpAttemptCount: Int
          _tmpAttemptCount = _stmt.getLong(_columnIndexOfAttemptCount).toInt()
          val _tmpSendCount: Int
          _tmpSendCount = _stmt.getLong(_columnIndexOfSendCount).toInt()
          val _tmpFlashCount: Int
          _tmpFlashCount = _stmt.getLong(_columnIndexOfFlashCount).toInt()
          val _tmpHardestSendValue: Int?
          if (_stmt.isNull(_columnIndexOfHardestSendValue)) {
            _tmpHardestSendValue = null
          } else {
            _tmpHardestSendValue = _stmt.getLong(_columnIndexOfHardestSendValue).toInt()
          }
          val _tmpHrAvg: Int?
          if (_stmt.isNull(_columnIndexOfHrAvg)) {
            _tmpHrAvg = null
          } else {
            _tmpHrAvg = _stmt.getLong(_columnIndexOfHrAvg).toInt()
          }
          val _tmpHrMax: Int?
          if (_stmt.isNull(_columnIndexOfHrMax)) {
            _tmpHrMax = null
          } else {
            _tmpHrMax = _stmt.getLong(_columnIndexOfHrMax).toInt()
          }
          val _tmpHrr60Avg: Int?
          if (_stmt.isNull(_columnIndexOfHrr60Avg)) {
            _tmpHrr60Avg = null
          } else {
            _tmpHrr60Avg = _stmt.getLong(_columnIndexOfHrr60Avg).toInt()
          }
          val _tmpCaloriesTotal: Double?
          if (_stmt.isNull(_columnIndexOfCaloriesTotal)) {
            _tmpCaloriesTotal = null
          } else {
            _tmpCaloriesTotal = _stmt.getDouble(_columnIndexOfCaloriesTotal)
          }
          val _tmpCaloriesOnWall: Double?
          if (_stmt.isNull(_columnIndexOfCaloriesOnWall)) {
            _tmpCaloriesOnWall = null
          } else {
            _tmpCaloriesOnWall = _stmt.getDouble(_columnIndexOfCaloriesOnWall)
          }
          val _tmpClimbHeightMeters: Double?
          if (_stmt.isNull(_columnIndexOfClimbHeightMeters)) {
            _tmpClimbHeightMeters = null
          } else {
            _tmpClimbHeightMeters = _stmt.getDouble(_columnIndexOfClimbHeightMeters)
          }
          val _tmpMaxClimbHeightMeters: Double?
          if (_stmt.isNull(_columnIndexOfMaxClimbHeightMeters)) {
            _tmpMaxClimbHeightMeters = null
          } else {
            _tmpMaxClimbHeightMeters = _stmt.getDouble(_columnIndexOfMaxClimbHeightMeters)
          }
          val _tmpRpe: Int?
          if (_stmt.isNull(_columnIndexOfRpe)) {
            _tmpRpe = null
          } else {
            _tmpRpe = _stmt.getLong(_columnIndexOfRpe).toInt()
          }
          val _tmpComputedAt: Long
          _tmpComputedAt = _stmt.getLong(_columnIndexOfComputedAt)
          _result = SessionSummaryEntity(_tmpSessionId,_tmpGymId,_tmpType,_tmpStartedAt,_tmpTotalMs,_tmpWorkMs,_tmpRestMs,_tmpPausedMs,_tmpAttemptCount,_tmpSendCount,_tmpFlashCount,_tmpHardestSendValue,_tmpHrAvg,_tmpHrMax,_tmpHrr60Avg,_tmpCaloriesTotal,_tmpCaloriesOnWall,_tmpClimbHeightMeters,_tmpMaxClimbHeightMeters,_tmpRpe,_tmpComputedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeBySession(sessionId: String): Flow<SessionSummaryEntity?> {
    val _sql: String = "SELECT * FROM session_summary WHERE sessionId = ?"
    return createFlow(__db, false, arrayOf("session_summary")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfGymId: Int = getColumnIndexOrThrow(_stmt, "gymId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfTotalMs: Int = getColumnIndexOrThrow(_stmt, "totalMs")
        val _columnIndexOfWorkMs: Int = getColumnIndexOrThrow(_stmt, "workMs")
        val _columnIndexOfRestMs: Int = getColumnIndexOrThrow(_stmt, "restMs")
        val _columnIndexOfPausedMs: Int = getColumnIndexOrThrow(_stmt, "pausedMs")
        val _columnIndexOfAttemptCount: Int = getColumnIndexOrThrow(_stmt, "attemptCount")
        val _columnIndexOfSendCount: Int = getColumnIndexOrThrow(_stmt, "sendCount")
        val _columnIndexOfFlashCount: Int = getColumnIndexOrThrow(_stmt, "flashCount")
        val _columnIndexOfHardestSendValue: Int = getColumnIndexOrThrow(_stmt, "hardestSendValue")
        val _columnIndexOfHrAvg: Int = getColumnIndexOrThrow(_stmt, "hrAvg")
        val _columnIndexOfHrMax: Int = getColumnIndexOrThrow(_stmt, "hrMax")
        val _columnIndexOfHrr60Avg: Int = getColumnIndexOrThrow(_stmt, "hrr60Avg")
        val _columnIndexOfCaloriesTotal: Int = getColumnIndexOrThrow(_stmt, "caloriesTotal")
        val _columnIndexOfCaloriesOnWall: Int = getColumnIndexOrThrow(_stmt, "caloriesOnWall")
        val _columnIndexOfClimbHeightMeters: Int = getColumnIndexOrThrow(_stmt, "climbHeightMeters")
        val _columnIndexOfMaxClimbHeightMeters: Int = getColumnIndexOrThrow(_stmt, "maxClimbHeightMeters")
        val _columnIndexOfRpe: Int = getColumnIndexOrThrow(_stmt, "rpe")
        val _columnIndexOfComputedAt: Int = getColumnIndexOrThrow(_stmt, "computedAt")
        val _result: SessionSummaryEntity?
        if (_stmt.step()) {
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpGymId: String?
          if (_stmt.isNull(_columnIndexOfGymId)) {
            _tmpGymId = null
          } else {
            _tmpGymId = _stmt.getText(_columnIndexOfGymId)
          }
          val _tmpType: SessionType
          _tmpType = __SessionType_stringToEnum(_stmt.getText(_columnIndexOfType))
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpTotalMs: Long
          _tmpTotalMs = _stmt.getLong(_columnIndexOfTotalMs)
          val _tmpWorkMs: Long
          _tmpWorkMs = _stmt.getLong(_columnIndexOfWorkMs)
          val _tmpRestMs: Long
          _tmpRestMs = _stmt.getLong(_columnIndexOfRestMs)
          val _tmpPausedMs: Long
          _tmpPausedMs = _stmt.getLong(_columnIndexOfPausedMs)
          val _tmpAttemptCount: Int
          _tmpAttemptCount = _stmt.getLong(_columnIndexOfAttemptCount).toInt()
          val _tmpSendCount: Int
          _tmpSendCount = _stmt.getLong(_columnIndexOfSendCount).toInt()
          val _tmpFlashCount: Int
          _tmpFlashCount = _stmt.getLong(_columnIndexOfFlashCount).toInt()
          val _tmpHardestSendValue: Int?
          if (_stmt.isNull(_columnIndexOfHardestSendValue)) {
            _tmpHardestSendValue = null
          } else {
            _tmpHardestSendValue = _stmt.getLong(_columnIndexOfHardestSendValue).toInt()
          }
          val _tmpHrAvg: Int?
          if (_stmt.isNull(_columnIndexOfHrAvg)) {
            _tmpHrAvg = null
          } else {
            _tmpHrAvg = _stmt.getLong(_columnIndexOfHrAvg).toInt()
          }
          val _tmpHrMax: Int?
          if (_stmt.isNull(_columnIndexOfHrMax)) {
            _tmpHrMax = null
          } else {
            _tmpHrMax = _stmt.getLong(_columnIndexOfHrMax).toInt()
          }
          val _tmpHrr60Avg: Int?
          if (_stmt.isNull(_columnIndexOfHrr60Avg)) {
            _tmpHrr60Avg = null
          } else {
            _tmpHrr60Avg = _stmt.getLong(_columnIndexOfHrr60Avg).toInt()
          }
          val _tmpCaloriesTotal: Double?
          if (_stmt.isNull(_columnIndexOfCaloriesTotal)) {
            _tmpCaloriesTotal = null
          } else {
            _tmpCaloriesTotal = _stmt.getDouble(_columnIndexOfCaloriesTotal)
          }
          val _tmpCaloriesOnWall: Double?
          if (_stmt.isNull(_columnIndexOfCaloriesOnWall)) {
            _tmpCaloriesOnWall = null
          } else {
            _tmpCaloriesOnWall = _stmt.getDouble(_columnIndexOfCaloriesOnWall)
          }
          val _tmpClimbHeightMeters: Double?
          if (_stmt.isNull(_columnIndexOfClimbHeightMeters)) {
            _tmpClimbHeightMeters = null
          } else {
            _tmpClimbHeightMeters = _stmt.getDouble(_columnIndexOfClimbHeightMeters)
          }
          val _tmpMaxClimbHeightMeters: Double?
          if (_stmt.isNull(_columnIndexOfMaxClimbHeightMeters)) {
            _tmpMaxClimbHeightMeters = null
          } else {
            _tmpMaxClimbHeightMeters = _stmt.getDouble(_columnIndexOfMaxClimbHeightMeters)
          }
          val _tmpRpe: Int?
          if (_stmt.isNull(_columnIndexOfRpe)) {
            _tmpRpe = null
          } else {
            _tmpRpe = _stmt.getLong(_columnIndexOfRpe).toInt()
          }
          val _tmpComputedAt: Long
          _tmpComputedAt = _stmt.getLong(_columnIndexOfComputedAt)
          _result = SessionSummaryEntity(_tmpSessionId,_tmpGymId,_tmpType,_tmpStartedAt,_tmpTotalMs,_tmpWorkMs,_tmpRestMs,_tmpPausedMs,_tmpAttemptCount,_tmpSendCount,_tmpFlashCount,_tmpHardestSendValue,_tmpHrAvg,_tmpHrMax,_tmpHrr60Avg,_tmpCaloriesTotal,_tmpCaloriesOnWall,_tmpClimbHeightMeters,_tmpMaxClimbHeightMeters,_tmpRpe,_tmpComputedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeRecent(limit: Int): Flow<List<SessionSummaryEntity>> {
    val _sql: String = "SELECT * FROM session_summary ORDER BY startedAt DESC LIMIT ?"
    return createFlow(__db, false, arrayOf("session_summary")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfGymId: Int = getColumnIndexOrThrow(_stmt, "gymId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfTotalMs: Int = getColumnIndexOrThrow(_stmt, "totalMs")
        val _columnIndexOfWorkMs: Int = getColumnIndexOrThrow(_stmt, "workMs")
        val _columnIndexOfRestMs: Int = getColumnIndexOrThrow(_stmt, "restMs")
        val _columnIndexOfPausedMs: Int = getColumnIndexOrThrow(_stmt, "pausedMs")
        val _columnIndexOfAttemptCount: Int = getColumnIndexOrThrow(_stmt, "attemptCount")
        val _columnIndexOfSendCount: Int = getColumnIndexOrThrow(_stmt, "sendCount")
        val _columnIndexOfFlashCount: Int = getColumnIndexOrThrow(_stmt, "flashCount")
        val _columnIndexOfHardestSendValue: Int = getColumnIndexOrThrow(_stmt, "hardestSendValue")
        val _columnIndexOfHrAvg: Int = getColumnIndexOrThrow(_stmt, "hrAvg")
        val _columnIndexOfHrMax: Int = getColumnIndexOrThrow(_stmt, "hrMax")
        val _columnIndexOfHrr60Avg: Int = getColumnIndexOrThrow(_stmt, "hrr60Avg")
        val _columnIndexOfCaloriesTotal: Int = getColumnIndexOrThrow(_stmt, "caloriesTotal")
        val _columnIndexOfCaloriesOnWall: Int = getColumnIndexOrThrow(_stmt, "caloriesOnWall")
        val _columnIndexOfClimbHeightMeters: Int = getColumnIndexOrThrow(_stmt, "climbHeightMeters")
        val _columnIndexOfMaxClimbHeightMeters: Int = getColumnIndexOrThrow(_stmt, "maxClimbHeightMeters")
        val _columnIndexOfRpe: Int = getColumnIndexOrThrow(_stmt, "rpe")
        val _columnIndexOfComputedAt: Int = getColumnIndexOrThrow(_stmt, "computedAt")
        val _result: MutableList<SessionSummaryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SessionSummaryEntity
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpGymId: String?
          if (_stmt.isNull(_columnIndexOfGymId)) {
            _tmpGymId = null
          } else {
            _tmpGymId = _stmt.getText(_columnIndexOfGymId)
          }
          val _tmpType: SessionType
          _tmpType = __SessionType_stringToEnum(_stmt.getText(_columnIndexOfType))
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpTotalMs: Long
          _tmpTotalMs = _stmt.getLong(_columnIndexOfTotalMs)
          val _tmpWorkMs: Long
          _tmpWorkMs = _stmt.getLong(_columnIndexOfWorkMs)
          val _tmpRestMs: Long
          _tmpRestMs = _stmt.getLong(_columnIndexOfRestMs)
          val _tmpPausedMs: Long
          _tmpPausedMs = _stmt.getLong(_columnIndexOfPausedMs)
          val _tmpAttemptCount: Int
          _tmpAttemptCount = _stmt.getLong(_columnIndexOfAttemptCount).toInt()
          val _tmpSendCount: Int
          _tmpSendCount = _stmt.getLong(_columnIndexOfSendCount).toInt()
          val _tmpFlashCount: Int
          _tmpFlashCount = _stmt.getLong(_columnIndexOfFlashCount).toInt()
          val _tmpHardestSendValue: Int?
          if (_stmt.isNull(_columnIndexOfHardestSendValue)) {
            _tmpHardestSendValue = null
          } else {
            _tmpHardestSendValue = _stmt.getLong(_columnIndexOfHardestSendValue).toInt()
          }
          val _tmpHrAvg: Int?
          if (_stmt.isNull(_columnIndexOfHrAvg)) {
            _tmpHrAvg = null
          } else {
            _tmpHrAvg = _stmt.getLong(_columnIndexOfHrAvg).toInt()
          }
          val _tmpHrMax: Int?
          if (_stmt.isNull(_columnIndexOfHrMax)) {
            _tmpHrMax = null
          } else {
            _tmpHrMax = _stmt.getLong(_columnIndexOfHrMax).toInt()
          }
          val _tmpHrr60Avg: Int?
          if (_stmt.isNull(_columnIndexOfHrr60Avg)) {
            _tmpHrr60Avg = null
          } else {
            _tmpHrr60Avg = _stmt.getLong(_columnIndexOfHrr60Avg).toInt()
          }
          val _tmpCaloriesTotal: Double?
          if (_stmt.isNull(_columnIndexOfCaloriesTotal)) {
            _tmpCaloriesTotal = null
          } else {
            _tmpCaloriesTotal = _stmt.getDouble(_columnIndexOfCaloriesTotal)
          }
          val _tmpCaloriesOnWall: Double?
          if (_stmt.isNull(_columnIndexOfCaloriesOnWall)) {
            _tmpCaloriesOnWall = null
          } else {
            _tmpCaloriesOnWall = _stmt.getDouble(_columnIndexOfCaloriesOnWall)
          }
          val _tmpClimbHeightMeters: Double?
          if (_stmt.isNull(_columnIndexOfClimbHeightMeters)) {
            _tmpClimbHeightMeters = null
          } else {
            _tmpClimbHeightMeters = _stmt.getDouble(_columnIndexOfClimbHeightMeters)
          }
          val _tmpMaxClimbHeightMeters: Double?
          if (_stmt.isNull(_columnIndexOfMaxClimbHeightMeters)) {
            _tmpMaxClimbHeightMeters = null
          } else {
            _tmpMaxClimbHeightMeters = _stmt.getDouble(_columnIndexOfMaxClimbHeightMeters)
          }
          val _tmpRpe: Int?
          if (_stmt.isNull(_columnIndexOfRpe)) {
            _tmpRpe = null
          } else {
            _tmpRpe = _stmt.getLong(_columnIndexOfRpe).toInt()
          }
          val _tmpComputedAt: Long
          _tmpComputedAt = _stmt.getLong(_columnIndexOfComputedAt)
          _item = SessionSummaryEntity(_tmpSessionId,_tmpGymId,_tmpType,_tmpStartedAt,_tmpTotalMs,_tmpWorkMs,_tmpRestMs,_tmpPausedMs,_tmpAttemptCount,_tmpSendCount,_tmpFlashCount,_tmpHardestSendValue,_tmpHrAvg,_tmpHrMax,_tmpHrr60Avg,_tmpCaloriesTotal,_tmpCaloriesOnWall,_tmpClimbHeightMeters,_tmpMaxClimbHeightMeters,_tmpRpe,_tmpComputedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun baseline(
    type: SessionType,
    gymId: String?,
    since: Long,
    excludeSessionId: String,
  ): SessionBaseline {
    val _sql: String = """
        |
        |        SELECT
        |            COUNT(*)              AS sessionCount,
        |            AVG(attemptCount)     AS avgAttempts,
        |            AVG(sendCount)        AS avgSends,
        |            AVG(workMs)           AS avgWorkMs,
        |            AVG(restMs)           AS avgRestMs,
        |            AVG(hrr60Avg)         AS avgHrr60,
        |            MAX(hardestSendValue) AS bestSendValue
        |        FROM session_summary
        |        WHERE type = ?
        |          AND (? IS NULL OR gymId = ?)
        |          AND startedAt >= ?
        |          AND sessionId <> ?
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, __SessionType_enumToString(type))
        _argIndex = 2
        if (gymId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, gymId)
        }
        _argIndex = 3
        if (gymId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, gymId)
        }
        _argIndex = 4
        _stmt.bindLong(_argIndex, since)
        _argIndex = 5
        _stmt.bindText(_argIndex, excludeSessionId)
        val _columnIndexOfSessionCount: Int = 0
        val _columnIndexOfAvgAttempts: Int = 1
        val _columnIndexOfAvgSends: Int = 2
        val _columnIndexOfAvgWorkMs: Int = 3
        val _columnIndexOfAvgRestMs: Int = 4
        val _columnIndexOfAvgHrr60: Int = 5
        val _columnIndexOfBestSendValue: Int = 6
        val _result: SessionBaseline
        if (_stmt.step()) {
          val _tmpSessionCount: Int
          _tmpSessionCount = _stmt.getLong(_columnIndexOfSessionCount).toInt()
          val _tmpAvgAttempts: Double?
          if (_stmt.isNull(_columnIndexOfAvgAttempts)) {
            _tmpAvgAttempts = null
          } else {
            _tmpAvgAttempts = _stmt.getDouble(_columnIndexOfAvgAttempts)
          }
          val _tmpAvgSends: Double?
          if (_stmt.isNull(_columnIndexOfAvgSends)) {
            _tmpAvgSends = null
          } else {
            _tmpAvgSends = _stmt.getDouble(_columnIndexOfAvgSends)
          }
          val _tmpAvgWorkMs: Double?
          if (_stmt.isNull(_columnIndexOfAvgWorkMs)) {
            _tmpAvgWorkMs = null
          } else {
            _tmpAvgWorkMs = _stmt.getDouble(_columnIndexOfAvgWorkMs)
          }
          val _tmpAvgRestMs: Double?
          if (_stmt.isNull(_columnIndexOfAvgRestMs)) {
            _tmpAvgRestMs = null
          } else {
            _tmpAvgRestMs = _stmt.getDouble(_columnIndexOfAvgRestMs)
          }
          val _tmpAvgHrr60: Double?
          if (_stmt.isNull(_columnIndexOfAvgHrr60)) {
            _tmpAvgHrr60 = null
          } else {
            _tmpAvgHrr60 = _stmt.getDouble(_columnIndexOfAvgHrr60)
          }
          val _tmpBestSendValue: Int?
          if (_stmt.isNull(_columnIndexOfBestSendValue)) {
            _tmpBestSendValue = null
          } else {
            _tmpBestSendValue = _stmt.getLong(_columnIndexOfBestSendValue).toInt()
          }
          _result = SessionBaseline(_tmpSessionCount,_tmpAvgAttempts,_tmpAvgSends,_tmpAvgWorkMs,_tmpAvgRestMs,_tmpAvgHrr60,_tmpBestSendValue)
        } else {
          error("The query result was empty, but expected a single row to return a NON-NULL object of type 'at.mentor.bouldclockapp.`data`.db.dao.SessionBaseline'.")
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeHrr60Trend(since: Long): Flow<List<Hrr60Point>> {
    val _sql: String = """
        |
        |        SELECT startedAt, hrr60Avg FROM session_summary
        |        WHERE hrr60Avg IS NOT NULL AND startedAt >= ?
        |        ORDER BY startedAt
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("session_summary")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, since)
        val _columnIndexOfStartedAt: Int = 0
        val _columnIndexOfHrr60Avg: Int = 1
        val _result: MutableList<Hrr60Point> = mutableListOf()
        while (_stmt.step()) {
          val _item: Hrr60Point
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpHrr60Avg: Int
          _tmpHrr60Avg = _stmt.getLong(_columnIndexOfHrr60Avg).toInt()
          _item = Hrr60Point(_tmpStartedAt,_tmpHrr60Avg)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __SessionType_enumToString(_value: SessionType): String = when (_value) {
    SessionType.FREE -> "FREE"
    SessionType.VOLUME -> "VOLUME"
    SessionType.LIMIT -> "LIMIT"
    SessionType.PROJECT -> "PROJECT"
    SessionType.CUSTOM -> "CUSTOM"
    SessionType.COMPETITION -> "COMPETITION"
    SessionType.KILTERBOARD -> "KILTERBOARD"
  }

  private fun __SessionType_stringToEnum(_value: String): SessionType = when (_value) {
    "FREE" -> SessionType.FREE
    "VOLUME" -> SessionType.VOLUME
    "LIMIT" -> SessionType.LIMIT
    "PROJECT" -> SessionType.PROJECT
    "CUSTOM" -> SessionType.CUSTOM
    "COMPETITION" -> SessionType.COMPETITION
    "KILTERBOARD" -> SessionType.KILTERBOARD
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}

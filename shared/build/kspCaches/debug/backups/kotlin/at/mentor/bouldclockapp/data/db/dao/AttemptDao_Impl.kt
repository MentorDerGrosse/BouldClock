package at.mentor.bouldclockapp.`data`.db.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import at.mentor.bouldclockapp.`data`.db.entity.AttemptEntity
import at.mentor.bouldclockapp.`data`.db.entity.RecordMeta
import at.mentor.bouldclockapp.core.model.AttemptKind
import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.SyncState
import javax.`annotation`.processing.Generated
import kotlin.Boolean
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
public class AttemptDao_Impl(
  __db: RoomDatabase,
) : AttemptDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfAttemptEntity: EntityUpsertAdapter<AttemptEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfAttemptEntity = EntityUpsertAdapter<AttemptEntity>(object : EntityInsertAdapter<AttemptEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `attempt` (`id`,`sessionId`,`problemId`,`ordinal`,`kind`,`startsNewBoulder`,`startedAt`,`endedAt`,`outcome`,`gradeValue`,`gradeSystem`,`topMoveReached`,`climbHeightMeters`,`boardAngleDegrees`,`hrAvg`,`hrMax`,`hrEnd`,`hrAfter60s`,`hrr60`,`restAfterMs`,`createdAt`,`updatedAt`,`deletedAt`,`syncState`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: AttemptEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.sessionId)
        val _tmpProblemId: String? = entity.problemId
        if (_tmpProblemId == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpProblemId)
        }
        statement.bindLong(4, entity.ordinal.toLong())
        statement.bindText(5, __AttemptKind_enumToString(entity.kind))
        val _tmp: Int = if (entity.startsNewBoulder) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindLong(7, entity.startedAt)
        val _tmpEndedAt: Long? = entity.endedAt
        if (_tmpEndedAt == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpEndedAt)
        }
        val _tmpOutcome: AttemptOutcome? = entity.outcome
        if (_tmpOutcome == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, __AttemptOutcome_enumToString(_tmpOutcome))
        }
        val _tmpGradeValue: Int? = entity.gradeValue
        if (_tmpGradeValue == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpGradeValue.toLong())
        }
        val _tmpGradeSystem: GradeSystem? = entity.gradeSystem
        if (_tmpGradeSystem == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, __GradeSystem_enumToString(_tmpGradeSystem))
        }
        val _tmpTopMoveReached: Int? = entity.topMoveReached
        if (_tmpTopMoveReached == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmpTopMoveReached.toLong())
        }
        val _tmpClimbHeightMeters: Double? = entity.climbHeightMeters
        if (_tmpClimbHeightMeters == null) {
          statement.bindNull(13)
        } else {
          statement.bindDouble(13, _tmpClimbHeightMeters)
        }
        val _tmpBoardAngleDegrees: Int? = entity.boardAngleDegrees
        if (_tmpBoardAngleDegrees == null) {
          statement.bindNull(14)
        } else {
          statement.bindLong(14, _tmpBoardAngleDegrees.toLong())
        }
        val _tmpHrAvg: Int? = entity.hrAvg
        if (_tmpHrAvg == null) {
          statement.bindNull(15)
        } else {
          statement.bindLong(15, _tmpHrAvg.toLong())
        }
        val _tmpHrMax: Int? = entity.hrMax
        if (_tmpHrMax == null) {
          statement.bindNull(16)
        } else {
          statement.bindLong(16, _tmpHrMax.toLong())
        }
        val _tmpHrEnd: Int? = entity.hrEnd
        if (_tmpHrEnd == null) {
          statement.bindNull(17)
        } else {
          statement.bindLong(17, _tmpHrEnd.toLong())
        }
        val _tmpHrAfter60s: Int? = entity.hrAfter60s
        if (_tmpHrAfter60s == null) {
          statement.bindNull(18)
        } else {
          statement.bindLong(18, _tmpHrAfter60s.toLong())
        }
        val _tmpHrr60: Int? = entity.hrr60
        if (_tmpHrr60 == null) {
          statement.bindNull(19)
        } else {
          statement.bindLong(19, _tmpHrr60.toLong())
        }
        val _tmpRestAfterMs: Long? = entity.restAfterMs
        if (_tmpRestAfterMs == null) {
          statement.bindNull(20)
        } else {
          statement.bindLong(20, _tmpRestAfterMs)
        }
        val _tmpMeta: RecordMeta = entity.meta
        statement.bindLong(21, _tmpMeta.createdAt)
        statement.bindLong(22, _tmpMeta.updatedAt)
        val _tmpDeletedAt: Long? = _tmpMeta.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(23)
        } else {
          statement.bindLong(23, _tmpDeletedAt)
        }
        statement.bindText(24, __SyncState_enumToString(_tmpMeta.syncState))
      }
    }, object : EntityDeleteOrUpdateAdapter<AttemptEntity>() {
      protected override fun createQuery(): String = "UPDATE `attempt` SET `id` = ?,`sessionId` = ?,`problemId` = ?,`ordinal` = ?,`kind` = ?,`startsNewBoulder` = ?,`startedAt` = ?,`endedAt` = ?,`outcome` = ?,`gradeValue` = ?,`gradeSystem` = ?,`topMoveReached` = ?,`climbHeightMeters` = ?,`boardAngleDegrees` = ?,`hrAvg` = ?,`hrMax` = ?,`hrEnd` = ?,`hrAfter60s` = ?,`hrr60` = ?,`restAfterMs` = ?,`createdAt` = ?,`updatedAt` = ?,`deletedAt` = ?,`syncState` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: AttemptEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.sessionId)
        val _tmpProblemId: String? = entity.problemId
        if (_tmpProblemId == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpProblemId)
        }
        statement.bindLong(4, entity.ordinal.toLong())
        statement.bindText(5, __AttemptKind_enumToString(entity.kind))
        val _tmp: Int = if (entity.startsNewBoulder) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindLong(7, entity.startedAt)
        val _tmpEndedAt: Long? = entity.endedAt
        if (_tmpEndedAt == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpEndedAt)
        }
        val _tmpOutcome: AttemptOutcome? = entity.outcome
        if (_tmpOutcome == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, __AttemptOutcome_enumToString(_tmpOutcome))
        }
        val _tmpGradeValue: Int? = entity.gradeValue
        if (_tmpGradeValue == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpGradeValue.toLong())
        }
        val _tmpGradeSystem: GradeSystem? = entity.gradeSystem
        if (_tmpGradeSystem == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, __GradeSystem_enumToString(_tmpGradeSystem))
        }
        val _tmpTopMoveReached: Int? = entity.topMoveReached
        if (_tmpTopMoveReached == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmpTopMoveReached.toLong())
        }
        val _tmpClimbHeightMeters: Double? = entity.climbHeightMeters
        if (_tmpClimbHeightMeters == null) {
          statement.bindNull(13)
        } else {
          statement.bindDouble(13, _tmpClimbHeightMeters)
        }
        val _tmpBoardAngleDegrees: Int? = entity.boardAngleDegrees
        if (_tmpBoardAngleDegrees == null) {
          statement.bindNull(14)
        } else {
          statement.bindLong(14, _tmpBoardAngleDegrees.toLong())
        }
        val _tmpHrAvg: Int? = entity.hrAvg
        if (_tmpHrAvg == null) {
          statement.bindNull(15)
        } else {
          statement.bindLong(15, _tmpHrAvg.toLong())
        }
        val _tmpHrMax: Int? = entity.hrMax
        if (_tmpHrMax == null) {
          statement.bindNull(16)
        } else {
          statement.bindLong(16, _tmpHrMax.toLong())
        }
        val _tmpHrEnd: Int? = entity.hrEnd
        if (_tmpHrEnd == null) {
          statement.bindNull(17)
        } else {
          statement.bindLong(17, _tmpHrEnd.toLong())
        }
        val _tmpHrAfter60s: Int? = entity.hrAfter60s
        if (_tmpHrAfter60s == null) {
          statement.bindNull(18)
        } else {
          statement.bindLong(18, _tmpHrAfter60s.toLong())
        }
        val _tmpHrr60: Int? = entity.hrr60
        if (_tmpHrr60 == null) {
          statement.bindNull(19)
        } else {
          statement.bindLong(19, _tmpHrr60.toLong())
        }
        val _tmpRestAfterMs: Long? = entity.restAfterMs
        if (_tmpRestAfterMs == null) {
          statement.bindNull(20)
        } else {
          statement.bindLong(20, _tmpRestAfterMs)
        }
        val _tmpMeta: RecordMeta = entity.meta
        statement.bindLong(21, _tmpMeta.createdAt)
        statement.bindLong(22, _tmpMeta.updatedAt)
        val _tmpDeletedAt: Long? = _tmpMeta.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(23)
        } else {
          statement.bindLong(23, _tmpDeletedAt)
        }
        statement.bindText(24, __SyncState_enumToString(_tmpMeta.syncState))
        statement.bindText(25, entity.id)
      }
    })
  }

  public override suspend fun upsert(attempt: AttemptEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfAttemptEntity.upsert(_connection, attempt)
  }

  public override suspend fun byId(id: String): AttemptEntity? {
    val _sql: String = "SELECT * FROM attempt WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfProblemId: Int = getColumnIndexOrThrow(_stmt, "problemId")
        val _columnIndexOfOrdinal: Int = getColumnIndexOrThrow(_stmt, "ordinal")
        val _columnIndexOfKind: Int = getColumnIndexOrThrow(_stmt, "kind")
        val _columnIndexOfStartsNewBoulder: Int = getColumnIndexOrThrow(_stmt, "startsNewBoulder")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfOutcome: Int = getColumnIndexOrThrow(_stmt, "outcome")
        val _columnIndexOfGradeValue: Int = getColumnIndexOrThrow(_stmt, "gradeValue")
        val _columnIndexOfGradeSystem: Int = getColumnIndexOrThrow(_stmt, "gradeSystem")
        val _columnIndexOfTopMoveReached: Int = getColumnIndexOrThrow(_stmt, "topMoveReached")
        val _columnIndexOfClimbHeightMeters: Int = getColumnIndexOrThrow(_stmt, "climbHeightMeters")
        val _columnIndexOfBoardAngleDegrees: Int = getColumnIndexOrThrow(_stmt, "boardAngleDegrees")
        val _columnIndexOfHrAvg: Int = getColumnIndexOrThrow(_stmt, "hrAvg")
        val _columnIndexOfHrMax: Int = getColumnIndexOrThrow(_stmt, "hrMax")
        val _columnIndexOfHrEnd: Int = getColumnIndexOrThrow(_stmt, "hrEnd")
        val _columnIndexOfHrAfter60s: Int = getColumnIndexOrThrow(_stmt, "hrAfter60s")
        val _columnIndexOfHrr60: Int = getColumnIndexOrThrow(_stmt, "hrr60")
        val _columnIndexOfRestAfterMs: Int = getColumnIndexOrThrow(_stmt, "restAfterMs")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: AttemptEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpProblemId: String?
          if (_stmt.isNull(_columnIndexOfProblemId)) {
            _tmpProblemId = null
          } else {
            _tmpProblemId = _stmt.getText(_columnIndexOfProblemId)
          }
          val _tmpOrdinal: Int
          _tmpOrdinal = _stmt.getLong(_columnIndexOfOrdinal).toInt()
          val _tmpKind: AttemptKind
          _tmpKind = __AttemptKind_stringToEnum(_stmt.getText(_columnIndexOfKind))
          val _tmpStartsNewBoulder: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfStartsNewBoulder).toInt()
          _tmpStartsNewBoulder = _tmp != 0
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpOutcome: AttemptOutcome?
          if (_stmt.isNull(_columnIndexOfOutcome)) {
            _tmpOutcome = null
          } else {
            _tmpOutcome = __AttemptOutcome_stringToEnum(_stmt.getText(_columnIndexOfOutcome))
          }
          val _tmpGradeValue: Int?
          if (_stmt.isNull(_columnIndexOfGradeValue)) {
            _tmpGradeValue = null
          } else {
            _tmpGradeValue = _stmt.getLong(_columnIndexOfGradeValue).toInt()
          }
          val _tmpGradeSystem: GradeSystem?
          if (_stmt.isNull(_columnIndexOfGradeSystem)) {
            _tmpGradeSystem = null
          } else {
            _tmpGradeSystem = __GradeSystem_stringToEnum(_stmt.getText(_columnIndexOfGradeSystem))
          }
          val _tmpTopMoveReached: Int?
          if (_stmt.isNull(_columnIndexOfTopMoveReached)) {
            _tmpTopMoveReached = null
          } else {
            _tmpTopMoveReached = _stmt.getLong(_columnIndexOfTopMoveReached).toInt()
          }
          val _tmpClimbHeightMeters: Double?
          if (_stmt.isNull(_columnIndexOfClimbHeightMeters)) {
            _tmpClimbHeightMeters = null
          } else {
            _tmpClimbHeightMeters = _stmt.getDouble(_columnIndexOfClimbHeightMeters)
          }
          val _tmpBoardAngleDegrees: Int?
          if (_stmt.isNull(_columnIndexOfBoardAngleDegrees)) {
            _tmpBoardAngleDegrees = null
          } else {
            _tmpBoardAngleDegrees = _stmt.getLong(_columnIndexOfBoardAngleDegrees).toInt()
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
          val _tmpHrEnd: Int?
          if (_stmt.isNull(_columnIndexOfHrEnd)) {
            _tmpHrEnd = null
          } else {
            _tmpHrEnd = _stmt.getLong(_columnIndexOfHrEnd).toInt()
          }
          val _tmpHrAfter60s: Int?
          if (_stmt.isNull(_columnIndexOfHrAfter60s)) {
            _tmpHrAfter60s = null
          } else {
            _tmpHrAfter60s = _stmt.getLong(_columnIndexOfHrAfter60s).toInt()
          }
          val _tmpHrr60: Int?
          if (_stmt.isNull(_columnIndexOfHrr60)) {
            _tmpHrr60 = null
          } else {
            _tmpHrr60 = _stmt.getLong(_columnIndexOfHrr60).toInt()
          }
          val _tmpRestAfterMs: Long?
          if (_stmt.isNull(_columnIndexOfRestAfterMs)) {
            _tmpRestAfterMs = null
          } else {
            _tmpRestAfterMs = _stmt.getLong(_columnIndexOfRestAfterMs)
          }
          val _tmpMeta: RecordMeta
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeletedAt: Long?
          if (_stmt.isNull(_columnIndexOfDeletedAt)) {
            _tmpDeletedAt = null
          } else {
            _tmpDeletedAt = _stmt.getLong(_columnIndexOfDeletedAt)
          }
          val _tmpSyncState: SyncState
          _tmpSyncState = __SyncState_stringToEnum(_stmt.getText(_columnIndexOfSyncState))
          _tmpMeta = RecordMeta(_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState)
          _result = AttemptEntity(_tmpId,_tmpSessionId,_tmpProblemId,_tmpOrdinal,_tmpKind,_tmpStartsNewBoulder,_tmpStartedAt,_tmpEndedAt,_tmpOutcome,_tmpGradeValue,_tmpGradeSystem,_tmpTopMoveReached,_tmpClimbHeightMeters,_tmpBoardAngleDegrees,_tmpHrAvg,_tmpHrMax,_tmpHrEnd,_tmpHrAfter60s,_tmpHrr60,_tmpRestAfterMs,_tmpMeta)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeBySession(sessionId: String): Flow<List<AttemptEntity>> {
    val _sql: String = "SELECT * FROM attempt WHERE sessionId = ? AND deletedAt IS NULL ORDER BY ordinal"
    return createFlow(__db, false, arrayOf("attempt")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfProblemId: Int = getColumnIndexOrThrow(_stmt, "problemId")
        val _columnIndexOfOrdinal: Int = getColumnIndexOrThrow(_stmt, "ordinal")
        val _columnIndexOfKind: Int = getColumnIndexOrThrow(_stmt, "kind")
        val _columnIndexOfStartsNewBoulder: Int = getColumnIndexOrThrow(_stmt, "startsNewBoulder")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfOutcome: Int = getColumnIndexOrThrow(_stmt, "outcome")
        val _columnIndexOfGradeValue: Int = getColumnIndexOrThrow(_stmt, "gradeValue")
        val _columnIndexOfGradeSystem: Int = getColumnIndexOrThrow(_stmt, "gradeSystem")
        val _columnIndexOfTopMoveReached: Int = getColumnIndexOrThrow(_stmt, "topMoveReached")
        val _columnIndexOfClimbHeightMeters: Int = getColumnIndexOrThrow(_stmt, "climbHeightMeters")
        val _columnIndexOfBoardAngleDegrees: Int = getColumnIndexOrThrow(_stmt, "boardAngleDegrees")
        val _columnIndexOfHrAvg: Int = getColumnIndexOrThrow(_stmt, "hrAvg")
        val _columnIndexOfHrMax: Int = getColumnIndexOrThrow(_stmt, "hrMax")
        val _columnIndexOfHrEnd: Int = getColumnIndexOrThrow(_stmt, "hrEnd")
        val _columnIndexOfHrAfter60s: Int = getColumnIndexOrThrow(_stmt, "hrAfter60s")
        val _columnIndexOfHrr60: Int = getColumnIndexOrThrow(_stmt, "hrr60")
        val _columnIndexOfRestAfterMs: Int = getColumnIndexOrThrow(_stmt, "restAfterMs")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: MutableList<AttemptEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AttemptEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpProblemId: String?
          if (_stmt.isNull(_columnIndexOfProblemId)) {
            _tmpProblemId = null
          } else {
            _tmpProblemId = _stmt.getText(_columnIndexOfProblemId)
          }
          val _tmpOrdinal: Int
          _tmpOrdinal = _stmt.getLong(_columnIndexOfOrdinal).toInt()
          val _tmpKind: AttemptKind
          _tmpKind = __AttemptKind_stringToEnum(_stmt.getText(_columnIndexOfKind))
          val _tmpStartsNewBoulder: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfStartsNewBoulder).toInt()
          _tmpStartsNewBoulder = _tmp != 0
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpOutcome: AttemptOutcome?
          if (_stmt.isNull(_columnIndexOfOutcome)) {
            _tmpOutcome = null
          } else {
            _tmpOutcome = __AttemptOutcome_stringToEnum(_stmt.getText(_columnIndexOfOutcome))
          }
          val _tmpGradeValue: Int?
          if (_stmt.isNull(_columnIndexOfGradeValue)) {
            _tmpGradeValue = null
          } else {
            _tmpGradeValue = _stmt.getLong(_columnIndexOfGradeValue).toInt()
          }
          val _tmpGradeSystem: GradeSystem?
          if (_stmt.isNull(_columnIndexOfGradeSystem)) {
            _tmpGradeSystem = null
          } else {
            _tmpGradeSystem = __GradeSystem_stringToEnum(_stmt.getText(_columnIndexOfGradeSystem))
          }
          val _tmpTopMoveReached: Int?
          if (_stmt.isNull(_columnIndexOfTopMoveReached)) {
            _tmpTopMoveReached = null
          } else {
            _tmpTopMoveReached = _stmt.getLong(_columnIndexOfTopMoveReached).toInt()
          }
          val _tmpClimbHeightMeters: Double?
          if (_stmt.isNull(_columnIndexOfClimbHeightMeters)) {
            _tmpClimbHeightMeters = null
          } else {
            _tmpClimbHeightMeters = _stmt.getDouble(_columnIndexOfClimbHeightMeters)
          }
          val _tmpBoardAngleDegrees: Int?
          if (_stmt.isNull(_columnIndexOfBoardAngleDegrees)) {
            _tmpBoardAngleDegrees = null
          } else {
            _tmpBoardAngleDegrees = _stmt.getLong(_columnIndexOfBoardAngleDegrees).toInt()
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
          val _tmpHrEnd: Int?
          if (_stmt.isNull(_columnIndexOfHrEnd)) {
            _tmpHrEnd = null
          } else {
            _tmpHrEnd = _stmt.getLong(_columnIndexOfHrEnd).toInt()
          }
          val _tmpHrAfter60s: Int?
          if (_stmt.isNull(_columnIndexOfHrAfter60s)) {
            _tmpHrAfter60s = null
          } else {
            _tmpHrAfter60s = _stmt.getLong(_columnIndexOfHrAfter60s).toInt()
          }
          val _tmpHrr60: Int?
          if (_stmt.isNull(_columnIndexOfHrr60)) {
            _tmpHrr60 = null
          } else {
            _tmpHrr60 = _stmt.getLong(_columnIndexOfHrr60).toInt()
          }
          val _tmpRestAfterMs: Long?
          if (_stmt.isNull(_columnIndexOfRestAfterMs)) {
            _tmpRestAfterMs = null
          } else {
            _tmpRestAfterMs = _stmt.getLong(_columnIndexOfRestAfterMs)
          }
          val _tmpMeta: RecordMeta
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeletedAt: Long?
          if (_stmt.isNull(_columnIndexOfDeletedAt)) {
            _tmpDeletedAt = null
          } else {
            _tmpDeletedAt = _stmt.getLong(_columnIndexOfDeletedAt)
          }
          val _tmpSyncState: SyncState
          _tmpSyncState = __SyncState_stringToEnum(_stmt.getText(_columnIndexOfSyncState))
          _tmpMeta = RecordMeta(_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState)
          _item = AttemptEntity(_tmpId,_tmpSessionId,_tmpProblemId,_tmpOrdinal,_tmpKind,_tmpStartsNewBoulder,_tmpStartedAt,_tmpEndedAt,_tmpOutcome,_tmpGradeValue,_tmpGradeSystem,_tmpTopMoveReached,_tmpClimbHeightMeters,_tmpBoardAngleDegrees,_tmpHrAvg,_tmpHrMax,_tmpHrEnd,_tmpHrAfter60s,_tmpHrr60,_tmpRestAfterMs,_tmpMeta)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun running(sessionId: String): AttemptEntity? {
    val _sql: String = """
        |
        |        SELECT * FROM attempt
        |        WHERE sessionId = ? AND endedAt IS NULL AND deletedAt IS NULL
        |        ORDER BY ordinal DESC LIMIT 1
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfProblemId: Int = getColumnIndexOrThrow(_stmt, "problemId")
        val _columnIndexOfOrdinal: Int = getColumnIndexOrThrow(_stmt, "ordinal")
        val _columnIndexOfKind: Int = getColumnIndexOrThrow(_stmt, "kind")
        val _columnIndexOfStartsNewBoulder: Int = getColumnIndexOrThrow(_stmt, "startsNewBoulder")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfOutcome: Int = getColumnIndexOrThrow(_stmt, "outcome")
        val _columnIndexOfGradeValue: Int = getColumnIndexOrThrow(_stmt, "gradeValue")
        val _columnIndexOfGradeSystem: Int = getColumnIndexOrThrow(_stmt, "gradeSystem")
        val _columnIndexOfTopMoveReached: Int = getColumnIndexOrThrow(_stmt, "topMoveReached")
        val _columnIndexOfClimbHeightMeters: Int = getColumnIndexOrThrow(_stmt, "climbHeightMeters")
        val _columnIndexOfBoardAngleDegrees: Int = getColumnIndexOrThrow(_stmt, "boardAngleDegrees")
        val _columnIndexOfHrAvg: Int = getColumnIndexOrThrow(_stmt, "hrAvg")
        val _columnIndexOfHrMax: Int = getColumnIndexOrThrow(_stmt, "hrMax")
        val _columnIndexOfHrEnd: Int = getColumnIndexOrThrow(_stmt, "hrEnd")
        val _columnIndexOfHrAfter60s: Int = getColumnIndexOrThrow(_stmt, "hrAfter60s")
        val _columnIndexOfHrr60: Int = getColumnIndexOrThrow(_stmt, "hrr60")
        val _columnIndexOfRestAfterMs: Int = getColumnIndexOrThrow(_stmt, "restAfterMs")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: AttemptEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpProblemId: String?
          if (_stmt.isNull(_columnIndexOfProblemId)) {
            _tmpProblemId = null
          } else {
            _tmpProblemId = _stmt.getText(_columnIndexOfProblemId)
          }
          val _tmpOrdinal: Int
          _tmpOrdinal = _stmt.getLong(_columnIndexOfOrdinal).toInt()
          val _tmpKind: AttemptKind
          _tmpKind = __AttemptKind_stringToEnum(_stmt.getText(_columnIndexOfKind))
          val _tmpStartsNewBoulder: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfStartsNewBoulder).toInt()
          _tmpStartsNewBoulder = _tmp != 0
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpOutcome: AttemptOutcome?
          if (_stmt.isNull(_columnIndexOfOutcome)) {
            _tmpOutcome = null
          } else {
            _tmpOutcome = __AttemptOutcome_stringToEnum(_stmt.getText(_columnIndexOfOutcome))
          }
          val _tmpGradeValue: Int?
          if (_stmt.isNull(_columnIndexOfGradeValue)) {
            _tmpGradeValue = null
          } else {
            _tmpGradeValue = _stmt.getLong(_columnIndexOfGradeValue).toInt()
          }
          val _tmpGradeSystem: GradeSystem?
          if (_stmt.isNull(_columnIndexOfGradeSystem)) {
            _tmpGradeSystem = null
          } else {
            _tmpGradeSystem = __GradeSystem_stringToEnum(_stmt.getText(_columnIndexOfGradeSystem))
          }
          val _tmpTopMoveReached: Int?
          if (_stmt.isNull(_columnIndexOfTopMoveReached)) {
            _tmpTopMoveReached = null
          } else {
            _tmpTopMoveReached = _stmt.getLong(_columnIndexOfTopMoveReached).toInt()
          }
          val _tmpClimbHeightMeters: Double?
          if (_stmt.isNull(_columnIndexOfClimbHeightMeters)) {
            _tmpClimbHeightMeters = null
          } else {
            _tmpClimbHeightMeters = _stmt.getDouble(_columnIndexOfClimbHeightMeters)
          }
          val _tmpBoardAngleDegrees: Int?
          if (_stmt.isNull(_columnIndexOfBoardAngleDegrees)) {
            _tmpBoardAngleDegrees = null
          } else {
            _tmpBoardAngleDegrees = _stmt.getLong(_columnIndexOfBoardAngleDegrees).toInt()
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
          val _tmpHrEnd: Int?
          if (_stmt.isNull(_columnIndexOfHrEnd)) {
            _tmpHrEnd = null
          } else {
            _tmpHrEnd = _stmt.getLong(_columnIndexOfHrEnd).toInt()
          }
          val _tmpHrAfter60s: Int?
          if (_stmt.isNull(_columnIndexOfHrAfter60s)) {
            _tmpHrAfter60s = null
          } else {
            _tmpHrAfter60s = _stmt.getLong(_columnIndexOfHrAfter60s).toInt()
          }
          val _tmpHrr60: Int?
          if (_stmt.isNull(_columnIndexOfHrr60)) {
            _tmpHrr60 = null
          } else {
            _tmpHrr60 = _stmt.getLong(_columnIndexOfHrr60).toInt()
          }
          val _tmpRestAfterMs: Long?
          if (_stmt.isNull(_columnIndexOfRestAfterMs)) {
            _tmpRestAfterMs = null
          } else {
            _tmpRestAfterMs = _stmt.getLong(_columnIndexOfRestAfterMs)
          }
          val _tmpMeta: RecordMeta
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeletedAt: Long?
          if (_stmt.isNull(_columnIndexOfDeletedAt)) {
            _tmpDeletedAt = null
          } else {
            _tmpDeletedAt = _stmt.getLong(_columnIndexOfDeletedAt)
          }
          val _tmpSyncState: SyncState
          _tmpSyncState = __SyncState_stringToEnum(_stmt.getText(_columnIndexOfSyncState))
          _tmpMeta = RecordMeta(_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState)
          _result = AttemptEntity(_tmpId,_tmpSessionId,_tmpProblemId,_tmpOrdinal,_tmpKind,_tmpStartsNewBoulder,_tmpStartedAt,_tmpEndedAt,_tmpOutcome,_tmpGradeValue,_tmpGradeSystem,_tmpTopMoveReached,_tmpClimbHeightMeters,_tmpBoardAngleDegrees,_tmpHrAvg,_tmpHrMax,_tmpHrEnd,_tmpHrAfter60s,_tmpHrr60,_tmpRestAfterMs,_tmpMeta)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun nextOrdinal(sessionId: String): Int {
    val _sql: String = "SELECT COALESCE(MAX(ordinal), 0) + 1 FROM attempt WHERE sessionId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun lastFinished(sessionId: String): AttemptEntity? {
    val _sql: String = """
        |
        |        SELECT * FROM attempt
        |        WHERE sessionId = ? AND endedAt IS NOT NULL AND deletedAt IS NULL
        |        ORDER BY ordinal DESC LIMIT 1
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfProblemId: Int = getColumnIndexOrThrow(_stmt, "problemId")
        val _columnIndexOfOrdinal: Int = getColumnIndexOrThrow(_stmt, "ordinal")
        val _columnIndexOfKind: Int = getColumnIndexOrThrow(_stmt, "kind")
        val _columnIndexOfStartsNewBoulder: Int = getColumnIndexOrThrow(_stmt, "startsNewBoulder")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfOutcome: Int = getColumnIndexOrThrow(_stmt, "outcome")
        val _columnIndexOfGradeValue: Int = getColumnIndexOrThrow(_stmt, "gradeValue")
        val _columnIndexOfGradeSystem: Int = getColumnIndexOrThrow(_stmt, "gradeSystem")
        val _columnIndexOfTopMoveReached: Int = getColumnIndexOrThrow(_stmt, "topMoveReached")
        val _columnIndexOfClimbHeightMeters: Int = getColumnIndexOrThrow(_stmt, "climbHeightMeters")
        val _columnIndexOfBoardAngleDegrees: Int = getColumnIndexOrThrow(_stmt, "boardAngleDegrees")
        val _columnIndexOfHrAvg: Int = getColumnIndexOrThrow(_stmt, "hrAvg")
        val _columnIndexOfHrMax: Int = getColumnIndexOrThrow(_stmt, "hrMax")
        val _columnIndexOfHrEnd: Int = getColumnIndexOrThrow(_stmt, "hrEnd")
        val _columnIndexOfHrAfter60s: Int = getColumnIndexOrThrow(_stmt, "hrAfter60s")
        val _columnIndexOfHrr60: Int = getColumnIndexOrThrow(_stmt, "hrr60")
        val _columnIndexOfRestAfterMs: Int = getColumnIndexOrThrow(_stmt, "restAfterMs")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: AttemptEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpProblemId: String?
          if (_stmt.isNull(_columnIndexOfProblemId)) {
            _tmpProblemId = null
          } else {
            _tmpProblemId = _stmt.getText(_columnIndexOfProblemId)
          }
          val _tmpOrdinal: Int
          _tmpOrdinal = _stmt.getLong(_columnIndexOfOrdinal).toInt()
          val _tmpKind: AttemptKind
          _tmpKind = __AttemptKind_stringToEnum(_stmt.getText(_columnIndexOfKind))
          val _tmpStartsNewBoulder: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfStartsNewBoulder).toInt()
          _tmpStartsNewBoulder = _tmp != 0
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpOutcome: AttemptOutcome?
          if (_stmt.isNull(_columnIndexOfOutcome)) {
            _tmpOutcome = null
          } else {
            _tmpOutcome = __AttemptOutcome_stringToEnum(_stmt.getText(_columnIndexOfOutcome))
          }
          val _tmpGradeValue: Int?
          if (_stmt.isNull(_columnIndexOfGradeValue)) {
            _tmpGradeValue = null
          } else {
            _tmpGradeValue = _stmt.getLong(_columnIndexOfGradeValue).toInt()
          }
          val _tmpGradeSystem: GradeSystem?
          if (_stmt.isNull(_columnIndexOfGradeSystem)) {
            _tmpGradeSystem = null
          } else {
            _tmpGradeSystem = __GradeSystem_stringToEnum(_stmt.getText(_columnIndexOfGradeSystem))
          }
          val _tmpTopMoveReached: Int?
          if (_stmt.isNull(_columnIndexOfTopMoveReached)) {
            _tmpTopMoveReached = null
          } else {
            _tmpTopMoveReached = _stmt.getLong(_columnIndexOfTopMoveReached).toInt()
          }
          val _tmpClimbHeightMeters: Double?
          if (_stmt.isNull(_columnIndexOfClimbHeightMeters)) {
            _tmpClimbHeightMeters = null
          } else {
            _tmpClimbHeightMeters = _stmt.getDouble(_columnIndexOfClimbHeightMeters)
          }
          val _tmpBoardAngleDegrees: Int?
          if (_stmt.isNull(_columnIndexOfBoardAngleDegrees)) {
            _tmpBoardAngleDegrees = null
          } else {
            _tmpBoardAngleDegrees = _stmt.getLong(_columnIndexOfBoardAngleDegrees).toInt()
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
          val _tmpHrEnd: Int?
          if (_stmt.isNull(_columnIndexOfHrEnd)) {
            _tmpHrEnd = null
          } else {
            _tmpHrEnd = _stmt.getLong(_columnIndexOfHrEnd).toInt()
          }
          val _tmpHrAfter60s: Int?
          if (_stmt.isNull(_columnIndexOfHrAfter60s)) {
            _tmpHrAfter60s = null
          } else {
            _tmpHrAfter60s = _stmt.getLong(_columnIndexOfHrAfter60s).toInt()
          }
          val _tmpHrr60: Int?
          if (_stmt.isNull(_columnIndexOfHrr60)) {
            _tmpHrr60 = null
          } else {
            _tmpHrr60 = _stmt.getLong(_columnIndexOfHrr60).toInt()
          }
          val _tmpRestAfterMs: Long?
          if (_stmt.isNull(_columnIndexOfRestAfterMs)) {
            _tmpRestAfterMs = null
          } else {
            _tmpRestAfterMs = _stmt.getLong(_columnIndexOfRestAfterMs)
          }
          val _tmpMeta: RecordMeta
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeletedAt: Long?
          if (_stmt.isNull(_columnIndexOfDeletedAt)) {
            _tmpDeletedAt = null
          } else {
            _tmpDeletedAt = _stmt.getLong(_columnIndexOfDeletedAt)
          }
          val _tmpSyncState: SyncState
          _tmpSyncState = __SyncState_stringToEnum(_stmt.getText(_columnIndexOfSyncState))
          _tmpMeta = RecordMeta(_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState)
          _result = AttemptEntity(_tmpId,_tmpSessionId,_tmpProblemId,_tmpOrdinal,_tmpKind,_tmpStartsNewBoulder,_tmpStartedAt,_tmpEndedAt,_tmpOutcome,_tmpGradeValue,_tmpGradeSystem,_tmpTopMoveReached,_tmpClimbHeightMeters,_tmpBoardAngleDegrees,_tmpHrAvg,_tmpHrMax,_tmpHrEnd,_tmpHrAfter60s,_tmpHrr60,_tmpRestAfterMs,_tmpMeta)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun previousFinished(sessionId: String, beforeAttemptId: String): AttemptEntity? {
    val _sql: String = """
        |
        |        SELECT * FROM attempt
        |        WHERE sessionId = ?
        |          AND deletedAt IS NULL
        |          AND endedAt IS NOT NULL
        |          AND ordinal < (SELECT ordinal FROM attempt WHERE id = ?)
        |        ORDER BY ordinal DESC LIMIT 1
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        _argIndex = 2
        _stmt.bindText(_argIndex, beforeAttemptId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfProblemId: Int = getColumnIndexOrThrow(_stmt, "problemId")
        val _columnIndexOfOrdinal: Int = getColumnIndexOrThrow(_stmt, "ordinal")
        val _columnIndexOfKind: Int = getColumnIndexOrThrow(_stmt, "kind")
        val _columnIndexOfStartsNewBoulder: Int = getColumnIndexOrThrow(_stmt, "startsNewBoulder")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfOutcome: Int = getColumnIndexOrThrow(_stmt, "outcome")
        val _columnIndexOfGradeValue: Int = getColumnIndexOrThrow(_stmt, "gradeValue")
        val _columnIndexOfGradeSystem: Int = getColumnIndexOrThrow(_stmt, "gradeSystem")
        val _columnIndexOfTopMoveReached: Int = getColumnIndexOrThrow(_stmt, "topMoveReached")
        val _columnIndexOfClimbHeightMeters: Int = getColumnIndexOrThrow(_stmt, "climbHeightMeters")
        val _columnIndexOfBoardAngleDegrees: Int = getColumnIndexOrThrow(_stmt, "boardAngleDegrees")
        val _columnIndexOfHrAvg: Int = getColumnIndexOrThrow(_stmt, "hrAvg")
        val _columnIndexOfHrMax: Int = getColumnIndexOrThrow(_stmt, "hrMax")
        val _columnIndexOfHrEnd: Int = getColumnIndexOrThrow(_stmt, "hrEnd")
        val _columnIndexOfHrAfter60s: Int = getColumnIndexOrThrow(_stmt, "hrAfter60s")
        val _columnIndexOfHrr60: Int = getColumnIndexOrThrow(_stmt, "hrr60")
        val _columnIndexOfRestAfterMs: Int = getColumnIndexOrThrow(_stmt, "restAfterMs")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: AttemptEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpProblemId: String?
          if (_stmt.isNull(_columnIndexOfProblemId)) {
            _tmpProblemId = null
          } else {
            _tmpProblemId = _stmt.getText(_columnIndexOfProblemId)
          }
          val _tmpOrdinal: Int
          _tmpOrdinal = _stmt.getLong(_columnIndexOfOrdinal).toInt()
          val _tmpKind: AttemptKind
          _tmpKind = __AttemptKind_stringToEnum(_stmt.getText(_columnIndexOfKind))
          val _tmpStartsNewBoulder: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfStartsNewBoulder).toInt()
          _tmpStartsNewBoulder = _tmp != 0
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpOutcome: AttemptOutcome?
          if (_stmt.isNull(_columnIndexOfOutcome)) {
            _tmpOutcome = null
          } else {
            _tmpOutcome = __AttemptOutcome_stringToEnum(_stmt.getText(_columnIndexOfOutcome))
          }
          val _tmpGradeValue: Int?
          if (_stmt.isNull(_columnIndexOfGradeValue)) {
            _tmpGradeValue = null
          } else {
            _tmpGradeValue = _stmt.getLong(_columnIndexOfGradeValue).toInt()
          }
          val _tmpGradeSystem: GradeSystem?
          if (_stmt.isNull(_columnIndexOfGradeSystem)) {
            _tmpGradeSystem = null
          } else {
            _tmpGradeSystem = __GradeSystem_stringToEnum(_stmt.getText(_columnIndexOfGradeSystem))
          }
          val _tmpTopMoveReached: Int?
          if (_stmt.isNull(_columnIndexOfTopMoveReached)) {
            _tmpTopMoveReached = null
          } else {
            _tmpTopMoveReached = _stmt.getLong(_columnIndexOfTopMoveReached).toInt()
          }
          val _tmpClimbHeightMeters: Double?
          if (_stmt.isNull(_columnIndexOfClimbHeightMeters)) {
            _tmpClimbHeightMeters = null
          } else {
            _tmpClimbHeightMeters = _stmt.getDouble(_columnIndexOfClimbHeightMeters)
          }
          val _tmpBoardAngleDegrees: Int?
          if (_stmt.isNull(_columnIndexOfBoardAngleDegrees)) {
            _tmpBoardAngleDegrees = null
          } else {
            _tmpBoardAngleDegrees = _stmt.getLong(_columnIndexOfBoardAngleDegrees).toInt()
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
          val _tmpHrEnd: Int?
          if (_stmt.isNull(_columnIndexOfHrEnd)) {
            _tmpHrEnd = null
          } else {
            _tmpHrEnd = _stmt.getLong(_columnIndexOfHrEnd).toInt()
          }
          val _tmpHrAfter60s: Int?
          if (_stmt.isNull(_columnIndexOfHrAfter60s)) {
            _tmpHrAfter60s = null
          } else {
            _tmpHrAfter60s = _stmt.getLong(_columnIndexOfHrAfter60s).toInt()
          }
          val _tmpHrr60: Int?
          if (_stmt.isNull(_columnIndexOfHrr60)) {
            _tmpHrr60 = null
          } else {
            _tmpHrr60 = _stmt.getLong(_columnIndexOfHrr60).toInt()
          }
          val _tmpRestAfterMs: Long?
          if (_stmt.isNull(_columnIndexOfRestAfterMs)) {
            _tmpRestAfterMs = null
          } else {
            _tmpRestAfterMs = _stmt.getLong(_columnIndexOfRestAfterMs)
          }
          val _tmpMeta: RecordMeta
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeletedAt: Long?
          if (_stmt.isNull(_columnIndexOfDeletedAt)) {
            _tmpDeletedAt = null
          } else {
            _tmpDeletedAt = _stmt.getLong(_columnIndexOfDeletedAt)
          }
          val _tmpSyncState: SyncState
          _tmpSyncState = __SyncState_stringToEnum(_stmt.getText(_columnIndexOfSyncState))
          _tmpMeta = RecordMeta(_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState)
          _result = AttemptEntity(_tmpId,_tmpSessionId,_tmpProblemId,_tmpOrdinal,_tmpKind,_tmpStartsNewBoulder,_tmpStartedAt,_tmpEndedAt,_tmpOutcome,_tmpGradeValue,_tmpGradeSystem,_tmpTopMoveReached,_tmpClimbHeightMeters,_tmpBoardAngleDegrees,_tmpHrAvg,_tmpHrMax,_tmpHrEnd,_tmpHrAfter60s,_tmpHrr60,_tmpRestAfterMs,_tmpMeta)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun allBySession(sessionId: String): List<AttemptEntity> {
    val _sql: String = "SELECT * FROM attempt WHERE sessionId = ? ORDER BY ordinal"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfProblemId: Int = getColumnIndexOrThrow(_stmt, "problemId")
        val _columnIndexOfOrdinal: Int = getColumnIndexOrThrow(_stmt, "ordinal")
        val _columnIndexOfKind: Int = getColumnIndexOrThrow(_stmt, "kind")
        val _columnIndexOfStartsNewBoulder: Int = getColumnIndexOrThrow(_stmt, "startsNewBoulder")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfOutcome: Int = getColumnIndexOrThrow(_stmt, "outcome")
        val _columnIndexOfGradeValue: Int = getColumnIndexOrThrow(_stmt, "gradeValue")
        val _columnIndexOfGradeSystem: Int = getColumnIndexOrThrow(_stmt, "gradeSystem")
        val _columnIndexOfTopMoveReached: Int = getColumnIndexOrThrow(_stmt, "topMoveReached")
        val _columnIndexOfClimbHeightMeters: Int = getColumnIndexOrThrow(_stmt, "climbHeightMeters")
        val _columnIndexOfBoardAngleDegrees: Int = getColumnIndexOrThrow(_stmt, "boardAngleDegrees")
        val _columnIndexOfHrAvg: Int = getColumnIndexOrThrow(_stmt, "hrAvg")
        val _columnIndexOfHrMax: Int = getColumnIndexOrThrow(_stmt, "hrMax")
        val _columnIndexOfHrEnd: Int = getColumnIndexOrThrow(_stmt, "hrEnd")
        val _columnIndexOfHrAfter60s: Int = getColumnIndexOrThrow(_stmt, "hrAfter60s")
        val _columnIndexOfHrr60: Int = getColumnIndexOrThrow(_stmt, "hrr60")
        val _columnIndexOfRestAfterMs: Int = getColumnIndexOrThrow(_stmt, "restAfterMs")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: MutableList<AttemptEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AttemptEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpProblemId: String?
          if (_stmt.isNull(_columnIndexOfProblemId)) {
            _tmpProblemId = null
          } else {
            _tmpProblemId = _stmt.getText(_columnIndexOfProblemId)
          }
          val _tmpOrdinal: Int
          _tmpOrdinal = _stmt.getLong(_columnIndexOfOrdinal).toInt()
          val _tmpKind: AttemptKind
          _tmpKind = __AttemptKind_stringToEnum(_stmt.getText(_columnIndexOfKind))
          val _tmpStartsNewBoulder: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfStartsNewBoulder).toInt()
          _tmpStartsNewBoulder = _tmp != 0
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpOutcome: AttemptOutcome?
          if (_stmt.isNull(_columnIndexOfOutcome)) {
            _tmpOutcome = null
          } else {
            _tmpOutcome = __AttemptOutcome_stringToEnum(_stmt.getText(_columnIndexOfOutcome))
          }
          val _tmpGradeValue: Int?
          if (_stmt.isNull(_columnIndexOfGradeValue)) {
            _tmpGradeValue = null
          } else {
            _tmpGradeValue = _stmt.getLong(_columnIndexOfGradeValue).toInt()
          }
          val _tmpGradeSystem: GradeSystem?
          if (_stmt.isNull(_columnIndexOfGradeSystem)) {
            _tmpGradeSystem = null
          } else {
            _tmpGradeSystem = __GradeSystem_stringToEnum(_stmt.getText(_columnIndexOfGradeSystem))
          }
          val _tmpTopMoveReached: Int?
          if (_stmt.isNull(_columnIndexOfTopMoveReached)) {
            _tmpTopMoveReached = null
          } else {
            _tmpTopMoveReached = _stmt.getLong(_columnIndexOfTopMoveReached).toInt()
          }
          val _tmpClimbHeightMeters: Double?
          if (_stmt.isNull(_columnIndexOfClimbHeightMeters)) {
            _tmpClimbHeightMeters = null
          } else {
            _tmpClimbHeightMeters = _stmt.getDouble(_columnIndexOfClimbHeightMeters)
          }
          val _tmpBoardAngleDegrees: Int?
          if (_stmt.isNull(_columnIndexOfBoardAngleDegrees)) {
            _tmpBoardAngleDegrees = null
          } else {
            _tmpBoardAngleDegrees = _stmt.getLong(_columnIndexOfBoardAngleDegrees).toInt()
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
          val _tmpHrEnd: Int?
          if (_stmt.isNull(_columnIndexOfHrEnd)) {
            _tmpHrEnd = null
          } else {
            _tmpHrEnd = _stmt.getLong(_columnIndexOfHrEnd).toInt()
          }
          val _tmpHrAfter60s: Int?
          if (_stmt.isNull(_columnIndexOfHrAfter60s)) {
            _tmpHrAfter60s = null
          } else {
            _tmpHrAfter60s = _stmt.getLong(_columnIndexOfHrAfter60s).toInt()
          }
          val _tmpHrr60: Int?
          if (_stmt.isNull(_columnIndexOfHrr60)) {
            _tmpHrr60 = null
          } else {
            _tmpHrr60 = _stmt.getLong(_columnIndexOfHrr60).toInt()
          }
          val _tmpRestAfterMs: Long?
          if (_stmt.isNull(_columnIndexOfRestAfterMs)) {
            _tmpRestAfterMs = null
          } else {
            _tmpRestAfterMs = _stmt.getLong(_columnIndexOfRestAfterMs)
          }
          val _tmpMeta: RecordMeta
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeletedAt: Long?
          if (_stmt.isNull(_columnIndexOfDeletedAt)) {
            _tmpDeletedAt = null
          } else {
            _tmpDeletedAt = _stmt.getLong(_columnIndexOfDeletedAt)
          }
          val _tmpSyncState: SyncState
          _tmpSyncState = __SyncState_stringToEnum(_stmt.getText(_columnIndexOfSyncState))
          _tmpMeta = RecordMeta(_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState)
          _item = AttemptEntity(_tmpId,_tmpSessionId,_tmpProblemId,_tmpOrdinal,_tmpKind,_tmpStartsNewBoulder,_tmpStartedAt,_tmpEndedAt,_tmpOutcome,_tmpGradeValue,_tmpGradeSystem,_tmpTopMoveReached,_tmpClimbHeightMeters,_tmpBoardAngleDegrees,_tmpHrAvg,_tmpHrMax,_tmpHrEnd,_tmpHrAfter60s,_tmpHrr60,_tmpRestAfterMs,_tmpMeta)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun finishedBySession(sessionId: String): List<AttemptEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM attempt
        |        WHERE sessionId = ? AND deletedAt IS NULL AND endedAt IS NOT NULL
        |          AND kind = 'ATTEMPT'
        |        ORDER BY ordinal
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfProblemId: Int = getColumnIndexOrThrow(_stmt, "problemId")
        val _columnIndexOfOrdinal: Int = getColumnIndexOrThrow(_stmt, "ordinal")
        val _columnIndexOfKind: Int = getColumnIndexOrThrow(_stmt, "kind")
        val _columnIndexOfStartsNewBoulder: Int = getColumnIndexOrThrow(_stmt, "startsNewBoulder")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfOutcome: Int = getColumnIndexOrThrow(_stmt, "outcome")
        val _columnIndexOfGradeValue: Int = getColumnIndexOrThrow(_stmt, "gradeValue")
        val _columnIndexOfGradeSystem: Int = getColumnIndexOrThrow(_stmt, "gradeSystem")
        val _columnIndexOfTopMoveReached: Int = getColumnIndexOrThrow(_stmt, "topMoveReached")
        val _columnIndexOfClimbHeightMeters: Int = getColumnIndexOrThrow(_stmt, "climbHeightMeters")
        val _columnIndexOfBoardAngleDegrees: Int = getColumnIndexOrThrow(_stmt, "boardAngleDegrees")
        val _columnIndexOfHrAvg: Int = getColumnIndexOrThrow(_stmt, "hrAvg")
        val _columnIndexOfHrMax: Int = getColumnIndexOrThrow(_stmt, "hrMax")
        val _columnIndexOfHrEnd: Int = getColumnIndexOrThrow(_stmt, "hrEnd")
        val _columnIndexOfHrAfter60s: Int = getColumnIndexOrThrow(_stmt, "hrAfter60s")
        val _columnIndexOfHrr60: Int = getColumnIndexOrThrow(_stmt, "hrr60")
        val _columnIndexOfRestAfterMs: Int = getColumnIndexOrThrow(_stmt, "restAfterMs")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: MutableList<AttemptEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AttemptEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpProblemId: String?
          if (_stmt.isNull(_columnIndexOfProblemId)) {
            _tmpProblemId = null
          } else {
            _tmpProblemId = _stmt.getText(_columnIndexOfProblemId)
          }
          val _tmpOrdinal: Int
          _tmpOrdinal = _stmt.getLong(_columnIndexOfOrdinal).toInt()
          val _tmpKind: AttemptKind
          _tmpKind = __AttemptKind_stringToEnum(_stmt.getText(_columnIndexOfKind))
          val _tmpStartsNewBoulder: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfStartsNewBoulder).toInt()
          _tmpStartsNewBoulder = _tmp != 0
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpOutcome: AttemptOutcome?
          if (_stmt.isNull(_columnIndexOfOutcome)) {
            _tmpOutcome = null
          } else {
            _tmpOutcome = __AttemptOutcome_stringToEnum(_stmt.getText(_columnIndexOfOutcome))
          }
          val _tmpGradeValue: Int?
          if (_stmt.isNull(_columnIndexOfGradeValue)) {
            _tmpGradeValue = null
          } else {
            _tmpGradeValue = _stmt.getLong(_columnIndexOfGradeValue).toInt()
          }
          val _tmpGradeSystem: GradeSystem?
          if (_stmt.isNull(_columnIndexOfGradeSystem)) {
            _tmpGradeSystem = null
          } else {
            _tmpGradeSystem = __GradeSystem_stringToEnum(_stmt.getText(_columnIndexOfGradeSystem))
          }
          val _tmpTopMoveReached: Int?
          if (_stmt.isNull(_columnIndexOfTopMoveReached)) {
            _tmpTopMoveReached = null
          } else {
            _tmpTopMoveReached = _stmt.getLong(_columnIndexOfTopMoveReached).toInt()
          }
          val _tmpClimbHeightMeters: Double?
          if (_stmt.isNull(_columnIndexOfClimbHeightMeters)) {
            _tmpClimbHeightMeters = null
          } else {
            _tmpClimbHeightMeters = _stmt.getDouble(_columnIndexOfClimbHeightMeters)
          }
          val _tmpBoardAngleDegrees: Int?
          if (_stmt.isNull(_columnIndexOfBoardAngleDegrees)) {
            _tmpBoardAngleDegrees = null
          } else {
            _tmpBoardAngleDegrees = _stmt.getLong(_columnIndexOfBoardAngleDegrees).toInt()
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
          val _tmpHrEnd: Int?
          if (_stmt.isNull(_columnIndexOfHrEnd)) {
            _tmpHrEnd = null
          } else {
            _tmpHrEnd = _stmt.getLong(_columnIndexOfHrEnd).toInt()
          }
          val _tmpHrAfter60s: Int?
          if (_stmt.isNull(_columnIndexOfHrAfter60s)) {
            _tmpHrAfter60s = null
          } else {
            _tmpHrAfter60s = _stmt.getLong(_columnIndexOfHrAfter60s).toInt()
          }
          val _tmpHrr60: Int?
          if (_stmt.isNull(_columnIndexOfHrr60)) {
            _tmpHrr60 = null
          } else {
            _tmpHrr60 = _stmt.getLong(_columnIndexOfHrr60).toInt()
          }
          val _tmpRestAfterMs: Long?
          if (_stmt.isNull(_columnIndexOfRestAfterMs)) {
            _tmpRestAfterMs = null
          } else {
            _tmpRestAfterMs = _stmt.getLong(_columnIndexOfRestAfterMs)
          }
          val _tmpMeta: RecordMeta
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeletedAt: Long?
          if (_stmt.isNull(_columnIndexOfDeletedAt)) {
            _tmpDeletedAt = null
          } else {
            _tmpDeletedAt = _stmt.getLong(_columnIndexOfDeletedAt)
          }
          val _tmpSyncState: SyncState
          _tmpSyncState = __SyncState_stringToEnum(_stmt.getText(_columnIndexOfSyncState))
          _tmpMeta = RecordMeta(_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState)
          _item = AttemptEntity(_tmpId,_tmpSessionId,_tmpProblemId,_tmpOrdinal,_tmpKind,_tmpStartsNewBoulder,_tmpStartedAt,_tmpEndedAt,_tmpOutcome,_tmpGradeValue,_tmpGradeSystem,_tmpTopMoveReached,_tmpClimbHeightMeters,_tmpBoardAngleDegrees,_tmpHrAvg,_tmpHrMax,_tmpHrEnd,_tmpHrAfter60s,_tmpHrr60,_tmpRestAfterMs,_tmpMeta)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun finishedBlocks(sessionId: String): List<AttemptEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM attempt
        |        WHERE sessionId = ? AND deletedAt IS NULL AND endedAt IS NOT NULL
        |        ORDER BY ordinal
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfProblemId: Int = getColumnIndexOrThrow(_stmt, "problemId")
        val _columnIndexOfOrdinal: Int = getColumnIndexOrThrow(_stmt, "ordinal")
        val _columnIndexOfKind: Int = getColumnIndexOrThrow(_stmt, "kind")
        val _columnIndexOfStartsNewBoulder: Int = getColumnIndexOrThrow(_stmt, "startsNewBoulder")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfOutcome: Int = getColumnIndexOrThrow(_stmt, "outcome")
        val _columnIndexOfGradeValue: Int = getColumnIndexOrThrow(_stmt, "gradeValue")
        val _columnIndexOfGradeSystem: Int = getColumnIndexOrThrow(_stmt, "gradeSystem")
        val _columnIndexOfTopMoveReached: Int = getColumnIndexOrThrow(_stmt, "topMoveReached")
        val _columnIndexOfClimbHeightMeters: Int = getColumnIndexOrThrow(_stmt, "climbHeightMeters")
        val _columnIndexOfBoardAngleDegrees: Int = getColumnIndexOrThrow(_stmt, "boardAngleDegrees")
        val _columnIndexOfHrAvg: Int = getColumnIndexOrThrow(_stmt, "hrAvg")
        val _columnIndexOfHrMax: Int = getColumnIndexOrThrow(_stmt, "hrMax")
        val _columnIndexOfHrEnd: Int = getColumnIndexOrThrow(_stmt, "hrEnd")
        val _columnIndexOfHrAfter60s: Int = getColumnIndexOrThrow(_stmt, "hrAfter60s")
        val _columnIndexOfHrr60: Int = getColumnIndexOrThrow(_stmt, "hrr60")
        val _columnIndexOfRestAfterMs: Int = getColumnIndexOrThrow(_stmt, "restAfterMs")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: MutableList<AttemptEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AttemptEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpProblemId: String?
          if (_stmt.isNull(_columnIndexOfProblemId)) {
            _tmpProblemId = null
          } else {
            _tmpProblemId = _stmt.getText(_columnIndexOfProblemId)
          }
          val _tmpOrdinal: Int
          _tmpOrdinal = _stmt.getLong(_columnIndexOfOrdinal).toInt()
          val _tmpKind: AttemptKind
          _tmpKind = __AttemptKind_stringToEnum(_stmt.getText(_columnIndexOfKind))
          val _tmpStartsNewBoulder: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfStartsNewBoulder).toInt()
          _tmpStartsNewBoulder = _tmp != 0
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpOutcome: AttemptOutcome?
          if (_stmt.isNull(_columnIndexOfOutcome)) {
            _tmpOutcome = null
          } else {
            _tmpOutcome = __AttemptOutcome_stringToEnum(_stmt.getText(_columnIndexOfOutcome))
          }
          val _tmpGradeValue: Int?
          if (_stmt.isNull(_columnIndexOfGradeValue)) {
            _tmpGradeValue = null
          } else {
            _tmpGradeValue = _stmt.getLong(_columnIndexOfGradeValue).toInt()
          }
          val _tmpGradeSystem: GradeSystem?
          if (_stmt.isNull(_columnIndexOfGradeSystem)) {
            _tmpGradeSystem = null
          } else {
            _tmpGradeSystem = __GradeSystem_stringToEnum(_stmt.getText(_columnIndexOfGradeSystem))
          }
          val _tmpTopMoveReached: Int?
          if (_stmt.isNull(_columnIndexOfTopMoveReached)) {
            _tmpTopMoveReached = null
          } else {
            _tmpTopMoveReached = _stmt.getLong(_columnIndexOfTopMoveReached).toInt()
          }
          val _tmpClimbHeightMeters: Double?
          if (_stmt.isNull(_columnIndexOfClimbHeightMeters)) {
            _tmpClimbHeightMeters = null
          } else {
            _tmpClimbHeightMeters = _stmt.getDouble(_columnIndexOfClimbHeightMeters)
          }
          val _tmpBoardAngleDegrees: Int?
          if (_stmt.isNull(_columnIndexOfBoardAngleDegrees)) {
            _tmpBoardAngleDegrees = null
          } else {
            _tmpBoardAngleDegrees = _stmt.getLong(_columnIndexOfBoardAngleDegrees).toInt()
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
          val _tmpHrEnd: Int?
          if (_stmt.isNull(_columnIndexOfHrEnd)) {
            _tmpHrEnd = null
          } else {
            _tmpHrEnd = _stmt.getLong(_columnIndexOfHrEnd).toInt()
          }
          val _tmpHrAfter60s: Int?
          if (_stmt.isNull(_columnIndexOfHrAfter60s)) {
            _tmpHrAfter60s = null
          } else {
            _tmpHrAfter60s = _stmt.getLong(_columnIndexOfHrAfter60s).toInt()
          }
          val _tmpHrr60: Int?
          if (_stmt.isNull(_columnIndexOfHrr60)) {
            _tmpHrr60 = null
          } else {
            _tmpHrr60 = _stmt.getLong(_columnIndexOfHrr60).toInt()
          }
          val _tmpRestAfterMs: Long?
          if (_stmt.isNull(_columnIndexOfRestAfterMs)) {
            _tmpRestAfterMs = null
          } else {
            _tmpRestAfterMs = _stmt.getLong(_columnIndexOfRestAfterMs)
          }
          val _tmpMeta: RecordMeta
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeletedAt: Long?
          if (_stmt.isNull(_columnIndexOfDeletedAt)) {
            _tmpDeletedAt = null
          } else {
            _tmpDeletedAt = _stmt.getLong(_columnIndexOfDeletedAt)
          }
          val _tmpSyncState: SyncState
          _tmpSyncState = __SyncState_stringToEnum(_stmt.getText(_columnIndexOfSyncState))
          _tmpMeta = RecordMeta(_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState)
          _item = AttemptEntity(_tmpId,_tmpSessionId,_tmpProblemId,_tmpOrdinal,_tmpKind,_tmpStartsNewBoulder,_tmpStartedAt,_tmpEndedAt,_tmpOutcome,_tmpGradeValue,_tmpGradeSystem,_tmpTopMoveReached,_tmpClimbHeightMeters,_tmpBoardAngleDegrees,_tmpHrAvg,_tmpHrMax,_tmpHrEnd,_tmpHrAfter60s,_tmpHrr60,_tmpRestAfterMs,_tmpMeta)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeByProblem(problemId: String): Flow<List<AttemptEntity>> {
    val _sql: String = "SELECT * FROM attempt WHERE problemId = ? AND deletedAt IS NULL ORDER BY startedAt"
    return createFlow(__db, false, arrayOf("attempt")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, problemId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfProblemId: Int = getColumnIndexOrThrow(_stmt, "problemId")
        val _columnIndexOfOrdinal: Int = getColumnIndexOrThrow(_stmt, "ordinal")
        val _columnIndexOfKind: Int = getColumnIndexOrThrow(_stmt, "kind")
        val _columnIndexOfStartsNewBoulder: Int = getColumnIndexOrThrow(_stmt, "startsNewBoulder")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfOutcome: Int = getColumnIndexOrThrow(_stmt, "outcome")
        val _columnIndexOfGradeValue: Int = getColumnIndexOrThrow(_stmt, "gradeValue")
        val _columnIndexOfGradeSystem: Int = getColumnIndexOrThrow(_stmt, "gradeSystem")
        val _columnIndexOfTopMoveReached: Int = getColumnIndexOrThrow(_stmt, "topMoveReached")
        val _columnIndexOfClimbHeightMeters: Int = getColumnIndexOrThrow(_stmt, "climbHeightMeters")
        val _columnIndexOfBoardAngleDegrees: Int = getColumnIndexOrThrow(_stmt, "boardAngleDegrees")
        val _columnIndexOfHrAvg: Int = getColumnIndexOrThrow(_stmt, "hrAvg")
        val _columnIndexOfHrMax: Int = getColumnIndexOrThrow(_stmt, "hrMax")
        val _columnIndexOfHrEnd: Int = getColumnIndexOrThrow(_stmt, "hrEnd")
        val _columnIndexOfHrAfter60s: Int = getColumnIndexOrThrow(_stmt, "hrAfter60s")
        val _columnIndexOfHrr60: Int = getColumnIndexOrThrow(_stmt, "hrr60")
        val _columnIndexOfRestAfterMs: Int = getColumnIndexOrThrow(_stmt, "restAfterMs")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: MutableList<AttemptEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AttemptEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpProblemId: String?
          if (_stmt.isNull(_columnIndexOfProblemId)) {
            _tmpProblemId = null
          } else {
            _tmpProblemId = _stmt.getText(_columnIndexOfProblemId)
          }
          val _tmpOrdinal: Int
          _tmpOrdinal = _stmt.getLong(_columnIndexOfOrdinal).toInt()
          val _tmpKind: AttemptKind
          _tmpKind = __AttemptKind_stringToEnum(_stmt.getText(_columnIndexOfKind))
          val _tmpStartsNewBoulder: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfStartsNewBoulder).toInt()
          _tmpStartsNewBoulder = _tmp != 0
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpOutcome: AttemptOutcome?
          if (_stmt.isNull(_columnIndexOfOutcome)) {
            _tmpOutcome = null
          } else {
            _tmpOutcome = __AttemptOutcome_stringToEnum(_stmt.getText(_columnIndexOfOutcome))
          }
          val _tmpGradeValue: Int?
          if (_stmt.isNull(_columnIndexOfGradeValue)) {
            _tmpGradeValue = null
          } else {
            _tmpGradeValue = _stmt.getLong(_columnIndexOfGradeValue).toInt()
          }
          val _tmpGradeSystem: GradeSystem?
          if (_stmt.isNull(_columnIndexOfGradeSystem)) {
            _tmpGradeSystem = null
          } else {
            _tmpGradeSystem = __GradeSystem_stringToEnum(_stmt.getText(_columnIndexOfGradeSystem))
          }
          val _tmpTopMoveReached: Int?
          if (_stmt.isNull(_columnIndexOfTopMoveReached)) {
            _tmpTopMoveReached = null
          } else {
            _tmpTopMoveReached = _stmt.getLong(_columnIndexOfTopMoveReached).toInt()
          }
          val _tmpClimbHeightMeters: Double?
          if (_stmt.isNull(_columnIndexOfClimbHeightMeters)) {
            _tmpClimbHeightMeters = null
          } else {
            _tmpClimbHeightMeters = _stmt.getDouble(_columnIndexOfClimbHeightMeters)
          }
          val _tmpBoardAngleDegrees: Int?
          if (_stmt.isNull(_columnIndexOfBoardAngleDegrees)) {
            _tmpBoardAngleDegrees = null
          } else {
            _tmpBoardAngleDegrees = _stmt.getLong(_columnIndexOfBoardAngleDegrees).toInt()
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
          val _tmpHrEnd: Int?
          if (_stmt.isNull(_columnIndexOfHrEnd)) {
            _tmpHrEnd = null
          } else {
            _tmpHrEnd = _stmt.getLong(_columnIndexOfHrEnd).toInt()
          }
          val _tmpHrAfter60s: Int?
          if (_stmt.isNull(_columnIndexOfHrAfter60s)) {
            _tmpHrAfter60s = null
          } else {
            _tmpHrAfter60s = _stmt.getLong(_columnIndexOfHrAfter60s).toInt()
          }
          val _tmpHrr60: Int?
          if (_stmt.isNull(_columnIndexOfHrr60)) {
            _tmpHrr60 = null
          } else {
            _tmpHrr60 = _stmt.getLong(_columnIndexOfHrr60).toInt()
          }
          val _tmpRestAfterMs: Long?
          if (_stmt.isNull(_columnIndexOfRestAfterMs)) {
            _tmpRestAfterMs = null
          } else {
            _tmpRestAfterMs = _stmt.getLong(_columnIndexOfRestAfterMs)
          }
          val _tmpMeta: RecordMeta
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeletedAt: Long?
          if (_stmt.isNull(_columnIndexOfDeletedAt)) {
            _tmpDeletedAt = null
          } else {
            _tmpDeletedAt = _stmt.getLong(_columnIndexOfDeletedAt)
          }
          val _tmpSyncState: SyncState
          _tmpSyncState = __SyncState_stringToEnum(_stmt.getText(_columnIndexOfSyncState))
          _tmpMeta = RecordMeta(_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState)
          _item = AttemptEntity(_tmpId,_tmpSessionId,_tmpProblemId,_tmpOrdinal,_tmpKind,_tmpStartsNewBoulder,_tmpStartedAt,_tmpEndedAt,_tmpOutcome,_tmpGradeValue,_tmpGradeSystem,_tmpTopMoveReached,_tmpClimbHeightMeters,_tmpBoardAngleDegrees,_tmpHrAvg,_tmpHrMax,_tmpHrEnd,_tmpHrAfter60s,_tmpHrr60,_tmpRestAfterMs,_tmpMeta)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun lastGradeValue(): Int? {
    val _sql: String = """
        |
        |        SELECT gradeValue FROM attempt
        |        WHERE gradeValue IS NOT NULL AND deletedAt IS NULL
        |        ORDER BY startedAt DESC LIMIT 1
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
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

  public override suspend fun lastBoardAngle(): Int? {
    val _sql: String = """
        |
        |        SELECT boardAngleDegrees FROM attempt
        |        WHERE boardAngleDegrees IS NOT NULL AND deletedAt IS NULL
        |        ORDER BY startedAt DESC LIMIT 1
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
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

  public override suspend fun aggregate(sessionId: String): AttemptAggregate {
    val _sql: String = """
        |
        |        SELECT
        |            COUNT(*)                                                      AS attemptCount,
        |            COALESCE(SUM(CASE WHEN outcome IN ('FLASH','TOP') THEN 1 ELSE 0 END), 0) AS sendCount,
        |            COALESCE(SUM(CASE WHEN outcome = 'FLASH' THEN 1 ELSE 0 END), 0)         AS flashCount,
        |            COALESCE(SUM(endedAt - startedAt), 0)                         AS workMs,
        |            MAX(CASE WHEN outcome IN ('FLASH','TOP') THEN gradeValue END) AS hardestSendValue,
        |            AVG(hrr60)                                                    AS hrr60Avg,
        |            SUM(climbHeightMeters)                                        AS climbHeightMeters,
        |            MAX(climbHeightMeters)                                        AS maxClimbHeightMeters
        |        FROM attempt
        |        WHERE sessionId = ?
        |          AND deletedAt IS NULL
        |          AND endedAt IS NOT NULL
        |          AND kind = 'ATTEMPT'
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfAttemptCount: Int = 0
        val _columnIndexOfSendCount: Int = 1
        val _columnIndexOfFlashCount: Int = 2
        val _columnIndexOfWorkMs: Int = 3
        val _columnIndexOfHardestSendValue: Int = 4
        val _columnIndexOfHrr60Avg: Int = 5
        val _columnIndexOfClimbHeightMeters: Int = 6
        val _columnIndexOfMaxClimbHeightMeters: Int = 7
        val _result: AttemptAggregate
        if (_stmt.step()) {
          val _tmpAttemptCount: Int
          _tmpAttemptCount = _stmt.getLong(_columnIndexOfAttemptCount).toInt()
          val _tmpSendCount: Int
          _tmpSendCount = _stmt.getLong(_columnIndexOfSendCount).toInt()
          val _tmpFlashCount: Int
          _tmpFlashCount = _stmt.getLong(_columnIndexOfFlashCount).toInt()
          val _tmpWorkMs: Long
          _tmpWorkMs = _stmt.getLong(_columnIndexOfWorkMs)
          val _tmpHardestSendValue: Int?
          if (_stmt.isNull(_columnIndexOfHardestSendValue)) {
            _tmpHardestSendValue = null
          } else {
            _tmpHardestSendValue = _stmt.getLong(_columnIndexOfHardestSendValue).toInt()
          }
          val _tmpHrr60Avg: Double?
          if (_stmt.isNull(_columnIndexOfHrr60Avg)) {
            _tmpHrr60Avg = null
          } else {
            _tmpHrr60Avg = _stmt.getDouble(_columnIndexOfHrr60Avg)
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
          _result = AttemptAggregate(_tmpAttemptCount,_tmpSendCount,_tmpFlashCount,_tmpWorkMs,_tmpHardestSendValue,_tmpHrr60Avg,_tmpClimbHeightMeters,_tmpMaxClimbHeightMeters)
        } else {
          error("The query result was empty, but expected a single row to return a NON-NULL object of type 'at.mentor.bouldclockapp.`data`.db.dao.AttemptAggregate'.")
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeGradeHistogram(since: Long): Flow<List<GradeBucket>> {
    val _sql: String = """
        |
        |        SELECT
        |            a.gradeValue                                                    AS gradeValue,
        |            COUNT(*)                                                        AS attemptCount,
        |            COALESCE(SUM(CASE WHEN a.outcome IN ('FLASH','TOP') THEN 1 ELSE 0 END), 0) AS sendCount,
        |            COALESCE(SUM(CASE WHEN a.outcome = 'FLASH' THEN 1 ELSE 0 END), 0)          AS flashCount
        |        FROM attempt a
        |        JOIN session s ON s.id = a.sessionId
        |        WHERE a.deletedAt IS NULL
        |          AND a.endedAt IS NOT NULL
        |          AND a.gradeValue IS NOT NULL
        |          AND a.kind = 'ATTEMPT'
        |          AND s.deletedAt IS NULL
        |          AND s.state = 'FINISHED'
        |          AND a.startedAt >= ?
        |        GROUP BY a.gradeValue
        |        ORDER BY a.gradeValue
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("attempt", "session")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, since)
        val _columnIndexOfGradeValue: Int = 0
        val _columnIndexOfAttemptCount: Int = 1
        val _columnIndexOfSendCount: Int = 2
        val _columnIndexOfFlashCount: Int = 3
        val _result: MutableList<GradeBucket> = mutableListOf()
        while (_stmt.step()) {
          val _item: GradeBucket
          val _tmpGradeValue: Int
          _tmpGradeValue = _stmt.getLong(_columnIndexOfGradeValue).toInt()
          val _tmpAttemptCount: Int
          _tmpAttemptCount = _stmt.getLong(_columnIndexOfAttemptCount).toInt()
          val _tmpSendCount: Int
          _tmpSendCount = _stmt.getLong(_columnIndexOfSendCount).toInt()
          val _tmpFlashCount: Int
          _tmpFlashCount = _stmt.getLong(_columnIndexOfFlashCount).toInt()
          _item = GradeBucket(_tmpGradeValue,_tmpAttemptCount,_tmpSendCount,_tmpFlashCount)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeProblemTallies(): Flow<List<ProblemTally>> {
    val _sql: String = """
        |
        |        SELECT
        |            a.problemId                                                     AS problemId,
        |            COUNT(*)                                                        AS attemptCount,
        |            COALESCE(SUM(CASE WHEN a.outcome IN ('FLASH','TOP') THEN 1 ELSE 0 END), 0) AS sendCount,
        |            MIN(a.startedAt)                                                AS firstAt,
        |            MAX(a.startedAt)                                                AS lastAt
        |        FROM attempt a
        |        JOIN session s ON s.id = a.sessionId
        |        WHERE a.problemId IS NOT NULL
        |          AND a.deletedAt IS NULL
        |          AND s.deletedAt IS NULL
        |        GROUP BY a.problemId
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("attempt", "session")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfProblemId: Int = 0
        val _columnIndexOfAttemptCount: Int = 1
        val _columnIndexOfSendCount: Int = 2
        val _columnIndexOfFirstAt: Int = 3
        val _columnIndexOfLastAt: Int = 4
        val _result: MutableList<ProblemTally> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProblemTally
          val _tmpProblemId: String
          _tmpProblemId = _stmt.getText(_columnIndexOfProblemId)
          val _tmpAttemptCount: Int
          _tmpAttemptCount = _stmt.getLong(_columnIndexOfAttemptCount).toInt()
          val _tmpSendCount: Int
          _tmpSendCount = _stmt.getLong(_columnIndexOfSendCount).toInt()
          val _tmpFirstAt: Long
          _tmpFirstAt = _stmt.getLong(_columnIndexOfFirstAt)
          val _tmpLastAt: Long
          _tmpLastAt = _stmt.getLong(_columnIndexOfLastAt)
          _item = ProblemTally(_tmpProblemId,_tmpAttemptCount,_tmpSendCount,_tmpFirstAt,_tmpLastAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: String) {
    val _sql: String = "DELETE FROM attempt WHERE id = ?"
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

  public override suspend fun softDelete(id: String, now: Long) {
    val _sql: String = "UPDATE attempt SET deletedAt = ?, updatedAt = ?, syncState = 'PENDING' WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, now)
        _argIndex = 2
        _stmt.bindLong(_argIndex, now)
        _argIndex = 3
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setProblem(
    attemptId: String,
    problemId: String?,
    now: Long,
  ) {
    val _sql: String = """
        |
        |        UPDATE attempt SET problemId = ?, updatedAt = ?, syncState = 'PENDING'
        |        WHERE id = ?
        |        
        """.trimMargin()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        if (problemId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, problemId)
        }
        _argIndex = 2
        _stmt.bindLong(_argIndex, now)
        _argIndex = 3
        _stmt.bindText(_argIndex, attemptId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __AttemptKind_enumToString(_value: AttemptKind): String = when (_value) {
    AttemptKind.ATTEMPT -> "ATTEMPT"
    AttemptKind.MOVE_TEST -> "MOVE_TEST"
  }

  private fun __AttemptOutcome_enumToString(_value: AttemptOutcome): String = when (_value) {
    AttemptOutcome.FLASH -> "FLASH"
    AttemptOutcome.TOP -> "TOP"
    AttemptOutcome.ZONE -> "ZONE"
    AttemptOutcome.FAIL -> "FAIL"
  }

  private fun __GradeSystem_enumToString(_value: GradeSystem): String = when (_value) {
    GradeSystem.FONT -> "FONT"
    GradeSystem.V_SCALE -> "V_SCALE"
    GradeSystem.COLOR -> "COLOR"
  }

  private fun __SyncState_enumToString(_value: SyncState): String = when (_value) {
    SyncState.PENDING -> "PENDING"
    SyncState.SYNCED -> "SYNCED"
  }

  private fun __AttemptKind_stringToEnum(_value: String): AttemptKind = when (_value) {
    "ATTEMPT" -> AttemptKind.ATTEMPT
    "MOVE_TEST" -> AttemptKind.MOVE_TEST
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  private fun __AttemptOutcome_stringToEnum(_value: String): AttemptOutcome = when (_value) {
    "FLASH" -> AttemptOutcome.FLASH
    "TOP" -> AttemptOutcome.TOP
    "ZONE" -> AttemptOutcome.ZONE
    "FAIL" -> AttemptOutcome.FAIL
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  private fun __GradeSystem_stringToEnum(_value: String): GradeSystem = when (_value) {
    "FONT" -> GradeSystem.FONT
    "V_SCALE" -> GradeSystem.V_SCALE
    "COLOR" -> GradeSystem.COLOR
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

package at.mentor.bouldclockapp.`data`.db.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.getTotalChangedRows
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import at.mentor.bouldclockapp.`data`.db.entity.RecordMeta
import at.mentor.bouldclockapp.`data`.db.entity.SessionEntity
import at.mentor.bouldclockapp.core.model.SessionState
import at.mentor.bouldclockapp.core.model.SessionType
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
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SessionDao_Impl(
  __db: RoomDatabase,
) : SessionDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfSessionEntity: EntityUpsertAdapter<SessionEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfSessionEntity = EntityUpsertAdapter<SessionEntity>(object : EntityInsertAdapter<SessionEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `session` (`id`,`gymId`,`type`,`state`,`startedAt`,`endedAt`,`pausedMs`,`restTargetMs`,`rpe`,`note`,`createdAt`,`updatedAt`,`deletedAt`,`syncState`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SessionEntity) {
        statement.bindText(1, entity.id)
        val _tmpGymId: String? = entity.gymId
        if (_tmpGymId == null) {
          statement.bindNull(2)
        } else {
          statement.bindText(2, _tmpGymId)
        }
        statement.bindText(3, __SessionType_enumToString(entity.type))
        statement.bindText(4, __SessionState_enumToString(entity.state))
        statement.bindLong(5, entity.startedAt)
        val _tmpEndedAt: Long? = entity.endedAt
        if (_tmpEndedAt == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpEndedAt)
        }
        statement.bindLong(7, entity.pausedMs)
        statement.bindLong(8, entity.restTargetMs)
        val _tmpRpe: Int? = entity.rpe
        if (_tmpRpe == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpRpe.toLong())
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpNote)
        }
        val _tmpMeta: RecordMeta = entity.meta
        statement.bindLong(11, _tmpMeta.createdAt)
        statement.bindLong(12, _tmpMeta.updatedAt)
        val _tmpDeletedAt: Long? = _tmpMeta.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmpDeletedAt)
        }
        statement.bindText(14, __SyncState_enumToString(_tmpMeta.syncState))
      }
    }, object : EntityDeleteOrUpdateAdapter<SessionEntity>() {
      protected override fun createQuery(): String = "UPDATE `session` SET `id` = ?,`gymId` = ?,`type` = ?,`state` = ?,`startedAt` = ?,`endedAt` = ?,`pausedMs` = ?,`restTargetMs` = ?,`rpe` = ?,`note` = ?,`createdAt` = ?,`updatedAt` = ?,`deletedAt` = ?,`syncState` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SessionEntity) {
        statement.bindText(1, entity.id)
        val _tmpGymId: String? = entity.gymId
        if (_tmpGymId == null) {
          statement.bindNull(2)
        } else {
          statement.bindText(2, _tmpGymId)
        }
        statement.bindText(3, __SessionType_enumToString(entity.type))
        statement.bindText(4, __SessionState_enumToString(entity.state))
        statement.bindLong(5, entity.startedAt)
        val _tmpEndedAt: Long? = entity.endedAt
        if (_tmpEndedAt == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpEndedAt)
        }
        statement.bindLong(7, entity.pausedMs)
        statement.bindLong(8, entity.restTargetMs)
        val _tmpRpe: Int? = entity.rpe
        if (_tmpRpe == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpRpe.toLong())
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpNote)
        }
        val _tmpMeta: RecordMeta = entity.meta
        statement.bindLong(11, _tmpMeta.createdAt)
        statement.bindLong(12, _tmpMeta.updatedAt)
        val _tmpDeletedAt: Long? = _tmpMeta.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmpDeletedAt)
        }
        statement.bindText(14, __SyncState_enumToString(_tmpMeta.syncState))
        statement.bindText(15, entity.id)
      }
    })
  }

  public override suspend fun upsert(session: SessionEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfSessionEntity.upsert(_connection, session)
  }

  public override suspend fun byId(id: String): SessionEntity? {
    val _sql: String = "SELECT * FROM session WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGymId: Int = getColumnIndexOrThrow(_stmt, "gymId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfState: Int = getColumnIndexOrThrow(_stmt, "state")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfPausedMs: Int = getColumnIndexOrThrow(_stmt, "pausedMs")
        val _columnIndexOfRestTargetMs: Int = getColumnIndexOrThrow(_stmt, "restTargetMs")
        val _columnIndexOfRpe: Int = getColumnIndexOrThrow(_stmt, "rpe")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: SessionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpGymId: String?
          if (_stmt.isNull(_columnIndexOfGymId)) {
            _tmpGymId = null
          } else {
            _tmpGymId = _stmt.getText(_columnIndexOfGymId)
          }
          val _tmpType: SessionType
          _tmpType = __SessionType_stringToEnum(_stmt.getText(_columnIndexOfType))
          val _tmpState: SessionState
          _tmpState = __SessionState_stringToEnum(_stmt.getText(_columnIndexOfState))
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpPausedMs: Long
          _tmpPausedMs = _stmt.getLong(_columnIndexOfPausedMs)
          val _tmpRestTargetMs: Long
          _tmpRestTargetMs = _stmt.getLong(_columnIndexOfRestTargetMs)
          val _tmpRpe: Int?
          if (_stmt.isNull(_columnIndexOfRpe)) {
            _tmpRpe = null
          } else {
            _tmpRpe = _stmt.getLong(_columnIndexOfRpe).toInt()
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
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
          _result = SessionEntity(_tmpId,_tmpGymId,_tmpType,_tmpState,_tmpStartedAt,_tmpEndedAt,_tmpPausedMs,_tmpRestTargetMs,_tmpRpe,_tmpNote,_tmpMeta)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observe(id: String): Flow<SessionEntity?> {
    val _sql: String = "SELECT * FROM session WHERE id = ?"
    return createFlow(__db, false, arrayOf("session")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGymId: Int = getColumnIndexOrThrow(_stmt, "gymId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfState: Int = getColumnIndexOrThrow(_stmt, "state")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfPausedMs: Int = getColumnIndexOrThrow(_stmt, "pausedMs")
        val _columnIndexOfRestTargetMs: Int = getColumnIndexOrThrow(_stmt, "restTargetMs")
        val _columnIndexOfRpe: Int = getColumnIndexOrThrow(_stmt, "rpe")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: SessionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpGymId: String?
          if (_stmt.isNull(_columnIndexOfGymId)) {
            _tmpGymId = null
          } else {
            _tmpGymId = _stmt.getText(_columnIndexOfGymId)
          }
          val _tmpType: SessionType
          _tmpType = __SessionType_stringToEnum(_stmt.getText(_columnIndexOfType))
          val _tmpState: SessionState
          _tmpState = __SessionState_stringToEnum(_stmt.getText(_columnIndexOfState))
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpPausedMs: Long
          _tmpPausedMs = _stmt.getLong(_columnIndexOfPausedMs)
          val _tmpRestTargetMs: Long
          _tmpRestTargetMs = _stmt.getLong(_columnIndexOfRestTargetMs)
          val _tmpRpe: Int?
          if (_stmt.isNull(_columnIndexOfRpe)) {
            _tmpRpe = null
          } else {
            _tmpRpe = _stmt.getLong(_columnIndexOfRpe).toInt()
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
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
          _result = SessionEntity(_tmpId,_tmpGymId,_tmpType,_tmpState,_tmpStartedAt,_tmpEndedAt,_tmpPausedMs,_tmpRestTargetMs,_tmpRpe,_tmpNote,_tmpMeta)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun findUnfinished(): SessionEntity? {
    val _sql: String = """
        |
        |        SELECT * FROM session
        |        WHERE state IN ('ACTIVE', 'PAUSED') AND deletedAt IS NULL
        |        ORDER BY startedAt DESC LIMIT 1
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGymId: Int = getColumnIndexOrThrow(_stmt, "gymId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfState: Int = getColumnIndexOrThrow(_stmt, "state")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfPausedMs: Int = getColumnIndexOrThrow(_stmt, "pausedMs")
        val _columnIndexOfRestTargetMs: Int = getColumnIndexOrThrow(_stmt, "restTargetMs")
        val _columnIndexOfRpe: Int = getColumnIndexOrThrow(_stmt, "rpe")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: SessionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpGymId: String?
          if (_stmt.isNull(_columnIndexOfGymId)) {
            _tmpGymId = null
          } else {
            _tmpGymId = _stmt.getText(_columnIndexOfGymId)
          }
          val _tmpType: SessionType
          _tmpType = __SessionType_stringToEnum(_stmt.getText(_columnIndexOfType))
          val _tmpState: SessionState
          _tmpState = __SessionState_stringToEnum(_stmt.getText(_columnIndexOfState))
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpPausedMs: Long
          _tmpPausedMs = _stmt.getLong(_columnIndexOfPausedMs)
          val _tmpRestTargetMs: Long
          _tmpRestTargetMs = _stmt.getLong(_columnIndexOfRestTargetMs)
          val _tmpRpe: Int?
          if (_stmt.isNull(_columnIndexOfRpe)) {
            _tmpRpe = null
          } else {
            _tmpRpe = _stmt.getLong(_columnIndexOfRpe).toInt()
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
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
          _result = SessionEntity(_tmpId,_tmpGymId,_tmpType,_tmpState,_tmpStartedAt,_tmpEndedAt,_tmpPausedMs,_tmpRestTargetMs,_tmpRpe,_tmpNote,_tmpMeta)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeRecent(limit: Int): Flow<List<SessionEntity>> {
    val _sql: String = """
        |
        |        SELECT * FROM session
        |        WHERE deletedAt IS NULL AND state = 'FINISHED'
        |        ORDER BY startedAt DESC LIMIT ?
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("session")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGymId: Int = getColumnIndexOrThrow(_stmt, "gymId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfState: Int = getColumnIndexOrThrow(_stmt, "state")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfPausedMs: Int = getColumnIndexOrThrow(_stmt, "pausedMs")
        val _columnIndexOfRestTargetMs: Int = getColumnIndexOrThrow(_stmt, "restTargetMs")
        val _columnIndexOfRpe: Int = getColumnIndexOrThrow(_stmt, "rpe")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: MutableList<SessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpGymId: String?
          if (_stmt.isNull(_columnIndexOfGymId)) {
            _tmpGymId = null
          } else {
            _tmpGymId = _stmt.getText(_columnIndexOfGymId)
          }
          val _tmpType: SessionType
          _tmpType = __SessionType_stringToEnum(_stmt.getText(_columnIndexOfType))
          val _tmpState: SessionState
          _tmpState = __SessionState_stringToEnum(_stmt.getText(_columnIndexOfState))
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpPausedMs: Long
          _tmpPausedMs = _stmt.getLong(_columnIndexOfPausedMs)
          val _tmpRestTargetMs: Long
          _tmpRestTargetMs = _stmt.getLong(_columnIndexOfRestTargetMs)
          val _tmpRpe: Int?
          if (_stmt.isNull(_columnIndexOfRpe)) {
            _tmpRpe = null
          } else {
            _tmpRpe = _stmt.getLong(_columnIndexOfRpe).toInt()
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
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
          _item = SessionEntity(_tmpId,_tmpGymId,_tmpType,_tmpState,_tmpStartedAt,_tmpEndedAt,_tmpPausedMs,_tmpRestTargetMs,_tmpRpe,_tmpNote,_tmpMeta)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun pendingSync(): List<SessionEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM session
        |        WHERE state = 'FINISHED' AND deletedAt IS NULL AND syncState = 'PENDING'
        |        ORDER BY startedAt
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGymId: Int = getColumnIndexOrThrow(_stmt, "gymId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfState: Int = getColumnIndexOrThrow(_stmt, "state")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfPausedMs: Int = getColumnIndexOrThrow(_stmt, "pausedMs")
        val _columnIndexOfRestTargetMs: Int = getColumnIndexOrThrow(_stmt, "restTargetMs")
        val _columnIndexOfRpe: Int = getColumnIndexOrThrow(_stmt, "rpe")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: MutableList<SessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpGymId: String?
          if (_stmt.isNull(_columnIndexOfGymId)) {
            _tmpGymId = null
          } else {
            _tmpGymId = _stmt.getText(_columnIndexOfGymId)
          }
          val _tmpType: SessionType
          _tmpType = __SessionType_stringToEnum(_stmt.getText(_columnIndexOfType))
          val _tmpState: SessionState
          _tmpState = __SessionState_stringToEnum(_stmt.getText(_columnIndexOfState))
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpPausedMs: Long
          _tmpPausedMs = _stmt.getLong(_columnIndexOfPausedMs)
          val _tmpRestTargetMs: Long
          _tmpRestTargetMs = _stmt.getLong(_columnIndexOfRestTargetMs)
          val _tmpRpe: Int?
          if (_stmt.isNull(_columnIndexOfRpe)) {
            _tmpRpe = null
          } else {
            _tmpRpe = _stmt.getLong(_columnIndexOfRpe).toInt()
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
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
          _item = SessionEntity(_tmpId,_tmpGymId,_tmpType,_tmpState,_tmpStartedAt,_tmpEndedAt,_tmpPausedMs,_tmpRestTargetMs,_tmpRpe,_tmpNote,_tmpMeta)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun previousComparable(
    type: SessionType,
    gymId: String?,
    before: Long,
  ): SessionEntity? {
    val _sql: String = """
        |
        |        SELECT * FROM session
        |        WHERE deletedAt IS NULL
        |          AND state = 'FINISHED'
        |          AND type = ?
        |          AND (? IS NULL OR gymId = ?)
        |          AND startedAt < ?
        |        ORDER BY startedAt DESC LIMIT 1
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
        _stmt.bindLong(_argIndex, before)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGymId: Int = getColumnIndexOrThrow(_stmt, "gymId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfState: Int = getColumnIndexOrThrow(_stmt, "state")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfPausedMs: Int = getColumnIndexOrThrow(_stmt, "pausedMs")
        val _columnIndexOfRestTargetMs: Int = getColumnIndexOrThrow(_stmt, "restTargetMs")
        val _columnIndexOfRpe: Int = getColumnIndexOrThrow(_stmt, "rpe")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: SessionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpGymId: String?
          if (_stmt.isNull(_columnIndexOfGymId)) {
            _tmpGymId = null
          } else {
            _tmpGymId = _stmt.getText(_columnIndexOfGymId)
          }
          val _tmpType: SessionType
          _tmpType = __SessionType_stringToEnum(_stmt.getText(_columnIndexOfType))
          val _tmpState: SessionState
          _tmpState = __SessionState_stringToEnum(_stmt.getText(_columnIndexOfState))
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long?
          if (_stmt.isNull(_columnIndexOfEndedAt)) {
            _tmpEndedAt = null
          } else {
            _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          }
          val _tmpPausedMs: Long
          _tmpPausedMs = _stmt.getLong(_columnIndexOfPausedMs)
          val _tmpRestTargetMs: Long
          _tmpRestTargetMs = _stmt.getLong(_columnIndexOfRestTargetMs)
          val _tmpRpe: Int?
          if (_stmt.isNull(_columnIndexOfRpe)) {
            _tmpRpe = null
          } else {
            _tmpRpe = _stmt.getLong(_columnIndexOfRpe).toInt()
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
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
          _result = SessionEntity(_tmpId,_tmpGymId,_tmpType,_tmpState,_tmpStartedAt,_tmpEndedAt,_tmpPausedMs,_tmpRestTargetMs,_tmpRpe,_tmpNote,_tmpMeta)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun abandonOpenExcept(keepId: String, now: Long): Int {
    val _sql: String = """
        |
        |        UPDATE session
        |        SET state = 'ABANDONED', updatedAt = ?, syncState = 'PENDING'
        |        WHERE state IN ('ACTIVE', 'PAUSED') AND deletedAt IS NULL AND id <> ?
        |        
        """.trimMargin()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, now)
        _argIndex = 2
        _stmt.bindText(_argIndex, keepId)
        _stmt.step()
        getTotalChangedRows(_connection)
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

  private fun __SessionState_enumToString(_value: SessionState): String = when (_value) {
    SessionState.ACTIVE -> "ACTIVE"
    SessionState.PAUSED -> "PAUSED"
    SessionState.FINISHED -> "FINISHED"
    SessionState.ABANDONED -> "ABANDONED"
  }

  private fun __SyncState_enumToString(_value: SyncState): String = when (_value) {
    SyncState.PENDING -> "PENDING"
    SyncState.SYNCED -> "SYNCED"
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

  private fun __SessionState_stringToEnum(_value: String): SessionState = when (_value) {
    "ACTIVE" -> SessionState.ACTIVE
    "PAUSED" -> SessionState.PAUSED
    "FINISHED" -> SessionState.FINISHED
    "ABANDONED" -> SessionState.ABANDONED
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

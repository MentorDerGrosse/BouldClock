package at.mentor.bouldclockapp.`data`.db.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import at.mentor.bouldclockapp.`data`.db.entity.ProblemEntity
import at.mentor.bouldclockapp.`data`.db.entity.RecordMeta
import at.mentor.bouldclockapp.core.model.SyncState
import at.mentor.bouldclockapp.core.model.WallAngle
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
public class ProblemDao_Impl(
  __db: RoomDatabase,
) : ProblemDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfProblemEntity: EntityUpsertAdapter<ProblemEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfProblemEntity = EntityUpsertAdapter<ProblemEntity>(object : EntityInsertAdapter<ProblemEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `problem` (`id`,`gymId`,`label`,`colorHex`,`gradeValue`,`wallAngle`,`firstSentAt`,`retiredAt`,`createdAt`,`updatedAt`,`deletedAt`,`syncState`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ProblemEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.gymId)
        statement.bindText(3, entity.label)
        val _tmpColorHex: String? = entity.colorHex
        if (_tmpColorHex == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpColorHex)
        }
        val _tmpGradeValue: Int? = entity.gradeValue
        if (_tmpGradeValue == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpGradeValue.toLong())
        }
        val _tmpWallAngle: WallAngle? = entity.wallAngle
        if (_tmpWallAngle == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, __WallAngle_enumToString(_tmpWallAngle))
        }
        val _tmpFirstSentAt: Long? = entity.firstSentAt
        if (_tmpFirstSentAt == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpFirstSentAt)
        }
        val _tmpRetiredAt: Long? = entity.retiredAt
        if (_tmpRetiredAt == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpRetiredAt)
        }
        val _tmpMeta: RecordMeta = entity.meta
        statement.bindLong(9, _tmpMeta.createdAt)
        statement.bindLong(10, _tmpMeta.updatedAt)
        val _tmpDeletedAt: Long? = _tmpMeta.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(11)
        } else {
          statement.bindLong(11, _tmpDeletedAt)
        }
        statement.bindText(12, __SyncState_enumToString(_tmpMeta.syncState))
      }
    }, object : EntityDeleteOrUpdateAdapter<ProblemEntity>() {
      protected override fun createQuery(): String = "UPDATE `problem` SET `id` = ?,`gymId` = ?,`label` = ?,`colorHex` = ?,`gradeValue` = ?,`wallAngle` = ?,`firstSentAt` = ?,`retiredAt` = ?,`createdAt` = ?,`updatedAt` = ?,`deletedAt` = ?,`syncState` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ProblemEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.gymId)
        statement.bindText(3, entity.label)
        val _tmpColorHex: String? = entity.colorHex
        if (_tmpColorHex == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpColorHex)
        }
        val _tmpGradeValue: Int? = entity.gradeValue
        if (_tmpGradeValue == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpGradeValue.toLong())
        }
        val _tmpWallAngle: WallAngle? = entity.wallAngle
        if (_tmpWallAngle == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, __WallAngle_enumToString(_tmpWallAngle))
        }
        val _tmpFirstSentAt: Long? = entity.firstSentAt
        if (_tmpFirstSentAt == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpFirstSentAt)
        }
        val _tmpRetiredAt: Long? = entity.retiredAt
        if (_tmpRetiredAt == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpRetiredAt)
        }
        val _tmpMeta: RecordMeta = entity.meta
        statement.bindLong(9, _tmpMeta.createdAt)
        statement.bindLong(10, _tmpMeta.updatedAt)
        val _tmpDeletedAt: Long? = _tmpMeta.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(11)
        } else {
          statement.bindLong(11, _tmpDeletedAt)
        }
        statement.bindText(12, __SyncState_enumToString(_tmpMeta.syncState))
        statement.bindText(13, entity.id)
      }
    })
  }

  public override suspend fun upsert(problem: ProblemEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfProblemEntity.upsert(_connection, problem)
  }

  public override suspend fun byId(id: String): ProblemEntity? {
    val _sql: String = "SELECT * FROM problem WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGymId: Int = getColumnIndexOrThrow(_stmt, "gymId")
        val _columnIndexOfLabel: Int = getColumnIndexOrThrow(_stmt, "label")
        val _columnIndexOfColorHex: Int = getColumnIndexOrThrow(_stmt, "colorHex")
        val _columnIndexOfGradeValue: Int = getColumnIndexOrThrow(_stmt, "gradeValue")
        val _columnIndexOfWallAngle: Int = getColumnIndexOrThrow(_stmt, "wallAngle")
        val _columnIndexOfFirstSentAt: Int = getColumnIndexOrThrow(_stmt, "firstSentAt")
        val _columnIndexOfRetiredAt: Int = getColumnIndexOrThrow(_stmt, "retiredAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: ProblemEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpGymId: String
          _tmpGymId = _stmt.getText(_columnIndexOfGymId)
          val _tmpLabel: String
          _tmpLabel = _stmt.getText(_columnIndexOfLabel)
          val _tmpColorHex: String?
          if (_stmt.isNull(_columnIndexOfColorHex)) {
            _tmpColorHex = null
          } else {
            _tmpColorHex = _stmt.getText(_columnIndexOfColorHex)
          }
          val _tmpGradeValue: Int?
          if (_stmt.isNull(_columnIndexOfGradeValue)) {
            _tmpGradeValue = null
          } else {
            _tmpGradeValue = _stmt.getLong(_columnIndexOfGradeValue).toInt()
          }
          val _tmpWallAngle: WallAngle?
          if (_stmt.isNull(_columnIndexOfWallAngle)) {
            _tmpWallAngle = null
          } else {
            _tmpWallAngle = __WallAngle_stringToEnum(_stmt.getText(_columnIndexOfWallAngle))
          }
          val _tmpFirstSentAt: Long?
          if (_stmt.isNull(_columnIndexOfFirstSentAt)) {
            _tmpFirstSentAt = null
          } else {
            _tmpFirstSentAt = _stmt.getLong(_columnIndexOfFirstSentAt)
          }
          val _tmpRetiredAt: Long?
          if (_stmt.isNull(_columnIndexOfRetiredAt)) {
            _tmpRetiredAt = null
          } else {
            _tmpRetiredAt = _stmt.getLong(_columnIndexOfRetiredAt)
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
          _result = ProblemEntity(_tmpId,_tmpGymId,_tmpLabel,_tmpColorHex,_tmpGradeValue,_tmpWallAngle,_tmpFirstSentAt,_tmpRetiredAt,_tmpMeta)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeActive(gymId: String): Flow<List<ProblemEntity>> {
    val _sql: String = """
        |
        |        SELECT * FROM problem
        |        WHERE gymId = ? AND deletedAt IS NULL AND retiredAt IS NULL
        |        ORDER BY gradeValue, label COLLATE NOCASE
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("problem")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, gymId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGymId: Int = getColumnIndexOrThrow(_stmt, "gymId")
        val _columnIndexOfLabel: Int = getColumnIndexOrThrow(_stmt, "label")
        val _columnIndexOfColorHex: Int = getColumnIndexOrThrow(_stmt, "colorHex")
        val _columnIndexOfGradeValue: Int = getColumnIndexOrThrow(_stmt, "gradeValue")
        val _columnIndexOfWallAngle: Int = getColumnIndexOrThrow(_stmt, "wallAngle")
        val _columnIndexOfFirstSentAt: Int = getColumnIndexOrThrow(_stmt, "firstSentAt")
        val _columnIndexOfRetiredAt: Int = getColumnIndexOrThrow(_stmt, "retiredAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: MutableList<ProblemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProblemEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpGymId: String
          _tmpGymId = _stmt.getText(_columnIndexOfGymId)
          val _tmpLabel: String
          _tmpLabel = _stmt.getText(_columnIndexOfLabel)
          val _tmpColorHex: String?
          if (_stmt.isNull(_columnIndexOfColorHex)) {
            _tmpColorHex = null
          } else {
            _tmpColorHex = _stmt.getText(_columnIndexOfColorHex)
          }
          val _tmpGradeValue: Int?
          if (_stmt.isNull(_columnIndexOfGradeValue)) {
            _tmpGradeValue = null
          } else {
            _tmpGradeValue = _stmt.getLong(_columnIndexOfGradeValue).toInt()
          }
          val _tmpWallAngle: WallAngle?
          if (_stmt.isNull(_columnIndexOfWallAngle)) {
            _tmpWallAngle = null
          } else {
            _tmpWallAngle = __WallAngle_stringToEnum(_stmt.getText(_columnIndexOfWallAngle))
          }
          val _tmpFirstSentAt: Long?
          if (_stmt.isNull(_columnIndexOfFirstSentAt)) {
            _tmpFirstSentAt = null
          } else {
            _tmpFirstSentAt = _stmt.getLong(_columnIndexOfFirstSentAt)
          }
          val _tmpRetiredAt: Long?
          if (_stmt.isNull(_columnIndexOfRetiredAt)) {
            _tmpRetiredAt = null
          } else {
            _tmpRetiredAt = _stmt.getLong(_columnIndexOfRetiredAt)
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
          _item = ProblemEntity(_tmpId,_tmpGymId,_tmpLabel,_tmpColorHex,_tmpGradeValue,_tmpWallAngle,_tmpFirstSentAt,_tmpRetiredAt,_tmpMeta)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeAll(): Flow<List<ProblemEntity>> {
    val _sql: String = "SELECT * FROM problem WHERE deletedAt IS NULL ORDER BY gradeValue DESC, label COLLATE NOCASE"
    return createFlow(__db, false, arrayOf("problem")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGymId: Int = getColumnIndexOrThrow(_stmt, "gymId")
        val _columnIndexOfLabel: Int = getColumnIndexOrThrow(_stmt, "label")
        val _columnIndexOfColorHex: Int = getColumnIndexOrThrow(_stmt, "colorHex")
        val _columnIndexOfGradeValue: Int = getColumnIndexOrThrow(_stmt, "gradeValue")
        val _columnIndexOfWallAngle: Int = getColumnIndexOrThrow(_stmt, "wallAngle")
        val _columnIndexOfFirstSentAt: Int = getColumnIndexOrThrow(_stmt, "firstSentAt")
        val _columnIndexOfRetiredAt: Int = getColumnIndexOrThrow(_stmt, "retiredAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: MutableList<ProblemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProblemEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpGymId: String
          _tmpGymId = _stmt.getText(_columnIndexOfGymId)
          val _tmpLabel: String
          _tmpLabel = _stmt.getText(_columnIndexOfLabel)
          val _tmpColorHex: String?
          if (_stmt.isNull(_columnIndexOfColorHex)) {
            _tmpColorHex = null
          } else {
            _tmpColorHex = _stmt.getText(_columnIndexOfColorHex)
          }
          val _tmpGradeValue: Int?
          if (_stmt.isNull(_columnIndexOfGradeValue)) {
            _tmpGradeValue = null
          } else {
            _tmpGradeValue = _stmt.getLong(_columnIndexOfGradeValue).toInt()
          }
          val _tmpWallAngle: WallAngle?
          if (_stmt.isNull(_columnIndexOfWallAngle)) {
            _tmpWallAngle = null
          } else {
            _tmpWallAngle = __WallAngle_stringToEnum(_stmt.getText(_columnIndexOfWallAngle))
          }
          val _tmpFirstSentAt: Long?
          if (_stmt.isNull(_columnIndexOfFirstSentAt)) {
            _tmpFirstSentAt = null
          } else {
            _tmpFirstSentAt = _stmt.getLong(_columnIndexOfFirstSentAt)
          }
          val _tmpRetiredAt: Long?
          if (_stmt.isNull(_columnIndexOfRetiredAt)) {
            _tmpRetiredAt = null
          } else {
            _tmpRetiredAt = _stmt.getLong(_columnIndexOfRetiredAt)
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
          _item = ProblemEntity(_tmpId,_tmpGymId,_tmpLabel,_tmpColorHex,_tmpGradeValue,_tmpWallAngle,_tmpFirstSentAt,_tmpRetiredAt,_tmpMeta)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeOpenProjects(): Flow<List<ProblemEntity>> {
    val _sql: String = """
        |
        |        SELECT p.* FROM problem p
        |        WHERE p.deletedAt IS NULL
        |          AND p.retiredAt IS NULL
        |          AND p.firstSentAt IS NULL
        |          AND EXISTS (SELECT 1 FROM attempt a WHERE a.problemId = p.id AND a.deletedAt IS NULL)
        |        ORDER BY p.gradeValue DESC
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("problem", "attempt")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGymId: Int = getColumnIndexOrThrow(_stmt, "gymId")
        val _columnIndexOfLabel: Int = getColumnIndexOrThrow(_stmt, "label")
        val _columnIndexOfColorHex: Int = getColumnIndexOrThrow(_stmt, "colorHex")
        val _columnIndexOfGradeValue: Int = getColumnIndexOrThrow(_stmt, "gradeValue")
        val _columnIndexOfWallAngle: Int = getColumnIndexOrThrow(_stmt, "wallAngle")
        val _columnIndexOfFirstSentAt: Int = getColumnIndexOrThrow(_stmt, "firstSentAt")
        val _columnIndexOfRetiredAt: Int = getColumnIndexOrThrow(_stmt, "retiredAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: MutableList<ProblemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProblemEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpGymId: String
          _tmpGymId = _stmt.getText(_columnIndexOfGymId)
          val _tmpLabel: String
          _tmpLabel = _stmt.getText(_columnIndexOfLabel)
          val _tmpColorHex: String?
          if (_stmt.isNull(_columnIndexOfColorHex)) {
            _tmpColorHex = null
          } else {
            _tmpColorHex = _stmt.getText(_columnIndexOfColorHex)
          }
          val _tmpGradeValue: Int?
          if (_stmt.isNull(_columnIndexOfGradeValue)) {
            _tmpGradeValue = null
          } else {
            _tmpGradeValue = _stmt.getLong(_columnIndexOfGradeValue).toInt()
          }
          val _tmpWallAngle: WallAngle?
          if (_stmt.isNull(_columnIndexOfWallAngle)) {
            _tmpWallAngle = null
          } else {
            _tmpWallAngle = __WallAngle_stringToEnum(_stmt.getText(_columnIndexOfWallAngle))
          }
          val _tmpFirstSentAt: Long?
          if (_stmt.isNull(_columnIndexOfFirstSentAt)) {
            _tmpFirstSentAt = null
          } else {
            _tmpFirstSentAt = _stmt.getLong(_columnIndexOfFirstSentAt)
          }
          val _tmpRetiredAt: Long?
          if (_stmt.isNull(_columnIndexOfRetiredAt)) {
            _tmpRetiredAt = null
          } else {
            _tmpRetiredAt = _stmt.getLong(_columnIndexOfRetiredAt)
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
          _item = ProblemEntity(_tmpId,_tmpGymId,_tmpLabel,_tmpColorHex,_tmpGradeValue,_tmpWallAngle,_tmpFirstSentAt,_tmpRetiredAt,_tmpMeta)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __WallAngle_enumToString(_value: WallAngle): String = when (_value) {
    WallAngle.SLAB -> "SLAB"
    WallAngle.VERTICAL -> "VERTICAL"
    WallAngle.SLIGHT_OVERHANG -> "SLIGHT_OVERHANG"
    WallAngle.STEEP -> "STEEP"
    WallAngle.ROOF -> "ROOF"
  }

  private fun __SyncState_enumToString(_value: SyncState): String = when (_value) {
    SyncState.PENDING -> "PENDING"
    SyncState.SYNCED -> "SYNCED"
  }

  private fun __WallAngle_stringToEnum(_value: String): WallAngle = when (_value) {
    "SLAB" -> WallAngle.SLAB
    "VERTICAL" -> WallAngle.VERTICAL
    "SLIGHT_OVERHANG" -> WallAngle.SLIGHT_OVERHANG
    "STEEP" -> WallAngle.STEEP
    "ROOF" -> WallAngle.ROOF
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

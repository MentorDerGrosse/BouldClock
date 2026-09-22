package at.mentor.bouldclockapp.`data`.db.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import at.mentor.bouldclockapp.`data`.db.entity.GymEntity
import at.mentor.bouldclockapp.`data`.db.entity.RecordMeta
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.SyncState
import javax.`annotation`.processing.Generated
import kotlin.Boolean
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
public class GymDao_Impl(
  __db: RoomDatabase,
) : GymDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfGymEntity: EntityUpsertAdapter<GymEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfGymEntity = EntityUpsertAdapter<GymEntity>(object : EntityInsertAdapter<GymEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `gym` (`id`,`name`,`gradeSystem`,`isDefault`,`createdAt`,`updatedAt`,`deletedAt`,`syncState`) VALUES (?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: GymEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, __GradeSystem_enumToString(entity.gradeSystem))
        val _tmp: Int = if (entity.isDefault) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        val _tmpMeta: RecordMeta = entity.meta
        statement.bindLong(5, _tmpMeta.createdAt)
        statement.bindLong(6, _tmpMeta.updatedAt)
        val _tmpDeletedAt: Long? = _tmpMeta.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpDeletedAt)
        }
        statement.bindText(8, __SyncState_enumToString(_tmpMeta.syncState))
      }
    }, object : EntityDeleteOrUpdateAdapter<GymEntity>() {
      protected override fun createQuery(): String = "UPDATE `gym` SET `id` = ?,`name` = ?,`gradeSystem` = ?,`isDefault` = ?,`createdAt` = ?,`updatedAt` = ?,`deletedAt` = ?,`syncState` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: GymEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, __GradeSystem_enumToString(entity.gradeSystem))
        val _tmp: Int = if (entity.isDefault) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        val _tmpMeta: RecordMeta = entity.meta
        statement.bindLong(5, _tmpMeta.createdAt)
        statement.bindLong(6, _tmpMeta.updatedAt)
        val _tmpDeletedAt: Long? = _tmpMeta.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpDeletedAt)
        }
        statement.bindText(8, __SyncState_enumToString(_tmpMeta.syncState))
        statement.bindText(9, entity.id)
      }
    })
  }

  public override suspend fun upsert(gym: GymEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfGymEntity.upsert(_connection, gym)
  }

  public override fun observeAll(): Flow<List<GymEntity>> {
    val _sql: String = "SELECT * FROM gym WHERE deletedAt IS NULL ORDER BY isDefault DESC, name COLLATE NOCASE"
    return createFlow(__db, false, arrayOf("gym")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfGradeSystem: Int = getColumnIndexOrThrow(_stmt, "gradeSystem")
        val _columnIndexOfIsDefault: Int = getColumnIndexOrThrow(_stmt, "isDefault")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: MutableList<GymEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: GymEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpGradeSystem: GradeSystem
          _tmpGradeSystem = __GradeSystem_stringToEnum(_stmt.getText(_columnIndexOfGradeSystem))
          val _tmpIsDefault: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDefault).toInt()
          _tmpIsDefault = _tmp != 0
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
          _item = GymEntity(_tmpId,_tmpName,_tmpGradeSystem,_tmpIsDefault,_tmpMeta)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun byId(id: String): GymEntity? {
    val _sql: String = "SELECT * FROM gym WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfGradeSystem: Int = getColumnIndexOrThrow(_stmt, "gradeSystem")
        val _columnIndexOfIsDefault: Int = getColumnIndexOrThrow(_stmt, "isDefault")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: GymEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpGradeSystem: GradeSystem
          _tmpGradeSystem = __GradeSystem_stringToEnum(_stmt.getText(_columnIndexOfGradeSystem))
          val _tmpIsDefault: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDefault).toInt()
          _tmpIsDefault = _tmp != 0
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
          _result = GymEntity(_tmpId,_tmpName,_tmpGradeSystem,_tmpIsDefault,_tmpMeta)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun defaultGym(): GymEntity? {
    val _sql: String = "SELECT * FROM gym WHERE deletedAt IS NULL AND isDefault = 1 LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfGradeSystem: Int = getColumnIndexOrThrow(_stmt, "gradeSystem")
        val _columnIndexOfIsDefault: Int = getColumnIndexOrThrow(_stmt, "isDefault")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: GymEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpGradeSystem: GradeSystem
          _tmpGradeSystem = __GradeSystem_stringToEnum(_stmt.getText(_columnIndexOfGradeSystem))
          val _tmpIsDefault: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDefault).toInt()
          _tmpIsDefault = _tmp != 0
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
          _result = GymEntity(_tmpId,_tmpName,_tmpGradeSystem,_tmpIsDefault,_tmpMeta)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun makeDefault(id: String, now: Long) {
    val _sql: String = "UPDATE gym SET isDefault = (id = ?), updatedAt = ?, syncState = 'PENDING'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _argIndex = 2
        _stmt.bindLong(_argIndex, now)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
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

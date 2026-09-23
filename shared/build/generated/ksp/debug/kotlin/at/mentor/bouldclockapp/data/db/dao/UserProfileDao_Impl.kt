package at.mentor.bouldclockapp.`data`.db.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import at.mentor.bouldclockapp.`data`.db.entity.RecordMeta
import at.mentor.bouldclockapp.`data`.db.entity.UserProfileEntity
import at.mentor.bouldclockapp.core.model.BiologicalSex
import at.mentor.bouldclockapp.core.model.SyncState
import javax.`annotation`.processing.Generated
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class UserProfileDao_Impl(
  __db: RoomDatabase,
) : UserProfileDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfUserProfileEntity: EntityUpsertAdapter<UserProfileEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfUserProfileEntity = EntityUpsertAdapter<UserProfileEntity>(object : EntityInsertAdapter<UserProfileEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `user_profile` (`id`,`weightKg`,`birthYear`,`sex`,`heightCm`,`restingHrBpm`,`maxHrBpm`,`createdAt`,`updatedAt`,`deletedAt`,`syncState`) VALUES (?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: UserProfileEntity) {
        statement.bindText(1, entity.id)
        statement.bindLong(2, entity.weightKg.toLong())
        statement.bindLong(3, entity.birthYear.toLong())
        statement.bindText(4, __BiologicalSex_enumToString(entity.sex))
        val _tmpHeightCm: Int? = entity.heightCm
        if (_tmpHeightCm == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpHeightCm.toLong())
        }
        val _tmpRestingHrBpm: Int? = entity.restingHrBpm
        if (_tmpRestingHrBpm == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpRestingHrBpm.toLong())
        }
        val _tmpMaxHrBpm: Int? = entity.maxHrBpm
        if (_tmpMaxHrBpm == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpMaxHrBpm.toLong())
        }
        val _tmpMeta: RecordMeta = entity.meta
        statement.bindLong(8, _tmpMeta.createdAt)
        statement.bindLong(9, _tmpMeta.updatedAt)
        val _tmpDeletedAt: Long? = _tmpMeta.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpDeletedAt)
        }
        statement.bindText(11, __SyncState_enumToString(_tmpMeta.syncState))
      }
    }, object : EntityDeleteOrUpdateAdapter<UserProfileEntity>() {
      protected override fun createQuery(): String = "UPDATE `user_profile` SET `id` = ?,`weightKg` = ?,`birthYear` = ?,`sex` = ?,`heightCm` = ?,`restingHrBpm` = ?,`maxHrBpm` = ?,`createdAt` = ?,`updatedAt` = ?,`deletedAt` = ?,`syncState` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: UserProfileEntity) {
        statement.bindText(1, entity.id)
        statement.bindLong(2, entity.weightKg.toLong())
        statement.bindLong(3, entity.birthYear.toLong())
        statement.bindText(4, __BiologicalSex_enumToString(entity.sex))
        val _tmpHeightCm: Int? = entity.heightCm
        if (_tmpHeightCm == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpHeightCm.toLong())
        }
        val _tmpRestingHrBpm: Int? = entity.restingHrBpm
        if (_tmpRestingHrBpm == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpRestingHrBpm.toLong())
        }
        val _tmpMaxHrBpm: Int? = entity.maxHrBpm
        if (_tmpMaxHrBpm == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpMaxHrBpm.toLong())
        }
        val _tmpMeta: RecordMeta = entity.meta
        statement.bindLong(8, _tmpMeta.createdAt)
        statement.bindLong(9, _tmpMeta.updatedAt)
        val _tmpDeletedAt: Long? = _tmpMeta.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpDeletedAt)
        }
        statement.bindText(11, __SyncState_enumToString(_tmpMeta.syncState))
        statement.bindText(12, entity.id)
      }
    })
  }

  public override suspend fun upsert(profile: UserProfileEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfUserProfileEntity.upsert(_connection, profile)
  }

  public override suspend fun `get`(): UserProfileEntity? {
    val _sql: String = "SELECT * FROM user_profile WHERE id = 'self' AND deletedAt IS NULL"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfWeightKg: Int = getColumnIndexOrThrow(_stmt, "weightKg")
        val _columnIndexOfBirthYear: Int = getColumnIndexOrThrow(_stmt, "birthYear")
        val _columnIndexOfSex: Int = getColumnIndexOrThrow(_stmt, "sex")
        val _columnIndexOfHeightCm: Int = getColumnIndexOrThrow(_stmt, "heightCm")
        val _columnIndexOfRestingHrBpm: Int = getColumnIndexOrThrow(_stmt, "restingHrBpm")
        val _columnIndexOfMaxHrBpm: Int = getColumnIndexOrThrow(_stmt, "maxHrBpm")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: UserProfileEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpWeightKg: Int
          _tmpWeightKg = _stmt.getLong(_columnIndexOfWeightKg).toInt()
          val _tmpBirthYear: Int
          _tmpBirthYear = _stmt.getLong(_columnIndexOfBirthYear).toInt()
          val _tmpSex: BiologicalSex
          _tmpSex = __BiologicalSex_stringToEnum(_stmt.getText(_columnIndexOfSex))
          val _tmpHeightCm: Int?
          if (_stmt.isNull(_columnIndexOfHeightCm)) {
            _tmpHeightCm = null
          } else {
            _tmpHeightCm = _stmt.getLong(_columnIndexOfHeightCm).toInt()
          }
          val _tmpRestingHrBpm: Int?
          if (_stmt.isNull(_columnIndexOfRestingHrBpm)) {
            _tmpRestingHrBpm = null
          } else {
            _tmpRestingHrBpm = _stmt.getLong(_columnIndexOfRestingHrBpm).toInt()
          }
          val _tmpMaxHrBpm: Int?
          if (_stmt.isNull(_columnIndexOfMaxHrBpm)) {
            _tmpMaxHrBpm = null
          } else {
            _tmpMaxHrBpm = _stmt.getLong(_columnIndexOfMaxHrBpm).toInt()
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
          _result = UserProfileEntity(_tmpId,_tmpWeightKg,_tmpBirthYear,_tmpSex,_tmpHeightCm,_tmpRestingHrBpm,_tmpMaxHrBpm,_tmpMeta)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observe(): Flow<UserProfileEntity?> {
    val _sql: String = "SELECT * FROM user_profile WHERE id = 'self' AND deletedAt IS NULL"
    return createFlow(__db, false, arrayOf("user_profile")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfWeightKg: Int = getColumnIndexOrThrow(_stmt, "weightKg")
        val _columnIndexOfBirthYear: Int = getColumnIndexOrThrow(_stmt, "birthYear")
        val _columnIndexOfSex: Int = getColumnIndexOrThrow(_stmt, "sex")
        val _columnIndexOfHeightCm: Int = getColumnIndexOrThrow(_stmt, "heightCm")
        val _columnIndexOfRestingHrBpm: Int = getColumnIndexOrThrow(_stmt, "restingHrBpm")
        val _columnIndexOfMaxHrBpm: Int = getColumnIndexOrThrow(_stmt, "maxHrBpm")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _columnIndexOfSyncState: Int = getColumnIndexOrThrow(_stmt, "syncState")
        val _result: UserProfileEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpWeightKg: Int
          _tmpWeightKg = _stmt.getLong(_columnIndexOfWeightKg).toInt()
          val _tmpBirthYear: Int
          _tmpBirthYear = _stmt.getLong(_columnIndexOfBirthYear).toInt()
          val _tmpSex: BiologicalSex
          _tmpSex = __BiologicalSex_stringToEnum(_stmt.getText(_columnIndexOfSex))
          val _tmpHeightCm: Int?
          if (_stmt.isNull(_columnIndexOfHeightCm)) {
            _tmpHeightCm = null
          } else {
            _tmpHeightCm = _stmt.getLong(_columnIndexOfHeightCm).toInt()
          }
          val _tmpRestingHrBpm: Int?
          if (_stmt.isNull(_columnIndexOfRestingHrBpm)) {
            _tmpRestingHrBpm = null
          } else {
            _tmpRestingHrBpm = _stmt.getLong(_columnIndexOfRestingHrBpm).toInt()
          }
          val _tmpMaxHrBpm: Int?
          if (_stmt.isNull(_columnIndexOfMaxHrBpm)) {
            _tmpMaxHrBpm = null
          } else {
            _tmpMaxHrBpm = _stmt.getLong(_columnIndexOfMaxHrBpm).toInt()
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
          _result = UserProfileEntity(_tmpId,_tmpWeightKg,_tmpBirthYear,_tmpSex,_tmpHeightCm,_tmpRestingHrBpm,_tmpMaxHrBpm,_tmpMeta)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __BiologicalSex_enumToString(_value: BiologicalSex): String = when (_value) {
    BiologicalSex.FEMALE -> "FEMALE"
    BiologicalSex.MALE -> "MALE"
    BiologicalSex.UNSPECIFIED -> "UNSPECIFIED"
  }

  private fun __SyncState_enumToString(_value: SyncState): String = when (_value) {
    SyncState.PENDING -> "PENDING"
    SyncState.SYNCED -> "SYNCED"
  }

  private fun __BiologicalSex_stringToEnum(_value: String): BiologicalSex = when (_value) {
    "FEMALE" -> BiologicalSex.FEMALE
    "MALE" -> BiologicalSex.MALE
    "UNSPECIFIED" -> BiologicalSex.UNSPECIFIED
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

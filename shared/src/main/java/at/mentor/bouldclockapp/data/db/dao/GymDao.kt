package at.mentor.bouldclockapp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import at.mentor.bouldclockapp.data.db.entity.GymEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GymDao {

    @Upsert
    suspend fun upsert(gym: GymEntity)

    @Query("SELECT * FROM gym WHERE deletedAt IS NULL ORDER BY isDefault DESC, name COLLATE NOCASE")
    fun observeAll(): Flow<List<GymEntity>>

    @Query("SELECT * FROM gym WHERE id = :id")
    suspend fun byId(id: String): GymEntity?

    @Query("SELECT * FROM gym WHERE deletedAt IS NULL AND isDefault = 1 LIMIT 1")
    suspend fun defaultGym(): GymEntity?

    @Query("UPDATE gym SET isDefault = (id = :id), updatedAt = :now, syncState = 'PENDING'")
    suspend fun makeDefault(id: String, now: Long)
}

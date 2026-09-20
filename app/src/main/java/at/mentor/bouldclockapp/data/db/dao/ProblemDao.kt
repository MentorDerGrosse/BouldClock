package at.mentor.bouldclockapp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import at.mentor.bouldclockapp.data.db.entity.ProblemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProblemDao {

    @Upsert
    suspend fun upsert(problem: ProblemEntity)

    @Query("SELECT * FROM problem WHERE id = :id")
    suspend fun byId(id: String): ProblemEntity?

    @Query(
        """
        SELECT * FROM problem
        WHERE gymId = :gymId AND deletedAt IS NULL AND retiredAt IS NULL
        ORDER BY gradeValue, label COLLATE NOCASE
        """,
    )
    fun observeActive(gymId: String): Flow<List<ProblemEntity>>

    /** Offene Projekte: schon probiert, noch nicht geschickt, haengt noch. */
    @Query(
        """
        SELECT p.* FROM problem p
        WHERE p.deletedAt IS NULL
          AND p.retiredAt IS NULL
          AND p.firstSentAt IS NULL
          AND EXISTS (SELECT 1 FROM attempt a WHERE a.problemId = p.id AND a.deletedAt IS NULL)
        ORDER BY p.gradeValue DESC
        """,
    )
    fun observeOpenProjects(): Flow<List<ProblemEntity>>
}

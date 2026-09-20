package at.mentor.bouldclockapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import at.mentor.bouldclockapp.data.db.dao.AttemptDao
import at.mentor.bouldclockapp.data.db.dao.GymDao
import at.mentor.bouldclockapp.data.db.dao.HrSampleDao
import at.mentor.bouldclockapp.data.db.dao.ProblemDao
import at.mentor.bouldclockapp.data.db.dao.SensorChunkDao
import at.mentor.bouldclockapp.data.db.dao.SessionDao
import at.mentor.bouldclockapp.data.db.dao.SessionSummaryDao
import at.mentor.bouldclockapp.data.db.entity.AttemptEntity
import at.mentor.bouldclockapp.data.db.entity.GymEntity
import at.mentor.bouldclockapp.data.db.entity.HrSampleEntity
import at.mentor.bouldclockapp.data.db.entity.ProblemEntity
import at.mentor.bouldclockapp.data.db.entity.SensorChunkEntity
import at.mentor.bouldclockapp.data.db.entity.SessionEntity
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity

@Database(
    entities = [
        GymEntity::class,
        ProblemEntity::class,
        SessionEntity::class,
        AttemptEntity::class,
        HrSampleEntity::class,
        SessionSummaryEntity::class,
        SensorChunkEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class BouldClockDatabase : RoomDatabase() {

    abstract fun gymDao(): GymDao
    abstract fun problemDao(): ProblemDao
    abstract fun sessionDao(): SessionDao
    abstract fun attemptDao(): AttemptDao
    abstract fun hrSampleDao(): HrSampleDao
    abstract fun sessionSummaryDao(): SessionSummaryDao
    abstract fun sensorChunkDao(): SensorChunkDao

    companion object {
        private const val NAME = "bouldclock.db"

        @Volatile
        private var instance: BouldClockDatabase? = null

        fun get(context: Context): BouldClockDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }

        private fun build(context: Context): BouldClockDatabase =
            Room.databaseBuilder(context, BouldClockDatabase::class.java, NAME)
                // Rooms Default, hier bewusst explizit: die Absturzsicherheit der
                // laufenden Session haengt genau daran.
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .build()
    }
}

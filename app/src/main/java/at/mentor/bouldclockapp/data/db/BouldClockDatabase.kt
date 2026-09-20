package at.mentor.bouldclockapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    // Bei JEDER Aenderung an einer Entity hochzaehlen und eine Migration
    // ergaenzen. Bleibt die Nummer stehen, weigert sich Room beim ersten
    // Datenbankzugriff, eine vorhandene Datei zu oeffnen - die App stirbt dann
    // beim Start, ohne dass ein Test das vorher merkt.
    version = 2,
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

        /** Boulder-Grenze am Versuch. Bestehende Zeilen gehoeren zum laufenden Boulder. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE attempt ADD COLUMN startsNewBoulder INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        private fun build(context: Context): BouldClockDatabase =
            Room.databaseBuilder(context, BouldClockDatabase::class.java, NAME)
                // Rooms Default, hier bewusst explizit: die Absturzsicherheit der
                // laufenden Session haengt genau daran.
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .addMigrations(MIGRATION_1_2)
                // Notnagel fuer die Entwicklung: eine vergessene Migration soll
                // die Datenbank leeren, nicht die App unstartbar machen. Vor der
                // ersten echten Veroeffentlichung muss das hier raus, sonst
                // verlieren Nutzer beim Update ihre Sessions.
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}

package at.mentor.bouldclockapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import at.mentor.bouldclockapp.data.db.dao.AttemptDao
import at.mentor.bouldclockapp.data.db.dao.CalorieSampleDao
import at.mentor.bouldclockapp.data.db.dao.GymDao
import at.mentor.bouldclockapp.data.db.dao.HrSampleDao
import at.mentor.bouldclockapp.data.db.dao.ProblemDao
import at.mentor.bouldclockapp.data.db.dao.SensorChunkDao
import at.mentor.bouldclockapp.data.db.dao.SessionDao
import at.mentor.bouldclockapp.data.db.dao.SessionSummaryDao
import at.mentor.bouldclockapp.data.db.dao.UserProfileDao
import at.mentor.bouldclockapp.data.db.entity.AttemptEntity
import at.mentor.bouldclockapp.data.db.entity.CalorieSampleEntity
import at.mentor.bouldclockapp.data.db.entity.GymEntity
import at.mentor.bouldclockapp.data.db.entity.HrSampleEntity
import at.mentor.bouldclockapp.data.db.entity.ProblemEntity
import at.mentor.bouldclockapp.data.db.entity.SensorChunkEntity
import at.mentor.bouldclockapp.data.db.entity.SessionEntity
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import at.mentor.bouldclockapp.data.db.entity.UserProfileEntity

@Database(
    entities = [
        GymEntity::class,
        ProblemEntity::class,
        SessionEntity::class,
        AttemptEntity::class,
        HrSampleEntity::class,
        CalorieSampleEntity::class,
        SessionSummaryEntity::class,
        SensorChunkEntity::class,
        UserProfileEntity::class,
    ],
    // Bei JEDER Aenderung an einer Entity hochzaehlen und eine Migration
    // ergaenzen. Bleibt die Nummer stehen, weigert sich Room beim ersten
    // Datenbankzugriff, eine vorhandene Datei zu oeffnen - die App stirbt dann
    // beim Start, ohne dass ein Test das vorher merkt.
    version = 5,
    exportSchema = true,
)
abstract class BouldClockDatabase : RoomDatabase() {

    abstract fun gymDao(): GymDao
    abstract fun problemDao(): ProblemDao
    abstract fun sessionDao(): SessionDao
    abstract fun attemptDao(): AttemptDao
    abstract fun hrSampleDao(): HrSampleDao
    abstract fun calorieSampleDao(): CalorieSampleDao
    abstract fun sessionSummaryDao(): SessionSummaryDao
    abstract fun sensorChunkDao(): SensorChunkDao
    abstract fun userProfileDao(): UserProfileDao

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

        /** Boardwinkel am Versuch. Altdaten stammen nicht vom Board, bleiben also leer. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE attempt ADD COLUMN boardAngleDegrees INTEGER")
            }
        }

        /**
         * Kalorienverlauf. SQL woertlich aus dem exportierten Schema 4.json -
         * eine von Hand getippte Tabelle weicht sonst irgendwo ab, und Room
         * merkt das erst beim Oeffnen auf dem Geraet.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `calorie_sample` (" +
                        "`sessionId` TEXT NOT NULL, " +
                        "`timestampMs` INTEGER NOT NULL, " +
                        "`kcalTotal` REAL NOT NULL, " +
                        "PRIMARY KEY(`sessionId`, `timestampMs`), " +
                        "FOREIGN KEY(`sessionId`) REFERENCES `session`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE )",
                )
            }
        }

        /** Nutzerprofil. SQL woertlich aus dem exportierten Schema 5.json. */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `user_profile` (" +
                        "`id` TEXT NOT NULL, " +
                        "`weightKg` INTEGER NOT NULL, " +
                        "`birthYear` INTEGER NOT NULL, " +
                        "`sex` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "`deletedAt` INTEGER, " +
                        "`syncState` TEXT NOT NULL, " +
                        "PRIMARY KEY(`id`))",
                )
            }
        }

        private fun build(context: Context): BouldClockDatabase =
            Room.databaseBuilder(context, BouldClockDatabase::class.java, NAME)
                // Rooms Default, hier bewusst explizit: die Absturzsicherheit der
                // laufenden Session haengt genau daran.
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                // Notnagel fuer die Entwicklung: eine vergessene Migration soll
                // die Datenbank leeren, nicht die App unstartbar machen. Vor der
                // ersten echten Veroeffentlichung muss das hier raus, sonst
                // verlieren Nutzer beim Update ihre Sessions.
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}

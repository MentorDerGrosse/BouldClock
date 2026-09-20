package at.mentor.bouldclockapp.`data`.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import at.mentor.bouldclockapp.`data`.db.dao.AttemptDao
import at.mentor.bouldclockapp.`data`.db.dao.AttemptDao_Impl
import at.mentor.bouldclockapp.`data`.db.dao.GymDao
import at.mentor.bouldclockapp.`data`.db.dao.GymDao_Impl
import at.mentor.bouldclockapp.`data`.db.dao.HrSampleDao
import at.mentor.bouldclockapp.`data`.db.dao.HrSampleDao_Impl
import at.mentor.bouldclockapp.`data`.db.dao.MetricSampleDao
import at.mentor.bouldclockapp.`data`.db.dao.MetricSampleDao_Impl
import at.mentor.bouldclockapp.`data`.db.dao.ProblemDao
import at.mentor.bouldclockapp.`data`.db.dao.ProblemDao_Impl
import at.mentor.bouldclockapp.`data`.db.dao.SensorChunkDao
import at.mentor.bouldclockapp.`data`.db.dao.SensorChunkDao_Impl
import at.mentor.bouldclockapp.`data`.db.dao.SessionDao
import at.mentor.bouldclockapp.`data`.db.dao.SessionDao_Impl
import at.mentor.bouldclockapp.`data`.db.dao.SessionSummaryDao
import at.mentor.bouldclockapp.`data`.db.dao.SessionSummaryDao_Impl
import at.mentor.bouldclockapp.`data`.db.dao.UserProfileDao
import at.mentor.bouldclockapp.`data`.db.dao.UserProfileDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class BouldClockDatabase_Impl : BouldClockDatabase() {
  private val _gymDao: Lazy<GymDao> = lazy {
    GymDao_Impl(this)
  }

  private val _problemDao: Lazy<ProblemDao> = lazy {
    ProblemDao_Impl(this)
  }

  private val _sessionDao: Lazy<SessionDao> = lazy {
    SessionDao_Impl(this)
  }

  private val _attemptDao: Lazy<AttemptDao> = lazy {
    AttemptDao_Impl(this)
  }

  private val _hrSampleDao: Lazy<HrSampleDao> = lazy {
    HrSampleDao_Impl(this)
  }

  private val _metricSampleDao: Lazy<MetricSampleDao> = lazy {
    MetricSampleDao_Impl(this)
  }

  private val _sessionSummaryDao: Lazy<SessionSummaryDao> = lazy {
    SessionSummaryDao_Impl(this)
  }

  private val _sensorChunkDao: Lazy<SensorChunkDao> = lazy {
    SensorChunkDao_Impl(this)
  }

  private val _userProfileDao: Lazy<UserProfileDao> = lazy {
    UserProfileDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(7, "4a665b20eeac703b75eae5b3bbabe875", "5251df1d6630543b2d626a83cd412468") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `gym` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `gradeSystem` TEXT NOT NULL, `isDefault` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER, `syncState` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `problem` (`id` TEXT NOT NULL, `gymId` TEXT NOT NULL, `label` TEXT NOT NULL, `colorHex` TEXT, `gradeValue` INTEGER, `wallAngle` TEXT, `firstSentAt` INTEGER, `retiredAt` INTEGER, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER, `syncState` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`gymId`) REFERENCES `gym`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_problem_gymId` ON `problem` (`gymId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `session` (`id` TEXT NOT NULL, `gymId` TEXT, `type` TEXT NOT NULL, `state` TEXT NOT NULL, `startedAt` INTEGER NOT NULL, `endedAt` INTEGER, `pausedMs` INTEGER NOT NULL, `restTargetMs` INTEGER NOT NULL, `rpe` INTEGER, `note` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER, `syncState` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`gymId`) REFERENCES `gym`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_session_gymId` ON `session` (`gymId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_session_startedAt` ON `session` (`startedAt`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_session_state` ON `session` (`state`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `attempt` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `problemId` TEXT, `ordinal` INTEGER NOT NULL, `startsNewBoulder` INTEGER NOT NULL, `startedAt` INTEGER NOT NULL, `endedAt` INTEGER, `outcome` TEXT, `gradeValue` INTEGER, `gradeSystem` TEXT, `topMoveReached` INTEGER, `climbHeightMeters` REAL, `boardAngleDegrees` INTEGER, `hrAvg` INTEGER, `hrMax` INTEGER, `hrEnd` INTEGER, `hrAfter60s` INTEGER, `hrr60` INTEGER, `restAfterMs` INTEGER, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER, `syncState` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`sessionId`) REFERENCES `session`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`problemId`) REFERENCES `problem`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_attempt_sessionId_ordinal` ON `attempt` (`sessionId`, `ordinal`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_attempt_problemId` ON `attempt` (`problemId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_attempt_startedAt` ON `attempt` (`startedAt`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `hr_sample` (`sessionId` TEXT NOT NULL, `timestampMs` INTEGER NOT NULL, `bpm` INTEGER NOT NULL, `accuracy` INTEGER NOT NULL, PRIMARY KEY(`sessionId`, `timestampMs`), FOREIGN KEY(`sessionId`) REFERENCES `session`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `metric_sample` (`sessionId` TEXT NOT NULL, `metric` TEXT NOT NULL, `timestampMs` INTEGER NOT NULL, `value` REAL NOT NULL, PRIMARY KEY(`sessionId`, `metric`, `timestampMs`), FOREIGN KEY(`sessionId`) REFERENCES `session`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `session_summary` (`sessionId` TEXT NOT NULL, `gymId` TEXT, `type` TEXT NOT NULL, `startedAt` INTEGER NOT NULL, `totalMs` INTEGER NOT NULL, `workMs` INTEGER NOT NULL, `restMs` INTEGER NOT NULL, `pausedMs` INTEGER NOT NULL, `attemptCount` INTEGER NOT NULL, `sendCount` INTEGER NOT NULL, `flashCount` INTEGER NOT NULL, `hardestSendValue` INTEGER, `hrAvg` INTEGER, `hrMax` INTEGER, `hrr60Avg` INTEGER, `caloriesTotal` REAL, `caloriesOnWall` REAL, `climbHeightMeters` REAL, `maxClimbHeightMeters` REAL, `rpe` INTEGER, `computedAt` INTEGER NOT NULL, PRIMARY KEY(`sessionId`), FOREIGN KEY(`sessionId`) REFERENCES `session`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_session_summary_startedAt` ON `session_summary` (`startedAt`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_session_summary_gymId` ON `session_summary` (`gymId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_session_summary_type` ON `session_summary` (`type`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `sensor_chunk` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `sensor` TEXT NOT NULL, `relativePath` TEXT NOT NULL, `startedAt` INTEGER NOT NULL, `endedAt` INTEGER NOT NULL, `sampleRateHz` INTEGER NOT NULL, `sampleCount` INTEGER NOT NULL, `sizeBytes` INTEGER NOT NULL, `syncState` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_sensor_chunk_sessionId` ON `sensor_chunk` (`sessionId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `user_profile` (`id` TEXT NOT NULL, `weightKg` INTEGER NOT NULL, `birthYear` INTEGER NOT NULL, `sex` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER, `syncState` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '4a665b20eeac703b75eae5b3bbabe875')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `gym`")
        connection.execSQL("DROP TABLE IF EXISTS `problem`")
        connection.execSQL("DROP TABLE IF EXISTS `session`")
        connection.execSQL("DROP TABLE IF EXISTS `attempt`")
        connection.execSQL("DROP TABLE IF EXISTS `hr_sample`")
        connection.execSQL("DROP TABLE IF EXISTS `metric_sample`")
        connection.execSQL("DROP TABLE IF EXISTS `session_summary`")
        connection.execSQL("DROP TABLE IF EXISTS `sensor_chunk`")
        connection.execSQL("DROP TABLE IF EXISTS `user_profile`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsGym: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsGym.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGym.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGym.put("gradeSystem", TableInfo.Column("gradeSystem", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGym.put("isDefault", TableInfo.Column("isDefault", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGym.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGym.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGym.put("deletedAt", TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGym.put("syncState", TableInfo.Column("syncState", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysGym: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesGym: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoGym: TableInfo = TableInfo("gym", _columnsGym, _foreignKeysGym, _indicesGym)
        val _existingGym: TableInfo = read(connection, "gym")
        if (!_infoGym.equals(_existingGym)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |gym(at.mentor.bouldclockapp.data.db.entity.GymEntity).
              | Expected:
              |""".trimMargin() + _infoGym + """
              |
              | Found:
              |""".trimMargin() + _existingGym)
        }
        val _columnsProblem: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsProblem.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProblem.put("gymId", TableInfo.Column("gymId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProblem.put("label", TableInfo.Column("label", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProblem.put("colorHex", TableInfo.Column("colorHex", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProblem.put("gradeValue", TableInfo.Column("gradeValue", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProblem.put("wallAngle", TableInfo.Column("wallAngle", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProblem.put("firstSentAt", TableInfo.Column("firstSentAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProblem.put("retiredAt", TableInfo.Column("retiredAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProblem.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProblem.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProblem.put("deletedAt", TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProblem.put("syncState", TableInfo.Column("syncState", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysProblem: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysProblem.add(TableInfo.ForeignKey("gym", "CASCADE", "NO ACTION", listOf("gymId"), listOf("id")))
        val _indicesProblem: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesProblem.add(TableInfo.Index("index_problem_gymId", false, listOf("gymId"), listOf("ASC")))
        val _infoProblem: TableInfo = TableInfo("problem", _columnsProblem, _foreignKeysProblem, _indicesProblem)
        val _existingProblem: TableInfo = read(connection, "problem")
        if (!_infoProblem.equals(_existingProblem)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |problem(at.mentor.bouldclockapp.data.db.entity.ProblemEntity).
              | Expected:
              |""".trimMargin() + _infoProblem + """
              |
              | Found:
              |""".trimMargin() + _existingProblem)
        }
        val _columnsSession: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSession.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSession.put("gymId", TableInfo.Column("gymId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSession.put("type", TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSession.put("state", TableInfo.Column("state", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSession.put("startedAt", TableInfo.Column("startedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSession.put("endedAt", TableInfo.Column("endedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSession.put("pausedMs", TableInfo.Column("pausedMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSession.put("restTargetMs", TableInfo.Column("restTargetMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSession.put("rpe", TableInfo.Column("rpe", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSession.put("note", TableInfo.Column("note", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSession.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSession.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSession.put("deletedAt", TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSession.put("syncState", TableInfo.Column("syncState", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSession: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysSession.add(TableInfo.ForeignKey("gym", "SET NULL", "NO ACTION", listOf("gymId"), listOf("id")))
        val _indicesSession: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSession.add(TableInfo.Index("index_session_gymId", false, listOf("gymId"), listOf("ASC")))
        _indicesSession.add(TableInfo.Index("index_session_startedAt", false, listOf("startedAt"), listOf("ASC")))
        _indicesSession.add(TableInfo.Index("index_session_state", false, listOf("state"), listOf("ASC")))
        val _infoSession: TableInfo = TableInfo("session", _columnsSession, _foreignKeysSession, _indicesSession)
        val _existingSession: TableInfo = read(connection, "session")
        if (!_infoSession.equals(_existingSession)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |session(at.mentor.bouldclockapp.data.db.entity.SessionEntity).
              | Expected:
              |""".trimMargin() + _infoSession + """
              |
              | Found:
              |""".trimMargin() + _existingSession)
        }
        val _columnsAttempt: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsAttempt.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("sessionId", TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("problemId", TableInfo.Column("problemId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("ordinal", TableInfo.Column("ordinal", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("startsNewBoulder", TableInfo.Column("startsNewBoulder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("startedAt", TableInfo.Column("startedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("endedAt", TableInfo.Column("endedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("outcome", TableInfo.Column("outcome", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("gradeValue", TableInfo.Column("gradeValue", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("gradeSystem", TableInfo.Column("gradeSystem", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("topMoveReached", TableInfo.Column("topMoveReached", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("climbHeightMeters", TableInfo.Column("climbHeightMeters", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("boardAngleDegrees", TableInfo.Column("boardAngleDegrees", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("hrAvg", TableInfo.Column("hrAvg", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("hrMax", TableInfo.Column("hrMax", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("hrEnd", TableInfo.Column("hrEnd", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("hrAfter60s", TableInfo.Column("hrAfter60s", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("hrr60", TableInfo.Column("hrr60", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("restAfterMs", TableInfo.Column("restAfterMs", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("deletedAt", TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAttempt.put("syncState", TableInfo.Column("syncState", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysAttempt: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysAttempt.add(TableInfo.ForeignKey("session", "CASCADE", "NO ACTION", listOf("sessionId"), listOf("id")))
        _foreignKeysAttempt.add(TableInfo.ForeignKey("problem", "SET NULL", "NO ACTION", listOf("problemId"), listOf("id")))
        val _indicesAttempt: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesAttempt.add(TableInfo.Index("index_attempt_sessionId_ordinal", true, listOf("sessionId", "ordinal"), listOf("ASC", "ASC")))
        _indicesAttempt.add(TableInfo.Index("index_attempt_problemId", false, listOf("problemId"), listOf("ASC")))
        _indicesAttempt.add(TableInfo.Index("index_attempt_startedAt", false, listOf("startedAt"), listOf("ASC")))
        val _infoAttempt: TableInfo = TableInfo("attempt", _columnsAttempt, _foreignKeysAttempt, _indicesAttempt)
        val _existingAttempt: TableInfo = read(connection, "attempt")
        if (!_infoAttempt.equals(_existingAttempt)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |attempt(at.mentor.bouldclockapp.data.db.entity.AttemptEntity).
              | Expected:
              |""".trimMargin() + _infoAttempt + """
              |
              | Found:
              |""".trimMargin() + _existingAttempt)
        }
        val _columnsHrSample: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsHrSample.put("sessionId", TableInfo.Column("sessionId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHrSample.put("timestampMs", TableInfo.Column("timestampMs", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHrSample.put("bpm", TableInfo.Column("bpm", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHrSample.put("accuracy", TableInfo.Column("accuracy", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysHrSample: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysHrSample.add(TableInfo.ForeignKey("session", "CASCADE", "NO ACTION", listOf("sessionId"), listOf("id")))
        val _indicesHrSample: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoHrSample: TableInfo = TableInfo("hr_sample", _columnsHrSample, _foreignKeysHrSample, _indicesHrSample)
        val _existingHrSample: TableInfo = read(connection, "hr_sample")
        if (!_infoHrSample.equals(_existingHrSample)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |hr_sample(at.mentor.bouldclockapp.data.db.entity.HrSampleEntity).
              | Expected:
              |""".trimMargin() + _infoHrSample + """
              |
              | Found:
              |""".trimMargin() + _existingHrSample)
        }
        val _columnsMetricSample: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMetricSample.put("sessionId", TableInfo.Column("sessionId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMetricSample.put("metric", TableInfo.Column("metric", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMetricSample.put("timestampMs", TableInfo.Column("timestampMs", "INTEGER", true, 3, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMetricSample.put("value", TableInfo.Column("value", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMetricSample: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysMetricSample.add(TableInfo.ForeignKey("session", "CASCADE", "NO ACTION", listOf("sessionId"), listOf("id")))
        val _indicesMetricSample: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoMetricSample: TableInfo = TableInfo("metric_sample", _columnsMetricSample, _foreignKeysMetricSample, _indicesMetricSample)
        val _existingMetricSample: TableInfo = read(connection, "metric_sample")
        if (!_infoMetricSample.equals(_existingMetricSample)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |metric_sample(at.mentor.bouldclockapp.data.db.entity.MetricSampleEntity).
              | Expected:
              |""".trimMargin() + _infoMetricSample + """
              |
              | Found:
              |""".trimMargin() + _existingMetricSample)
        }
        val _columnsSessionSummary: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSessionSummary.put("sessionId", TableInfo.Column("sessionId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("gymId", TableInfo.Column("gymId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("type", TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("startedAt", TableInfo.Column("startedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("totalMs", TableInfo.Column("totalMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("workMs", TableInfo.Column("workMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("restMs", TableInfo.Column("restMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("pausedMs", TableInfo.Column("pausedMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("attemptCount", TableInfo.Column("attemptCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("sendCount", TableInfo.Column("sendCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("flashCount", TableInfo.Column("flashCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("hardestSendValue", TableInfo.Column("hardestSendValue", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("hrAvg", TableInfo.Column("hrAvg", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("hrMax", TableInfo.Column("hrMax", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("hrr60Avg", TableInfo.Column("hrr60Avg", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("caloriesTotal", TableInfo.Column("caloriesTotal", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("caloriesOnWall", TableInfo.Column("caloriesOnWall", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("climbHeightMeters", TableInfo.Column("climbHeightMeters", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("maxClimbHeightMeters", TableInfo.Column("maxClimbHeightMeters", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("rpe", TableInfo.Column("rpe", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessionSummary.put("computedAt", TableInfo.Column("computedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSessionSummary: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysSessionSummary.add(TableInfo.ForeignKey("session", "CASCADE", "NO ACTION", listOf("sessionId"), listOf("id")))
        val _indicesSessionSummary: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSessionSummary.add(TableInfo.Index("index_session_summary_startedAt", false, listOf("startedAt"), listOf("ASC")))
        _indicesSessionSummary.add(TableInfo.Index("index_session_summary_gymId", false, listOf("gymId"), listOf("ASC")))
        _indicesSessionSummary.add(TableInfo.Index("index_session_summary_type", false, listOf("type"), listOf("ASC")))
        val _infoSessionSummary: TableInfo = TableInfo("session_summary", _columnsSessionSummary, _foreignKeysSessionSummary, _indicesSessionSummary)
        val _existingSessionSummary: TableInfo = read(connection, "session_summary")
        if (!_infoSessionSummary.equals(_existingSessionSummary)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |session_summary(at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity).
              | Expected:
              |""".trimMargin() + _infoSessionSummary + """
              |
              | Found:
              |""".trimMargin() + _existingSessionSummary)
        }
        val _columnsSensorChunk: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSensorChunk.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSensorChunk.put("sessionId", TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSensorChunk.put("sensor", TableInfo.Column("sensor", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSensorChunk.put("relativePath", TableInfo.Column("relativePath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSensorChunk.put("startedAt", TableInfo.Column("startedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSensorChunk.put("endedAt", TableInfo.Column("endedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSensorChunk.put("sampleRateHz", TableInfo.Column("sampleRateHz", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSensorChunk.put("sampleCount", TableInfo.Column("sampleCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSensorChunk.put("sizeBytes", TableInfo.Column("sizeBytes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSensorChunk.put("syncState", TableInfo.Column("syncState", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSensorChunk: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSensorChunk: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSensorChunk.add(TableInfo.Index("index_sensor_chunk_sessionId", false, listOf("sessionId"), listOf("ASC")))
        val _infoSensorChunk: TableInfo = TableInfo("sensor_chunk", _columnsSensorChunk, _foreignKeysSensorChunk, _indicesSensorChunk)
        val _existingSensorChunk: TableInfo = read(connection, "sensor_chunk")
        if (!_infoSensorChunk.equals(_existingSensorChunk)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |sensor_chunk(at.mentor.bouldclockapp.data.db.entity.SensorChunkEntity).
              | Expected:
              |""".trimMargin() + _infoSensorChunk + """
              |
              | Found:
              |""".trimMargin() + _existingSensorChunk)
        }
        val _columnsUserProfile: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsUserProfile.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("weightKg", TableInfo.Column("weightKg", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("birthYear", TableInfo.Column("birthYear", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("sex", TableInfo.Column("sex", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("deletedAt", TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProfile.put("syncState", TableInfo.Column("syncState", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysUserProfile: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesUserProfile: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoUserProfile: TableInfo = TableInfo("user_profile", _columnsUserProfile, _foreignKeysUserProfile, _indicesUserProfile)
        val _existingUserProfile: TableInfo = read(connection, "user_profile")
        if (!_infoUserProfile.equals(_existingUserProfile)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |user_profile(at.mentor.bouldclockapp.data.db.entity.UserProfileEntity).
              | Expected:
              |""".trimMargin() + _infoUserProfile + """
              |
              | Found:
              |""".trimMargin() + _existingUserProfile)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "gym", "problem", "session", "attempt", "hr_sample", "metric_sample", "session_summary", "sensor_chunk", "user_profile")
  }

  public override fun clearAllTables() {
    super.performClear(true, "gym", "problem", "session", "attempt", "hr_sample", "metric_sample", "session_summary", "sensor_chunk", "user_profile")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(GymDao::class, GymDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ProblemDao::class, ProblemDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SessionDao::class, SessionDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(AttemptDao::class, AttemptDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(HrSampleDao::class, HrSampleDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(MetricSampleDao::class, MetricSampleDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SessionSummaryDao::class, SessionSummaryDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SensorChunkDao::class, SensorChunkDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(UserProfileDao::class, UserProfileDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun gymDao(): GymDao = _gymDao.value

  public override fun problemDao(): ProblemDao = _problemDao.value

  public override fun sessionDao(): SessionDao = _sessionDao.value

  public override fun attemptDao(): AttemptDao = _attemptDao.value

  public override fun hrSampleDao(): HrSampleDao = _hrSampleDao.value

  public override fun metricSampleDao(): MetricSampleDao = _metricSampleDao.value

  public override fun sessionSummaryDao(): SessionSummaryDao = _sessionSummaryDao.value

  public override fun sensorChunkDao(): SensorChunkDao = _sensorChunkDao.value

  public override fun userProfileDao(): UserProfileDao = _userProfileDao.value
}

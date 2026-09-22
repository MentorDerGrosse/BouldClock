package at.mentor.bouldclockapp.session

import at.mentor.bouldclockapp.core.model.SessionMetric
import at.mentor.bouldclockapp.core.model.SessionState
import at.mentor.bouldclockapp.core.model.SyncState
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.core.model.AttemptKind
import at.mentor.bouldclockapp.data.db.dao.AttemptAggregate
import at.mentor.bouldclockapp.data.db.dao.AttemptDao
import at.mentor.bouldclockapp.data.db.dao.HrSampleDao
import at.mentor.bouldclockapp.data.db.dao.MetricSampleDao
import at.mentor.bouldclockapp.data.db.dao.Hrr60Point
import at.mentor.bouldclockapp.data.db.dao.GradeBucket
import at.mentor.bouldclockapp.data.db.dao.ProblemTally
import at.mentor.bouldclockapp.data.db.dao.SessionBaseline
import at.mentor.bouldclockapp.data.db.dao.SessionDao
import at.mentor.bouldclockapp.data.db.dao.SessionSummaryDao
import at.mentor.bouldclockapp.data.db.dao.UserProfileDao
import at.mentor.bouldclockapp.data.db.entity.AttemptEntity
import at.mentor.bouldclockapp.data.db.entity.HrSampleEntity
import at.mentor.bouldclockapp.data.db.entity.MetricSampleEntity
import at.mentor.bouldclockapp.data.db.entity.SessionEntity
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import at.mentor.bouldclockapp.data.db.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.math.abs

/**
 * In-Memory-Doubles der DAOs.
 *
 * Room laesst sich nur auf einem Geraet ausfuehren; die Zustandsmaschine soll
 * aber ohne Emulator pruefbar sein. Die DAOs sind Interfaces - also reicht das.
 */
class FakeSessionDao : SessionDao {
    val sessions = linkedMapOf<String, SessionEntity>()

    override suspend fun upsert(session: SessionEntity) {
        sessions[session.id] = session
    }

    override suspend fun byId(id: String): SessionEntity? = sessions[id]
    override fun observe(id: String): Flow<SessionEntity?> = flowOf(sessions[id])

    override suspend fun findUnfinished(): SessionEntity? =
        sessions.values.filter { it.state.isOpen && it.meta.deletedAt == null }
            .maxByOrNull { it.startedAt }

    override suspend fun abandonOpenExcept(keepId: String, now: Long): Int {
        val victims = sessions.values.filter {
            it.state.isOpen && it.meta.deletedAt == null && it.id != keepId
        }
        victims.forEach {
            sessions[it.id] = it.copy(
                state = at.mentor.bouldclockapp.core.model.SessionState.ABANDONED,
                meta = it.meta.touched(now),
            )
        }
        return victims.size
    }

    override suspend fun pendingSync(): List<SessionEntity> =
        sessions.values.filter {
            it.state == SessionState.FINISHED &&
                it.meta.deletedAt == null &&
                it.meta.syncState == SyncState.PENDING
        }.sortedBy { it.startedAt }

    override fun observeRecent(limit: Int): Flow<List<SessionEntity>> = flowOf(emptyList())

    override suspend fun previousComparable(
        type: SessionType,
        gymId: String?,
        before: Long,
    ): SessionEntity? = null

    override suspend fun finishedIds(): List<String> =
        sessions.values
            .filter { it.state == SessionState.FINISHED && it.meta.deletedAt == null }
            .sortedByDescending { it.startedAt }
            .map { it.id }

    override suspend fun softDelete(id: String, now: Long) {
        sessions[id]?.let { sessions[id] = it.copy(meta = it.meta.copy(deletedAt = now, updatedAt = now)) }
    }

    override suspend fun setGym(id: String, gymId: String?, now: Long) {
        sessions[id]?.let { sessions[id] = it.copy(gymId = gymId, meta = it.meta.touched(now)) }
    }
}

class FakeAttemptDao : AttemptDao {
    val attempts = linkedMapOf<String, AttemptEntity>()

    private fun of(sessionId: String) =
        attempts.values.filter { it.sessionId == sessionId && it.meta.deletedAt == null }

    override suspend fun upsert(attempt: AttemptEntity) {
        attempts[attempt.id] = attempt
    }

    override suspend fun byId(id: String): AttemptEntity? = attempts[id]

    override fun observeBySession(sessionId: String): Flow<List<AttemptEntity>> =
        flowOf(of(sessionId).sortedBy { it.ordinal })

    override suspend fun running(sessionId: String): AttemptEntity? =
        of(sessionId).filter { it.endedAt == null }.maxByOrNull { it.ordinal }

    override suspend fun nextOrdinal(sessionId: String): Int =
        (attempts.values.filter { it.sessionId == sessionId }.maxOfOrNull { it.ordinal } ?: 0) + 1

    override suspend fun lastFinished(sessionId: String): AttemptEntity? =
        of(sessionId).filter { it.endedAt != null }.maxByOrNull { it.ordinal }

    override fun observeByProblem(problemId: String): Flow<List<AttemptEntity>> = flowOf(emptyList())

    override suspend fun lastBoardAngle(): Int? =
        attempts.values
            .filter { it.boardAngleDegrees != null && it.meta.deletedAt == null }
            .maxByOrNull { it.startedAt }
            ?.boardAngleDegrees

    override suspend fun previousFinished(
        sessionId: String,
        beforeAttemptId: String,
    ): AttemptEntity? {
        val pivot = attempts[beforeAttemptId] ?: return null
        return of(sessionId)
            .filter { it.endedAt != null && it.ordinal < pivot.ordinal }
            .maxByOrNull { it.ordinal }
    }

    override suspend fun allBySession(sessionId: String): List<AttemptEntity> =
        of(sessionId).sortedBy { it.ordinal }

    /** Wie die echte Abfrage: **ohne** Zugproben. */
    override suspend fun finishedBySession(sessionId: String): List<AttemptEntity> =
        of(sessionId)
            .filter { it.endedAt != null && it.kind == AttemptKind.ATTEMPT }
            .sortedBy { it.ordinal }

    override suspend fun delete(id: String) {
        attempts.remove(id)
    }

    override suspend fun lastGradeValue(): Int? =
        attempts.values
            .filter { it.gradeValue != null && it.meta.deletedAt == null }
            .maxByOrNull { it.startedAt }
            ?.gradeValue

    override suspend fun softDelete(id: String, now: Long) {
        attempts[id]?.let { attempts[id] = it.copy(meta = it.meta.deleted(now)) }
    }

    override suspend fun aggregate(sessionId: String): AttemptAggregate {
        // Zugproben zaehlen nicht - wie in der echten Abfrage.
        val done = of(sessionId).filter { it.endedAt != null && it.kind == AttemptKind.ATTEMPT }
        return AttemptAggregate(
            attemptCount = done.size,
            sendCount = done.count { it.outcome?.isSend == true },
            flashCount = done.count { it.outcome == at.mentor.bouldclockapp.core.model.AttemptOutcome.FLASH },
            workMs = done.sumOf { (it.endedAt ?: 0L) - it.startedAt },
            hardestSendValue = done.filter { it.outcome?.isSend == true }.mapNotNull { it.gradeValue }.maxOrNull(),
            hrr60Avg = done.mapNotNull { it.hrr60 }.map { it.toDouble() }.average().takeIf { !it.isNaN() },
            climbHeightMeters = done.mapNotNull { it.climbHeightMeters }.sum().takeIf { it > 0.0 },
            maxClimbHeightMeters = done.mapNotNull { it.climbHeightMeters }.maxOrNull(),
        )
    }

    /** Alle beendeten Bloecke, Zugproben eingeschlossen. */
    override suspend fun finishedBlocks(sessionId: String): List<AttemptEntity> =
        of(sessionId).filter { it.endedAt != null }.sortedBy { it.ordinal }

    override fun observeGradeHistogram(since: Long): Flow<List<GradeBucket>> = flowOf(emptyList())

    override suspend fun setProblem(attemptId: String, problemId: String?, now: Long) {
        attempts[attemptId]?.let {
            attempts[attemptId] = it.copy(problemId = problemId, meta = it.meta.touched(now))
        }
    }

    override fun observeProblemTallies(): Flow<List<ProblemTally>> = flowOf(emptyList())
}

/** Liefert Pulswerte aus einer vorgegebenen Kurve. */
class FakeHrSampleDao(private val samples: MutableList<HrSampleEntity> = mutableListOf()) : HrSampleDao {

    fun add(sessionId: String, at: Long, bpm: Int, accuracy: Int = 3) {
        samples += HrSampleEntity(sessionId, at, bpm, accuracy)
    }

    override suspend fun insertAll(samples: List<HrSampleEntity>) {
        this.samples += samples
    }

    override suspend fun between(sessionId: String, from: Long, to: Long): List<HrSampleEntity> =
        samples.filter { it.sessionId == sessionId && it.timestampMs in from..to }

    override suspend fun nearest(sessionId: String, at: Long, toleranceMs: Long, minAccuracy: Int): Int? =
        samples.filter {
            it.sessionId == sessionId && it.accuracy >= minAccuracy && abs(it.timestampMs - at) <= toleranceMs
        }.minByOrNull { abs(it.timestampMs - at) }?.bpm

    override suspend fun avgBetween(sessionId: String, from: Long, to: Long, minAccuracy: Int): Int? =
        between(sessionId, from, to).filter { it.accuracy >= minAccuracy }
            .map { it.bpm }.average().takeIf { !it.isNaN() }?.toInt()

    override suspend fun maxBetween(sessionId: String, from: Long, to: Long, minAccuracy: Int): Int? =
        between(sessionId, from, to).filter { it.accuracy >= minAccuracy }.maxOfOrNull { it.bpm }

    override suspend fun bySession(sessionId: String): List<HrSampleEntity> =
        samples.filter { it.sessionId == sessionId }.sortedBy { it.timestampMs }

    override suspend fun deleteForSession(sessionId: String) {
        samples.removeAll { it.sessionId == sessionId }
    }
}

class FakeMetricSampleDao : MetricSampleDao {
    val samples = mutableListOf<MetricSampleEntity>()

    fun add(sessionId: String, metric: SessionMetric, at: Long, value: Double) {
        samples += MetricSampleEntity(sessionId, metric, at, value)
    }

    override suspend fun insertAll(samples: List<MetricSampleEntity>) {
        this.samples += samples
    }

    override suspend fun total(sessionId: String, metric: SessionMetric): Double? =
        samples.filter { it.sessionId == sessionId && it.metric == metric }.maxOfOrNull { it.value }

    override suspend fun latestAt(sessionId: String, metric: SessionMetric, at: Long): Double? =
        samples.filter { it.sessionId == sessionId && it.metric == metric && it.timestampMs <= at }
            .maxByOrNull { it.timestampMs }?.value

    override suspend fun bySession(sessionId: String): List<MetricSampleEntity> =
        samples.filter { it.sessionId == sessionId }.sortedBy { it.timestampMs }

    override suspend fun deleteForSession(sessionId: String) {
        samples.removeAll { it.sessionId == sessionId }
    }
}

class FakeSummaryDao : SessionSummaryDao {
    val summaries = linkedMapOf<String, SessionSummaryEntity>()

    override suspend fun upsert(summary: SessionSummaryEntity) {
        summaries[summary.sessionId] = summary
    }

    override suspend fun bySession(sessionId: String): SessionSummaryEntity? = summaries[sessionId]

    override fun observeBySession(sessionId: String): Flow<SessionSummaryEntity?> =
        flowOf(summaries[sessionId])

    override fun observeRecent(limit: Int): Flow<List<SessionSummaryEntity>> = flowOf(summaries.values.toList())

    override suspend fun baseline(
        type: SessionType,
        gymId: String?,
        since: Long,
        excludeSessionId: String,
    ): SessionBaseline = SessionBaseline(0, null, null, null, null, null, null)

    override fun observeHrr60Trend(since: Long): Flow<List<Hrr60Point>> = flowOf(emptyList())

    override fun observeAll(): Flow<List<SessionSummaryEntity>> = flowOf(summaries.values.toList())

    override suspend fun deleteForSession(sessionId: String) {
        summaries.remove(sessionId)
    }
}

/** Ein Profil im Speicher. */
class FakeUserProfileDao(var profile: UserProfileEntity? = null) : UserProfileDao {
    override suspend fun upsert(profile: UserProfileEntity) {
        this.profile = profile
    }

    override suspend fun get(): UserProfileEntity? = profile?.takeIf { it.meta.deletedAt == null }

    override fun observe(): Flow<UserProfileEntity?> =
        flowOf(profile?.takeIf { it.meta.deletedAt == null })
}

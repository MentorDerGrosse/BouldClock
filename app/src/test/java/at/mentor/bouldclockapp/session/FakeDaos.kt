package at.mentor.bouldclockapp.session

import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.data.db.dao.AttemptAggregate
import at.mentor.bouldclockapp.data.db.dao.AttemptDao
import at.mentor.bouldclockapp.data.db.dao.HrSampleDao
import at.mentor.bouldclockapp.data.db.dao.Hrr60Point
import at.mentor.bouldclockapp.data.db.dao.SessionBaseline
import at.mentor.bouldclockapp.data.db.dao.SessionDao
import at.mentor.bouldclockapp.data.db.dao.SessionSummaryDao
import at.mentor.bouldclockapp.data.db.entity.AttemptEntity
import at.mentor.bouldclockapp.data.db.entity.HrSampleEntity
import at.mentor.bouldclockapp.data.db.entity.SessionEntity
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
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

    override fun observeRecent(limit: Int): Flow<List<SessionEntity>> = flowOf(emptyList())

    override suspend fun previousComparable(
        type: SessionType,
        gymId: String?,
        before: Long,
    ): SessionEntity? = null
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

    override suspend fun lastGrade(): at.mentor.bouldclockapp.data.db.dao.LastGrade? =
        attempts.values
            .filter { it.gradeValue != null && it.meta.deletedAt == null }
            .maxByOrNull { it.startedAt }
            ?.let { at.mentor.bouldclockapp.data.db.dao.LastGrade(it.gradeValue!!, it.gradeSystem) }

    override suspend fun softDelete(id: String, now: Long) {
        attempts[id]?.let { attempts[id] = it.copy(meta = it.meta.deleted(now)) }
    }

    override suspend fun aggregate(sessionId: String): AttemptAggregate {
        val done = of(sessionId).filter { it.endedAt != null && it.outcome?.countsAsAttempt != false }
        return AttemptAggregate(
            attemptCount = done.size,
            sendCount = done.count { it.outcome?.isSend == true },
            flashCount = done.count { it.outcome == at.mentor.bouldclockapp.core.model.AttemptOutcome.FLASH },
            workMs = done.sumOf { (it.endedAt ?: 0L) - it.startedAt },
            hardestSendValue = done.filter { it.outcome?.isSend == true }.mapNotNull { it.gradeValue }.maxOrNull(),
            hrr60Avg = done.mapNotNull { it.hrr60 }.map { it.toDouble() }.average().takeIf { !it.isNaN() },
        )
    }
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

    override fun observeRecent(limit: Int): Flow<List<SessionSummaryEntity>> = flowOf(summaries.values.toList())

    override suspend fun baseline(
        type: SessionType,
        gymId: String?,
        since: Long,
        excludeSessionId: String,
    ): SessionBaseline = SessionBaseline(0, null, null, null, null, null, null)

    override fun observeHrr60Trend(since: Long): Flow<List<Hrr60Point>> = flowOf(emptyList())
}

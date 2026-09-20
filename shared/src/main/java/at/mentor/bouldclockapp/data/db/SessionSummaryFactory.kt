package at.mentor.bouldclockapp.data.db

import at.mentor.bouldclockapp.core.metrics.SessionMetrics
import at.mentor.bouldclockapp.data.db.dao.AttemptAggregate
import at.mentor.bouldclockapp.data.db.entity.SessionEntity
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import kotlin.math.roundToInt

/**
 * Baut die vorberechnete Zusammenfassung einer Session.
 *
 * Wird beim Sessionende aufgerufen - und erneut, wenn jemand nachtraeglich
 * Versuche korrigiert. Deshalb idempotent: gleiche Eingabe, gleiches Ergebnis.
 */
fun buildSessionSummary(
    session: SessionEntity,
    aggregate: AttemptAggregate,
    hrAvg: Int?,
    hrMax: Int?,
    caloriesTotal: Double?,
    caloriesOnWall: Double?,
    now: Long,
): SessionSummaryEntity {
    val endedAt = session.endedAt ?: now
    val split = SessionMetrics.timeSplit(
        totalMs = endedAt - session.startedAt,
        workMs = aggregate.workMs,
        pausedMs = session.pausedMs,
    )

    return SessionSummaryEntity(
        sessionId = session.id,
        gymId = session.gymId,
        type = session.type,
        startedAt = session.startedAt,
        totalMs = split.totalMs,
        workMs = split.workMs,
        restMs = split.restMs,
        pausedMs = split.pausedMs,
        attemptCount = aggregate.attemptCount,
        sendCount = aggregate.sendCount,
        flashCount = aggregate.flashCount,
        hardestSendValue = aggregate.hardestSendValue,
        hrAvg = hrAvg,
        hrMax = hrMax,
        hrr60Avg = aggregate.hrr60Avg?.roundToInt(),
        caloriesTotal = caloriesTotal,
        caloriesOnWall = caloriesOnWall,
        climbHeightMeters = aggregate.climbHeightMeters,
        maxClimbHeightMeters = aggregate.maxClimbHeightMeters,
        rpe = session.rpe,
        computedAt = now,
    )
}

package at.mentor.bouldclockapp.data.sync

import at.mentor.bouldclockapp.core.model.SyncState
import at.mentor.bouldclockapp.data.db.BouldClockDatabase
import at.mentor.bouldclockapp.data.db.buildSessionSummary
import at.mentor.bouldclockapp.data.db.entity.SessionEntity

/**
 * Packt Sessions fuer die Uebertragung und legt ankommende ab.
 *
 * Auf beiden Geraeten dieselbe Klasse - die Uhr benutzt [buildPayload], das
 * Handy [apply]. Spaeter, wenn am Handy bearbeitet wird, laeuft es umgekehrt.
 */
class SessionSyncRepository(private val db: BouldClockDatabase) {

    /** Beendete Sessions, die noch nicht uebertragen wurden. */
    suspend fun pending(): List<SessionEntity> = db.sessionDao().pendingSync()

    suspend fun buildPayload(sessionId: String): SessionPayload? {
        val session = db.sessionDao().byId(sessionId) ?: return null
        return SessionPayload(
            session = session,
            attempts = db.attemptDao().allBySession(sessionId),
            hrSamples = db.hrSampleDao().bySession(sessionId),
            metricSamples = db.metricSampleDao().bySession(sessionId),
        )
    }

    /**
     * Legt ein angekommenes Paket ab.
     *
     * Die Zusammenfassung wird **neu gerechnet statt uebertragen**: sie ist ein
     * Zwischenspeicher, kein Rohdatum. Wuerde sie mitgeschickt, muesste jede
     * Aenderung an ihrer Berechnung auf beiden Geraeten gleichzeitig ausgerollt
     * werden.
     */
    suspend fun apply(payload: SessionPayload) {
        val existing = db.sessionDao().byId(payload.session.id)

        // Das juengere gewinnt - dieselbe Regel wie beim Profil.
        if (existing != null && existing.meta.updatedAt > payload.session.meta.updatedAt) return

        db.sessionDao().upsert(payload.session)
        payload.attempts.forEach { db.attemptDao().upsert(it) }
        db.hrSampleDao().insertAll(payload.hrSamples)
        db.metricSampleDao().insertAll(payload.metricSamples)

        recomputeSummary(payload.session)
    }

    private suspend fun recomputeSummary(session: SessionEntity) {
        val endedAt = session.endedAt ?: return
        db.sessionSummaryDao().upsert(
            buildSessionSummary(
                session = session,
                aggregate = db.attemptDao().aggregate(session.id),
                hrAvg = db.hrSampleDao().avgBetween(session.id, session.startedAt, endedAt, MIN_ACCURACY),
                hrMax = db.hrSampleDao().maxBetween(session.id, session.startedAt, endedAt, MIN_ACCURACY),
                caloriesTotal = db.metricSampleDao()
                    .total(session.id, at.mentor.bouldclockapp.core.model.SessionMetric.CALORIES),
                caloriesOnWall = null,
                now = System.currentTimeMillis(),
            ),
        )
    }

    /** Merkt, dass eine Session uebertragen wurde. */
    suspend fun markSynced(sessionId: String) {
        val session = db.sessionDao().byId(sessionId) ?: return
        db.sessionDao().upsert(
            session.copy(meta = session.meta.copy(syncState = SyncState.SYNCED)),
        )
    }

    private companion object {
        const val MIN_ACCURACY = 3
    }
}

package at.mentor.bouldclockapp.data.sync

import at.mentor.bouldclockapp.core.model.SyncState
import at.mentor.bouldclockapp.data.db.BouldClockDatabase
import at.mentor.bouldclockapp.data.db.SessionAnalysis
import at.mentor.bouldclockapp.data.db.buildSessionSummary
import at.mentor.bouldclockapp.data.db.entity.SensorChunkEntity
import at.mentor.bouldclockapp.data.db.entity.SessionEntity
import at.mentor.bouldclockapp.data.db.entity.UserProfileEntity
import java.io.File

/**
 * Packt Sessions fuer die Uebertragung und legt ankommende ab.
 *
 * Auf beiden Geraeten dieselbe Klasse - die Uhr benutzt [buildPayload], das
 * Handy [apply]. Spaeter, wenn am Handy bearbeitet wird, laeuft es umgekehrt.
 *
 * [filesDir] ist der App-interne Speicher: nur dort laesst sich nachsehen, ob
 * eine Sensordatei wirklich liegt. Ohne das muesste man dem Stand der
 * Gegenstelle glauben, und der sagt etwas anderes - siehe [apply].
 */
class SessionSyncRepository(
    private val db: BouldClockDatabase,
    private val filesDir: File,
) {

    /** Beendete Sessions, die noch nicht uebertragen wurden. */
    suspend fun pending(): List<SessionEntity> = db.sessionDao().pendingSync()

    suspend fun buildPayload(sessionId: String): SessionPayload? {
        val session = db.sessionDao().byId(sessionId) ?: return null
        val attempts = db.attemptDao().allBySession(sessionId)

        // Halle und Boulder muessen mit, sonst weist der Fremdschluessel auf
        // der Gegenseite das ganze Paket ab. Nur die verwendeten Zeilen.
        val problems = attempts.mapNotNull { it.problemId }.distinct()
            .mapNotNull { db.problemDao().byId(it) }
        val gymIds = (problems.map { it.gymId } + listOfNotNull(session.gymId)).distinct()
        val gyms = gymIds.mapNotNull { db.gymDao().byId(it) }

        return SessionPayload(
            session = session,
            attempts = attempts,
            hrSamples = db.hrSampleDao().bySession(sessionId),
            metricSamples = db.metricSampleDao().bySession(sessionId),
            sensorChunks = db.sensorChunkDao().bySession(sessionId),
            gyms = gyms,
            problems = problems,
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

        // Reihenfolge nach Fremdschluesseln: Halle vor Session und Boulder,
        // Boulder vor Versuch. Andersherum weist SQLite das ganze Paket ab.
        payload.gyms.forEach { db.gymDao().upsert(it) }
        db.sessionDao().upsert(payload.session)
        payload.problems.forEach { db.problemDao().upsert(it) }

        // Zeigt ein Versuch trotzdem auf einen unbekannten Boulder - etwa weil
        // die Gegenstelle noch eine aeltere Fassung hat - dann lieber die
        // Zuordnung fallen lassen als die ganze Session.
        val knownProblems = payload.problems.map { it.id }.toSet()
        payload.attempts.forEach { attempt ->
            val resolvable = attempt.problemId == null ||
                attempt.problemId in knownProblems ||
                db.problemDao().byId(attempt.problemId) != null
            db.attemptDao().upsert(
                if (resolvable) attempt else attempt.copy(problemId = null),
            )
        }
        db.hrSampleDao().insertAll(payload.hrSamples)
        db.metricSampleDao().insertAll(payload.metricSamples)

        // Metadaten aus dem Paket, Ankunftsstand aus der vorhandenen Zeile: eine
        // schon eingetroffene Datei soll nicht auf "fehlt noch" zurueckfallen,
        // eine nur aus dem Pfad gebaute Zeile aber ihre Luecken gefuellt bekommen.
        payload.sensorChunks.forEach { chunk ->
            val known = db.sensorChunkDao().byRelativePath(chunk.relativePath)
            db.sensorChunkDao().upsert(
                if (known == null) {
                    // "SYNCED" der Gegenstelle heisst nur, dass sie die Datei
                    // abgeschickt hat - nicht, dass sie hier liegt. Also
                    // nachsehen: die Datei kann laengst da sein, wenn ihre
                    // Zeile erst spaeter kommt.
                    chunk.copy(syncState = arrivalState(chunk.relativePath))
                } else {
                    chunk.copy(
                        id = known.id,
                        syncState = known.syncState,
                        // Bei einer angekommenen Datei zaehlt die gemessene Groesse.
                        sizeBytes = if (known.syncState == SyncState.SYNCED) {
                            known.sizeBytes
                        } else {
                            chunk.sizeBytes
                        },
                    )
                },
            )
        }

        recomputeSummary(payload.session)
    }

    /** Nach einer Aenderung am Handy neu rechnen - die Zusammenfassung ist nur ein Zwischenspeicher. */
    suspend fun refreshSummary(sessionId: String) {
        db.sessionDao().byId(sessionId)?.let { recomputeSummary(it) }
    }

    private suspend fun recomputeSummary(session: SessionEntity) {
        // Eine geloeschte Session hat keine Zusammenfassung mehr. Das ist nicht
        // Kosmetik: saemtliche Summen - Hoehenmeter, Diagramme, Historie -
        // lesen aus session_summary. Bleibt die Zeile stehen, zaehlt der
        // geloeschte Abend ueberall weiter mit.
        if (session.meta.deletedAt != null) {
            db.sessionSummaryDao().deleteForSession(session.id)
            return
        }

        val endedAt = session.endedAt ?: return
        val now = System.currentTimeMillis()

        // Erholung neu bestimmen: wird am Handy ein Versuch zur Zugprobe
        // gemacht oder geloescht, aendert das, welche Pausen ungestoert waren.
        SessionAnalysis.applyRecovery(db.attemptDao(), db.hrSampleDao(), session.id, now)

        // Dieselbe Kalorienrechnung wie auf der Uhr. Liefe sie hier anders,
        // wuerde jede Korrektur am Handy die Zahlen der Uhr still veraendern.
        val energy = SessionAnalysis.energy(
            db.attemptDao(), db.hrSampleDao(), db.userProfileDao(), session,
        )

        db.sessionSummaryDao().upsert(
            buildSessionSummary(
                session = session,
                aggregate = db.attemptDao().aggregate(session.id),
                hrAvg = db.hrSampleDao().avgBetween(session.id, session.startedAt, endedAt, MIN_ACCURACY),
                hrMax = db.hrSampleDao().maxBetween(session.id, session.startedAt, endedAt, MIN_ACCURACY),
                caloriesTotal = energy?.totalKcal,
                caloriesOnWall = energy?.onWallKcal,
                now = now,
            ),
        )
    }

    suspend fun profile(): UserProfileEntity? = db.userProfileDao().get()

    /** Sensordateien einer Session, die noch nicht uebertragen wurden. */
    suspend fun pendingChunks(sessionId: String): List<SensorChunkEntity> =
        db.sensorChunkDao().bySession(sessionId).filter { it.syncState == SyncState.PENDING }

    suspend fun sessionsWithPendingFiles(): List<String> =
        db.sensorChunkDao().sessionsWithPendingFiles()

    suspend fun markChunkSynced(chunk: SensorChunkEntity) {
        db.sensorChunkDao().upsert(chunk.copy(syncState = SyncState.SYNCED))
    }

    /** Vermerkt, dass eine Datei angekommen ist. */
    suspend fun markFileArrived(relativePath: String, sizeBytes: Long) {
        val dao = db.sensorChunkDao()
        val existing = dao.byRelativePath(relativePath)
        if (existing != null) {
            dao.upsert(existing.copy(syncState = SyncState.SYNCED, sizeBytes = sizeBytes))
            return
        }

        // Datei ohne Metadaten. Frueher fiel sie hier stillschweigend heraus:
        // gespeichert, aber fuer die App unsichtbar, weil nichts auf sie zeigte.
        // Jetzt reicht der Pfad fuer eine Zeile, den Rest traegt das Paket nach.
        SensorChunkEntity.fromArrivedFile(relativePath, sizeBytes)?.let { dao.upsert(it) }
    }

    /** Liegt die Datei hier, gilt sie als angekommen - egal, was das Paket sagt. */
    private fun arrivalState(relativePath: String): SyncState =
        if (File(filesDir, relativePath).exists()) SyncState.SYNCED else SyncState.PENDING

    /**
     * Legt ein angekommenes Profil ab - wieder gewinnt das juengere.
     *
     * Gibt zurueck, ob uebernommen wurde; die Uhr protokolliert das, weil ein
     * stillschweigend verworfenes Profil sonst schwer zu erklaeren waere.
     */
    suspend fun applyProfile(payload: ProfilePayload): Boolean {
        val existing = db.userProfileDao().get()
        if (existing != null && existing.meta.updatedAt >= payload.profile.meta.updatedAt) return false
        db.userProfileDao().upsert(payload.profile)
        return true
    }

    /** Merkt, dass das Profil uebertragen wurde. */
    suspend fun markProfileSynced() {
        val profile = db.userProfileDao().get() ?: return
        db.userProfileDao().upsert(
            profile.copy(meta = profile.meta.copy(syncState = SyncState.SYNCED)),
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

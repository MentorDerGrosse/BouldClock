package at.mentor.bouldclockapp.data.db

import at.mentor.bouldclockapp.core.metrics.BodyProfile
import at.mentor.bouldclockapp.core.metrics.Energy
import at.mentor.bouldclockapp.core.metrics.EnergyEstimate
import at.mentor.bouldclockapp.core.metrics.HeartBeat
import at.mentor.bouldclockapp.core.metrics.Recovery
import at.mentor.bouldclockapp.core.metrics.SessionMetrics
import at.mentor.bouldclockapp.core.metrics.WallBlock
import at.mentor.bouldclockapp.data.db.dao.AttemptDao
import at.mentor.bouldclockapp.data.db.dao.HrSampleDao
import at.mentor.bouldclockapp.data.db.dao.UserProfileDao
import at.mentor.bouldclockapp.data.db.entity.AttemptEntity
import at.mentor.bouldclockapp.data.db.entity.SessionEntity
import java.time.LocalDate

/**
 * Auswertung einer Session aus den gespeicherten Rohwerten.
 *
 * An einer Stelle, weil **beide Seiten sie brauchen**: die Uhr am Sessionende,
 * das Handy bei jeder Nachbearbeitung. Liefen sie auseinander, wuerde eine
 * Korrektur am Handy die Zahlen der Uhr stillschweigend veraendern.
 *
 * Gerechnet wird in [at.mentor.bouldclockapp.core.metrics] - hier wird nur
 * gesammelt, umgeformt und zurueckgeschrieben.
 */
object SessionAnalysis {

    /**
     * Traegt die Herzfrequenz-Erholung je Versuch nach.
     *
     * Gibt zurueck, ob sich etwas geaendert hat. Loescht auch wieder: wird am
     * Handy nachtraeglich eine Zugprobe markiert, faellt der Erholungswert des
     * Versuchs davor zu Recht weg.
     */
    suspend fun applyRecovery(
        attemptDao: AttemptDao,
        hrSampleDao: HrSampleDao,
        sessionId: String,
        now: Long,
    ): Boolean {
        val attempts = attemptDao.finishedBlocks(sessionId)
        if (attempts.isEmpty()) return false

        val beats = beatsOf(hrSampleDao, sessionId)
        val blocks = attempts.associateWith { it.toBlock() }
        val recovery = Recovery.forAll(blocks.values.toList(), beats)

        var changed = false
        attempts.forEach { attempt ->
            val found = recovery[blocks.getValue(attempt)]
            val hrEnd = found?.hrEnd
            val hrAfter = found?.hrAfter60s
            val drop = found?.drop

            if (attempt.hrEnd == hrEnd && attempt.hrAfter60s == hrAfter && attempt.hrr60 == drop) {
                return@forEach
            }
            attemptDao.upsert(
                attempt.copy(
                    hrEnd = hrEnd,
                    hrAfter60s = hrAfter,
                    hrr60 = drop,
                    meta = attempt.meta.touched(now),
                ),
            )
            changed = true
        }
        return changed
    }

    /**
     * Energieverbrauch der Session.
     *
     * `null`, wenn kein Profil angelegt ist - ohne Gewicht ist jede Zahl
     * geraten, und eine geratene Kalorienzahl ist schlechter als keine.
     */
    suspend fun energy(
        attemptDao: AttemptDao,
        hrSampleDao: HrSampleDao,
        userProfileDao: UserProfileDao,
        session: SessionEntity,
    ): EnergyEstimate? {
        val profile = userProfileDao.get() ?: return null
        val endedAt = session.endedAt ?: return null

        return Energy.estimate(
            beats = beatsOf(hrSampleDao, session.id),
            blocks = attemptDao.finishedBlocks(session.id).map { it.toBlock() },
            profile = BodyProfile(
                weightKg = profile.weightKg.toDouble(),
                ageYears = profile.ageInYears(LocalDate.now()),
                sex = profile.sex,
                heightCm = profile.heightCm,
                restingHrBpm = profile.restingHrBpm,
                maxHrBpm = profile.maxHrBpm,
            ),
            sessionStartedAt = session.startedAt,
            sessionEndedAt = endedAt,
        )
    }

    /**
     * Fuehrt den hoechsten je gesehenen Puls im Profil nach.
     *
     * Kostenlos und ueber Monate genauer als jede Altersformel - und er geht
     * ueber die geschaetzte Sauerstoffaufnahme direkt in die Kalorien ein.
     */
    suspend fun trackMaxHeartRate(
        hrSampleDao: HrSampleDao,
        userProfileDao: UserProfileDao,
        sessionId: String,
        now: Long,
    ): Boolean {
        val profile = userProfileDao.get() ?: return false
        val observed = beatsOf(hrSampleDao, sessionId).maxOfOrNull { it.bpm } ?: return false
        if (observed <= (profile.maxHrBpm ?: 0)) return false

        userProfileDao.upsert(
            profile.copy(maxHrBpm = observed, meta = profile.meta.touched(now)),
        )
        return true
    }

    private suspend fun beatsOf(hrSampleDao: HrSampleDao, sessionId: String): List<HeartBeat> =
        hrSampleDao.bySession(sessionId)
            .filter { it.accuracy >= SessionMetrics.MIN_HR_ACCURACY }
            .map { HeartBeat(it.timestampMs, it.bpm) }

    private fun AttemptEntity.toBlock() = WallBlock(
        startedAt = startedAt,
        endedAt = endedAt ?: startedAt,
        isAttempt = kind.isAttempt,
    )
}

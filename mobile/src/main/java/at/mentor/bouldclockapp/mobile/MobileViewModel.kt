package at.mentor.bouldclockapp.mobile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import at.mentor.bouldclockapp.core.metrics.AttemptFact
import at.mentor.bouldclockapp.core.metrics.BodyProfile
import at.mentor.bouldclockapp.core.metrics.Energy
import at.mentor.bouldclockapp.core.metrics.HeartRateZone
import at.mentor.bouldclockapp.core.metrics.Readiness
import at.mentor.bouldclockapp.core.metrics.ReadinessLevel
import at.mentor.bouldclockapp.core.metrics.SessionMetrics
import at.mentor.bouldclockapp.core.metrics.lowerBpm
import at.mentor.bouldclockapp.core.metrics.readiness
import at.mentor.bouldclockapp.core.metrics.AttemptRun
import at.mentor.bouldclockapp.core.metrics.HeightTotals
import at.mentor.bouldclockapp.core.metrics.Period
import at.mentor.bouldclockapp.core.metrics.PeriodBucket
import at.mentor.bouldclockapp.core.metrics.SessionFact
import at.mentor.bouldclockapp.core.metrics.bucket
import at.mentor.bouldclockapp.core.metrics.fillGaps
import at.mentor.bouldclockapp.core.metrics.groupRuns
import at.mentor.bouldclockapp.core.metrics.heightTotals
import at.mentor.bouldclockapp.core.metrics.startOfPeriod
import at.mentor.bouldclockapp.core.model.AttemptKind
import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.BiologicalSex
import at.mentor.bouldclockapp.core.model.LandmarkComparison
import at.mentor.bouldclockapp.core.model.Landmarks
import at.mentor.bouldclockapp.core.model.ProfileRanges
import at.mentor.bouldclockapp.data.db.BouldClockDatabase
import at.mentor.bouldclockapp.data.db.dao.GradeBucket
import at.mentor.bouldclockapp.data.db.dao.HrSessionRange
import at.mentor.bouldclockapp.data.db.dao.HrZoneSeconds
import at.mentor.bouldclockapp.data.db.entity.HrSampleEntity
import at.mentor.bouldclockapp.data.db.dao.Hrr60Point
import at.mentor.bouldclockapp.data.db.dao.ProblemTally
import at.mentor.bouldclockapp.data.db.entity.AttemptEntity
import at.mentor.bouldclockapp.data.db.entity.GymEntity
import at.mentor.bouldclockapp.data.db.entity.ProblemEntity
import at.mentor.bouldclockapp.data.db.entity.RecordMeta
import at.mentor.bouldclockapp.data.db.entity.SessionEntity
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import at.mentor.bouldclockapp.data.db.entity.UserProfileEntity
import at.mentor.bouldclockapp.data.sync.SessionSyncRepository
import at.mentor.bouldclockapp.data.sync.SessionSyncSender
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Eine Session mit allem, was der Detailbildschirm braucht. */
data class SessionDetail(
    val session: SessionEntity,
    val summary: SessionSummaryEntity?,
    val attempts: List<AttemptEntity>,
    val gym: GymEntity?,

    /** Sekuendlicher Puls der Session - fuer Verlauf und Startpuls je Versuch. */
    val heartBeats: List<HrSampleEntity> = emptyList(),
) {

    /**
     * Puls beim Losklettern, je Versuch.
     *
     * Das ehrlichere Ermuedungssignal als HRR60: wer den achten Boulder mit 132
     * beginnt und den ersten mit 99 hatte, ist nicht erholt - und dafuer braucht
     * es keine Fensterregeln.
     */
    val startBpm: Map<String, Int> get() = attempts.associate { attempt ->
        attempt.id to (
            heartBeats
                .minByOrNull { kotlin.math.abs(it.timestampMs - attempt.startedAt) }
                ?.takeIf { kotlin.math.abs(it.timestampMs - attempt.startedAt) <= 5_000L }
                ?.bpm ?: 0
            )
    }.filterValues { it > 0 }
    /**
     * Die Versuche zu Bouldern gruppiert.
     *
     * Dieselbe Regel wie in der Zusammenfassung auf der Uhr: getrennt wird an
     * der beim Protokollieren gesetzten Grenze, nicht an gleichen Graden.
     */
    /** Nur echte Versuche - eine Zugprobe gehoert zum laufenden Boulder, zaehlt aber nicht. */
    val realAttempts: List<AttemptEntity>
        get() = attempts.filter { it.meta.deletedAt == null && it.kind.isAttempt }

    val boulders: List<BoulderRun> get() {
        val visible = attempts.filter {
            it.meta.deletedAt == null && it.endedAt != null && it.kind.isAttempt
        }
        val runs = groupRuns(visible.map { it.toFact() })
        var index = 0
        return runs.map { run ->
            val size = run.attempts
            val slice = visible.subList(index, index + size)
            index += size
            BoulderRun(run = run, attempts = slice)
        }
    }
}

/** Ein Boulder innerhalb einer Session, samt der Versuche, aus denen er besteht. */
data class BoulderRun(
    val run: AttemptRun,
    val attempts: List<AttemptEntity>,
) {
    val problemId: String? get() = attempts.firstNotNullOfOrNull { it.problemId }
    val startedAt: Long get() = attempts.first().startedAt
}

/** Was auf dem Dashboard steht. */
data class DashboardState(
    val lastSession: SessionSummaryEntity?,
    val heights: HeightTotals = HeightTotals(0.0, 0.0, 0.0, 0.0),
    val landmark: LandmarkComparison = Landmarks.compare(0.0),
    val recentWeeks: List<PeriodBucket> = emptyList(),
    val thisWeek: PeriodBucket? = null,
    val lastWeek: PeriodBucket? = null,
    val sessionCount: Int = 0,

    /** Ueber alle Sessions - die Zahl, die man zum Spass ganz vorne sehen will. */
    val flashRate: Double? = null,
    val sendRate: Double? = null,

    /** Sessions je Woche, ueber die Wochen mit Aktivitaet. */
    val sessionsPerWeek: Double? = null,

    /** Puls der letzten acht Wochen, fuer den Verlauf auf der Uebersicht. */
    val recentPulse: List<PeriodBucket> = emptyList(),
)

/** Eine Kalenderwoche in der Sessionliste. */
data class SessionWeek(
    val start: LocalDate,
    val summaries: List<SessionSummaryEntity>,
) {
    val climbHeightMeters: Double get() = summaries.sumOf { it.climbHeightMeters ?: 0.0 }
}

/**
 * Die Handy-App.
 *
 * Jede Aenderung wird erst lokal gespeichert und dann zur Uhr geschickt. Geht
 * das Verschicken schief, bleibt die Aenderung trotzdem da - die Session steht
 * dann weiter auf `PENDING` und geht beim naechsten Mal mit.
 *
 * Zusammengefasst wird nicht in SQL, sondern in
 * [at.mentor.bouldclockapp.core.metrics]: ein paar hundert Zeilen kosten nichts,
 * und dort ist das Rechnen ohne Datenbank und ohne Android pruefbar.
 */
class MobileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BouldClockDatabase.get(application)
    private val repository = SessionSyncRepository(db, application.filesDir)
    private val sender = SessionSyncSender(application, repository)
    private val zone: ZoneId = ZoneId.systemDefault()

    init {
        // Beim Start alles neu durchrechnen.
        //
        // Dieselbe Rechnung wie auf der Uhr, nur von dieser Seite angestossen -
        // damit die Kalorien nach einer Aenderung am Modell auch dann stimmen,
        // wenn die Uhr gerade nicht in Reichweite ist. Idempotent: gleiche
        // Rohwerte, gleiches Ergebnis.
        viewModelScope.launch {
            db.sessionDao().finishedIds().forEach { repository.refreshSummary(it) }
        }
    }

    // --- Bestaende ---

    val summaries: StateFlow<List<SessionSummaryEntity>> =
        db.sessionSummaryDao().observeAll()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val gyms: StateFlow<List<GymEntity>> = db.gymDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val problems: StateFlow<List<ProblemEntity>> = db.problemDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val problemTallies: StateFlow<List<ProblemTally>> = db.attemptDao().observeProblemTallies()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val gradeHistogram: StateFlow<List<GradeBucket>> =
        db.attemptDao().observeGradeHistogram(since = 0L)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val hrr60Trend: StateFlow<List<Hrr60Point>> =
        db.sessionSummaryDao().observeHrr60Trend(since = 0L)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val profile: StateFlow<UserProfileEntity?> = db.userProfileDao().observe()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Puls je Session - Minimum, Schnitt, Maximum. */
    val hrRanges: StateFlow<Map<String, HrSessionRange>> =
        db.hrSampleDao().observeSessionRanges(SessionMetrics.MIN_HR_ACCURACY)
            .map { list -> list.associateBy { it.sessionId } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /**
     * Sekunden je Pulszone, je Session.
     *
     * Die Zonengrenzen haengen am Profil, deshalb wird die Abfrage neu
     * aufgesetzt, sobald sich Ruhe- oder Maximalpuls aendern.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val zoneSeconds: StateFlow<Map<String, HrZoneSeconds>> = profile
        .flatMapLatest { current ->
            val bounds = zoneBounds(current)
            db.hrSampleDao().observeZoneSeconds(
                SessionMetrics.MIN_HR_ACCURACY,
                bounds[0], bounds[1], bounds[2], bounds[3], bounds[4],
            )
        }
        .map { list -> list.associateBy { it.sessionId } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /** Die Zonengrenzen in Schlaegen - fuers Beschriften. */
    val zoneBounds: StateFlow<List<Int>> = profile
        .map { zoneBounds(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, zoneBounds(null))

    // --- Abgeleitetes ---

    val dashboard: StateFlow<DashboardState> = summaries
        .map { list -> buildDashboard(list) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DashboardState(lastSession = null))

    /**
     * Sessions in Wochenabschnitte.
     *
     * Absaetze, keine Klappmenues: eine Woche ist eine Ueberschrift, unter der
     * die Sessions stehen. Wer scrollt, will lesen und nicht erst oeffnen.
     */
    val sessionWeeks: StateFlow<List<SessionWeek>> = summaries
        .map { list ->
            list.groupBy { startOfPeriod(it.startedAt.toLocalDate(), Period.WEEK) }
                .map { (start, group) -> SessionWeek(start, group.sortedByDescending { it.startedAt }) }
                .sortedByDescending { it.start }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Ist heute ein guter Tag zum Bouldern?
     *
     * Aus Abstand zur letzten Session, Wochenvolumen und letztem RPE - mit
     * ausgeschriebener Begruendung statt einer Punktzahl.
     */
    val readiness: StateFlow<Readiness> = summaries
        .map { list ->
            readiness(
                facts = list.map { it.toFact() },
                lastRpe = list.maxByOrNull { it.startedAt }?.rpe,
                today = LocalDate.now(zone),
                zone = zone,
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            Readiness(ReadinessLevel.UNKNOWN, emptyList()),
        )

    private val historyPeriod = MutableStateFlow(Period.WEEK)
    val period: StateFlow<Period> = historyPeriod

    val historyBuckets: StateFlow<List<PeriodBucket>> =
        combine(summaries, historyPeriod) { list, selected ->
            val buckets = bucket(list.map { it.toFact() }, selected, zone)
            fillGaps(buckets, selected, count = slotsFor(selected), until = LocalDate.now(zone))
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Zeit je Pulszone ueber den gewaehlten Zeitraum.
     *
     * Eine Verteilung, kein Verlauf: die Frage ist "wie viel vom Abend war
     * wirklich hart", nicht "wann".
     */
    val periodZones: StateFlow<Map<HeartRateZone, Int>> =
        combine(historyBuckets, zoneSeconds, summaries) { buckets, zones, list ->
            if (buckets.isEmpty()) return@combine emptyMap()
            val from = buckets.first().start.atStartOfDay(zone).toInstant().toEpochMilli()
            val ids = list.filter { it.startedAt >= from }.map { it.sessionId }.toSet()

            val relevant = zones.filterKeys { it in ids }.values
            if (relevant.isEmpty()) return@combine emptyMap()

            mapOf(
                HeartRateZone.REST to relevant.sumOf { it.restSeconds },
                HeartRateZone.RECOVERY to relevant.sumOf { it.recoverySeconds },
                HeartRateZone.BASE to relevant.sumOf { it.baseSeconds },
                HeartRateZone.TEMPO to relevant.sumOf { it.tempoSeconds },
                HeartRateZone.THRESHOLD to relevant.sumOf { it.thresholdSeconds },
                HeartRateZone.MAXIMAL to relevant.sumOf { it.maximalSeconds },
            ).filterValues { it > 0 }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /** Kennzahlen ueber den gewaehlten Zeitraum, fuer Kacheln statt Kurven. */
    val periodTotals: StateFlow<PeriodBucket?> = historyBuckets
        .map { buckets ->
            buckets.filter { it.sessionCount > 0 }.takeIf { it.isNotEmpty() }?.let { active ->
                PeriodBucket(
                    period = active.first().period,
                    start = active.first().start,
                    sessionCount = active.sumOf { it.sessionCount },
                    attemptCount = active.sumOf { it.attemptCount },
                    sendCount = active.sumOf { it.sendCount },
                    flashCount = active.sumOf { it.flashCount },
                    climbHeightMeters = active.sumOf { it.climbHeightMeters },
                    caloriesTotal = active.sumOf { it.caloriesTotal },
                    workMs = active.sumOf { it.workMs },
                    totalMs = active.sumOf { it.totalMs },
                    hardestSendValue = active.mapNotNull { it.hardestSendValue }.maxOrNull(),
                    hrAvg = active.mapNotNull { it.hrAvg }.average().takeIf { !it.isNaN() }?.toInt(),
                    hrMax = active.mapNotNull { it.hrMax }.maxOrNull(),
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun selectPeriod(value: Period) {
        historyPeriod.value = value
    }

    private val openSessionId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val detail: StateFlow<SessionDetail?> = openSessionId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                combine(
                    db.sessionDao().observe(id),
                    db.sessionSummaryDao().observeBySession(id),
                    db.attemptDao().observeBySession(id),
                    db.gymDao().observeAll(),
                    db.hrSampleDao().observeBySession(id, SessionMetrics.MIN_HR_ACCURACY),
                ) { session, summary, attempts, allGyms, beats ->
                    session?.let {
                        SessionDetail(
                            session = it,
                            summary = summary,
                            attempts = attempts,
                            gym = allGyms.firstOrNull { gym -> gym.id == it.gymId },
                            heartBeats = beats,
                        )
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun openSession(sessionId: String?) {
        openSessionId.value = sessionId
    }

    // --- Session bearbeiten ---

    fun setRpe(sessionId: String, rpe: Int?) = edit(sessionId) { session ->
        db.sessionDao().upsert(session.copy(rpe = rpe, meta = session.meta.touched(now())))
    }

    fun setNote(sessionId: String, note: String?) = edit(sessionId) { session ->
        db.sessionDao().upsert(
            session.copy(note = note?.takeIf { it.isNotBlank() }, meta = session.meta.touched(now())),
        )
    }

    fun assignGym(sessionId: String, gymId: String?) = edit(sessionId) {
        db.sessionDao().setGym(sessionId, gymId, now())
    }

    /**
     * Loescht eine vergangene Session.
     *
     * Weich, damit die Loeschung bei der Uhr ankommt - eine entfernte Zeile
     * laesst sich nicht uebertragen. Die Zusammenfassung faellt dabei weg, und
     * damit verschwindet der Abend ueberall: Hoehenmeter, Historie, Diagramme
     * lesen alle aus `session_summary`.
     */
    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            val timestamp = now()
            db.sessionDao().softDelete(sessionId, timestamp)
            db.sessionSummaryDao().deleteForSession(sessionId)
            openSessionId.value = null
            sender.sendSession(sessionId)
        }
    }

    // --- Versuche bearbeiten ---

    fun setAttemptOutcome(sessionId: String, attemptId: String, outcome: AttemptOutcome?) =
        editAttempt(sessionId, attemptId) { it.copy(outcome = outcome) }

    fun setAttemptGrade(sessionId: String, attemptId: String, gradeValue: Int?) =
        editAttempt(sessionId, attemptId) { it.copy(gradeValue = gradeValue) }

    fun setAttemptAngle(sessionId: String, attemptId: String, degrees: Int?) =
        editAttempt(sessionId, attemptId) { it.copy(boardAngleDegrees = degrees) }

    /** Bis zu welchem Zug gekommen - fuettert den Projektverlauf. */
    fun setAttemptTopMove(sessionId: String, attemptId: String, move: Int?) =
        editAttempt(sessionId, attemptId) { it.copy(topMoveReached = move?.takeIf { m -> m > 0 }) }

    /**
     * Macht aus einem Versuch eine Zugprobe und zurueck.
     *
     * Der Ausweg, wenn der Knopf auf der Uhr vergessen wurde - und damit muss
     * man ihn dort nicht treffen. Eine Zugprobe faellt aus Versuchszahl,
     * Erfolgsquote und Gradpyramide heraus, zaehlt aber weiter fuer die
     * Kalorien, und die Erholung des Versuchs davor wird neu bestimmt.
     */
    fun setAttemptKind(sessionId: String, attemptId: String, kind: AttemptKind) =
        editAttempt(sessionId, attemptId) { it.copy(kind = kind) }

    /** Weich geloescht: ein hart entfernter Versuch kaeme bei der Uhr nie an. */
    fun deleteAttempt(sessionId: String, attemptId: String) = edit(sessionId) {
        db.attemptDao().softDelete(attemptId, now())
    }

    // --- Boulder ---

    /**
     * Ordnet alle Versuche eines Boulders demselben Eintrag zu.
     *
     * Das ist das Zusammenfuehren: zwei Gruppen auf denselben Boulder gelegt
     * sind ab dann ein Projekt - "diese drei Gruppen waren der blaue 7A".
     */
    fun assignProblem(sessionId: String, attemptIds: List<String>, problemId: String?) =
        edit(sessionId) {
            val timestamp = now()
            attemptIds.forEach { db.attemptDao().setProblem(it, problemId, timestamp) }
        }

    /**
     * Legt einen Boulder an und ordnet ihm die Versuche zu.
     *
     * Ein Boulder haengt immer in einer Halle - deshalb die der Session, sonst
     * die Stammhalle. Gibt es beides nicht, passiert nichts; die Oberflaeche
     * bietet dann vorher das Anlegen einer Halle an.
     */
    fun createProblemFor(
        sessionId: String,
        attemptIds: List<String>,
        label: String,
        gradeValue: Int?,
    ) {
        viewModelScope.launch {
            val session = db.sessionDao().byId(sessionId) ?: return@launch
            val gymId = session.gymId ?: db.gymDao().defaultGym()?.id ?: return@launch
            val timestamp = now()
            val problem = ProblemEntity(
                id = UUID.randomUUID().toString(),
                gymId = gymId,
                label = label.trim(),
                gradeValue = gradeValue,
                meta = RecordMeta.now(timestamp),
            )
            db.problemDao().upsert(problem)
            attemptIds.forEach { db.attemptDao().setProblem(it, problem.id, timestamp) }
            repository.refreshSummary(sessionId)
            sender.sendSession(sessionId)
        }
    }

    // --- Hallen ---

    fun createGym(name: String, makeDefault: Boolean = false) {
        viewModelScope.launch {
            val timestamp = now()
            val gym = GymEntity(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                isDefault = makeDefault || db.gymDao().defaultGym() == null,
                meta = RecordMeta.now(timestamp),
            )
            db.gymDao().upsert(gym)
            if (gym.isDefault) db.gymDao().makeDefault(gym.id, timestamp)
        }
    }

    fun renameGym(gymId: String, name: String) {
        viewModelScope.launch {
            val gym = db.gymDao().byId(gymId) ?: return@launch
            db.gymDao().upsert(gym.copy(name = name.trim(), meta = gym.meta.touched(now())))
        }
    }

    fun makeGymDefault(gymId: String) {
        viewModelScope.launch { db.gymDao().makeDefault(gymId, now()) }
    }

    // --- Profil ---

    fun saveProfile(
        weightKg: Int,
        ageYears: Int,
        sex: BiologicalSex,
        heightCm: Int?,
        restingHrBpm: Int?,
    ) {
        viewModelScope.launch {
            val existing = db.userProfileDao().get()
            val timestamp = now()
            db.userProfileDao().upsert(
                UserProfileEntity(
                    weightKg = ProfileRanges.clampWeight(weightKg),
                    birthYear = LocalDate.now().year - ProfileRanges.clampAge(ageYears),
                    sex = sex,
                    heightCm = heightCm,
                    restingHrBpm = restingHrBpm,
                    // Nicht eingebbar: der hoechste je gemessene Wert gehoert
                    // der Uhr, nicht der Meinung.
                    maxHrBpm = existing?.maxHrBpm,
                    meta = existing?.meta?.touched(timestamp) ?: RecordMeta.now(timestamp),
                ),
            )
            sender.sendProfile()
        }
    }

    // --- Innereien ---

    private fun buildDashboard(list: List<SessionSummaryEntity>): DashboardState {
        val today = LocalDate.now(zone)
        val facts = list.map { it.toFact() }
        val weeks = bucket(facts, Period.WEEK, zone)
        val thisWeekStart = startOfPeriod(today, Period.WEEK)
        val totalHeight = facts.sumOf { it.climbHeightMeters ?: 0.0 }

        val attempts = list.sumOf { it.attemptCount }
        val activeWeeks = weeks.count { it.sessionCount > 0 }

        return DashboardState(
            flashRate = list.sumOf { it.flashCount }.toDouble().takeIf { attempts > 0 }?.div(attempts),
            sendRate = list.sumOf { it.sendCount }.toDouble().takeIf { attempts > 0 }?.div(attempts),
            sessionsPerWeek = list.size.toDouble().takeIf { activeWeeks > 0 }?.div(activeWeeks),
            recentPulse = fillGaps(weeks, Period.WEEK, count = 8, until = today)
                .filter { it.hrAvg != null || it.sessionCount == 0 },
            lastSession = list.maxByOrNull { it.startedAt },
            heights = heightTotals(facts, today, zone),
            landmark = Landmarks.compare(totalHeight),
            recentWeeks = fillGaps(weeks, Period.WEEK, count = 8, until = today),
            thisWeek = weeks.firstOrNull { it.start == thisWeekStart },
            lastWeek = weeks.firstOrNull { it.start == thisWeekStart.minusWeeks(1) },
            sessionCount = list.size,
        )
    }

    /**
     * Speichert lokal, rechnet die Zusammenfassung neu und schickt die Session
     * zur Uhr. Die Reihenfolge ist wichtig - erst sicher ablegen, dann senden.
     */
    private fun edit(sessionId: String, change: suspend (SessionEntity) -> Unit) {
        viewModelScope.launch {
            val session = db.sessionDao().byId(sessionId) ?: return@launch
            change(session)
            repository.refreshSummary(sessionId)
            sender.sendSession(sessionId)
        }
    }

    private fun editAttempt(
        sessionId: String,
        attemptId: String,
        change: (AttemptEntity) -> AttemptEntity,
    ) = edit(sessionId) {
        db.attemptDao().byId(attemptId)?.let { attempt ->
            db.attemptDao().upsert(change(attempt).copy(meta = attempt.meta.touched(now())))
        }
    }

    /**
     * Untergrenzen der fuenf Zonen in Schlaegen.
     *
     * Ohne Profil mit den Annahmen aus [Energy] - dieselben, auf denen auch die
     * Kalorien stehen, damit Zonen und Verbrauch nicht auseinanderlaufen.
     */
    private fun zoneBounds(profile: UserProfileEntity?): List<Int> {
        val body = BodyProfile(
            weightKg = (profile?.weightKg ?: 70).toDouble(),
            ageYears = profile?.ageInYears(LocalDate.now()) ?: 30,
            sex = profile?.sex ?: BiologicalSex.UNSPECIFIED,
            restingHrBpm = profile?.restingHrBpm,
            maxHrBpm = profile?.maxHrBpm,
        )
        val resting = profile?.restingHrBpm ?: Energy.ASSUMED_RESTING_HR
        val max = Energy.maxHeartRate(body)
        return HeartRateZone.entries.drop(1).map { it.lowerBpm(resting, max) }
    }

    private fun now() = System.currentTimeMillis()

    private fun Long.toLocalDate(): LocalDate =
        java.time.Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

    /** Wie viele Zeitraeume die Historie zeigt - so viel, wie quer lesbar bleibt. */
    private fun slotsFor(period: Period): Int = when (period) {
        Period.ALL -> 1
        Period.DAY -> 14
        Period.WEEK -> 12
        Period.MONTH -> 12
        Period.YEAR -> 5
    }
}

private fun SessionSummaryEntity.toFact() = SessionFact(
    startedAt = startedAt,
    attemptCount = attemptCount,
    sendCount = sendCount,
    flashCount = flashCount,
    climbHeightMeters = climbHeightMeters,
    caloriesTotal = caloriesTotal,
    workMs = workMs,
    totalMs = totalMs,
    hardestSendValue = hardestSendValue,
    hrAvg = hrAvg,
    hrMax = hrMax,
)

private fun AttemptEntity.toFact() = AttemptFact(
    gradeValue = gradeValue,
    boardAngleDegrees = boardAngleDegrees,
    startsNewBoulder = startsNewBoulder,
    isSend = outcome == AttemptOutcome.FLASH || outcome == AttemptOutcome.TOP,
    workMs = (endedAt ?: startedAt) - startedAt,
    hrMax = hrMax,
)

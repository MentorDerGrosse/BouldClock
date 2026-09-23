package at.mentor.bouldclockapp.core.metrics

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields

/** Zeitraum, ueber den zusammengefasst wird. */
enum class Period(val displayName: String) {
    DAY("Tage"),
    WEEK("Wochen"),
    MONTH("Monate"),
    YEAR("Jahre"),

    /**
     * Alles in einem Eimer.
     *
     * Ergibt bewusst **kein** Diagramm - ein einzelner Balken ist keines.
     * Die Oberflaeche zeigt dafuer Kennzahlen statt Kurven.
     */
    ALL("Gesamt"),
    ;

    val isSingleBucket: Boolean get() = this == ALL
}

/**
 * Eine abgeschlossene Session, reduziert auf das, was die Historie braucht.
 *
 * Bewusst keine Datenbankzeile: dieselbe Trennung wie bei [AttemptFact]. Das
 * Zusammenfassen ist reine Rechnerei und soll ohne Room und ohne Android
 * testbar bleiben.
 */
data class SessionFact(
    val startedAt: Long,
    val attemptCount: Int,
    val sendCount: Int,
    val flashCount: Int = 0,
    val climbHeightMeters: Double?,
    val caloriesTotal: Double?,
    val workMs: Long,
    val totalMs: Long,
    val hardestSendValue: Int?,
    val hrAvg: Int? = null,
    val hrMax: Int? = null,
)

/** Alle Sessions eines Tages, einer Woche, eines Monats oder eines Jahres. */
data class PeriodBucket(
    val period: Period,

    /** Erster Tag des Zeitraums - Montag, Monatsanfang, 1. Jaenner. */
    val start: LocalDate,
    val sessionCount: Int,
    val attemptCount: Int,
    val sendCount: Int,
    val flashCount: Int,
    val climbHeightMeters: Double,
    val caloriesTotal: Double,
    val workMs: Long,
    val totalMs: Long,
    val hardestSendValue: Int?,

    /** Mittel der Sessionmittel - nicht ueber alle Messwerte gewichtet. */
    val hrAvg: Int? = null,
    val hrMax: Int? = null,
) {
    val sendRate: Double? get() = if (attemptCount > 0) sendCount.toDouble() / attemptCount else null

    /** Anteil der Versuche, die beim ersten Mal durchgingen. */
    val flashRate: Double? get() = if (attemptCount > 0) flashCount.toDouble() / attemptCount else null

    /** Erster Tag nach diesem Zeitraum - zum Filtern von Sessions. */
    val endExclusive: LocalDate
        get() = when (period) {
            Period.ALL -> LocalDate.MAX
            Period.DAY -> start.plusDays(1)
            Period.WEEK -> start.plusWeeks(1)
            Period.MONTH -> start.plusMonths(1)
            Period.YEAR -> start.plusYears(1)
        }
}

/** Kletterhoehe in den ueblichen Zeitraeumen, fuer das Dashboard. */
data class HeightTotals(
    val week: Double,
    val month: Double,
    val year: Double,
    val total: Double,
)

/**
 * Fasst Sessions zu Zeitraeumen zusammen.
 *
 * Aufsteigend sortiert, und **Luecken bleiben Luecken**: eine Woche ohne
 * Session taucht nicht auf. Fuer ein Balkendiagramm ueber die letzten acht
 * Wochen will man die Nullen sehen - dafuer gibt es [fillGaps], damit die
 * Entscheidung beim Aufrufer liegt und nicht hier.
 *
 * Gerechnet wird in der Zeitzone des Geraets: eine Session, die um 23:40 beginnt
 * und um 1:00 endet, gehoert zu dem Tag, an dem sie angefangen hat. Alles andere
 * waere beim Bouldern am Abend dauernd verwirrend.
 */
fun bucket(
    facts: List<SessionFact>,
    period: Period,
    zone: ZoneId = ZoneId.systemDefault(),
): List<PeriodBucket> = facts
    .groupBy { startOfPeriod(it.startedAt.toLocalDate(zone), period) }
    .map { (start, group) -> fold(period, start, group) }
    .sortedBy { it.start }

/**
 * Ergaenzt fehlende Zeitraeume mit leeren Eimern, bis einschliesslich [until].
 *
 * Damit steht in einem Verlauf die Null da, wo nicht geklettert wurde - sonst
 * ruecken zwei Wochen Pause optisch zusammen und der Verlauf luegt.
 */
fun fillGaps(
    buckets: List<PeriodBucket>,
    period: Period,
    count: Int,
    until: LocalDate,
): List<PeriodBucket> {
    val byStart = buckets.associateBy { it.start }
    val last = startOfPeriod(until, period)
    return (count - 1 downTo 0).map { back ->
        val start = shift(last, period, -back.toLong())
        byStart[start] ?: empty(period, start)
    }
}

/** Summen fuer "diese Woche / diesen Monat / dieses Jahr". */
fun heightTotals(
    facts: List<SessionFact>,
    today: LocalDate,
    zone: ZoneId = ZoneId.systemDefault(),
): HeightTotals {
    val weekStart = startOfPeriod(today, Period.WEEK)
    val monthStart = startOfPeriod(today, Period.MONTH)
    val yearStart = startOfPeriod(today, Period.YEAR)

    var week = 0.0
    var month = 0.0
    var year = 0.0
    var total = 0.0

    facts.forEach { fact ->
        val meters = fact.climbHeightMeters ?: return@forEach
        val date = fact.startedAt.toLocalDate(zone)
        total += meters
        if (!date.isBefore(yearStart)) year += meters
        if (!date.isBefore(monthStart)) month += meters
        if (!date.isBefore(weekStart)) week += meters
    }

    return HeightTotals(week = week, month = month, year = year, total = total)
}

/** Erster Tag des Zeitraums, in dem [date] liegt. */
fun startOfPeriod(date: LocalDate, period: Period): LocalDate = when (period) {
    // Ein fester Anker, damit alles in denselben Eimer faellt.
    Period.ALL -> LocalDate.EPOCH
    Period.DAY -> date
    // Montag, nach ISO - nicht die amerikanische Woche ab Sonntag.
    Period.WEEK -> date.with(WeekFields.ISO.dayOfWeek(), 1L)
    Period.MONTH -> date.withDayOfMonth(1)
    Period.YEAR -> date.withDayOfYear(1)
}

private fun shift(start: LocalDate, period: Period, steps: Long): LocalDate = when (period) {
    Period.ALL -> start
    Period.DAY -> start.plusDays(steps)
    Period.WEEK -> start.plusWeeks(steps)
    Period.MONTH -> start.plusMonths(steps)
    Period.YEAR -> start.plusYears(steps)
}

private fun fold(period: Period, start: LocalDate, group: List<SessionFact>) = PeriodBucket(
    period = period,
    start = start,
    sessionCount = group.size,
    attemptCount = group.sumOf { it.attemptCount },
    sendCount = group.sumOf { it.sendCount },
    flashCount = group.sumOf { it.flashCount },
    climbHeightMeters = group.sumOf { it.climbHeightMeters ?: 0.0 },
    caloriesTotal = group.sumOf { it.caloriesTotal ?: 0.0 },
    workMs = group.sumOf { it.workMs },
    totalMs = group.sumOf { it.totalMs },
    hardestSendValue = group.mapNotNull { it.hardestSendValue }.maxOrNull(),
    hrAvg = group.mapNotNull { it.hrAvg }.average().takeIf { !it.isNaN() }?.toInt(),
    hrMax = group.mapNotNull { it.hrMax }.maxOrNull(),
)

private fun empty(period: Period, start: LocalDate) = PeriodBucket(
    period = period,
    start = start,
    sessionCount = 0,
    attemptCount = 0,
    sendCount = 0,
    flashCount = 0,
    climbHeightMeters = 0.0,
    caloriesTotal = 0.0,
    workMs = 0L,
    totalMs = 0L,
    hardestSendValue = null,
)

private fun Long.toLocalDate(zone: ZoneId): LocalDate =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

package at.mentor.bouldclockapp.mobile

import at.mentor.bouldclockapp.core.metrics.Period
import at.mentor.bouldclockapp.core.metrics.PeriodBucket
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val AT = Locale.GERMAN

private val SESSION_DATE = SimpleDateFormat("EEEE, d. MMMM yyyy, HH:mm", AT)
private val SHORT_DATE = SimpleDateFormat("EEE, d. MMM", AT)
private val TIME_ONLY = SimpleDateFormat("HH:mm", AT)

fun formatSessionDate(startedAt: Long): String = SESSION_DATE.format(Date(startedAt))

fun formatShortDate(startedAt: Long): String = SHORT_DATE.format(Date(startedAt))

fun formatTime(startedAt: Long): String = TIME_ONLY.format(Date(startedAt))

/** Ganze Meter ab 100, darunter eine Nachkommastelle - "1247 m" statt "1247,3 m". */
fun formatMeters(meters: Double): String = when {
    meters >= 100.0 -> "${meters.roundToInt()} m"
    else -> String.format(AT, "%.1f m", meters)
}

fun formatKcal(kcal: Double): String = "${kcal.roundToInt()} kcal"

/** "42:07" unter einer Stunde, sonst "1:24:07". */
fun formatDuration(ms: Long): String {
    val total = (ms / 1_000L).coerceAtLeast(0L)
    val hours = total / 3600
    val minutes = (total % 3600) / 60
    val seconds = total % 60
    return if (hours > 0) {
        "$hours:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "$minutes:${seconds.toString().padStart(2, '0')}"
    }
}

/**
 * Grobe Dauer fuer Uebersichten: "1 h 25" statt "1:25:07".
 *
 * Unter einer Minute in Sekunden - "0 min" waere die Behauptung, es sei nichts
 * gewesen.
 */
fun formatDurationShort(ms: Long): String {
    val seconds = (ms / 1_000L).coerceAtLeast(0L)
    val minutes = seconds / 60
    return when {
        minutes >= 60 -> "${minutes / 60} h ${(minutes % 60).toString().padStart(2, '0')}"
        minutes >= 1 -> "$minutes min"
        else -> "$seconds s"
    }
}

/**
 * Beschriftung eines Zeitraums.
 *
 * Bleibt hier und nicht im shared-Modul: Monatsnamen und Wochenzaehlung sind
 * Anzeige, nicht Rechnung. Das Zusammenfassen selbst kennt keine Sprache.
 */
fun label(bucket: PeriodBucket): String = label(bucket.start, bucket.period)

fun label(start: LocalDate, period: Period): String = when (period) {
    Period.DAY -> "${start.dayOfWeek.getDisplayName(TextStyle.SHORT, AT)}, ${start.dayOfMonth}.${start.monthValue}."
    Period.WEEK -> "KW ${start.get(WeekFields.ISO.weekOfWeekBasedYear())}"
    Period.MONTH -> start.month.getDisplayName(TextStyle.FULL, AT)
    Period.YEAR -> start.year.toString()
}

/** Kurzform fuer enge Achsen: "21.9.", "KW 38", "Sep", "2026". */
fun axisLabel(bucket: PeriodBucket): String = when (bucket.period) {
    Period.DAY -> "${bucket.start.dayOfMonth}.${bucket.start.monthValue}."
    Period.WEEK -> bucket.start.get(WeekFields.ISO.weekOfWeekBasedYear()).toString()
    Period.MONTH -> bucket.start.month.getDisplayName(TextStyle.SHORT, AT)
    Period.YEAR -> bucket.start.year.toString()
}

/** "2,4 ×" - der Faktor beim Bauwerksvergleich. */
fun formatTimes(times: Double): String = String.format(AT, "%.1f ×", times)

fun formatPercent(share: Double): String = "${(share * 100).roundToInt()} %"

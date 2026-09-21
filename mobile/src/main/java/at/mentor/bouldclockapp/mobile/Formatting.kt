package at.mentor.bouldclockapp.mobile

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SESSION_DATE = SimpleDateFormat("EEEE, d. MMMM yyyy, HH:mm", Locale.GERMAN)

fun formatSessionDate(startedAt: Long): String = SESSION_DATE.format(Date(startedAt))

fun formatMeters(meters: Double): String = String.format(Locale.GERMAN, "%.1f m", meters)

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

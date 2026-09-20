package at.mentor.bouldclockapp.core.model

import kotlin.math.abs

/**
 * Pausenlaenge als Minuten und Sekunden.
 *
 * Untergrenze eine Minute: darunter laesst sich HRR60 nicht mehr erheben, weil
 * der naechste Versuch ins Messfenster fiele. Die App wuerde dann eine Erholung
 * behaupten, die nie stattgefunden hat.
 *
 * Sekunden in Viertelminuten statt sekundengenau - auf einem Drehkranz will
 * niemand an sechzig Rasten vorbeischrubben, und zwischen 2:15 und 2:20
 * entscheidet sich nichts.
 */
object RestDurations {

    const val MIN_MS: Long = 60_000L

    val MINUTES: List<Int> = (1..20).toList()
    val SECONDS: List<Int> = listOf(0, 15, 30, 45)

    val MAX_MS: Long = of(MINUTES.last(), SECONDS.last())

    fun of(minutes: Int, seconds: Int): Long =
        minutes * 60_000L + seconds * 1_000L

    /** "1:00", "3:30", "20:45" */
    fun format(ms: Long): String {
        val totalSeconds = (ms / 1_000L).coerceAtLeast(0L)
        return "${totalSeconds / 60}:${(totalSeconds % 60).toString().padStart(2, '0')}"
    }

    fun clamp(ms: Long): Long = ms.coerceIn(MIN_MS, MAX_MS)

    fun minutesOf(ms: Long): Int = (clamp(ms) / 60_000L).toInt()

    /** Naechstgelegene Viertelminute - gespeicherte Werte muessen nicht auf dem Raster liegen. */
    fun secondsOf(ms: Long): Int {
        val seconds = ((clamp(ms) % 60_000L) / 1_000L).toInt()
        return SECONDS.minBy { abs(it - seconds) }
    }

    fun minuteIndexOf(ms: Long): Int = MINUTES.indexOf(minutesOf(ms)).coerceAtLeast(0)

    fun secondIndexOf(ms: Long): Int = SECONDS.indexOf(secondsOf(ms)).coerceAtLeast(0)

    /**
     * Was die gewaehlte Pause trainiert.
     *
     * Die haeufigste stille Fehlentscheidung im Bouldern: man will an
     * Maximalkraft arbeiten, pausiert eine Minute und trainiert versehentlich
     * Kraftausdauer.
     */
    fun trainingHint(ms: Long): String = when {
        ms < 150_000L -> "Kraftausdauer"
        ms < 300_000L -> "gemischt"
        else -> "Maximalkraft"
    }
}

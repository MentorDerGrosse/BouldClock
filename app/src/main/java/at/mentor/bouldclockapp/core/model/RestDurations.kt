package at.mentor.bouldclockapp.core.model

/**
 * Auswahlwerte fuer den Pausen-Timer.
 *
 * Bewusst eine kuratierte Liste statt eines Minuten/Sekunden-Rades: auf der Uhr
 * zaehlt jede Interaktion, und die Aufloesung ist dort fein, wo sie etwas
 * aendert. Unter drei Minuten entscheiden 30 Sekunden darueber, ob man Kraft
 * oder Kraftausdauer trainiert - bei Limit-Versuchen ist der Unterschied
 * zwischen 7:00 und 7:30 belanglos.
 *
 * Erweitern heisst: Werte in [OPTIONS] ergaenzen, sonst nichts.
 */
object RestDurations {

    val OPTIONS: List<Long> = buildList {
        for (seconds in 30..180 step 30) add(seconds * 1_000L)
        for (minutes in 4..10) add(minutes * 60_000L)
        add(12 * 60_000L)
        add(15 * 60_000L)
    }

    val MIN_MS: Long = OPTIONS.first()
    val MAX_MS: Long = OPTIONS.last()

    /** "0:30", "3:00", "15:00" */
    fun format(ms: Long): String {
        val totalSeconds = (ms / 1_000L).coerceAtLeast(0L)
        return "${totalSeconds / 60}:${(totalSeconds % 60).toString().padStart(2, '0')}"
    }

    /**
     * Naechstgelegener Listeneintrag. Noetig, weil gespeicherte Werte aus einer
     * aelteren [OPTIONS]-Liste stammen koennen.
     */
    fun nearestIndex(ms: Long): Int =
        OPTIONS.indices.minBy { kotlin.math.abs(OPTIONS[it] - ms) }

    fun snap(ms: Long): Long = OPTIONS[nearestIndex(ms)]
}

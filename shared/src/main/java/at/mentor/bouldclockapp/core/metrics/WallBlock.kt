package at.mentor.bouldclockapp.core.metrics

/**
 * Ein Block Zeit an der Wand.
 *
 * Versuch oder Zugprobe - fuer Energie und Erholung zaehlt beides gleich, fuer
 * die Statistik nicht. Reduziert auf das Noetige, damit die Rechnung ohne Room
 * und ohne Android pruefbar bleibt; dasselbe Muster wie [AttemptFact].
 */
data class WallBlock(
    val startedAt: Long,
    val endedAt: Long,
    val isAttempt: Boolean,
) {
    val durationMs: Long get() = (endedAt - startedAt).coerceAtLeast(0L)

    fun contains(timestampMs: Long): Boolean = timestampMs in startedAt..endedAt
}

/**
 * Ein Pulsmesswert.
 *
 * Bereits auf brauchbare Genauigkeit gefiltert - siehe
 * [SessionMetrics.MIN_HR_ACCURACY]. Was hier ankommt, wird geglaubt.
 */
data class HeartBeat(val timestampMs: Long, val bpm: Int)

package at.mentor.bouldclockapp.core.metrics

import kotlin.math.abs

/** Herzfrequenz-Erholung eines Versuchs. */
data class Hrr60(val hrEnd: Int, val hrAfter60s: Int) {
    /** Abfall in Schlaegen. Hoeher ist besser. */
    val drop: Int get() = hrEnd - hrAfter60s
}

/**
 * Erholung nach einem Versuch, aus den aufgezeichneten Pulswerten.
 *
 * **Am Sessionende gerechnet, nicht waehrend der Session.** Frueher hing das an
 * einem verzoegerten Auftrag, der abgebrochen wurde, sobald sich die Phase
 * aenderte - ein Wettlauf. In einer echten Session vom 21.09.2026 fehlte der
 * Wert deshalb bei zwei von zehn Versuchen, einmal sogar nach 216 Sekunden
 * Pause, obwohl lueckenlose Pulswerte vorlagen.
 *
 * Am Ende liegen alle Pulswerte und alle Blockzeiten vor. Dann ist die Frage
 * eindeutig zu beantworten, deterministisch und nachtraeglich wiederholbar.
 */
object Recovery {

    /**
     * Erholung nach [block], oder `null`, wenn sie nicht ehrlich messbar ist.
     *
     * Nicht messbar heisst: in den 60 Sekunden danach wurde wieder geklettert -
     * ein neuer Versuch **oder eine Zugprobe**. Dann misst man den naechsten
     * Block mit, und die Zahl behauptet eine Erholung, die nie stattfand.
     * Lieber keine Zahl als eine falsche; darauf baut der Fitnesstrend auf.
     */
    fun after(
        block: WallBlock,
        blocks: List<WallBlock>,
        beats: List<HeartBeat>,
        toleranceMs: Long = SessionMetrics.HR_MATCH_TOLERANCE_MS,
    ): Hrr60? {
        if (!block.isAttempt) return null

        val windowEnd = block.endedAt + SessionMetrics.HRR_WINDOW_MS
        val interrupted = blocks.any { other ->
            other !== block && other.startedAt > block.startedAt && other.startedAt < windowEnd
        }
        if (interrupted) return null

        val hrEnd = nearest(beats, block.endedAt, toleranceMs) ?: return null
        val hrAfter = nearest(beats, windowEnd, toleranceMs) ?: return null
        return Hrr60(hrEnd = hrEnd, hrAfter60s = hrAfter)
    }

    /** Alle messbaren Erholungswerte einer Session, je Versuch. */
    fun forAll(
        blocks: List<WallBlock>,
        beats: List<HeartBeat>,
        toleranceMs: Long = SessionMetrics.HR_MATCH_TOLERANCE_MS,
    ): Map<WallBlock, Hrr60> = blocks
        .mapNotNull { block -> after(block, blocks, beats, toleranceMs)?.let { block to it } }
        .toMap()

    /** Der Pulswert, der [at] am naechsten liegt - sofern nah genug. */
    private fun nearest(beats: List<HeartBeat>, at: Long, toleranceMs: Long): Int? = beats
        .minByOrNull { abs(it.timestampMs - at) }
        ?.takeIf { abs(it.timestampMs - at) <= toleranceMs }
        ?.bpm
}

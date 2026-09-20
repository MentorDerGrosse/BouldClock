package at.mentor.bouldclockapp.core.metrics

/**
 * Die Rechenregeln der Session - bewusst reine Funktionen auf Primitiven,
 * ohne Room und ohne Android. Hier liegt die Fachlichkeit, die stimmen muss.
 */
object SessionMetrics {

    /** Messfenster der Herzfrequenz-Erholung. */
    const val HRR_WINDOW_MS: Long = 60_000L

    /** Wie weit ein Pulswert vom gesuchten Zeitpunkt abweichen darf. */
    const val HR_MATCH_TOLERANCE_MS: Long = 5_000L

    /**
     * Mindestgenauigkeit, ab der ein Pulswert verwendet wird.
     *
     * Skala von Health Services: 0 unbekannt, 1 kein Hautkontakt, 2 unzuverlaessig,
     * 3 niedrig, 4 mittel, 5 hoch. Ab 3 wird gerechnet - waehrend eines Zuges
     * liefert der optische Sensor am Handgelenk regelmaessig Mist (gebeugtes
     * Gelenk, kontrahierter Unterarm, Griffdruck), und "niedrig" ist dort schon
     * das Erreichbare.
     */
    const val MIN_HR_ACCURACY: Int = 3

    /**
     * Herzfrequenz-Erholung ueber 60 s, in Schlaegen.
     *
     * Gibt `null` zurueck, wenn der Wert nicht ehrlich messbar ist - insbesondere
     * wenn der naechste Versuch vor Ablauf des Fensters startete. Dann misst man
     * den naechsten Burn mit, und die Zahl behauptet eine Erholung, die nie stattfand.
     * Lieber keine Zahl als eine falsche: darauf baut spaeter der Fitnesstrend auf.
     */
    fun hrr60(hrEnd: Int?, hrAfter60s: Int?, restAfterMs: Long?): Int? {
        if (hrEnd == null || hrAfter60s == null) return null
        if (restAfterMs != null && restAfterMs < HRR_WINDOW_MS) return null
        return hrEnd - hrAfter60s
    }

    /**
     * Zerlegt die Bruttodauer in Wand-, Pausen- und Auszeit.
     *
     * `rest` ist der Rest und wird nicht gemessen, sondern gerechnet: alles, was
     * weder an der Wand noch Auto-Pause war. Die Werte werden gegeneinander
     * begrenzt, damit gerundete Sensorzeiten keine negativen Dauern erzeugen.
     */
    fun timeSplit(totalMs: Long, workMs: Long, pausedMs: Long): TimeSplit {
        val total = totalMs.coerceAtLeast(0L)
        val work = workMs.coerceIn(0L, total)
        val paused = pausedMs.coerceIn(0L, total - work)
        return TimeSplit(
            totalMs = total,
            workMs = work,
            pausedMs = paused,
            restMs = total - work - paused,
        )
    }
}

data class TimeSplit(
    val totalMs: Long,
    val workMs: Long,
    val pausedMs: Long,
    val restMs: Long,
) {
    /** Pause je Sekunde an der Wand. 1:5 ist Maximalkraft, 1:1 ist Kraftausdauer. */
    val restPerWork: Double? get() = if (workMs > 0L) restMs.toDouble() / workMs else null
}

package at.mentor.bouldclockapp.core.metrics

/**
 * Pulszone - ein Bereich zwischen Ruhe- und Maximalpuls.
 *
 * Die Grenzen sind Anteile der **Pulsreserve** (Karvonen), nicht des
 * Maximalpuls: so passen sie sich dem einzelnen Herzen an, statt einem
 * Durchschnittsmenschen zu folgen. Dieselbe Groesse, auf der auch die
 * Kalorienrechnung steht, siehe [Energy].
 */
enum class HeartRateZone(
    val displayName: String,
    val description: String,
    /** Untergrenze als Anteil der Pulsreserve. */
    val lowerBound: Double,
) {
    REST("Ruhe", "Stehen, chalken, zuschauen", 0.0),
    RECOVERY("Erholung", "Lockeres Bewegen zwischen den Versuchen", 0.5),
    BASE("Grundlage", "Leichte Dauerbelastung", 0.6),
    TEMPO("Tempo", "Zügiges Klettern, viele Züge am Stück", 0.7),
    THRESHOLD("Schwelle", "Hart, aber haltbar", 0.8),
    MAXIMAL("Maximal", "Ans Limit", 0.9),
    ;

    companion object {
        /** Die Zone, in die [bpm] faellt. */
        fun of(bpm: Int, restingHr: Int, maxHr: Int): HeartRateZone {
            if (maxHr <= restingHr) return REST
            val reserve = (bpm - restingHr).toDouble() / (maxHr - restingHr)
            return entries.last { reserve >= it.lowerBound }
        }
    }
}

/** Untergrenze einer Zone in Schlaegen - fuers Beschriften. */
fun HeartRateZone.lowerBpm(restingHr: Int, maxHr: Int): Int =
    (restingHr + lowerBound * (maxHr - restingHr)).toInt()

/**
 * Wie lange in welcher Zone.
 *
 * Gerechnet ueber die Abstaende zwischen den Messwerten - bei sekuendlichem
 * Puls ist ein Messwert ungefaehr eine Sekunde, aber eben nur ungefaehr, und
 * Luecken duerfen nicht als Belastung zaehlen. Deshalb wird gemessen statt
 * gezaehlt, mit gekappten Luecken.
 */
fun timeInZones(
    beats: List<HeartBeat>,
    restingHr: Int,
    maxHr: Int,
    maxGapMs: Long = 5_000L,
): Map<HeartRateZone, Long> {
    if (beats.size < 2) return emptyMap()

    val result = mutableMapOf<HeartRateZone, Long>()
    for (i in 1..beats.lastIndex) {
        val span = (beats[i].timestampMs - beats[i - 1].timestampMs).coerceIn(0L, maxGapMs)
        val zone = HeartRateZone.of(beats[i].bpm, restingHr, maxHr)
        result[zone] = (result[zone] ?: 0L) + span
    }
    return result
}

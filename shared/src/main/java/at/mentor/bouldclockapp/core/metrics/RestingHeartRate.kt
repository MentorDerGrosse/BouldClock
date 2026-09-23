package at.mentor.bouldclockapp.core.metrics

/**
 * Ruhepuls aus einer kurzen Messung.
 *
 * Selbst gemessen statt von der Plattform uebernommen: Samsungs Nachtwert ist
 * eine fremde Auswertung, Garmins waere eine andere, und die Kalorienrechnung
 * bekaeme dieselbe Naht wie bei den Kalorien selbst. Was hier entsteht, ist auf
 * jeder Uhr dieselbe Zahl - gebraucht wird nur ein roher Pulswert.
 *
 * Er geht doppelt in die Energierechnung ein: in die Pulsreserve und ueber
 * [Energy.vo2Max] in die geschaetzte Ausdauer. Deshalb lohnt die Sorgfalt.
 */
object RestingHeartRate {

    /** Wie lange gemessen wird. Kuerzer erwischt die Beruhigung nicht. */
    const val MEASURE_SECONDS: Int = 120

    /** Darunter ist das Ergebnis nicht belastbar. */
    const val MIN_SAMPLES: Int = 20

    /** So viele der niedrigsten Werte bilden das Ergebnis. */
    private const val LOWEST_WINDOW: Int = 10

    /**
     * Der Ruhepuls aus den gemessenen Werten.
     *
     * Der Median der niedrigsten zehn, nicht das Minimum: ein einzelner
     * Ausreisser nach unten - ein verrutschter Sensor, eine Bewegung - wuerde
     * sonst den ganzen Wert bestimmen, und der Fehler ginge doppelt in die
     * Kalorien ein.
     *
     * `null`, wenn zu wenige Werte vorliegen. Lieber weiter schaetzen als eine
     * schlechte Messung festschreiben.
     */
    fun fromSamples(bpm: List<Int>): Int? {
        if (bpm.size < MIN_SAMPLES) return null

        val lowest = bpm.sorted().take(LOWEST_WINDOW)
        val middle = lowest.size / 2
        return if (lowest.size % 2 == 0) {
            (lowest[middle - 1] + lowest[middle]) / 2
        } else {
            lowest[middle]
        }
    }
}

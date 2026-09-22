package at.mentor.bouldclockapp.core.metrics

/**
 * Hoehe aus dem Luftdruckverlauf.
 *
 * Auf der Galaxy Watch gemessen: zwischen zwei Messpunkten rauscht der Sensor um
 * rund 0,01 hPa, das sind **8 Zentimeter**. Ueber Minuten driftet er dagegen um
 * etwa einen Meter.
 *
 * Daraus folgt die ganze Bauart hier: die Hoehe wird **je Versuch** bestimmt,
 * nie als absoluter Wert ueber den Abend. Innerhalb der zehn bis dreissig
 * Sekunden eines Versuchs ist die Drift vernachlaessigbar, ueber zwei Stunden
 * waere sie groesser als der Boulder.
 */
object Barometry {

    /** Naeherung nahe Meereshoehe. Genauer ginge es, aber nicht sinnvoller. */
    const val METERS_PER_HPA: Double = 8.3

    /** Unter dem gilt es als Rauschen, nicht als Boulder. */
    const val MIN_PLAUSIBLE_METERS: Double = 0.5

    /** Darueber war es kein Boulder mehr - vermutlich ein Sensorsprung. */
    const val MAX_PLAUSIBLE_METERS: Double = 20.0

    /** So viele Messpunkte werden zu einem robusten Wert zusammengefasst. */
    private const val ROBUST_WINDOW = 3

    /**
     * Erreichte Hoehe waehrend eines Versuchs, in Metern.
     *
     * Ausgangspunkt ist der Druck am Anfang, Gipfel der niedrigste Druck im
     * Fenster. Beide werden ueber drei Messpunkte gemittelt, damit ein einzelner
     * Ausreisser nicht vierzig Zentimeter erfindet.
     *
     * Gibt `null` zurueck, wenn zu wenige Messpunkte vorliegen oder das Ergebnis
     * unplausibel ist - lieber keine Zahl als eine erfundene.
     */
    fun climbHeightMeters(
        pressureHpa: List<Double>,
    ): Double? {
        if (pressureHpa.size < ROBUST_WINDOW * 2) return null

        val baseline = pressureHpa.take(ROBUST_WINDOW).median()
        val peak = pressureHpa.sorted().take(ROBUST_WINDOW).median()

        val meters = (baseline - peak) * METERS_PER_HPA
        return meters.takeIf { it in MIN_PLAUSIBLE_METERS..MAX_PLAUSIBLE_METERS }
    }

    private fun List<Double>.median(): Double {
        val sorted = sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[middle - 1] + sorted[middle]) / 2.0
        } else {
            sorted[middle]
        }
    }
}

package at.mentor.bouldclockapp.core.model

/** Ein Bauwerk oder Berg als Massstab fuer Kletterhoehe. */
data class Landmark(val name: String, val heightMeters: Double)

/**
 * Macht aus einer Zahl ein Bild.
 *
 * "347 Höhenmeter" sagt niemandem etwas; "zweieinhalb Mal der Stephansdom"
 * schon. Die Leiter beginnt bewusst niedrig - nach einem einzigen Abend soll
 * schon etwas dastehen und nicht "0,04 Mal der Großglockner".
 *
 * Oesterreichisch gefaerbt, weil die App fuer den Autor und seine Freunde ist.
 */
object Landmarks {

    val ALL: List<Landmark> = listOf(
        Landmark("Kletterwand", 4.5),
        Landmark("Einfamilienhaus", 8.0),
        Landmark("Riesenrad", 64.8),
        Landmark("Stephansdom", 136.4),
        Landmark("Donauturm", 252.0),
        Landmark("Eiffelturm", 330.0),
        Landmark("Burj Khalifa", 828.0),
        Landmark("Schneeberg", 2076.0),
        Landmark("Dachstein", 2995.0),
        Landmark("Großglockner", 3798.0),
        Landmark("Mont Blanc", 4808.0),
        Landmark("Kilimandscharo", 5895.0),
        Landmark("Mount Everest", 8849.0),
    )

    /**
     * Das groesste Bauwerk, das in [meters] hineinpasst, und das naechste Ziel.
     *
     * [reached] ist null, solange nicht einmal das kleinste erreicht ist -
     * dann steht eben noch nichts da, statt einer beschoenigenden Null.
     */
    fun compare(meters: Double): LandmarkComparison {
        val reached = ALL.lastOrNull { meters >= it.heightMeters }
        val next = ALL.firstOrNull { meters < it.heightMeters }
        return LandmarkComparison(
            meters = meters,
            reached = reached,
            times = reached?.let { meters / it.heightMeters } ?: 0.0,
            next = next,
            missingMeters = next?.let { it.heightMeters - meters } ?: 0.0,
        )
    }
}

data class LandmarkComparison(
    val meters: Double,
    val reached: Landmark?,
    val times: Double,
    val next: Landmark?,
    val missingMeters: Double,
) {
    /** Anteil am naechsten Ziel, 0..1 - fuer einen Fortschrittsbalken. */
    val progressToNext: Float
        get() = next?.let { (meters / it.heightMeters).coerceIn(0.0, 1.0).toFloat() } ?: 1f
}

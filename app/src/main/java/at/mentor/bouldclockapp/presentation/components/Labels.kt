package at.mentor.bouldclockapp.presentation.components

/**
 * Beschriftungen, die an mehreren Stellen gleich lauten muessen.
 *
 * Die Einzahl stand zuerst nur in der Zusammenfassung, im Fortschritt-Fenster
 * hiess es dann "1 Versuche". Solche Dinge fallen nur auf, wenn man sie auf dem
 * Geraet sieht - deshalb hier einmal statt dreimal.
 */
fun attemptLabel(count: Int): String =
    if (count == 1) "1 Versuch" else "$count Versuche"

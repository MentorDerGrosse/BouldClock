package at.mentor.bouldclockapp.core.text

/**
 * Beschriftungen, die auf Uhr und Handy gleich lauten muessen.
 *
 * Die Einzahl stand zuerst nur in der Sessionzusammenfassung, dann fehlte sie im
 * Fortschritt-Fenster, dann in der Handy-App - dreimal derselbe Fehler, weil die
 * Funktion zweimal am falschen Ort lag. Hier sieht sie jeder.
 */
fun attemptLabel(count: Int): String =
    if (count == 1) "1 Versuch" else "$count Versuche"

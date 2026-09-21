package at.mentor.bouldclockapp.core.text

/**
 * Beschriftungen, die auf Uhr und Handy gleich lauten muessen.
 *
 * Die Einzahl stand zuerst nur in der Sessionzusammenfassung, dann fehlte sie im
 * Fortschritt-Fenster, dann in der Handy-App - dreimal derselbe Fehler, weil die
 * Funktion zweimal am falschen Ort lag. Hier sieht sie jeder.
 */
fun attemptLabel(count: Int): String = "$count ${attemptNoun(count)}"

/**
 * Nur das Wort, ohne Zahl.
 *
 * Fuer Kacheln, in denen die Zahl schon gross darueber steht - dort waere
 * "1 Versuche" derselbe Fehler noch einmal, nur getrennt gesetzt.
 */
fun attemptNoun(count: Int): String = if (count == 1) "Versuch" else "Versuche"

fun sessionNoun(count: Int): String = if (count == 1) "Session" else "Sessions"

fun topNoun(count: Int): String = if (count == 1) "Top" else "Tops"


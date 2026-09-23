package at.mentor.bouldclockapp.mobile.detail

/**
 * Eine Groesse, die sich ueber Zeitraeume anschauen laesst.
 *
 * Traegt ihre eigene Erklaerung: was gemessen wird, in welcher Einheit und
 * warum. Eine Zahl ohne diese drei Angaben ist nur Dekoration - und bei
 * Kletterhoehe aus Luftdruck oder selbst gerechneten Kalorien ist die Herkunft
 * kein Nebensatz, sondern die halbe Information.
 */
enum class Metric(
    val title: String,
    val unit: String,
    val what: String,
    val why: String,

    /**
     * Pegel statt Menge.
     *
     * Ein Balken behauptet eine Menge ab null - ein Puls von 126 ist keine
     * Menge. Solche Groessen bekommen eine Linie.
     */
    val isLevel: Boolean = false,
) {
    HEIGHT(
        title = "Höhenmeter",
        unit = "Meter",
        what = "Die Summe der Höhen, die du in den einzelnen Versuchen erreicht hast.",
        why = "Gemessen über den Luftdruck während jedes Versuchs – je Versuch, " +
            "weil der Sensor über Minuten um etwa einen Meter driftet, über " +
            "Sekunden aber kaum. Bei einem Sturz ist das zugleich deine Fallhöhe.",
    ),

    VOLUME(
        title = "Volumen",
        unit = "Versuche",
        what = "Wie viele Versuche du gemacht hast und wie viele davon durchgingen.",
        why = "Die schlichteste Belastungsgröße, die es gibt. Zugproben zählen " +
            "nicht mit – die sind Arbeit, aber keine Versuche.",
    ),

    CALORIES(
        title = "Kalorien",
        unit = "kcal",
        what = "Der geschätzte Verbrauch, inklusive Nachbrennen in den Pausen.",
        why = "Selbst gerechnet aus deinem Puls und deinem Profil, nicht von der " +
            "Uhr übernommen – die meldete in einer echten Session durchgehend " +
            "null. Beim Bouldern kostet ein Boulder in der Pause mehr als an " +
            "der Wand: zwanzig Sekunden am Limit laufen anaerob, bezahlt wird danach.\n\n" +
            "Der Überschuss über dem Ruheverbrauch klingt nach dem Absteigen mit " +
            "einer Zeitkonstante von 2,5 Minuten ab. Das ist der einzige Wert im " +
            "Modell, der sich nicht aus deinen Daten ableiten lässt – er " +
            "beschreibt ja gerade, wie stark der Puls in der Erholung lügt.",
    ),

    PULSE(
        title = "Puls",
        unit = "bpm",
        what = "Der mittlere Puls je Zeitraum, dazu die höchste Spitze.",
        why = "Sekündlich gemessen. Der Schnitt sagt, wie intensiv der Abend " +
            "insgesamt war; die Spitze, wie weit du hochgegangen bist.",
        isLevel = true,
    ),

    FALLS(
        title = "Stürze",
        unit = "Anzahl und Meter",
        what = "Wie oft du abgeflogen bist und aus welcher Höhe zusammengerechnet.",
        why = "Ein Sturz ist ein protokollierter Fehlversuch – nicht aus dem " +
            "Beschleunigungssensor erkannt. Das wurde an echten Daten geprüft " +
            "und verworfen: beim Bouldern fällt man nicht passiv, das " +
            "Handgelenk ist durchgehend aktiv beschleunigt.\n\n" +
            "Die Fallhöhe ist die gemessene Kletterhöhe – man fällt vom " +
            "höchsten Punkt, es ist dieselbe Zahl unter anderem Namen.",
    ),
    ;
}

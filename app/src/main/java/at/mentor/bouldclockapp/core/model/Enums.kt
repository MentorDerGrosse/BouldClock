package at.mentor.bouldclockapp.core.model

/**
 * ACHTUNG: Alle Enums hier werden von Room als Konstantenname (String) persistiert.
 * Die Namen sind damit Teil des Datenbankvertrags - Umbenennen erfordert eine Migration.
 * Neue Werte hinzufuegen ist dagegen jederzeit gefahrlos, ebenso neue
 * Konstruktorparameter wie [SessionType.defaultRestMs].
 */

enum class SessionType(
    val displayName: String,
    /**
     * Voreingestellte Pausenlaenge. Keine Kosmetik, sondern der Unterschied
     * zwischen den Trainingsformen: kurze Pausen trainieren Kraftausdauer,
     * lange Pausen Maximalkraft. Ueberschreibbar in den Einstellungen.
     */
    val defaultRestMs: Long,
) {
    /** Normales Bouldern, kein Protokoll. */
    FREE("Frei", 3 * 60_000L),

    /** Viele leichte Boulder, kurze Pausen. */
    VOLUME("Volumen", 60_000L),

    /** Wenige Maximalversuche - hier sind 5 min Pause der Sinn der Uebung. */
    LIMIT("Limit", 5 * 60_000L),

    /** Ein Projekt, viele Burns. */
    PROJECT("Projekt", 4 * 60_000L),

    /**
     * Pause selbst gewaehlt. Eigener Typ und nicht bloss ein abweichender Wert,
     * damit Vergleiche ehrlich bleiben - eine Session mit selbstgesetzter Pause
     * ist mit einer Limit-Session nicht ohne Weiteres vergleichbar.
     */
    CUSTOM("Benutzerdefiniert", 3 * 60_000L),

    /**
     * Wettkampf. Bewusst anders gebaut als der Rest: keine Grade, kein
     * Pausenziel, kein Zwischenschritt. Wettkampfformate sehen jedes Mal anders
     * aus, deshalb konfiguriert der Modus nichts - er zaehlt nur mit.
     *
     * Der Wert hier wird nie angezeigt; die Pause ist im Wettkampf uninteressant.
     */
    COMPETITION("Wettkampf", 4 * 60_000L),

    /**
     * Verstellbares Board - Kilter, Tension, Moon. Fragt vor dem Grad den
     * Neigungswinkel ab, weil derselbe Boulder bei 25 und bei 45 Grad zwei
     * verschiedene Schwierigkeiten sind.
     */
    KILTERBOARD("Kilterboard", 3 * 60_000L),
    ;

    /** Laeuft ohne Gradabfrage und ohne Pausenziel. */
    val isCompetition: Boolean get() = this == COMPETITION

    /** Fragt zusaetzlich den Boardwinkel ab. */
    val hasBoardAngle: Boolean get() = this == KILTERBOARD
}

enum class SessionState {
    ACTIVE,
    PAUSED,
    FINISHED,

    /** Nie sauber beendet und beim Wiederherstellen verworfen. */
    ABANDONED,
    ;

    val isOpen: Boolean get() = this == ACTIVE || this == PAUSED
}

enum class AttemptOutcome(val displayName: String) {
    /**
     * Top im ersten Versuch. Bewusst ein eigener Wert statt abgeleitet: ohne
     * identifizierten Boulder laesst sich "erster Versuch" nicht rekonstruieren.
     */
    FLASH("Flash"),
    TOP("Top"),

    /**
     * Wettkampf-Zone erreicht, aber kein Top.
     *
     * Bewusst nicht in der normalen Bedienung: Hallenboulder haben keine Zone,
     * das ist eine Wertung aus dem Wettkampf. Gehoert spaeter in einen eigenen
     * Wettkampfmodus, nicht neben Flash und Top in den Alltag.
     */
    ZONE("Zone"),
    FAIL("Sturz"),
    ;

    val isSend: Boolean get() = this == FLASH || this == TOP
}

enum class WallAngle(val displayName: String) {
    SLAB("Platte"),
    VERTICAL("Senkrecht"),
    SLIGHT_OVERHANG("Leicht überhängend"),
    STEEP("Steil"),
    ROOF("Dach"),
}

enum class SensorKind { ACCELEROMETER, GYROSCOPE, PRESSURE }

/**
 * Fortlaufend mitgezaehlte Messgroessen einer Session.
 *
 * Alle nach demselben Muster: ein monoton steigender Stand, den Health Services
 * meldet. Deshalb eine gemeinsame Tabelle statt einer je Groesse - Distanz oder
 * Schritte kaemen sonst als dritte und vierte dazu.
 */
enum class SessionMetric(val displayName: String, val unit: String) {
    CALORIES("Kalorien", "kcal"),

    /** Summe der ueberwundenen Hoehenmeter - beim Bouldern die Kletterhoehe. */
    ELEVATION_GAIN("Kletterhöhe", "m"),
}

/**
 * Biologisches Geschlecht - einziger Zweck ist der Grundumsatz in der
 * Kalorienformel.
 *
 * [UNSPECIFIED] rechnet mit einem Mittelwert. Wer das nicht angeben will, soll
 * die App trotzdem benutzen koennen; der Fehler daraus ist kleiner als der, den
 * die Kalorienschaetzung beim Bouldern ohnehin hat.
 */
enum class BiologicalSex(val displayName: String) {
    FEMALE("Weiblich"),
    MALE("Männlich"),
    UNSPECIFIED("Keine Angabe"),
}

enum class SyncState {
    /** Liegt nur lokal, muss noch aufs Handy. */
    PENDING,
    SYNCED,
}

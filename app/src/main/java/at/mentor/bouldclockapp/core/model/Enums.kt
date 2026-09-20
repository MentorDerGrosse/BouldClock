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
    SLIGHT_OVERHANG("Leicht ueberhaengend"),
    STEEP("Steil"),
    ROOF("Dach"),
}

enum class SensorKind { ACCELEROMETER, GYROSCOPE, PRESSURE }

enum class SyncState {
    /** Liegt nur lokal, muss noch aufs Handy. */
    PENDING,
    SYNCED,
}

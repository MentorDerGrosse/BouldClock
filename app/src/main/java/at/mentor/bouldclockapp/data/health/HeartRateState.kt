package at.mentor.bouldclockapp.data.health

/**
 * Warum gerade kein Puls dasteht.
 *
 * Ohne diese Unterscheidung zeigt die Live-Seite in drei voellig verschiedenen
 * Lagen denselben Strich: die Messung laeuft gerade an, die Uhr sitzt nicht am
 * Handgelenk, oder der Sensor liefert grundsaetzlich nichts. Nur die erste geht
 * von selbst vorbei - das sollte man sehen koennen.
 */
enum class HeartRateState {
    /** Angefordert, aber noch kein Wert - der Sensor braucht ein paar Sekunden. */
    STARTING,

    /** Werte kommen. */
    MEASURING,

    /** Kein Hautkontakt. */
    OFF_BODY,

    /** Der Sensor meldet sich ab. */
    UNAVAILABLE,
}

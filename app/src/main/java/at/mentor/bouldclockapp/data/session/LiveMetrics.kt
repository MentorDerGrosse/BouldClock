package at.mentor.bouldclockapp.data.session

import at.mentor.bouldclockapp.data.health.HeartRateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Laufende Messwerte der Session, zum Anzeigen.
 *
 * Bewusst ein einzelnes Objekt und keine Abhaengigkeitsinjektion: der
 * Aufzeichnungsdienst und die Oberflaeche sind zwei getrennte Prozessteile, die
 * sich sonst nur ueber die Datenbank erreichen wuerden - und dort ist zu
 * langsam, was sekuendlich blinken soll. Persistiert wird trotzdem alles
 * ordentlich in `hr_sample` und `metric_sample`; das hier ist nur die Anzeige.
 *
 * Gespeist wird es direkt aus dem Rueckruf der Uhr, nicht aus der
 * Schreibschleife: die laeuft alle zehn Sekunden, und so lange will niemand auf
 * seinen Puls warten.
 */
object LiveMetrics {

    private val _bpm = MutableStateFlow<Int?>(null)
    val bpm: StateFlow<Int?> = _bpm.asStateFlow()

    /** Warum gerade kein Puls dasteht. */
    private val _heartRate = MutableStateFlow(HeartRateState.STARTING)
    val heartRate: StateFlow<HeartRateState> = _heartRate.asStateFlow()

    private val _kcal = MutableStateFlow<Double?>(null)
    val kcal: StateFlow<Double?> = _kcal.asStateFlow()

    /** Bisher in dieser Session geklettene Hoehe in Metern. */
    private val _elevationGainMeters = MutableStateFlow<Double?>(null)
    val elevationGainMeters: StateFlow<Double?> = _elevationGainMeters.asStateFlow()

    fun update(
        bpm: Int?,
        heartRate: HeartRateState,
        kcal: Double?,
        elevationGainMeters: Double?,
    ) {
        _bpm.value = bpm
        _heartRate.value = heartRate
        _kcal.value = kcal
        _elevationGainMeters.value = elevationGainMeters
    }

    fun clear() = update(null, HeartRateState.STARTING, null, null)
}

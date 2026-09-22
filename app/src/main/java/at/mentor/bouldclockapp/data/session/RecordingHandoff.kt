package at.mentor.bouldclockapp.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Meldet, wann eine Aufzeichnung vollstaendig weggeschrieben ist.
 *
 * Dasselbe Muster wie [LiveMetrics] und aus demselben Grund: Aufzeichnungsdienst
 * und Oberflaeche sind zwei Prozessteile, die sich sonst nur ueber die Datenbank
 * erreichen - und hier geht es gerade darum, **nicht zu frueh** in die Datenbank
 * zu schauen.
 *
 * Der Anlass: die Kletterhoehe wird am Sessionende aus dem Luftdruckverlauf
 * gerechnet, und den findet die Auswertung ueber die Zeile in `sensor_chunk`.
 * Die schreibt aber der Dienst beim Beenden - frueher lief die Auswertung davor,
 * fand nichts, und **jede** Session bekam still eine leere Hoehe. An einem
 * echten Abend mit zehn Versuchen waren acht davon messbar gewesen.
 */
object RecordingHandoff {

    private val _writtenSessionId = MutableStateFlow<String?>(null)

    /** Zuletzt fertig geschriebene Session. */
    val writtenSessionId: StateFlow<String?> = _writtenSessionId.asStateFlow()

    /** Vor dem Stoppen aufrufen, damit eine alte Meldung nicht durchrutscht. */
    fun expect() {
        _writtenSessionId.value = null
    }

    fun markWritten(sessionId: String) {
        _writtenSessionId.value = sessionId
    }
}

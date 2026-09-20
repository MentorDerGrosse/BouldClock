package at.mentor.bouldclockapp.core.metrics

/** Ein Luftdruckmesspunkt. */
data class PressurePoint(val timestampMs: Long, val hpa: Double)

/**
 * Liefert den Luftdruckverlauf einer Session.
 *
 * Als Schnittstelle, damit die Auswertung ohne Dateien und ohne Uhr pruefbar
 * bleibt - im Test kommt ein synthetischer Verlauf, auf dem Geraet die
 * aufgezeichnete Datei.
 */
fun interface PressureTraceSource {

    suspend fun trace(sessionId: String): List<PressurePoint>

    companion object {
        /** Voreinstellung, solange nichts aufgezeichnet wurde. */
        val None: PressureTraceSource = PressureTraceSource { emptyList() }
    }
}

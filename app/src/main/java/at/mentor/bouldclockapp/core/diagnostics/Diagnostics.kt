package at.mentor.bouldclockapp.core.diagnostics

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Schreibt Diagnosemeldungen zusaetzlich in eine Datei.
 *
 * Notwendig, weil diese Galaxy Watch App-Protokolle nicht an `adb logcat`
 * durchreicht - in allen Puffern zusammen standen 41 Zeilen, keine davon von
 * uns. Ohne Datei laesst sich auf dem Geraet nichts nachvollziehen.
 *
 * Auszulesen mit `adb exec-out run-as at.mentor.bouldclockapp cat files/diagnostics.log`.
 */
object Diagnostics {

    private const val FILE_NAME = "diagnostics.log"
    private const val MAX_BYTES = 64L * 1024L

    private val timestamp = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.ROOT)

    fun log(context: Context, tag: String, message: String, error: Throwable? = null) {
        if (error == null) Log.i(tag, message) else Log.e(tag, message, error)

        runCatching {
            val file = File(context.filesDir, FILE_NAME)
            // Nicht unbegrenzt wachsen lassen - eine Uhr ist kein Server.
            if (file.length() > MAX_BYTES) file.delete()
            file.appendText(
                buildString {
                    append(timestamp.format(Date()))
                    append("  ").append(tag).append("  ").append(message)
                    error?.let { append("  <- ").append(it.toString()) }
                    append('\n')
                },
            )
        }
    }
}

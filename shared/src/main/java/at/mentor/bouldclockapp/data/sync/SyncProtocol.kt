package at.mentor.bouldclockapp.data.sync

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Was Uhr und Handy miteinander vereinbaren.
 *
 * Ein Datenpunkt je Session unter einem eigenen Pfad. Die Wearable Data Layer
 * API haelt diese Punkte vor und liefert sie nach, sobald die Gegenseite wieder
 * erreichbar ist - deshalb ueberlebt eine Session in der Halle ohne Handy in der
 * Naehe.
 *
 * Die Nutzdaten stecken als Anhang im Datenpunkt, nicht im Datenpunkt selbst:
 * der ist auf hundert Kilobyte begrenzt, ein Abend mit siebentausend Pulswerten
 * ist groesser.
 */
object SyncProtocol {

    const val SESSION_PATH_PREFIX: String = "/bouldclock/session"

    /** Das Profil - eine einzige Zeile, die in beide Richtungen wandert. */
    const val PROFILE_PATH: String = "/bouldclock/profile"

    /**
     * Sensordateien gehen ueber einen eigenen Kanal, nicht als Datenpunkt.
     *
     * Ein Datenpunkt samt Anhang ist fuer kleine, dauerhaft vorgehaltene Daten
     * gedacht; eine Session bringt mehrere Megabyte Rohsensorik mit. Der Kanal
     * ueberträgt am Stueck und braucht dafuer beide Geraete gleichzeitig.
     */
    const val FILE_PATH_PREFIX: String = "/bouldclock/file/"

    fun filePath(relativePath: String): String = FILE_PATH_PREFIX + relativePath

    /**
     * Zielpfad aus einem Kanalpfad - oder `null`, wenn er nicht taugt.
     *
     * Prueft auf Ausbrueche mit "..": der Pfad kommt vom anderen Geraet, und
     * eine Datei ausserhalb des App-Verzeichnisses zu schreiben waere das Letzte,
     * was eine Synchronisierung tun sollte.
     */
    fun relativePathFrom(channelPath: String): String? {
        if (!channelPath.startsWith(FILE_PATH_PREFIX)) return null
        val relative = channelPath.removePrefix(FILE_PATH_PREFIX)
        if (relative.isEmpty() || relative.startsWith("/")) return null
        if (relative.split("/").any { it == ".." || it.isEmpty() }) return null
        return relative
    }
    const val KEY_PAYLOAD: String = "payload"
    const val KEY_UPDATED_AT: String = "updatedAt"

    fun sessionPath(sessionId: String): String = "$SESSION_PATH_PREFIX/$sessionId"

    /** Gepackt uebertragen - JSON aus gleichfoermigen Zahlenreihen schrumpft stark. */
    fun pack(json: String): ByteArray {
        val out = ByteArrayOutputStream()
        GZIPOutputStream(out).use { it.write(json.toByteArray(Charsets.UTF_8)) }
        return out.toByteArray()
    }

    fun unpack(bytes: ByteArray): String =
        GZIPInputStream(ByteArrayInputStream(bytes)).use { it.readBytes().toString(Charsets.UTF_8) }
}

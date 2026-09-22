package at.mentor.bouldclockapp.data.sensor

import at.mentor.bouldclockapp.core.model.SensorKind
import java.io.BufferedOutputStream
import java.io.Closeable
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Dateiformat der Rohsensordaten.
 *
 * Warum nicht in die Datenbank: bei 50 Hz sind das hunderttausende Werte pro
 * Session. Eine Zeile je Messwert waere das Ende von Room, und abgefragt wird
 * das Zeug ohnehin nie einzeln - es wird am Stueck gelesen, um spaeter die
 * automatische Versuchserkennung zu trainieren.
 *
 * Aufbau, gzip-komprimiert:
 *
 *     "BCS1" | Sensorname | Werte je Messpunkt | Startzeit (ms seit Epoche)
 *     dann je Messpunkt: Abstand zur Startzeit in ms (Int) | Werte (Float)
 *
 * Der Zeitabstand ist ein Int statt eines Longs - das halbiert den Zeitanteil
 * und reicht fuer 24 Tage. Gzip drueckt den Rest, weil Sensordaten glatt sind.
 *
 * Der Strom wird mit `syncFlush` erzeugt: ohne das haelt gzip alles im Puffer
 * bis zum Schliessen, und eine abgestuerzte Session haette eine leere Datei
 * hinterlassen. Kostet ein paar Prozent Kompression und rettet im Zweifel den
 * ganzen Abend.
 */
object SensorChunkFormat {
    const val MAGIC: String = "BCS1"
}

/** Schreibt einen Messdatenstrom. Nicht threadsicher - nur vom Sensor-Thread benutzen. */
class SensorChunkWriter(
    sink: OutputStream,
    private val sensor: SensorKind,
    private val valuesPerSample: Int,
    private val startedAtMillis: Long,
) : Closeable {

    private val out = DataOutputStream(
        GZIPOutputStream(BufferedOutputStream(sink), /* syncFlush = */ true),
    )

    var sampleCount: Int = 0
        private set

    var lastTimestampMs: Long = startedAtMillis
        private set

    init {
        out.writeUTF(SensorChunkFormat.MAGIC)
        out.writeUTF(sensor.name)
        out.writeByte(valuesPerSample)
        out.writeLong(startedAtMillis)
    }

    /**
     * Haengt einen Messpunkt an.
     *
     * Messpunkte vor der Startzeit oder mit falscher Wertezahl werden verworfen,
     * statt die Datei unbrauchbar zu machen - der Sensor liefert beim Start
     * gelegentlich noch gepufferte Werte von vorher.
     */
    fun append(timestampMs: Long, values: FloatArray): Boolean {
        if (values.size != valuesPerSample) return false
        val delta = timestampMs - startedAtMillis
        if (delta < 0L || delta > Int.MAX_VALUE) return false

        out.writeInt(delta.toInt())
        values.forEach(out::writeFloat)
        sampleCount++
        lastTimestampMs = timestampMs
        return true
    }

    /** Schreibt das bisher Gepufferte in die Datei. Regelmaessig aufrufen. */
    fun flush() {
        out.flush()
    }

    override fun close() {
        out.close()
    }
}

/** Ein eingelesener Messdatenstrom. */
data class SensorChunkData(
    val sensor: SensorKind,
    val valuesPerSample: Int,
    val startedAtMillis: Long,
    val samples: List<SensorSample>,
)

data class SensorSample(
    val timestampMs: Long,
    val values: List<Float>,
)

/**
 * Liest eine Datei zurueck.
 *
 * Bricht bei abgeschnittenen Dateien nicht ab, sondern liefert, was lesbar war:
 * wenn die Uhr mitten in der Session stirbt, ist der Rest trotzdem brauchbar.
 */
object SensorChunkReader {

    fun read(source: InputStream): SensorChunkData {
        DataInputStream(GZIPInputStream(source)).use { input ->
            val magic = input.readUTF()
            require(magic == SensorChunkFormat.MAGIC) { "Kein Sensorstrom: $magic" }

            val sensor = SensorKind.valueOf(input.readUTF())
            val valuesPerSample = input.readByte().toInt()
            val startedAtMillis = input.readLong()

            val samples = mutableListOf<SensorSample>()
            try {
                while (true) {
                    val delta = input.readInt()
                    val values = List(valuesPerSample) { input.readFloat() }
                    samples += SensorSample(startedAtMillis + delta, values)
                }
            } catch (_: EOFException) {
                // Regulaeres Ende oder abgeschnittene Datei - beides in Ordnung.
            }

            return SensorChunkData(sensor, valuesPerSample, startedAtMillis, samples)
        }
    }
}

package at.mentor.bouldclockapp.sensor

import at.mentor.bouldclockapp.core.model.SensorKind
import at.mentor.bouldclockapp.data.sensor.SensorChunkReader
import at.mentor.bouldclockapp.data.sensor.SensorChunkWriter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class SensorChunkFormatTest {

    private val start = 1_700_000_000_000L

    @Test
    fun `geschriebene Messpunkte kommen unveraendert zurueck`() {
        val sink = ByteArrayOutputStream()
        SensorChunkWriter(sink, SensorKind.ACCELEROMETER, 3, start).use { writer ->
            writer.append(start, floatArrayOf(0.1f, -9.81f, 0.3f))
            writer.append(start + 20, floatArrayOf(1.5f, -8.0f, 0.0f))
            writer.append(start + 40, floatArrayOf(-2.5f, 12.0f, 4.25f))
            assertEquals(3, writer.sampleCount)
        }

        val data = SensorChunkReader.read(ByteArrayInputStream(sink.toByteArray()))
        assertEquals(SensorKind.ACCELEROMETER, data.sensor)
        assertEquals(3, data.valuesPerSample)
        assertEquals(start, data.startedAtMillis)
        assertEquals(listOf(start, start + 20, start + 40), data.samples.map { it.timestampMs })
        assertEquals(listOf(-2.5f, 12.0f, 4.25f), data.samples.last().values)
    }

    /** Der Sensor liefert beim Start gelegentlich gepufferte Werte von vorher. */
    @Test
    fun `Messpunkte vor der Startzeit werden verworfen`() {
        val sink = ByteArrayOutputStream()
        SensorChunkWriter(sink, SensorKind.PRESSURE, 1, start).use { writer ->
            assertFalse(writer.append(start - 500, floatArrayOf(1013f)))
            assertTrue(writer.append(start + 10, floatArrayOf(1012f)))
            assertEquals(1, writer.sampleCount)
        }

        val data = SensorChunkReader.read(ByteArrayInputStream(sink.toByteArray()))
        assertEquals(1, data.samples.size)
    }

    @Test
    fun `falsche Wertezahl macht die Datei nicht kaputt`() {
        val sink = ByteArrayOutputStream()
        SensorChunkWriter(sink, SensorKind.GYROSCOPE, 3, start).use { writer ->
            assertFalse(writer.append(start, floatArrayOf(1f)))
            assertTrue(writer.append(start + 5, floatArrayOf(1f, 2f, 3f)))
        }

        val data = SensorChunkReader.read(ByteArrayInputStream(sink.toByteArray()))
        assertEquals(1, data.samples.size)
        assertEquals(listOf(1f, 2f, 3f), data.samples.single().values)
    }

    /**
     * Der Fall, der auf der Uhr aufgefallen ist: ohne Flush hielt gzip alles im
     * Puffer, und eine abgestuerzte Session hinterliess eine leere Datei.
     */
    @Test
    fun `nach einem Flush ist die Datei auch ohne Schliessen lesbar`() {
        val sink = ByteArrayOutputStream()
        val writer = SensorChunkWriter(sink, SensorKind.ACCELEROMETER, 3, start)
        writer.append(start, floatArrayOf(1f, 2f, 3f))
        writer.append(start + 20, floatArrayOf(4f, 5f, 6f))
        writer.flush()

        // Absichtlich nicht schliessen - die Uhr ist gestorben.
        val data = SensorChunkReader.read(ByteArrayInputStream(sink.toByteArray()))
        assertEquals(2, data.samples.size)
        assertEquals(listOf(4f, 5f, 6f), data.samples.last().values)
    }

    /** Eine Session ueber zwei Stunden bei 50 Hz muss in den Int-Abstand passen. */
    @Test
    fun `lange Sessions passen in das Format`() {
        val sink = ByteArrayOutputStream()
        val twoHours = 2 * 60 * 60 * 1000L
        SensorChunkWriter(sink, SensorKind.ACCELEROMETER, 3, start).use { writer ->
            assertTrue(writer.append(start + twoHours, floatArrayOf(1f, 2f, 3f)))
        }

        val data = SensorChunkReader.read(ByteArrayInputStream(sink.toByteArray()))
        assertEquals(start + twoHours, data.samples.single().timestampMs)
    }
}

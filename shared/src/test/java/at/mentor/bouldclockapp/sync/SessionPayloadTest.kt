package at.mentor.bouldclockapp.sync

import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.SensorKind
import at.mentor.bouldclockapp.core.model.SessionMetric
import at.mentor.bouldclockapp.core.model.SessionState
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.core.model.SyncState
import at.mentor.bouldclockapp.data.db.entity.AttemptEntity
import at.mentor.bouldclockapp.data.db.entity.HrSampleEntity
import at.mentor.bouldclockapp.data.db.entity.MetricSampleEntity
import at.mentor.bouldclockapp.data.db.entity.RecordMeta
import at.mentor.bouldclockapp.data.db.entity.SensorChunkEntity
import at.mentor.bouldclockapp.data.db.entity.SessionEntity
import at.mentor.bouldclockapp.data.sync.SessionPayload
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionPayloadTest {

    private val start = 1_758_000_000_000L

    private val session = SessionEntity(
        id = "s1",
        gymId = null,
        type = SessionType.LIMIT,
        state = SessionState.FINISHED,
        startedAt = start,
        endedAt = start + 3_600_000L,
        pausedMs = 60_000L,
        restTargetMs = 300_000L,
        rpe = 8,
        note = "Gute Session",
        meta = RecordMeta(createdAt = start, updatedAt = start + 10),
    )

    private val attempt = AttemptEntity(
        id = "a1",
        sessionId = "s1",
        ordinal = 1,
        startsNewBoulder = true,
        startedAt = start + 1000,
        endedAt = start + 31_000,
        outcome = AttemptOutcome.TOP,
        gradeValue = 11,
        gradeSystem = GradeSystem.FONT,
        climbHeightMeters = 4.2,
        boardAngleDegrees = 40,
        hrAvg = 150,
        hrMax = 171,
        hrEnd = 168,
        hrAfter60s = 132,
        hrr60 = 36,
        restAfterMs = 180_000L,
        meta = RecordMeta(createdAt = start, updatedAt = start + 20),
    )

    private val payload = SessionPayload(
        session = session,
        attempts = listOf(attempt),
        hrSamples = listOf(
            HrSampleEntity("s1", start + 1000, 142, 5),
            HrSampleEntity("s1", start + 2000, 145, 5),
        ),
        metricSamples = listOf(
            MetricSampleEntity("s1", SessionMetric.CALORIES, start + 1000, 12.5),
            MetricSampleEntity("s1", SessionMetric.ELEVATION_GAIN, start + 1000, 4.2),
        ),
    )

    @Test
    fun `ein Paket ueberlebt Hin- und Rueckweg unveraendert`() {
        val zurueck = SessionPayload.fromJson(payload.toJson())

        assertEquals(session.copy(meta = session.meta.copy(syncState = SyncState.SYNCED)), zurueck.session)
        assertEquals(1, zurueck.attempts.size)
        assertEquals(attempt.copy(meta = attempt.meta.copy(syncState = SyncState.SYNCED)), zurueck.attempts.single())
        assertEquals(payload.hrSamples, zurueck.hrSamples)
        assertEquals(payload.metricSamples, zurueck.metricSamples)
    }

    /** Angekommene Daten muessen nicht zurueckgeschickt werden. */
    @Test
    fun `empfangene Daten gelten als synchronisiert`() {
        val zurueck = SessionPayload.fromJson(payload.toJson())
        assertEquals(SyncState.SYNCED, zurueck.session.meta.syncState)
        assertTrue(zurueck.attempts.all { it.meta.syncState == SyncState.SYNCED })
    }

    /**
     * Der Fall, fuer den JSON gewaehlt wurde: die Uhr schickt ein Feld, das die
     * Handy-App noch nicht kennt. Das darf die Synchronisierung nicht stoppen.
     */
    @Test
    fun `unbekannte Felder werden ignoriert`() {
        val mitZusatz = JSONObject(payload.toJson()).apply {
            put("wasGanzNeues", "aus einer spaeteren Version")
            getJSONObject("session").put("nochEinFeld", 42)
        }.toString()

        val zurueck = SessionPayload.fromJson(mitZusatz)
        assertEquals("s1", zurueck.session.id)
        assertEquals(1, zurueck.attempts.size)
    }

    /** Und der umgekehrte Fall: ein Feld fehlt, weil die Gegenseite aelter ist. */
    @Test
    fun `fehlende Felder bekommen Voreinstellungen`() {
        val ohne = JSONObject(payload.toJson()).apply {
            getJSONObject("session").remove("restTargetMs")
            getJSONArray("attempts").getJSONObject(0).remove("climbHeightMeters")
        }.toString()

        val zurueck = SessionPayload.fromJson(ohne)
        assertEquals(SessionType.FREE.defaultRestMs, zurueck.session.restTargetMs)
        assertNull(zurueck.attempts.single().climbHeightMeters)
    }

    @Test
    fun `ein kaputter Versuch kippt nicht das ganze Paket`() {
        val kaputt = JSONObject(payload.toJson()).apply {
            getJSONArray("attempts").getJSONObject(0).remove("ordinal")
        }.toString()

        val zurueck = SessionPayload.fromJson(kaputt)
        assertEquals("s1", zurueck.session.id)
        assertTrue(zurueck.attempts.isEmpty())
        assertEquals(2, zurueck.hrSamples.size)
    }

    /** Massendaten als Zahlenreihen - das spart bei tausenden Pulswerten viel. */
    @Test
    fun `Pulswerte werden kompakt geschrieben`() {
        val viele = payload.copy(
            hrSamples = (0 until 1000).map {
                HrSampleEntity("s1", start + it * 1000L, 140 + it % 20, 5)
            },
        )
        val bytesJeWert = viele.toJson().length / 1000
        assertTrue("Zu gross: $bytesJeWert Byte je Pulswert", bytesJeWert < 30)
    }
}

class SyncProtocolPathTest {

    @Test
    fun `ein normaler Pfad wird uebernommen`() {
        val path = at.mentor.bouldclockapp.data.sync.SyncProtocol
            .filePath("sensors/abc-123/ACCELEROMETER.bcs")
        assertEquals(
            "sensors/abc-123/ACCELEROMETER.bcs",
            at.mentor.bouldclockapp.data.sync.SyncProtocol.relativePathFrom(path),
        )
    }

    /**
     * Der Pfad kommt vom anderen Geraet. Eine Datei ausserhalb des
     * App-Verzeichnisses zu schreiben waere das Letzte, was eine
     * Synchronisierung tun sollte.
     */
    @Test
    fun `Ausbruchsversuche werden abgewiesen`() {
        val protocol = at.mentor.bouldclockapp.data.sync.SyncProtocol
        assertNull(protocol.relativePathFrom("/bouldclock/file/../../etc/passwd"))
        assertNull(protocol.relativePathFrom("/bouldclock/file//absolut"))
        assertNull(protocol.relativePathFrom("/bouldclock/file/"))
        assertNull(protocol.relativePathFrom("/ganz/woanders/datei"))
        assertNull(protocol.relativePathFrom("/bouldclock/file/a//b"))
    }

    @Test
    fun `gepackte Daten kommen unveraendert zurueck`() {
        val protocol = at.mentor.bouldclockapp.data.sync.SyncProtocol
        val text = """{"viele":"gleichfoermige","zahlen":[1,2,3,4,5]}"""
        assertEquals(text, protocol.unpack(protocol.pack(text)))
    }
}

/**
 * Eine angekommene Datei muss sich aus ihrem Pfad einordnen lassen.
 *
 * Sonst liegt sie zwar auf der Platte, aber keine Zeile zeigt auf sie - und
 * genau das ist passiert: 21 uebertragene Dateien, null Zeilen in der Tabelle.
 */
class ArrivedSensorFileTest {

    @Test
    fun `Pfad liefert Session und Sensor`() {
        val path = "sensors/abc-123/GYROSCOPE.bcs"
        val chunk = SensorChunkEntity.fromArrivedFile(path, sizeBytes = 4711L)

        assertEquals("abc-123", chunk?.sessionId)
        assertEquals(SensorKind.GYROSCOPE, chunk?.sensor)
        assertEquals(path, chunk?.relativePath)
        assertEquals(4711L, chunk?.sizeBytes)
        // Die Datei ist da - was fehlt, sind nur die Metadaten.
        assertEquals(SyncState.SYNCED, chunk?.syncState)
        assertEquals(0, chunk?.sampleCount)
    }

    @Test
    fun `Schreiben und Einordnen benutzen denselben Pfad`() {
        assertEquals(
            "sensors/s1/PRESSURE.bcs",
            SensorChunkEntity.relativePath("s1", SensorKind.PRESSURE),
        )
        assertEquals(
            SensorKind.PRESSURE,
            SensorChunkEntity
                .fromArrivedFile(SensorChunkEntity.relativePath("s1", SensorKind.PRESSURE), 1L)
                ?.sensor,
        )
    }

    @Test
    fun `fremde Pfade werden nicht eingeordnet`() {
        assertNull(SensorChunkEntity.fromArrivedFile("woanders/s1/GYROSCOPE.bcs", 1L))
        assertNull(SensorChunkEntity.fromArrivedFile("sensors/s1/UNBEKANNT.bcs", 1L))
        assertNull(SensorChunkEntity.fromArrivedFile("sensors//GYROSCOPE.bcs", 1L))
        assertNull(SensorChunkEntity.fromArrivedFile("sensors/s1/tief/GYROSCOPE.bcs", 1L))
        assertNull(SensorChunkEntity.fromArrivedFile("GYROSCOPE.bcs", 1L))
    }
}

/**
 * Halle und Boulder muessen mitfahren.
 *
 * Sonst weist der Fremdschluessel auf der Gegenseite das ganze Paket ab: eine
 * Versuchszeile mit unbekannter `problemId` laesst SQLite nicht einfuegen, und
 * die Session faellt komplett durch. Am Geraet beobachtet, als am Handy der
 * erste Boulder benannt wurde.
 */
class PayloadReferencesTest {

    private val start = 1_758_000_000_000L

    private val gym = at.mentor.bouldclockapp.data.db.entity.GymEntity(
        id = "g1",
        name = "Kletterhalle Wien",
        gradeSystem = GradeSystem.FONT,
        isDefault = true,
        meta = RecordMeta(createdAt = start, updatedAt = start),
    )

    private val problem = at.mentor.bouldclockapp.data.db.entity.ProblemEntity(
        id = "p1",
        gymId = "g1",
        label = "Blau 14",
        gradeValue = 11,
        meta = RecordMeta(createdAt = start, updatedAt = start),
    )

    private fun payloadWith(problemId: String?) = SessionPayload(
        session = SessionEntity(
            id = "s1",
            gymId = "g1",
            type = SessionType.FREE,
            state = SessionState.FINISHED,
            startedAt = start,
            endedAt = start + 1000,
            restTargetMs = 180_000L,
            meta = RecordMeta(createdAt = start, updatedAt = start),
        ),
        attempts = listOf(
            AttemptEntity(
                id = "a1",
                sessionId = "s1",
                problemId = problemId,
                ordinal = 1,
                startedAt = start,
                endedAt = start + 100,
                meta = RecordMeta(createdAt = start, updatedAt = start),
            ),
        ),
        hrSamples = emptyList(),
        metricSamples = emptyList(),
        gyms = listOf(gym),
        problems = listOf(problem),
    )

    @Test
    fun `Halle und Boulder ueberstehen die Uebertragung`() {
        val restored = SessionPayload.fromJson(payloadWith("p1").toJson())

        assertEquals(1, restored.gyms.size)
        assertEquals("Kletterhalle Wien", restored.gyms.single().name)
        assertTrue(restored.gyms.single().isDefault)

        assertEquals(1, restored.problems.size)
        assertEquals("Blau 14", restored.problems.single().label)
        assertEquals("g1", restored.problems.single().gymId)
        assertEquals(11, restored.problems.single().gradeValue)

        assertEquals("p1", restored.attempts.single().problemId)
        assertEquals("g1", restored.session.gymId)
    }

    /**
     * Ein Paket von einer aelteren Fassung kennt die Felder nicht. Es muss
     * trotzdem ankommen - sonst bricht die Synchronisierung, sobald nur ein
     * Geraet aktualisiert wurde.
     */
    @Test
    fun `altes Paket ohne Halle und Boulder bleibt lesbar`() {
        val old = JSONObject(payloadWith(null).toJson()).apply {
            remove("gyms")
            remove("problems")
        }.toString()

        val restored = SessionPayload.fromJson(old)

        assertTrue(restored.gyms.isEmpty())
        assertTrue(restored.problems.isEmpty())
        assertEquals("s1", restored.session.id)
        assertEquals(1, restored.attempts.size)
    }
}

package at.mentor.bouldclockapp.data.sync

import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.SensorKind
import at.mentor.bouldclockapp.core.model.SessionMetric
import at.mentor.bouldclockapp.core.model.SessionState
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.core.model.SyncState
import at.mentor.bouldclockapp.core.model.WallAngle
import at.mentor.bouldclockapp.data.db.entity.AttemptEntity
import at.mentor.bouldclockapp.data.db.entity.GymEntity
import at.mentor.bouldclockapp.data.db.entity.ProblemEntity
import at.mentor.bouldclockapp.data.db.entity.HrSampleEntity
import at.mentor.bouldclockapp.data.db.entity.MetricSampleEntity
import at.mentor.bouldclockapp.data.db.entity.RecordMeta
import at.mentor.bouldclockapp.data.db.entity.SensorChunkEntity
import at.mentor.bouldclockapp.data.db.entity.SessionEntity
import org.json.JSONArray
import org.json.JSONObject

/**
 * Eine Session als uebertragbares Paket.
 *
 * JSON statt eines Binaerformats, obwohl das kompakter waere: Uhr und Handy
 * werden nicht immer gleichzeitig aktualisiert. Wer die Handy-App neu aufspielt
 * und die Uhr vergisst, schickt ein Paket nach altem Stand - unbekannte Felder
 * werden hier ignoriert, fehlende bekommen Voreinstellungen, und die
 * Synchronisierung laeuft weiter statt abzubrechen.
 *
 * Die Massendaten (Puls, Kalorien) stehen als Zahlenreihen statt als Objekte -
 * bei siebentausend Pulswerten spart das ein Vielfaches an Groesse.
 */
data class SessionPayload(
    val session: SessionEntity,
    val attempts: List<AttemptEntity>,
    val hrSamples: List<HrSampleEntity>,
    val metricSamples: List<MetricSampleEntity>,

    /**
     * Beschreibung der Sensordateien - nicht ihr Inhalt.
     *
     * Die Dateien selbst kommen ueber einen eigenen Kanal hinterher. Ihre
     * Metadaten fahren hier mit, damit das Handy vorher weiss, was es erwartet,
     * und eine Session auch dann vollstaendig aussieht, wenn die Uebertragung
     * der Dateien noch laeuft.
     */
    val sensorChunks: List<SensorChunkEntity> = emptyList(),

    /**
     * Halle und Boulder, auf die diese Session zeigt.
     *
     * Muessen mit, weil beides Fremdschluessel sind: eine Versuchszeile mit
     * einer `problemId`, die die Gegenstelle nicht kennt, laesst sich dort
     * nicht einfuegen - SQLite weist sie ab, und damit faellt das **ganze
     * Paket** durch, nicht nur die Zuordnung. Genau das ist passiert, als am
     * Handy der erste Boulder benannt wurde.
     *
     * Nur die tatsaechlich verwendeten Zeilen, nicht der ganze Bestand.
     */
    val gyms: List<GymEntity> = emptyList(),
    val problems: List<ProblemEntity> = emptyList(),
) {

    fun toJson(): String = JSONObject().apply {
        put(KEY_VERSION, FORMAT_VERSION)
        put(KEY_SESSION, session.toJson())
        put(KEY_ATTEMPTS, JSONArray(attempts.map { it.toJson() }))
        put(KEY_HR, JSONArray(hrSamples.map { JSONArray(listOf(it.timestampMs, it.bpm, it.accuracy)) }))
        put(
            KEY_METRICS,
            JSONArray(metricSamples.map { JSONArray(listOf(it.metric.name, it.timestampMs, it.value)) }),
        )
        put(KEY_CHUNKS, JSONArray(sensorChunks.map { it.toJson() }))
        put(KEY_GYMS, JSONArray(gyms.map { it.toJson() }))
        put(KEY_PROBLEMS, JSONArray(problems.map { it.toJson() }))
    }.toString()

    companion object {
        const val FORMAT_VERSION = 1

        private const val KEY_VERSION = "v"
        private const val KEY_SESSION = "session"
        private const val KEY_ATTEMPTS = "attempts"
        private const val KEY_HR = "hr"
        private const val KEY_METRICS = "metrics"
        private const val KEY_CHUNKS = "chunks"
        private const val KEY_GYMS = "gyms"
        private const val KEY_PROBLEMS = "problems"

        /**
         * Liest ein Paket. Wirft nur, wenn nicht einmal die Session lesbar ist -
         * alles andere fehlt lieber, als die Synchronisierung zu stoppen.
         */
        fun fromJson(raw: String): SessionPayload {
            val root = JSONObject(raw)
            val session = root.getJSONObject(KEY_SESSION).toSession()

            val attempts = root.optJSONArray(KEY_ATTEMPTS).objects().mapNotNull { obj ->
                runCatching { obj.toAttempt(session.id) }.getOrNull()
            }
            val hrSamples = root.optJSONArray(KEY_HR).arrays().mapNotNull { row ->
                runCatching {
                    HrSampleEntity(session.id, row.getLong(0), row.getInt(1), row.getInt(2))
                }.getOrNull()
            }
            val metricSamples = root.optJSONArray(KEY_METRICS).arrays().mapNotNull { row ->
                runCatching {
                    MetricSampleEntity(
                        sessionId = session.id,
                        metric = SessionMetric.valueOf(row.getString(0)),
                        timestampMs = row.getLong(1),
                        value = row.getDouble(2),
                    )
                }.getOrNull()
            }

            val sensorChunks = root.optJSONArray(KEY_CHUNKS).objects().mapNotNull { obj ->
                runCatching { obj.toSensorChunk(session.id) }.getOrNull()
            }

            val gyms = root.optJSONArray(KEY_GYMS).objects().map { it.toGym() }
            val problems = root.optJSONArray(KEY_PROBLEMS).objects().map { it.toProblem() }

            return SessionPayload(
                session = session,
                attempts = attempts,
                hrSamples = hrSamples,
                metricSamples = metricSamples,
                sensorChunks = sensorChunks,
                gyms = gyms,
                problems = problems,
            )
        }
    }
}

// --- Umwandlung, bewusst ausgeschrieben statt per Reflexion ---

private fun SessionEntity.toJson() = JSONObject().apply {
    put("id", id)
    putOpt("gymId", gymId)
    put("type", type.name)
    put("state", state.name)
    put("startedAt", startedAt)
    putOpt("endedAt", endedAt)
    put("pausedMs", pausedMs)
    put("restTargetMs", restTargetMs)
    putOpt("rpe", rpe)
    putOpt("note", note)
    putMeta(meta)
}

private fun JSONObject.toSession() = SessionEntity(
    id = getString("id"),
    gymId = optStringOrNull("gymId"),
    type = optEnum(SessionType.entries, "type", SessionType.FREE),
    state = optEnum(SessionState.entries, "state", SessionState.FINISHED),
    startedAt = getLong("startedAt"),
    endedAt = optLongOrNull("endedAt"),
    pausedMs = optLong("pausedMs", 0L),
    restTargetMs = optLong("restTargetMs", SessionType.FREE.defaultRestMs),
    rpe = optIntOrNull("rpe"),
    note = optStringOrNull("note"),
    meta = readMeta(),
)

private fun AttemptEntity.toJson() = JSONObject().apply {
    put("id", id)
    putOpt("problemId", problemId)
    put("ordinal", ordinal)
    put("startsNewBoulder", startsNewBoulder)
    put("startedAt", startedAt)
    putOpt("endedAt", endedAt)
    putOpt("outcome", outcome?.name)
    putOpt("gradeValue", gradeValue)
    putOpt("gradeSystem", gradeSystem?.name)
    putOpt("topMoveReached", topMoveReached)
    putOpt("climbHeightMeters", climbHeightMeters)
    putOpt("boardAngleDegrees", boardAngleDegrees)
    putOpt("hrAvg", hrAvg)
    putOpt("hrMax", hrMax)
    putOpt("hrEnd", hrEnd)
    putOpt("hrAfter60s", hrAfter60s)
    putOpt("hrr60", hrr60)
    putOpt("restAfterMs", restAfterMs)
    putMeta(meta)
}

private fun JSONObject.toAttempt(sessionId: String) = AttemptEntity(
    id = getString("id"),
    sessionId = sessionId,
    problemId = optStringOrNull("problemId"),
    ordinal = getInt("ordinal"),
    startsNewBoulder = optBoolean("startsNewBoulder", false),
    startedAt = getLong("startedAt"),
    endedAt = optLongOrNull("endedAt"),
    outcome = optStringOrNull("outcome")?.let { name ->
        AttemptOutcome.entries.firstOrNull { it.name == name }
    },
    gradeValue = optIntOrNull("gradeValue"),
    gradeSystem = optStringOrNull("gradeSystem")?.let { name ->
        GradeSystem.entries.firstOrNull { it.name == name }
    },
    topMoveReached = optIntOrNull("topMoveReached"),
    climbHeightMeters = optDoubleOrNull("climbHeightMeters"),
    boardAngleDegrees = optIntOrNull("boardAngleDegrees"),
    hrAvg = optIntOrNull("hrAvg"),
    hrMax = optIntOrNull("hrMax"),
    hrEnd = optIntOrNull("hrEnd"),
    hrAfter60s = optIntOrNull("hrAfter60s"),
    hrr60 = optIntOrNull("hrr60"),
    restAfterMs = optLongOrNull("restAfterMs"),
    meta = readMeta(),
)

private fun SensorChunkEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("sensor", sensor.name)
    put("relativePath", relativePath)
    put("startedAt", startedAt)
    put("endedAt", endedAt)
    put("sampleRateHz", sampleRateHz)
    put("sampleCount", sampleCount)
    put("sizeBytes", sizeBytes)
}

private fun JSONObject.toSensorChunk(sessionId: String) = SensorChunkEntity(
    id = getString("id"),
    sessionId = sessionId,
    sensor = SensorKind.valueOf(getString("sensor")),
    relativePath = getString("relativePath"),
    startedAt = getLong("startedAt"),
    endedAt = getLong("endedAt"),
    sampleRateHz = optInt("sampleRateHz", 0),
    sampleCount = optInt("sampleCount", 0),
    sizeBytes = optLong("sizeBytes", 0L),
    // Auf der Empfaengerseite heisst PENDING: Datei noch nicht da.
    syncState = at.mentor.bouldclockapp.core.model.SyncState.PENDING,
)

private fun GymEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("gradeSystem", gradeSystem.name)
    put("isDefault", isDefault)
    putMeta(meta)
}

private fun JSONObject.toGym() = GymEntity(
    id = getString("id"),
    name = optString("name"),
    gradeSystem = optStringOrNull("gradeSystem")?.let { name ->
        GradeSystem.entries.firstOrNull { it.name == name }
    } ?: GradeSystem.FONT,
    isDefault = optBoolean("isDefault", false),
    meta = readMeta(),
)

private fun ProblemEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("gymId", gymId)
    put("label", label)
    putOpt("colorHex", colorHex)
    putOpt("gradeValue", gradeValue)
    putOpt("wallAngle", wallAngle?.name)
    putOpt("firstSentAt", firstSentAt)
    putOpt("retiredAt", retiredAt)
    putMeta(meta)
}

private fun JSONObject.toProblem() = ProblemEntity(
    id = getString("id"),
    gymId = getString("gymId"),
    label = optString("label"),
    colorHex = optStringOrNull("colorHex"),
    gradeValue = optIntOrNull("gradeValue"),
    wallAngle = optStringOrNull("wallAngle")?.let { name ->
        WallAngle.entries.firstOrNull { it.name == name }
    },
    firstSentAt = optLongOrNull("firstSentAt"),
    retiredAt = optLongOrNull("retiredAt"),
    meta = readMeta(),
)

private fun JSONObject.putMeta(meta: RecordMeta) {
    put("createdAt", meta.createdAt)
    put("updatedAt", meta.updatedAt)
    putOpt("deletedAt", meta.deletedAt)
    put("syncState", meta.syncState.name)
}

private fun JSONObject.readMeta() = RecordMeta(
    createdAt = optLong("createdAt", 0L),
    updatedAt = optLong("updatedAt", 0L),
    deletedAt = optLongOrNull("deletedAt"),
    // Angekommene Daten sind auf dieser Seite nicht mehr zu verschicken.
    syncState = SyncState.SYNCED,
)

private fun JSONObject.optStringOrNull(key: String): String? =
    if (isNull(key)) null else optString(key).takeIf { it.isNotEmpty() }

private fun JSONObject.optLongOrNull(key: String): Long? = if (isNull(key)) null else optLong(key)

private fun JSONObject.optIntOrNull(key: String): Int? = if (isNull(key)) null else optInt(key)

private fun JSONObject.optDoubleOrNull(key: String): Double? =
    if (isNull(key)) null else optDouble(key).takeIf { !it.isNaN() }

private fun <T : Enum<T>> JSONObject.optEnum(values: List<T>, key: String, fallback: T): T =
    optStringOrNull(key)?.let { name -> values.firstOrNull { it.name == name } } ?: fallback

private fun JSONArray?.objects(): List<JSONObject> =
    if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it) }

private fun JSONArray?.arrays(): List<JSONArray> =
    if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONArray(it) }

package at.mentor.bouldclockapp.data.sync

import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.SessionMetric
import at.mentor.bouldclockapp.core.model.SessionState
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.core.model.SyncState
import at.mentor.bouldclockapp.data.db.entity.AttemptEntity
import at.mentor.bouldclockapp.data.db.entity.HrSampleEntity
import at.mentor.bouldclockapp.data.db.entity.MetricSampleEntity
import at.mentor.bouldclockapp.data.db.entity.RecordMeta
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
    }.toString()

    companion object {
        const val FORMAT_VERSION = 1

        private const val KEY_VERSION = "v"
        private const val KEY_SESSION = "session"
        private const val KEY_ATTEMPTS = "attempts"
        private const val KEY_HR = "hr"
        private const val KEY_METRICS = "metrics"

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

            return SessionPayload(session, attempts, hrSamples, metricSamples)
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

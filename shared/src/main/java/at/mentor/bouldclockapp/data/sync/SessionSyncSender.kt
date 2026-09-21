package at.mentor.bouldclockapp.data.sync

import android.content.Context
import at.mentor.bouldclockapp.core.diagnostics.Diagnostics
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import java.io.File
import kotlinx.coroutines.tasks.await

/**
 * Schickt beendete Sessions ans Handy.
 *
 * Verschickt wird nicht nur die gerade beendete Session, sondern alles noch
 * Offene: wer eine Woche ohne Handy in der Halle war, holt beim naechsten Mal
 * alles auf einmal nach.
 */
class SessionSyncSender(
    private val context: Context,
    private val repository: SessionSyncRepository,
) {

    private val dataClient by lazy { Wearable.getDataClient(context) }
    private val channelClient by lazy { Wearable.getChannelClient(context) }
    private val nodeClient by lazy { Wearable.getNodeClient(context) }

    /** Gibt zurueck, wie viele Sessions abgelegt wurden. */
    suspend fun syncPending(): Int {
        val pending = repository.pending()
        var sent = 0

        pending.forEach { session ->
            val payload = repository.buildPayload(session.id) ?: return@forEach
            val packed = SyncProtocol.pack(payload.toJson())

            val result = runCatching {
                val request = PutDataMapRequest.create(SyncProtocol.sessionPath(session.id)).apply {
                    dataMap.putAsset(SyncProtocol.KEY_PAYLOAD, Asset.createFromBytes(packed))
                    dataMap.putLong(SyncProtocol.KEY_UPDATED_AT, session.meta.updatedAt)
                }.asPutDataRequest().setUrgent()

                dataClient.putDataItem(request).await()
            }

            result.onSuccess {
                repository.markSynced(session.id)
                sent++
                Diagnostics.log(
                    context,
                    TAG,
                    "Session ${session.id.take(8)} abgelegt, ${packed.size} Byte",
                )
                // Erst die Zeilen, dann die Dateien - das Handy soll die Session
                // schon kennen, wenn sie eintreffen.
                sendSensorFiles(session.id)
            }.onFailure {
                // Nicht markieren - beim naechsten Versuch noch einmal.
                Diagnostics.log(context, TAG, "Session ${session.id.take(8)} nicht abgelegt", it)
            }
        }

        // Dateien getrennt nachziehen: sie koennen offen sein, obwohl die
        // Session selbst laengst drueben ist.
        repository.sessionsWithPendingFiles().forEach { sendSensorFiles(it) }

        return sent
    }

    /** Schickt eine einzelne Session, etwa nach einer Aenderung am Handy. */
    suspend fun sendSession(sessionId: String): Boolean {
        val payload = repository.buildPayload(sessionId) ?: return false
        val packed = SyncProtocol.pack(payload.toJson())
        return runCatching {
            val request = PutDataMapRequest.create(SyncProtocol.sessionPath(sessionId)).apply {
                dataMap.putAsset(SyncProtocol.KEY_PAYLOAD, Asset.createFromBytes(packed))
                dataMap.putLong(SyncProtocol.KEY_UPDATED_AT, System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()
            dataClient.putDataItem(request).await()
            repository.markSynced(sessionId)
            true
        }.onFailure {
            Diagnostics.log(context, TAG, "Session ${sessionId.take(8)} nicht abgelegt", it)
        }.getOrDefault(false)
    }

    /**
     * Schickt die Rohsensordateien einer Session.
     *
     * Ueber einen Kanal statt als Datenpunkt: mehrere Megabyte gehoeren nicht in
     * einen Speicher, der dauerhaft vorgehalten wird. Der Kanal ueberträgt am
     * Stueck - dafuer muessen beide Geraete gleichzeitig erreichbar sein.
     * Schlaegt es fehl, bleibt die Datei auf PENDING und geht beim naechsten
     * Mal mit.
     *
     * Danach gehen die Zeilen noch einmal raus. Beim Beenden einer Session ist
     * das Paket naemlich meist schon unterwegs, bevor die Dateien ueberhaupt in
     * der Datenbank stehen - gemessen lagen zwischen beidem 200 ms. Erst dieser
     * zweite Lauf enthaelt sie also sicher. Ohne ihn kaeme die Datei an, ohne
     * dass je eine Zeile auf sie zeigt.
     */
    suspend fun sendSensorFiles(sessionId: String): Int {
        val chunks = repository.pendingChunks(sessionId)
        if (chunks.isEmpty()) return 0

        val nodes = runCatching { nodeClient.connectedNodes.await() }.getOrDefault(emptyList())
        if (nodes.isEmpty()) {
            Diagnostics.log(context, TAG, "Keine Gegenstelle - Dateien warten")
            return 0
        }

        var sent = 0
        chunks.forEach { chunk ->
            val file = File(context.filesDir, chunk.relativePath)
            if (!file.exists()) return@forEach

            val ok = nodes.all { node ->
                runCatching {
                    val channel = channelClient
                        .openChannel(node.id, SyncProtocol.filePath(chunk.relativePath))
                        .await()
                    channelClient.getOutputStream(channel).await().use { out ->
                        file.inputStream().use { it.copyTo(out) }
                    }
                }.onFailure {
                    Diagnostics.log(context, TAG, "${chunk.relativePath} nicht gesendet", it)
                }.isSuccess
            }

            if (ok) {
                repository.markChunkSynced(chunk)
                sent++
                Diagnostics.log(
                    context,
                    TAG,
                    "${chunk.relativePath} gesendet, ${file.length()} Byte",
                )
            }
        }

        // Jetzt stehen die Zeilen zu diesen Dateien fest - nachreichen.
        if (sent > 0) sendSession(sessionId)

        return sent
    }

    /** Schickt das Profil zur Gegenseite. */
    suspend fun sendProfile(): Boolean {
        val profile = repository.profile() ?: return false
        val packed = SyncProtocol.pack(ProfilePayload(profile).toJson())
        return runCatching {
            val request = PutDataMapRequest.create(SyncProtocol.PROFILE_PATH).apply {
                dataMap.putAsset(SyncProtocol.KEY_PAYLOAD, Asset.createFromBytes(packed))
                dataMap.putLong(SyncProtocol.KEY_UPDATED_AT, profile.meta.updatedAt)
            }.asPutDataRequest().setUrgent()
            dataClient.putDataItem(request).await()
            repository.markProfileSynced()
            Diagnostics.log(context, TAG, "Profil abgelegt")
            true
        }.onFailure {
            Diagnostics.log(context, TAG, "Profil nicht abgelegt", it)
        }.getOrDefault(false)
    }

    private companion object {
        const val TAG = "BouldClockSync"
    }
}

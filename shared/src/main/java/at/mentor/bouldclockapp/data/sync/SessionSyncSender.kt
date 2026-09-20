package at.mentor.bouldclockapp.data.sync

import android.content.Context
import at.mentor.bouldclockapp.core.diagnostics.Diagnostics
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
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
            }.onFailure {
                // Nicht markieren - beim naechsten Versuch noch einmal.
                Diagnostics.log(context, TAG, "Session ${session.id.take(8)} nicht abgelegt", it)
            }
        }
        return sent
    }

    private companion object {
        const val TAG = "BouldClockSync"
    }
}

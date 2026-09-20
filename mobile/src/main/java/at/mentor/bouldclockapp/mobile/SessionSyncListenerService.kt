package at.mentor.bouldclockapp.mobile

import at.mentor.bouldclockapp.core.diagnostics.Diagnostics
import at.mentor.bouldclockapp.data.db.BouldClockDatabase
import at.mentor.bouldclockapp.data.sync.SessionPayload
import at.mentor.bouldclockapp.data.sync.SessionSyncRepository
import at.mentor.bouldclockapp.data.sync.SyncProtocol
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.runBlocking

/**
 * Nimmt Sessions von der Uhr entgegen.
 *
 * Wird vom System aufgeweckt, sobald ein Datenpunkt ankommt - die App muss dafuer
 * nicht offen sein. Genau deshalb der Weg ueber die Data Layer API und nicht
 * ueber eine direkte Verbindung.
 */
class SessionSyncListenerService : WearableListenerService() {

    override fun onDataChanged(events: DataEventBuffer) {
        val repository = SessionSyncRepository(BouldClockDatabase.get(applicationContext))

        events.forEach { event ->
            if (event.type != DataEvent.TYPE_CHANGED) return@forEach
            val path = event.dataItem.uri.path ?: return@forEach
            if (!path.startsWith(SyncProtocol.SESSION_PATH_PREFIX)) return@forEach

            val asset = DataMapItem.fromDataItem(event.dataItem)
                .dataMap
                .getAsset(SyncProtocol.KEY_PAYLOAD)
                ?: return@forEach

            // onDataChanged laeuft bereits auf einem Hintergrundthread.
            runCatching {
                val bytes = readAsset(asset)
                val payload = SessionPayload.fromJson(SyncProtocol.unpack(bytes))
                runBlocking { repository.apply(payload) }
                Diagnostics.log(
                    applicationContext,
                    TAG,
                    "Session ${payload.session.id.take(8)} uebernommen: " +
                        "${payload.attempts.size} Versuche, ${payload.hrSamples.size} Pulswerte",
                )
            }.onFailure {
                Diagnostics.log(applicationContext, TAG, "Paket von $path nicht lesbar", it)
            }
        }
    }

    private fun readAsset(asset: Asset): ByteArray =
        Tasks.await(Wearable.getDataClient(applicationContext).getFdForAsset(asset))
            .inputStream
            .use { it.readBytes() }

    private companion object {
        const val TAG = "BouldClockSync"
    }
}

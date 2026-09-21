package at.mentor.bouldclockapp.data.sync

import at.mentor.bouldclockapp.core.diagnostics.Diagnostics
import at.mentor.bouldclockapp.data.db.BouldClockDatabase
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Nimmt Sessions und Profil von der Gegenseite entgegen.
 *
 * Auf **beiden** Geraeten dieselbe Klasse, in beiden Manifesten eingetragen:
 * Sessions wandern von der Uhr zum Handy, bearbeitete Sessions und das Profil
 * den umgekehrten Weg. Das System weckt die App dafuer auf, sie muss nicht offen
 * sein.
 *
 * Kein Kreisverkehr: [SessionSyncRepository] uebernimmt nur, was juenger ist als
 * das Vorhandene, und verschickt von sich aus nichts.
 */
class SyncListenerService : WearableListenerService() {

    override fun onDataChanged(events: DataEventBuffer) {
        val repository = SessionSyncRepository(
            BouldClockDatabase.get(applicationContext),
            applicationContext.filesDir,
        )

        events.forEach { event ->
            if (event.type != DataEvent.TYPE_CHANGED) return@forEach
            val path = event.dataItem.uri.path ?: return@forEach

            val asset = DataMapItem.fromDataItem(event.dataItem)
                .dataMap
                .getAsset(SyncProtocol.KEY_PAYLOAD)
                ?: return@forEach

            // onDataChanged laeuft bereits auf einem Hintergrundthread.
            runCatching {
                val json = SyncProtocol.unpack(readAsset(asset))
                when {
                    path == SyncProtocol.PROFILE_PATH -> handleProfile(repository, json)
                    path.startsWith(SyncProtocol.SESSION_PATH_PREFIX) -> handleSession(repository, json)
                    else -> Unit
                }
            }.onFailure {
                Diagnostics.log(applicationContext, TAG, "Paket von $path nicht lesbar", it)
            }
        }
    }

    /**
     * Nimmt eine Sensordatei entgegen.
     *
     * Der Pfad kommt vom anderen Geraet und wird geprueft, bevor irgendwo
     * geschrieben wird - eine Datei ausserhalb des App-Verzeichnisses waere das
     * Letzte, was eine Synchronisierung anrichten sollte.
     */
    override fun onChannelOpened(channel: ChannelClient.Channel) {
        val relativePath = SyncProtocol.relativePathFrom(channel.path) ?: return
        val client = Wearable.getChannelClient(applicationContext)
        val repository = SessionSyncRepository(
            BouldClockDatabase.get(applicationContext),
            applicationContext.filesDir,
        )

        runCatching {
            val target = File(applicationContext.filesDir, relativePath)
            target.parentFile?.mkdirs()

            Tasks.await(client.getInputStream(channel)).use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }

            runBlocking { repository.markFileArrived(relativePath, target.length()) }
            Diagnostics.log(
                applicationContext,
                TAG,
                "$relativePath empfangen, ${target.length()} Byte",
            )
        }.onFailure {
            Diagnostics.log(applicationContext, TAG, "$relativePath nicht empfangen", it)
        }
    }

    private fun handleSession(repository: SessionSyncRepository, json: String) {
        val payload = SessionPayload.fromJson(json)
        runBlocking { repository.apply(payload) }
        Diagnostics.log(
            applicationContext,
            TAG,
            "Session ${payload.session.id.take(8)} uebernommen: " +
                "${payload.attempts.size} Versuche, ${payload.hrSamples.size} Pulswerte",
        )
    }

    private fun handleProfile(repository: SessionSyncRepository, json: String) {
        val payload = ProfilePayload.fromJson(json)
        val taken = runBlocking { repository.applyProfile(payload) }
        Diagnostics.log(
            applicationContext,
            TAG,
            if (taken) {
                "Profil uebernommen: ${payload.profile.weightKg} kg"
            } else {
                "Profil verworfen - vorhandenes ist neuer"
            },
        )
    }

    private fun readAsset(asset: Asset): ByteArray =
        Tasks.await(Wearable.getDataClient(applicationContext).getFdForAsset(asset))
            .inputStream
            .use { it.readBytes() }

    private companion object {
        const val TAG = "BouldClockSync"
    }
}

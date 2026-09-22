package at.mentor.bouldclockapp.data.health

import android.content.Context
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.data.HeartRateAccuracy
import at.mentor.bouldclockapp.core.diagnostics.Diagnostics
import at.mentor.bouldclockapp.core.metrics.SessionMetrics
import kotlin.math.roundToInt
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Ein Messwert samt Zustand, damit die Anzeige erklaeren kann, warum nichts kommt. */
data class Measurement(val bpm: Int?, val state: HeartRateState)

/**
 * Puls auf Abruf, ausserhalb einer Session.
 *
 * Health Services hat dafuer den `MeasureClient` - eine Einzelmessung, ohne
 * eine Uebung zu starten. Genau das, was die Ruhepulsmessung braucht: nichts
 * wird aufgezeichnet, nichts gezaehlt, es geht nur um die Zahl.
 *
 * Nur waehrend jemand hinschaut: die Messung zieht spuerbar Strom, weil der
 * optische Sensor dauerhaft leuchtet.
 */
class HeartRateMeasurer(private val context: Context) {

    private val measureClient = HealthServices.getClient(context).measureClient

    fun measure(): Flow<Measurement> = callbackFlow {
        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(
                dataType: DeltaDataType<*, *>,
                availability: Availability,
            ) {
                val state = when (availability) {
                    DataTypeAvailability.AVAILABLE -> HeartRateState.MEASURING
                    DataTypeAvailability.ACQUIRING -> HeartRateState.STARTING
                    DataTypeAvailability.UNAVAILABLE_DEVICE_OFF_BODY -> HeartRateState.OFF_BODY
                    DataTypeAvailability.UNAVAILABLE -> HeartRateState.UNAVAILABLE
                    else -> return
                }
                trySend(Measurement(bpm = null, state = state))
            }

            override fun onDataReceived(data: DataPointContainer) {
                data.getData(DataType.HEART_RATE_BPM).forEach { point ->
                    val accuracy = (point.accuracy as? HeartRateAccuracy)?.sensorStatus?.id ?: 0
                    if (accuracy < SessionMetrics.MIN_HR_ACCURACY) return@forEach
                    trySend(
                        Measurement(
                            bpm = point.value.roundToInt(),
                            state = HeartRateState.MEASURING,
                        ),
                    )
                }
            }
        }

        runCatching { measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback) }
            .onFailure {
                Diagnostics.log(context, TAG, "Messung nicht gestartet", it)
                trySend(Measurement(null, HeartRateState.UNAVAILABLE))
            }

        awaitClose {
            runCatching {
                measureClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, callback)
            }
        }
    }

    private companion object {
        const val TAG = "BouldClockMeasure"
    }
}

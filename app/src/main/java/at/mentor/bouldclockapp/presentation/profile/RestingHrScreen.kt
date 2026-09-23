package at.mentor.bouldclockapp.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import at.mentor.bouldclockapp.core.model.RestDurations
import at.mentor.bouldclockapp.data.health.HeartRateState
import at.mentor.bouldclockapp.presentation.session.RestingHrState

/**
 * Ruhepuls messen.
 *
 * Zwei Minuten still sitzen. Die Zahl ist der wichtigste einzelne Wert der
 * Kalorienrechnung - sie geht doppelt ein, in die Pulsreserve und in die daraus
 * geschaetzte Ausdauer. Und sie entsteht hier selbst, nicht in Samsungs
 * Nachtauswertung: dieselbe Messung ergibt spaeter auf einer Garmin dieselbe Zahl.
 */
@Composable
fun RestingHrScreen(
    state: RestingHrState,
    onStart: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    /** Beim ersten Start: erklaeren und ueberspringen lassen. */
    firstRun: Boolean = false,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        when (state) {
            RestingHrState.Idle -> {
                Text("Ruhepuls", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (firstRun) {
                        "Für die Kalorien brauchen wir deinen Ruhepuls. " +
                            "Zwei Minuten still sitzen – danach nie wieder."
                    } else {
                        "Zwei Minuten ruhig sitzen bleiben."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Button(onClick = onStart) {
                    Text("Messen")
                }
                if (firstRun) {
                    // Ueberspringbar: ohne Messung rechnet die App mit einer
                    // Annahme weiter, nur ungenauer. Das ist besser als jemand,
                    // der beim ersten Start zwei Minuten warten soll.
                    Button(onClick = onBack, colors = ButtonDefaults.filledTonalButtonColors()) {
                        Text("Später")
                    }
                }
            }

            is RestingHrState.Measuring -> {
                Text(
                    text = state.bpm?.toString() ?: "–",
                    style = MaterialTheme.typography.numeralMedium,
                )
                Text(
                    text = when (state.sensor) {
                        HeartRateState.OFF_BODY -> "Uhr anlegen"
                        HeartRateState.UNAVAILABLE -> "Sensor meldet sich ab"
                        else -> RestDurations.format(state.remainingSeconds * 1000L)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${state.samples} Messwerte",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            is RestingHrState.Done -> {
                Text(
                    text = state.bpm?.toString() ?: "–",
                    style = MaterialTheme.typography.numeralMedium,
                )
                Text(
                    text = if (state.bpm != null) {
                        "Ruhepuls gespeichert"
                    } else {
                        "Zu wenige Messwerte – bitte noch einmal"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Button(onClick = onBack, colors = ButtonDefaults.filledTonalButtonColors()) {
                    Text("Fertig")
                }
            }
        }
    }
}

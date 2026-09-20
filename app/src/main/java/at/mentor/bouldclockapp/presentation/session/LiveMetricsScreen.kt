package at.mentor.bouldclockapp.presentation.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import at.mentor.bouldclockapp.presentation.theme.BouldClockAppTheme
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Die Zahlen der laufenden Session, erreichbar durch Blaettern nach unten.
 *
 * Bewusst ohne Ausloeser: hier wird geschaut, nicht geschaltet. Wer beim
 * Datengucken versehentlich einen Versuch startet, verliert mehr als er gewinnt.
 *
 * Werte, die noch nicht vorliegen, stehen als Strich da. Eine Null waere eine
 * Behauptung - der Puls ist nicht null, er ist unbekannt, etwa wenn die Uhr
 * keinen Hautkontakt hat.
 */
@Composable
fun LiveMetricsScreen(
    bpm: Int?,
    kcal: Double?,
    climbHeightMeters: Double?,
    attemptCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = bpm?.toString() ?: "–",
            style = MaterialTheme.typography.numeralMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Puls",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LiveRow("Kalorien", kcal?.let { "${it.roundToInt()} kcal" })
        LiveRow("Höhe", climbHeightMeters?.let { String.format(Locale.GERMAN, "%.1f m", it) })
        LiveRow("Versuche", attemptCount.toString())
    }
}

@Composable
private fun LiveRow(label: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value ?: "–",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@WearPreviewDevices
@Composable
private fun LiveMetricsPreview() {
    BouldClockAppTheme {
        LiveMetricsScreen(
            bpm = 142,
            kcal = 218.0,
            climbHeightMeters = 27.4,
            attemptCount = 14,
        )
    }
}

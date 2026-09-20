package at.mentor.bouldclockapp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Picker
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.rememberPickerState
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import at.mentor.bouldclockapp.core.model.RestDurations
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.presentation.theme.BouldClockAppTheme

/**
 * Soll-Pause zwischen zwei Versuchen, per Drehkranz.
 *
 * Nicht zu verwechseln mit dem 60-Sekunden-Fenster der Herzfrequenz-Erholung:
 * das ist ein fester Messzeitraum, dies hier ist deine Trainingsentscheidung.
 * Sie greifen nur insofern ineinander, als bei einer Soll-Pause unter 60 s kein
 * HRR60 mehr erhoben werden kann - der naechste Versuch faellt dann ins Fenster.
 */
@Composable
fun RestDurationPicker(
    valueMs: Long,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = RestDurations.OPTIONS
    val pickerState = rememberPickerState(
        initialNumberOfOptions = options.size,
        initiallySelectedIndex = RestDurations.nearestIndex(valueMs),
        shouldRepeatOptions = false,
    )

    LaunchedEffect(pickerState) {
        snapshotFlow { pickerState.selectedOptionIndex }
            .collect { index -> options.getOrNull(index)?.let(onValueChange) }
    }

    Picker(
        state = pickerState,
        contentDescription = {
            RestDurations.format(options.getOrElse(pickerState.selectedOptionIndex) { valueMs })
        },
        modifier = modifier,
    ) { index ->
        val isSelected = index == selectedOptionIndex
        Text(
            text = RestDurations.format(options.getOrElse(index) { 0L }),
            textAlign = TextAlign.Center,
            style = if (isSelected) {
                MaterialTheme.typography.numeralMedium
            } else {
                MaterialTheme.typography.numeralSmall
            },
            color = if (isSelected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

/** Pausenauswahl mit Begruendung darunter - warum diese Laenge zu diesem Training passt. */
@Composable
fun RestDurationPickerWithHint(
    valueMs: Long,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        RestDurationPicker(valueMs = valueMs, onValueChange = onValueChange)
        Text(
            text = restHint(valueMs),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Was die gewaehlte Pause trainiert.
 *
 * Steht hier, weil es die haeufigste stille Fehlentscheidung im Bouldern ist:
 * man will an Maximalkraft arbeiten, pausiert eine Minute und trainiert
 * versehentlich Kraftausdauer.
 */
private fun restHint(valueMs: Long): String = when {
    valueMs < 60_000L -> "Kraftausdauer, kein HRR60"
    valueMs < 150_000L -> "Kraftausdauer"
    valueMs < 300_000L -> "gemischt"
    else -> "Maximalkraft"
}

@WearPreviewDevices
@Composable
private fun RestDurationPickerPreview() {
    BouldClockAppTheme {
        var rest by remember { mutableLongStateOf(SessionType.LIMIT.defaultRestMs) }
        RestDurationPickerWithHint(valueMs = rest, onValueChange = { rest = it })
    }
}

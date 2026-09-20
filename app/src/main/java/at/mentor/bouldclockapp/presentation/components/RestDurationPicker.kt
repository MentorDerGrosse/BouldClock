package at.mentor.bouldclockapp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.PickerGroup
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.rememberPickerState
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import at.mentor.bouldclockapp.core.model.RestDurations
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.presentation.theme.BouldClockAppTheme

/**
 * Pausenlaenge als Minuten und Sekunden, zwei Raeder nebeneinander.
 *
 * Antippen waehlt das Rad, der Drehkranz stellt es. Minuten ab eins - unter
 * einer Minute liesse sich HRR60 nicht mehr erheben.
 */
@Composable
fun RestDurationPicker(
    valueMs: Long,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val minuteState = rememberPickerState(
        initialNumberOfOptions = RestDurations.MINUTES.size,
        initiallySelectedIndex = RestDurations.minuteIndexOf(valueMs),
        shouldRepeatOptions = false,
    )
    val secondState = rememberPickerState(
        initialNumberOfOptions = RestDurations.SECONDS.size,
        initiallySelectedIndex = RestDurations.secondIndexOf(valueMs),
        shouldRepeatOptions = false,
    )
    var minutesActive by remember { mutableStateOf(true) }

    LaunchedEffect(minuteState, secondState) {
        snapshotFlow { minuteState.selectedOptionIndex to secondState.selectedOptionIndex }
            .collect { (minuteIndex, secondIndex) ->
                onValueChange(
                    RestDurations.of(
                        minutes = RestDurations.MINUTES.getOrElse(minuteIndex) { 1 },
                        seconds = RestDurations.SECONDS.getOrElse(secondIndex) { 0 },
                    ),
                )
            }
    }

    PickerGroup(
        modifier = modifier,
        selectedPickerState = if (minutesActive) minuteState else secondState,
    ) {
        PickerGroupItem(
            pickerState = minuteState,
            selected = minutesActive,
            onSelected = { minutesActive = true },
            contentDescription = {
                "${RestDurations.MINUTES.getOrElse(minuteState.selectedOptionIndex) { 1 }} Minuten"
            },
        ) { index, pickerSelected ->
            PickerNumber(
                text = RestDurations.MINUTES.getOrElse(index) { 1 }.toString(),
                highlighted = pickerSelected && index == selectedOptionIndex,
            )
        }

        Text(
            text = ":",
            style = MaterialTheme.typography.numeralSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        PickerGroupItem(
            pickerState = secondState,
            selected = !minutesActive,
            onSelected = { minutesActive = false },
            contentDescription = {
                "${RestDurations.SECONDS.getOrElse(secondState.selectedOptionIndex) { 0 }} Sekunden"
            },
        ) { index, pickerSelected ->
            PickerNumber(
                text = RestDurations.SECONDS.getOrElse(index) { 0 }.toString().padStart(2, '0'),
                highlighted = pickerSelected && index == selectedOptionIndex,
            )
        }
    }
}

@Composable
private fun PickerNumber(text: String, highlighted: Boolean) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        style = if (highlighted) {
            MaterialTheme.typography.numeralMedium
        } else {
            MaterialTheme.typography.numeralSmall
        },
        color = if (highlighted) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    )
}

/** Auswahlbildschirm fuer die Pause, mit Hinweis was die Laenge trainiert. */
@Composable
fun RestDurationScreen(
    valueMs: Long,
    onValueChange: (Long) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, top = 22.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        RestDurationPicker(
            valueMs = valueMs,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = RestDurations.trainingHint(valueMs),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CompactButton(
            onClick = onConfirm,
            label = { Text("Start", style = MaterialTheme.typography.labelMedium, maxLines = 1) },
        )
    }
}

@WearPreviewDevices
@Composable
private fun RestDurationPreview() {
    BouldClockAppTheme {
        var rest by remember { mutableLongStateOf(SessionType.LIMIT.defaultRestMs) }
        RestDurationScreen(valueMs = rest, onValueChange = { rest = it }, onConfirm = {})
    }
}

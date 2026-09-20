package at.mentor.bouldclockapp.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Picker
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.rememberPickerState

/**
 * Zahlenrad am Drehkranz.
 *
 * Gemeinsame Grundlage fuer Winkel, Gewicht und Alter - dreimal dasselbe
 * Verhalten: kein Umlauf, ausgewaehlter Wert gross, Nachbarn gedaempft.
 * Die Gradauswahl bleibt eigenstaendig, weil sie Skalen umrechnet.
 */
@Composable
fun ValuePicker(
    values: List<Int>,
    selected: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    format: (Int) -> String = { it.toString() },
) {
    val startIndex = remember(values) { values.indexOf(selected).coerceAtLeast(0) }
    val pickerState = rememberPickerState(
        initialNumberOfOptions = values.size,
        initiallySelectedIndex = startIndex,
        shouldRepeatOptions = false,
    )

    LaunchedEffect(pickerState, values) {
        snapshotFlow { pickerState.selectedOptionIndex }
            .collect { index -> values.getOrNull(index)?.let(onSelected) }
    }

    Picker(
        state = pickerState,
        contentDescription = { format(values.getOrElse(pickerState.selectedOptionIndex) { selected }) },
        modifier = modifier,
    ) { index ->
        val isSelected = index == selectedOptionIndex
        Text(
            text = format(values.getOrElse(index) { selected }),
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

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
import at.mentor.bouldclockapp.core.model.BoardAngles

/**
 * Boardwinkel per Drehkranz.
 *
 * Wie beim Grad: vorgeschlagen wird der zuletzt eingestellte Winkel, denn
 * meistens klettert man mehrere Boulder am selben. Kein Umlauf - von 70 zurueck
 * auf 0 zu springen waere immer ein Versehen.
 */
@Composable
fun AnglePicker(
    degrees: Int,
    onDegreesChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val values = remember { BoardAngles.DEGREES }
    val pickerState = rememberPickerState(
        initialNumberOfOptions = values.size,
        initiallySelectedIndex = BoardAngles.indexOf(degrees),
        shouldRepeatOptions = false,
    )

    LaunchedEffect(pickerState) {
        snapshotFlow { pickerState.selectedOptionIndex }
            .collect { index -> values.getOrNull(index)?.let(onDegreesChange) }
    }

    Picker(
        state = pickerState,
        contentDescription = {
            BoardAngles.format(values.getOrElse(pickerState.selectedOptionIndex) { degrees })
        },
        modifier = modifier,
    ) { index ->
        val isSelected = index == selectedOptionIndex
        Text(
            text = BoardAngles.format(values.getOrElse(index) { BoardAngles.DEFAULT }),
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

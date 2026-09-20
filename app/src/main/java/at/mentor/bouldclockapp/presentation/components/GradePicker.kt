package at.mentor.bouldclockapp.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Picker
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.rememberPickerState
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.presentation.theme.BouldClockAppTheme
import kotlin.math.abs

/**
 * Gradauswahl per Drehkranz - das Gegenstueck zum Dropdown auf der Uhr.
 *
 * Bedient wird das mit kalkigen Fingern in einer lauten Halle, deshalb Bezel
 * statt Tippen: [Picker] bringt das Rotary-Snapping mit, eine Raste je Stufe.
 *
 * [value] ist die Stufe auf der kanonischen Leiter, nicht der Index im Picker.
 * Beides faellt nur in der Font-Skala zusammen, weil V-Grade mehrere Stufen
 * unter einer Beschriftung zusammenfassen.
 */
@Composable
fun GradePicker(
    system: GradeSystem,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Beim Skalenwechsel aendert sich die Anzahl der Optionen (24 vs. 19).
    // key() baut den Picker-State neu auf, statt ihn inkonsistent weiterzufuehren.
    key(system) {
        val values = remember(system) { Grades.pickerValues(system) }
        val startIndex = remember(system) { nearestIndex(values, value) }

        val pickerState = rememberPickerState(
            initialNumberOfOptions = values.size,
            initiallySelectedIndex = startIndex,
            // Kein Umlauf: von 9A zurueck auf 3 zu springen ist immer ein Versehen.
            shouldRepeatOptions = false,
        )

        LaunchedEffect(pickerState, values) {
            snapshotFlow { pickerState.selectedOptionIndex }
                .collect { index -> values.getOrNull(index)?.let(onValueChange) }
        }

        Picker(
            state = pickerState,
            contentDescription = {
                Grades.label(values.getOrElse(pickerState.selectedOptionIndex) { value }, system)
            },
            modifier = modifier,
        ) { index ->
            val isSelected = index == selectedOptionIndex
            Text(
                text = Grades.label(values.getOrElse(index) { Grades.MIN_VALUE }, system),
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
}

/** Stufe, die [target] am naechsten kommt - der exakte Wert kann in dieser Skala fehlen. */
private fun nearestIndex(values: List<Int>, target: Int): Int =
    values.indices.minByOrNull { abs(values[it] - target) } ?: 0

@WearPreviewDevices
@Composable
private fun GradePickerPreview() {
    BouldClockAppTheme {
        var grade by remember { mutableIntStateOf(Grades.parse("6C") ?: 0) }
        GradePicker(
            system = GradeSystem.FONT,
            value = grade,
            onValueChange = { grade = it },
        )
    }
}

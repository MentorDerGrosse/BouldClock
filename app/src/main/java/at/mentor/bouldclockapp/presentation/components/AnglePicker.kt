package at.mentor.bouldclockapp.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import at.mentor.bouldclockapp.core.model.BoardAngles

/**
 * Boardwinkel per Drehkranz.
 *
 * Vorgeschlagen wird der zuletzt eingestellte Winkel, denn meistens klettert man
 * mehrere Boulder am selben.
 */
@Composable
fun AnglePicker(
    degrees: Int,
    onDegreesChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    ValuePicker(
        values = BoardAngles.DEGREES,
        selected = BoardAngles.clamp(degrees),
        onSelected = onDegreesChange,
        modifier = modifier,
        format = BoardAngles::format,
    )
}

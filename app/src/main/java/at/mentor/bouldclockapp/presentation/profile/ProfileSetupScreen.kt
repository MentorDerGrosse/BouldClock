package at.mentor.bouldclockapp.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import at.mentor.bouldclockapp.core.model.BiologicalSex
import at.mentor.bouldclockapp.core.model.ProfileRanges
import at.mentor.bouldclockapp.presentation.components.ValuePicker
import at.mentor.bouldclockapp.presentation.theme.BouldClockAppTheme

/**
 * Einmalige Abfrage der Koerperdaten beim ersten Start.
 *
 * Drei Schritte statt eines Bildschirms mit drei Raedern: auf 438 Pixel waere
 * das unlesbar, und mehr als dreimal "Weiter" tippt hier ohnehin niemand.
 *
 * Bearbeiten spaeter nur am Handy - die Uhr fragt genau einmal.
 */
@Composable
fun ProfileSetupScreen(
    onSave: (weightKg: Int, ageYears: Int, sex: BiologicalSex) -> Unit,
    modifier: Modifier = Modifier,
) {
    var step by remember { mutableStateOf(Step.WEIGHT) }
    var weightKg by remember { mutableIntStateOf(ProfileRanges.DEFAULT_WEIGHT_KG) }
    var ageYears by remember { mutableIntStateOf(ProfileRanges.DEFAULT_AGE_YEARS) }

    when (step) {
        Step.WEIGHT -> SetupStep(
            caption = "Gewicht",
            onConfirm = { step = Step.AGE },
            modifier = modifier,
        ) {
            ValuePicker(
                values = ProfileRanges.WEIGHT_KG,
                selected = weightKg,
                onSelected = { weightKg = it },
                modifier = Modifier.weight(1f),
                format = { "$it kg" },
            )
        }

        Step.AGE -> SetupStep(
            caption = "Alter",
            onConfirm = { step = Step.SEX },
            modifier = modifier,
        ) {
            ValuePicker(
                values = ProfileRanges.AGE_YEARS,
                selected = ageYears,
                onSelected = { ageYears = it },
                modifier = Modifier.weight(1f),
            )
        }

        // Drei feste Moeglichkeiten - da ist ein Rad umstaendlicher als Knoepfe.
        Step.SEX -> Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = "Geschlecht",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BiologicalSex.entries.forEach { sex ->
                CompactButton(
                    onClick = { onSave(weightKg, ageYears, sex) },
                    label = {
                        Text(
                            text = sex.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun SetupStep(
    caption: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    picker: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, top = 22.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        picker()
        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CompactButton(
            onClick = onConfirm,
            label = { Text("Weiter", style = MaterialTheme.typography.labelMedium, maxLines = 1) },
        )
    }
}

private enum class Step { WEIGHT, AGE, SEX }

@WearPreviewDevices
@Composable
private fun ProfileSetupPreview() {
    BouldClockAppTheme {
        ProfileSetupScreen(onSave = { _, _, _ -> })
    }
}

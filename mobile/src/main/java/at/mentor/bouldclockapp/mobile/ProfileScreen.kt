package at.mentor.bouldclockapp.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.mentor.bouldclockapp.core.model.BiologicalSex
import at.mentor.bouldclockapp.core.model.ProfileRanges
import at.mentor.bouldclockapp.data.db.entity.UserProfileEntity

/**
 * Koerperdaten bearbeiten.
 *
 * Die Uhr fragt sie einmal beim ersten Start ab, geaendert werden sie hier - so
 * war es abgemacht. Beim Speichern geht das Profil zur Uhr zurueck; dort
 * gewinnt der juengere Zeitstempel.
 *
 * Ohne Gewicht keine Kalorien, deshalb ist das Feld nie leer, sondern
 * vorbelegt.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    profile: UserProfileEntity?,
    onSave: (weightKg: Int, ageYears: Int, sex: BiologicalSex) -> Unit,
    onBack: () -> Unit,
) {
    var weightKg by remember(profile) {
        mutableIntStateOf(profile?.weightKg ?: ProfileRanges.DEFAULT_WEIGHT_KG)
    }
    var ageYears by remember(profile) {
        mutableIntStateOf(profile?.ageInYears() ?: ProfileRanges.DEFAULT_AGE_YEARS)
    }
    var sex by remember(profile) {
        mutableStateOf(profile?.sex ?: BiologicalSex.UNSPECIFIED)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Zurück") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            ValueSlider(
                label = "Gewicht",
                value = weightKg,
                display = "$weightKg kg",
                range = ProfileRanges.WEIGHT_KG,
                onChange = { weightKg = it },
            )

            ValueSlider(
                label = "Alter",
                value = ageYears,
                display = "$ageYears Jahre",
                range = ProfileRanges.AGE_YEARS,
                onChange = { ageYears = it },
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Geschlecht", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BiologicalSex.entries.forEach { option ->
                        FilterChip(
                            selected = sex == option,
                            onClick = { sex = option },
                            label = { Text(option.displayName) },
                        )
                    }
                }
                Text(
                    text = "Wird nur für den Grundumsatz in der Kalorienberechnung verwendet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Button(
                onClick = { onSave(weightKg, ageYears, sex) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Speichern")
            }
        }
    }
}

@Composable
private fun ValueSlider(
    label: String,
    value: Int,
    display: String,
    range: List<Int>,
    onChange: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(label = label, value = display)
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = range.first().toFloat()..range.last().toFloat(),
            steps = range.size - 2,
        )
    }
}

@Composable
private fun Row(label: String, value: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

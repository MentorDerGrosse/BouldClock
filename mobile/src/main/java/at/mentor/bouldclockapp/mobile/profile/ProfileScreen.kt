package at.mentor.bouldclockapp.mobile.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import at.mentor.bouldclockapp.mobile.ui.BouldCard
import at.mentor.bouldclockapp.mobile.ui.SectionHeader
import at.mentor.bouldclockapp.mobile.ui.StatRow
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * Das Nutzerprofil.
 *
 * Angelegt wird es auf der Uhr beim ersten Start, bearbeitet nur hier - Gewicht
 * und Alter tippt niemand gerne auf einem Drehkranz nach. Beim Speichern gewinnt
 * das juengere: was hier steht, ueberschreibt die Uhr.
 *
 * Gebraucht wird es fuer die Kalorien; ohne Gewicht sind die geraten.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    profile: UserProfileEntity?,
    onSave: (
        weightKg: Int,
        ageYears: Int,
        sex: BiologicalSex,
        heightCm: Int?,
        restingHrBpm: Int?,
    ) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    val currentYear = LocalDate.now().year
    var weight by remember(profile) {
        mutableIntStateOf(profile?.weightKg ?: ProfileRanges.DEFAULT_WEIGHT_KG)
    }
    var age by remember(profile) {
        mutableIntStateOf(profile?.birthYear?.let { currentYear - it } ?: ProfileRanges.DEFAULT_AGE_YEARS)
    }
    var sex by remember(profile) {
        mutableStateOf(profile?.sex ?: BiologicalSex.UNSPECIFIED)
    }
    var height by remember(profile) { mutableIntStateOf(profile?.heightCm ?: DEFAULT_HEIGHT_CM) }
    var restingHr by remember(profile) {
        mutableIntStateOf(profile?.restingHrBpm ?: DEFAULT_RESTING_HR)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SectionHeader("Körper")
        BouldCard {
            ValueSlider(
                label = "Gewicht",
                value = "$weight kg",
                position = weight.toFloat(),
                range = ProfileRanges.WEIGHT_KG.first().toFloat()..ProfileRanges.WEIGHT_KG.last().toFloat(),
                onChange = { weight = it.roundToInt() },
            )
            ValueSlider(
                label = "Größe",
                value = "$height cm",
                position = height.toFloat(),
                range = MIN_HEIGHT_CM.toFloat()..MAX_HEIGHT_CM.toFloat(),
                onChange = { height = it.roundToInt() },
            )
            ValueSlider(
                label = "Alter",
                value = "$age Jahre",
                position = age.toFloat(),
                range = ProfileRanges.AGE_YEARS.first().toFloat()..ProfileRanges.AGE_YEARS.last().toFloat(),
                onChange = { age = it.roundToInt() },
            )
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
        }

        SectionHeader("Herz")
        BouldCard {
            ValueSlider(
                label = "Ruhepuls",
                value = "$restingHr bpm",
                position = restingHr.toFloat(),
                range = MIN_RESTING_HR.toFloat()..MAX_RESTING_HR.toFloat(),
                onChange = { restingHr = it.roundToInt() },
            )
            Text(
                text = "Der wichtigste Wert für die Kalorien: er geht doppelt ein – " +
                    "in die Pulsreserve und in die daraus geschätzte Ausdauer. " +
                    "Genauer als schätzen ist „Ruhepuls messen“ auf der Uhr.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StatRow(
                label = "Höchster gemessener Puls",
                value = profile?.maxHrBpm?.let { "$it bpm" } ?: "noch keiner",
            )
        }

        Button(
            onClick = { onSave(weight, age, sex, height, restingHr) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Speichern") }

        Text(
            text = "Wird für die Kalorienberechnung gebraucht und sofort zur Uhr " +
                "geschickt. Der jüngere Stand gewinnt.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ValueSlider(
    label: String,
    value: String,
    position: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
        Slider(value = position, onValueChange = onChange, valueRange = range)
    }
}

private const val DEFAULT_HEIGHT_CM = 175
private const val MIN_HEIGHT_CM = 130
private const val MAX_HEIGHT_CM = 220

private const val DEFAULT_RESTING_HR = 60
private const val MIN_RESTING_HR = 35
private const val MAX_RESTING_HR = 100

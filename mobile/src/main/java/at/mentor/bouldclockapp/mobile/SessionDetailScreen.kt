package at.mentor.bouldclockapp.mobile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.core.text.attemptLabel
import at.mentor.bouldclockapp.data.db.entity.AttemptEntity
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import kotlin.math.roundToInt

/**
 * Eine Session im Detail - und der Ort, an dem nachgetragen wird.
 *
 * Alles, was auf der Uhr zu umstaendlich waere: Anstrengung, Notiz, Ergebnis und
 * Grad einzelner Versuche, und Versuche loeschen, die nie welche waren.
 *
 * Jede Aenderung geht sofort zur Uhr zurueck. Kein Speichern-Knopf - bei einem
 * Schieberegler und ein paar Auswahlfeldern waere er nur eine zusaetzliche
 * Gelegenheit, Aenderungen zu verlieren.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SessionDetailScreen(
    detail: SessionDetail?,
    onBack: () -> Unit,
    onRpeChange: (Int?) -> Unit,
    onNoteChange: (String?) -> Unit,
    onOutcomeChange: (attemptId: String, outcome: AttemptOutcome?) -> Unit,
    onGradeChange: (attemptId: String, gradeValue: Int?) -> Unit,
    onDeleteAttempt: (attemptId: String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.let { formatSessionDate(it.session.startedAt) } ?: "Session") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Zurück") } },
            )
        },
    ) { padding ->
        if (detail == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Session nicht gefunden", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        val visibleAttempts = detail.attempts.filter { it.meta.deletedAt == null }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { StatsCard(detail.summary, visibleAttempts.size) }
            item { RpeCard(detail.session.rpe, onRpeChange) }
            item { NoteCard(detail.session.note, onNoteChange) }

            item {
                Text(
                    text = "Versuche",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            if (visibleAttempts.isEmpty()) {
                item {
                    Text(
                        text = "Keine Versuche aufgezeichnet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(visibleAttempts, key = { it.id }) { attempt ->
                    AttemptCard(
                        attempt = attempt,
                        onOutcomeChange = { onOutcomeChange(attempt.id, it) },
                        onGradeChange = { onGradeChange(attempt.id, it) },
                        onDelete = { onDeleteAttempt(attempt.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsCard(summary: SessionSummaryEntity?, attemptCount: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            StatLine("Versuche", attemptLabel(attemptCount))
            summary?.let {
                StatLine("Tops", "${it.sendCount}")
                StatLine("Dauer", formatDuration(it.totalMs))
                StatLine("Wandzeit", formatDuration(it.workMs))
                it.hrAvg?.let { avg -> StatLine("Puls", it.hrMax?.let { max -> "$avg / $max" } ?: "$avg") }
                it.caloriesTotal?.let { kcal -> StatLine("Kalorien", "${kcal.roundToInt()} kcal") }
                it.climbHeightMeters?.let { m -> StatLine("Kletterhöhe", formatMeters(m)) }
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value)
    }
}

/**
 * Subjektive Anstrengung von 1 bis 10.
 *
 * Die billigste und erstaunlich aussagekraeftigste Zahl im Training - aber auf
 * der Uhr nach einer Session niemand tippt. Deshalb hier.
 */
@Composable
private fun RpeCard(rpe: Int?, onChange: (Int?) -> Unit) {
    // Waehrend des Ziehens nur oertlich mitfuehren und erst beim Loslassen
    // uebernehmen: sonst loest jeder Zwischenwert eine eigene Uebertragung zur
    // Uhr aus - eine Reglerbewegung waren drei Pakete ueber Bluetooth.
    var dragged by remember(rpe) { mutableStateOf((rpe ?: 5).toFloat()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Anstrengung", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = if (rpe == null) "–" else dragged.roundToInt().toString(),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Slider(
                value = dragged,
                onValueChange = { dragged = it },
                onValueChangeFinished = { onChange(dragged.roundToInt()) },
                valueRange = 1f..10f,
                steps = 8,
            )
            if (rpe != null) {
                TextButton(onClick = { onChange(null) }) { Text("Zurücksetzen") }
            }
        }
    }
}

@Composable
private fun NoteCard(note: String?, onChange: (String?) -> Unit) {
    var text by remember(note) { mutableStateOf(note.orEmpty()) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Notiz") },
                modifier = Modifier.fillMaxWidth(),
            )
            if (text != note.orEmpty()) {
                TextButton(onClick = { onChange(text) }) { Text("Notiz speichern") }
            }
        }
    }
}

/** Ein Versuch, aufklappbar zum Bearbeiten. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AttemptCard(
    attempt: AttemptEntity,
    onOutcomeChange: (AttemptOutcome?) -> Unit,
    onGradeChange: (Int?) -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("#${attempt.ordinal}", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = summarize(attempt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (expanded) {
                HorizontalDivider()

                Text("Ergebnis", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AttemptOutcome.entries.forEach { outcome ->
                        FilterChip(
                            selected = attempt.outcome == outcome,
                            onClick = {
                                onOutcomeChange(if (attempt.outcome == outcome) null else outcome)
                            },
                            label = { Text(outcome.displayName) },
                        )
                    }
                }

                Text("Grad", style = MaterialTheme.typography.labelLarge)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    TextButton(
                        onClick = {
                            val next = (attempt.gradeValue ?: Grades.DEFAULT_VALUE) - 1
                            onGradeChange(Grades.clamp(next))
                        },
                    ) { Text("−") }
                    Text(
                        text = attempt.gradeValue?.let { Grades.label(it, GradeSystem.FONT) } ?: "–",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    TextButton(
                        onClick = {
                            val next = (attempt.gradeValue ?: Grades.DEFAULT_VALUE) + 1
                            onGradeChange(Grades.clamp(next))
                        },
                    ) { Text("+") }
                }

                // Weich geloescht - ein hart entfernter Versuch kaeme bei der Uhr nie an.
                TextButton(onClick = onDelete) {
                    Text("Versuch löschen", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

private fun summarize(attempt: AttemptEntity): String = listOfNotNull(
    attempt.gradeValue?.let { Grades.label(it, GradeSystem.FONT) },
    attempt.boardAngleDegrees?.let { "$it°" },
    attempt.outcome?.displayName,
    attempt.climbHeightMeters?.let { formatMeters(it) },
).joinToString(" · ").ifEmpty { "ohne Angabe" }

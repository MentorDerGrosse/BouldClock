package at.mentor.bouldclockapp.mobile.sessions

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.BoardAngles
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.core.text.attemptLabel
import at.mentor.bouldclockapp.data.db.entity.AttemptEntity
import at.mentor.bouldclockapp.data.db.entity.GymEntity
import at.mentor.bouldclockapp.data.db.entity.ProblemEntity
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import at.mentor.bouldclockapp.mobile.BoulderRun
import at.mentor.bouldclockapp.mobile.SessionDetail
import at.mentor.bouldclockapp.mobile.formatDuration
import at.mentor.bouldclockapp.mobile.formatKcal
import at.mentor.bouldclockapp.mobile.formatMeters
import at.mentor.bouldclockapp.mobile.ui.BouldCard
import at.mentor.bouldclockapp.mobile.ui.EmptyState
import at.mentor.bouldclockapp.mobile.ui.SectionHeader
import at.mentor.bouldclockapp.mobile.ui.StatRow
import kotlin.math.roundToInt

/** Alles, was der Detailbildschirm auslöst. Gebuendelt, damit die Signatur lesbar bleibt. */
data class SessionDetailActions(
    val onRpeChange: (Int?) -> Unit,
    val onNoteChange: (String?) -> Unit,
    val onGymChange: (String?) -> Unit,
    val onDeleteSession: () -> Unit,
    val onOutcomeChange: (attemptId: String, outcome: AttemptOutcome?) -> Unit,
    val onGradeChange: (attemptId: String, gradeValue: Int?) -> Unit,
    val onAngleChange: (attemptId: String, degrees: Int?) -> Unit,
    val onTopMoveChange: (attemptId: String, move: Int?) -> Unit,
    val onDeleteAttempt: (attemptId: String) -> Unit,
    val onAssignProblem: (attemptIds: List<String>, problemId: String?) -> Unit,
    val onCreateProblem: (attemptIds: List<String>, label: String, gradeValue: Int?) -> Unit,
)

/**
 * Eine Session im Detail - und der Ort, an dem nachgetragen wird.
 *
 * Alles, was auf der Uhr zu umstaendlich waere: Halle, Anstrengung, Notiz,
 * Ergebnis, Grad, Winkel und Zughoehe einzelner Versuche, das Zusammenfuehren
 * von Bouldern - und das Loeschen ganzer Abende.
 *
 * Jede Aenderung geht sofort zur Uhr zurueck. Kein Speichern-Knopf; er waere
 * nur eine zusaetzliche Gelegenheit, Aenderungen zu verlieren.
 */
@Composable
fun SessionDetailScreen(
    detail: SessionDetail?,
    gyms: List<GymEntity>,
    problems: List<ProblemEntity>,
    actions: SessionDetailActions,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    if (detail == null) {
        EmptyState(
            title = "Session nicht gefunden",
            hint = "Vermutlich gerade gelöscht.",
            modifier = modifier.fillMaxSize().padding(contentPadding),
        )
        return
    }

    val visibleAttempts = detail.attempts.filter { it.meta.deletedAt == null }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { StatsCard(detail.summary, visibleAttempts.size) }

        item { SectionHeader("Halle") }
        item { GymCard(detail.gym, gyms, actions.onGymChange) }

        item { SectionHeader("Anstrengung") }
        item { RpeCard(detail.session.rpe, actions.onRpeChange) }

        item { SectionHeader("Notiz") }
        item { NoteCard(detail.session.note, actions.onNoteChange) }

        val boulders = detail.boulders
        if (boulders.isNotEmpty()) {
            item { SectionHeader("Boulder", trailing = "${boulders.size}") }
            boulders.forEachIndexed { index, boulder ->
                item(key = "boulder-${boulder.attempts.first().id}") {
                    BoulderCard(
                        index = index + 1,
                        boulder = boulder,
                        problems = problems,
                        canName = detail.gym != null || gyms.isNotEmpty(),
                        onAssign = { actions.onAssignProblem(boulder.attempts.map { it.id }, it) },
                        onCreate = { label, grade ->
                            actions.onCreateProblem(boulder.attempts.map { it.id }, label, grade)
                        },
                    )
                }
            }
        }

        item { SectionHeader("Versuche") }
        if (visibleAttempts.isEmpty()) {
            item {
                Text(
                    text = "Keine Versuche aufgezeichnet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            visibleAttempts.forEach { attempt ->
                item(key = attempt.id) { AttemptCard(attempt, actions) }
            }
        }

        item { DeleteSessionCard(actions.onDeleteSession) }
    }
}

@Composable
private fun StatsCard(summary: SessionSummaryEntity?, attemptCount: Int) {
    BouldCard {
        StatRow("Versuche", attemptLabel(attemptCount))
        summary?.let {
            StatRow("Tops", "${it.sendCount} · ${it.flashCount} Flash")
            StatRow("Dauer", formatDuration(it.totalMs))
            StatRow("Wandzeit", formatDuration(it.workMs))
            it.sendRate?.let { rate -> StatRow("Erfolgsquote", "${(rate * 100).roundToInt()} %") }
            it.hrAvg?.let { avg ->
                StatRow("Puls", it.hrMax?.let { max -> "$avg / $max" } ?: "$avg")
            }
            it.hrr60Avg?.let { hrr -> StatRow("Erholung 60 s", "−$hrr") }
            it.caloriesTotal?.let { kcal ->
                StatRow(
                    label = "Kalorien",
                    value = it.caloriesOnWall?.let { wall ->
                        "${formatKcal(kcal)} · ${formatKcal(wall)} an der Wand"
                    } ?: formatKcal(kcal),
                )
            }
            it.climbHeightMeters?.let { m -> StatRow("Kletterhöhe", formatMeters(m)) }
            it.maxClimbHeightMeters?.let { m -> StatRow("Höchster Boulder", formatMeters(m)) }
        }
    }
}

/** Halle zuordnen - und, wenn es noch keine gibt, eine anlegen. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GymCard(current: GymEntity?, gyms: List<GymEntity>, onChange: (String?) -> Unit) {
    BouldCard {
        if (gyms.isEmpty()) {
            Text(
                text = "Noch keine Halle angelegt. Das geht unter „Hallen“ in der Seitenleiste.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                gyms.forEach { gym ->
                    FilterChip(
                        selected = current?.id == gym.id,
                        onClick = { onChange(if (current?.id == gym.id) null else gym.id) },
                        label = { Text(gym.name) },
                    )
                }
            }
        }
    }
}

/**
 * Subjektive Anstrengung von 1 bis 10.
 *
 * Die billigste und erstaunlich aussagekraeftigste Zahl im Training - die aber
 * auf der Uhr nach einer Session niemand tippt. Deshalb hier.
 */
@Composable
private fun RpeCard(rpe: Int?, onChange: (Int?) -> Unit) {
    // Waehrend des Ziehens nur oertlich mitfuehren und erst beim Loslassen
    // uebernehmen: sonst loest jeder Zwischenwert eine eigene Uebertragung zur
    // Uhr aus - eine Reglerbewegung waren drei Pakete ueber Bluetooth.
    var dragged by remember(rpe) { mutableStateOf((rpe ?: 5).toFloat()) }

    BouldCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("RPE", style = MaterialTheme.typography.labelLarge)
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

@Composable
private fun NoteCard(note: String?, onChange: (String?) -> Unit) {
    var text by remember(note) { mutableStateOf(note.orEmpty()) }
    BouldCard {
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

/**
 * Ein Boulder: die Versuche zwischen zwei Grenzen.
 *
 * Hier passiert das Zusammenfuehren - zwei Gruppen auf denselben Eintrag
 * gelegt sind ab dann ein Projekt, ueber Sessions hinweg.
 */
@Composable
private fun BoulderCard(
    index: Int,
    boulder: BoulderRun,
    problems: List<ProblemEntity>,
    canName: Boolean,
    onAssign: (String?) -> Unit,
    onCreate: (String, Int?) -> Unit,
) {
    var picking by remember { mutableStateOf(false) }
    val problem = problems.firstOrNull { it.id == boulder.problemId }

    BouldCard(modifier = Modifier.clickable(enabled = canName) { picking = true }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = problem?.label ?: "Boulder $index",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = listOfNotNull(
                        boulder.run.gradeValue?.let { Grades.label(it, GradeSystem.FONT) },
                        attemptLabel(boulder.run.attempts),
                        if (boulder.run.isFlash) "Flash" else if (boulder.run.isSent) "Top" else null,
                        boulder.run.boardAngleDegrees?.let { BoardAngles.format(it) },
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (canName) {
                Text(
                    text = if (problem == null) "benennen" else "ändern",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }

    if (picking) {
        ProblemPickerDialog(
            problems = problems,
            current = problem,
            suggestedGrade = boulder.run.gradeValue,
            onDismiss = { picking = false },
            onAssign = { onAssign(it); picking = false },
            onCreate = { label, grade -> onCreate(label, grade); picking = false },
        )
    }
}

/**
 * Boulder zuordnen oder neu anlegen.
 *
 * Beides in einem Dialog: beim ersten Mal legt man an, beim zweiten waehlt man
 * - und genau dieser zweite Fall ist das Zusammenfuehren.
 */
@Composable
private fun ProblemPickerDialog(
    problems: List<ProblemEntity>,
    current: ProblemEntity?,
    suggestedGrade: Int?,
    onDismiss: () -> Unit,
    onAssign: (String?) -> Unit,
    onCreate: (String, Int?) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Boulder zuordnen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (problems.isNotEmpty()) {
                    Text("Vorhanden", style = MaterialTheme.typography.labelMedium)
                    problems.take(MAX_PICKER_ROWS).forEach { problem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAssign(problem.id) }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(problem.label)
                            problem.gradeValue?.let {
                                Text(
                                    text = Grades.label(it, GradeSystem.FONT),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Neuer Boulder, z. B. „Blau 14“") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (current != null) {
                    TextButton(onClick = { onAssign(null) }) { Text("Zuordnung entfernen") }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onCreate(name, suggestedGrade) },
            ) { Text("Anlegen") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } },
    )
}

/** Ein Versuch, aufklappbar zum Bearbeiten. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AttemptCard(attempt: AttemptEntity, actions: SessionDetailActions) {
    var expanded by remember { mutableStateOf(false) }

    BouldCard(modifier = Modifier.clickable { expanded = !expanded }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("#${attempt.ordinal}", style = MaterialTheme.typography.titleSmall)
            Text(
                text = summarize(attempt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (!expanded) return@BouldCard

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Text("Ergebnis", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AttemptOutcome.entries.forEach { outcome ->
                FilterChip(
                    selected = attempt.outcome == outcome,
                    onClick = {
                        actions.onOutcomeChange(
                            attempt.id,
                            if (attempt.outcome == outcome) null else outcome,
                        )
                    },
                    label = { Text(outcome.displayName) },
                )
            }
        }

        Stepper(
            label = "Grad",
            value = attempt.gradeValue?.let { Grades.label(it, GradeSystem.FONT) },
            onDown = {
                actions.onGradeChange(
                    attempt.id,
                    Grades.clamp((attempt.gradeValue ?: Grades.DEFAULT_VALUE) - 1),
                )
            },
            onUp = {
                actions.onGradeChange(
                    attempt.id,
                    Grades.clamp((attempt.gradeValue ?: Grades.DEFAULT_VALUE) + 1),
                )
            },
            onClear = { actions.onGradeChange(attempt.id, null) },
        )

        Stepper(
            label = "Wandneigung",
            value = attempt.boardAngleDegrees?.let { BoardAngles.format(it) },
            onDown = {
                val next = (attempt.boardAngleDegrees ?: BoardAngles.DEFAULT) - ANGLE_STEP
                actions.onAngleChange(attempt.id, BoardAngles.clamp(next))
            },
            onUp = {
                val next = (attempt.boardAngleDegrees ?: BoardAngles.DEFAULT) + ANGLE_STEP
                actions.onAngleChange(attempt.id, BoardAngles.clamp(next))
            },
            onClear = { actions.onAngleChange(attempt.id, null) },
        )

        // Bis zu welchem Zug gekommen - das Mass fuer Fortschritt an einem
        // Projekt, das noch nicht durchgeht.
        Stepper(
            label = "Bis Zug",
            value = attempt.topMoveReached?.toString(),
            onDown = { actions.onTopMoveChange(attempt.id, (attempt.topMoveReached ?: 1) - 1) },
            onUp = { actions.onTopMoveChange(attempt.id, (attempt.topMoveReached ?: 0) + 1) },
            onClear = { actions.onTopMoveChange(attempt.id, null) },
        )

        // Weich geloescht - ein hart entfernter Versuch kaeme bei der Uhr nie an.
        TextButton(onClick = { actions.onDeleteAttempt(attempt.id) }) {
            Text("Versuch löschen", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun Stepper(
    label: String,
    value: String?,
    onDown: () -> Unit,
    onUp: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onDown) { Text("−") }
            Text(
                text = value ?: "–",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.clickable(enabled = value != null, onClick = onClear),
            )
            TextButton(onClick = onUp) { Text("+") }
        }
    }
}

/**
 * Session loeschen.
 *
 * Mit Rueckfrage, und die Rueckfrage sagt, was verschwindet: die Zahlen des
 * Abends stecken in saemtlichen Summen, und die aendern sich sofort mit.
 */
@Composable
private fun DeleteSessionCard(onDelete: () -> Unit) {
    var asking by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { asking = true },
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
    ) {
        Text("Session löschen", color = MaterialTheme.colorScheme.error)
    }

    if (asking) {
        AlertDialog(
            onDismissRequest = { asking = false },
            title = { Text("Session löschen?") },
            text = {
                Text(
                    "Der Abend verschwindet aus der Historie, und Höhenmeter, " +
                        "Diagramme und Summen ändern sich entsprechend. " +
                        "Auf der Uhr wird er ebenfalls entfernt.",
                )
            },
            confirmButton = {
                TextButton(onClick = { asking = false; onDelete() }) {
                    Text("Löschen", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { asking = false }) { Text("Behalten") } },
        )
    }
}

private fun summarize(attempt: AttemptEntity): String = listOfNotNull(
    attempt.gradeValue?.let { Grades.label(it, GradeSystem.FONT) },
    attempt.boardAngleDegrees?.let { BoardAngles.format(it) },
    attempt.outcome?.displayName,
    attempt.topMoveReached?.let { "bis Zug $it" },
    attempt.climbHeightMeters?.let { formatMeters(it) },
).joinToString(" · ").ifEmpty { "ohne Angabe" }

private const val ANGLE_STEP = 5
private const val MAX_PICKER_ROWS = 8

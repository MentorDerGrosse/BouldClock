package at.mentor.bouldclockapp.presentation.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import at.mentor.bouldclockapp.core.metrics.AttemptRun
import at.mentor.bouldclockapp.core.model.BoardAngles
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import at.mentor.bouldclockapp.data.session.FinishedSession
import at.mentor.bouldclockapp.core.text.attemptLabel
import at.mentor.bouldclockapp.presentation.theme.BouldClockAppTheme
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Was am Ende der Session dasteht.
 *
 * Die Boulder-Liste kommt aus aufeinanderfolgenden Versuchen desselben Grades:
 * sechsmal 7A hintereinander war ein Boulder, danach ein 6B, danach wieder 7A -
 * ein anderer. Das ersetzt eine Versuchsliste waehrend der Session, die man beim
 * Klettern ohnehin nicht liest.
 */
@Composable
fun SessionSummaryScreen(
    finished: FinishedSession,
    gradeSystem: GradeSystem,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()
    val summary = finished.summary

    ScreenScaffold(scrollState = listState, modifier = modifier) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            item {
                ListHeader(
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                    transformation = SurfaceTransformation(spec),
                ) {
                    Text(formatDuration(summary.totalMs))
                }
            }

            item { StatRow("Versuche", "${summary.attemptCount}") }
            item { StatRow("Tops", "${summary.sendCount}") }

            // Wie viele Versuche gingen durch. Ueber eine einzelne Session
            // verrauscht - ein hartes Projekt drueckt sie auf zehn Prozent -,
            // aber im Verlauf die Effizienzzahl.
            summary.sendRate?.let { rate ->
                item { StatRow("Quote", "${(rate * 100).roundToInt()} %") }
            }
            // "Wandzeit" statt "An der Wand" - weiter unten steht der
            // Kalorienanteil an der Wand, und zweimal dieselbe Beschriftung
            // fuer Zeit und Energie waere genau die Zweideutigkeit von neulich.
            item { StatRow("Wandzeit", formatDuration(summary.workMs)) }
            item { StatRow("Pause", formatDuration(summary.restMs)) }

            // Das Verhaeltnis nur zeigen, wenn es etwas aussagt. Bei drei Sekunden
            // an der Wand ist "1:297" rechnerisch richtig und als Aussage wertlos.
            summary.meaningfulRestRatio()?.let { ratio ->
                item { StatRow("Verhältnis", "1:$ratio") }
            }

            summary.hrAvg?.let { avg ->
                item { StatRow("Puls", summary.hrMax?.let { "$avg / $it" } ?: "$avg") }
            }

            summary.caloriesTotal?.let { total ->
                item { StatRow("Kalorien", "${total.roundToInt()} kcal") }
            }
            // Der ehrlichere Massstab fuer die Trainingshaerte: ohne die
            // Erholung, die die generische Berechnung als Anstrengung mitzaehlt.
            summary.caloriesOnWall?.let { onWall ->
                item { StatRow("davon Wand", "${onWall.roundToInt()} kcal") }
            }

            summary.climbHeightMeters?.let { meters ->
                item { StatRow("Kletterhöhe", formatMeters(meters)) }
            }
            summary.maxClimbHeightMeters?.let { meters ->
                item { StatRow("Höchster", formatMeters(meters)) }
            }

            if (finished.runs.isNotEmpty()) {
                item {
                    ListHeader(
                        modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                        transformation = SurfaceTransformation(spec),
                    ) {
                        Text("Boulder")
                    }
                }
                finished.runs.forEach { run ->
                    item {
                        StatRow(
                            label = run.gradeLabel(gradeSystem),
                            value = attemptLabel(run.attempts),
                            note = listOfNotNull(
                                run.boardAngleDegrees?.let { BoardAngles.format(it) },
                                run.resultLabel(),
                            ).joinToString(" · ").takeIf { it.isNotEmpty() },
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                    transformation = SurfaceTransformation(spec),
                ) {
                    Text("Fertig")
                }
            }
        }
    }
}

/**
 * Eine Zeile der Werttabelle.
 *
 * Bewusst kein deaktivierter Knopf: der graut seinen Text aus, und
 * Zusammenfassungen liest man im Sitzen mit muedem Blick.
 */
@Composable
private fun StatRow(label: String, value: String, note: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = note?.let { "$value · $it" } ?: value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Nur aussagekraeftig, wenn ueberhaupt nennenswert geklettert wurde. */
private fun SessionSummaryEntity.meaningfulRestRatio(): Int? {
    if (workMs < 30_000L) return null
    return restPerWork?.roundToInt()?.takeIf { it in 1..60 }
}

private fun AttemptRun.gradeLabel(system: GradeSystem): String =
    gradeValue?.let { Grades.label(it, system) } ?: "ohne Grad"

private fun AttemptRun.resultLabel(): String? = when {
    isFlash -> "Flash"
    isSent -> "Top"
    else -> null
}

private fun formatMeters(meters: Double): String =
    String.format(Locale.GERMAN, "%.1f m", meters)

/** "42:07" unter einer Stunde, sonst "1:24:07". */
private fun formatDuration(ms: Long): String {
    val total = (ms / 1_000L).coerceAtLeast(0L)
    val hours = total / 3600
    val minutes = (total % 3600) / 60
    val seconds = total % 60
    return if (hours > 0) {
        "$hours:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "$minutes:${seconds.toString().padStart(2, '0')}"
    }
}

@WearPreviewDevices
@Composable
private fun SessionSummaryPreview() {
    BouldClockAppTheme {
        SessionSummaryScreen(
            finished = FinishedSession(
                summary = SessionSummaryEntity(
                    sessionId = "preview",
                    gymId = null,
                    type = SessionType.FREE,
                    startedAt = 0L,
                    totalMs = 84 * 60_000L,
                    workMs = 6 * 60_000L,
                    restMs = 70 * 60_000L,
                    pausedMs = 8 * 60_000L,
                    attemptCount = 10,
                    sendCount = 3,
                    flashCount = 1,
                    hardestSendValue = Grades.parse("7A"),
                    hrAvg = 128,
                    hrMax = 171,
                    caloriesTotal = 412.0,
                    caloriesOnWall = 96.0,
                    climbHeightMeters = 41.4,
                    maxClimbHeightMeters = 4.6,
                    computedAt = 0L,
                ),
                runs = listOf(
                    AttemptRun(Grades.parse("7A"), attempts = 6, sends = 1, workMs = 0, hrMax = null),
                    AttemptRun(Grades.parse("6B"), attempts = 1, sends = 1, workMs = 0, hrMax = null),
                    AttemptRun(Grades.parse("7A"), attempts = 3, sends = 0, workMs = 0, hrMax = null),
                ),
            ),
            gradeSystem = GradeSystem.FONT,
            onDismiss = {},
        )
    }
}

package at.mentor.bouldclockapp.presentation.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import at.mentor.bouldclockapp.core.text.attemptLabel
import at.mentor.bouldclockapp.presentation.theme.BouldClockAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Die letzten Sessions auf einen Blick.
 *
 * Bewusst knapp: das grosse Archiv mit Diagrammen gehoert aufs Handy. Hier steht
 * nur, was man zwischen zwei Bouldern wissen will - wie viel war es diesmal, und
 * wie liegt das zu sonst.
 */
@Composable
fun ProgressScreen(
    summaries: List<SessionSummaryEntity>,
    gradeSystem: GradeSystem,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()
    val averageAttempts = summaries
        .map { it.attemptCount }
        .takeIf { it.isNotEmpty() }
        ?.average()

    ScreenScaffold(scrollState = listState, modifier = modifier) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            item {
                ListHeader(
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                    transformation = SurfaceTransformation(spec),
                ) {
                    Text("Fortschritt")
                }
            }

            if (summaries.isEmpty()) {
                item {
                    Text(
                        text = "Noch keine beendete Session",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                    )
                }
            } else {
                averageAttempts?.let { average ->
                    item {
                        SummaryRow(
                            left = "Schnitt",
                            right = attemptLabel(average.toInt()),
                            highlight = false,
                        )
                    }
                }
                summaries.forEach { summary ->
                    item {
                        SummaryRow(
                            left = DATE_FORMAT.format(Date(summary.startedAt)),
                            right = describe(summary, gradeSystem),
                            highlight = true,
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                    transformation = SurfaceTransformation(spec),
                ) {
                    Text("Zurück")
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(left: String, right: String, highlight: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = left,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = right,
            style = MaterialTheme.typography.labelMedium,
            color = if (highlight) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

/** "24 Versuche · 7A" - Anzahl und haertester Durchstieg, mehr passt nicht in eine Zeile. */
private fun describe(summary: SessionSummaryEntity, system: GradeSystem): String =
    listOfNotNull(
        attemptLabel(summary.attemptCount),
        summary.hardestSendValue?.let { Grades.label(it, system) },
    ).joinToString(" · ")

private val DATE_FORMAT = SimpleDateFormat("dd.MM.", Locale.GERMAN)

@WearPreviewDevices
@Composable
private fun ProgressPreview() {
    BouldClockAppTheme {
        ProgressScreen(
            summaries = listOf(
                preview(startedAt = 1_758_000_000_000L, attempts = 24, hardest = "7A"),
                preview(startedAt = 1_757_600_000_000L, attempts = 31, hardest = "6C+"),
                preview(startedAt = 1_757_200_000_000L, attempts = 18, hardest = null),
            ),
            gradeSystem = GradeSystem.FONT,
            onBack = {},
        )
    }
}

private fun preview(startedAt: Long, attempts: Int, hardest: String?) = SessionSummaryEntity(
    sessionId = "p$startedAt",
    gymId = null,
    type = SessionType.FREE,
    startedAt = startedAt,
    totalMs = 0L,
    workMs = 0L,
    restMs = 0L,
    pausedMs = 0L,
    attemptCount = attempts,
    sendCount = 0,
    flashCount = 0,
    hardestSendValue = hardest?.let { Grades.parse(it) },
    computedAt = 0L,
)

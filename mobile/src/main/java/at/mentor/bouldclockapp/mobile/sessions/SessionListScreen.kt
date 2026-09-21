package at.mentor.bouldclockapp.mobile.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.mentor.bouldclockapp.core.metrics.Period
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.core.text.attemptLabel
import at.mentor.bouldclockapp.core.text.topNoun
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import at.mentor.bouldclockapp.mobile.SessionWeek
import at.mentor.bouldclockapp.mobile.formatKcal
import at.mentor.bouldclockapp.mobile.formatMeters
import at.mentor.bouldclockapp.mobile.formatShortDate
import at.mentor.bouldclockapp.mobile.formatTime
import at.mentor.bouldclockapp.mobile.label
import at.mentor.bouldclockapp.mobile.ui.BouldCard
import at.mentor.bouldclockapp.mobile.ui.EmptyState
import at.mentor.bouldclockapp.mobile.ui.SectionHeader

/**
 * Alle Sessions, nach Kalenderwochen gegliedert.
 *
 * Die Woche ist eine Ueberschrift, kein Klappmenue: wer in der Liste scrollt,
 * will lesen und nicht erst oeffnen. Rechts neben der Ueberschrift stehen die
 * Hoehenmeter der Woche - so hat das Scrollen nebenbei einen Verlauf.
 */
@Composable
fun SessionListScreen(
    weeks: List<SessionWeek>,
    onOpenSession: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    if (weeks.isEmpty()) {
        EmptyState(
            title = "Noch keine Sessions angekommen",
            hint = "Beende eine Session auf der Uhr.",
            modifier = modifier.fillMaxSize().padding(contentPadding),
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        weeks.forEach { week ->
            item(key = "week-${week.start}") {
                SectionHeader(
                    text = label(week.start, Period.WEEK),
                    trailing = formatMeters(week.climbHeightMeters),
                )
            }
            items(week.summaries, key = { it.sessionId }) { summary ->
                SessionRow(summary) { onOpenSession(summary.sessionId) }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.items(
    summaries: List<SessionSummaryEntity>,
    key: (SessionSummaryEntity) -> Any,
    content: @Composable (SessionSummaryEntity) -> Unit,
) = summaries.forEach { summary -> item(key = key(summary)) { content(summary) } }

@Composable
private fun SessionRow(summary: SessionSummaryEntity, onClick: () -> Unit) {
    BouldCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = formatShortDate(summary.startedAt),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${formatTime(summary.startedAt)} · ${summary.type.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            summary.hardestSendValue?.let {
                Text(
                    text = Grades.label(it, GradeSystem.FONT),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Text(
            text = describe(summary),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun describe(summary: SessionSummaryEntity): String = listOfNotNull(
    attemptLabel(summary.attemptCount),
    "${summary.sendCount} ${topNoun(summary.sendCount)}",
    summary.climbHeightMeters?.let { formatMeters(it) },
    summary.caloriesTotal?.let { formatKcal(it) },
).joinToString(" · ")

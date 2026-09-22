package at.mentor.bouldclockapp.mobile.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.core.model.LandmarkComparison
import at.mentor.bouldclockapp.core.text.attemptNoun
import at.mentor.bouldclockapp.core.text.sessionNoun
import at.mentor.bouldclockapp.core.text.topNoun
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import at.mentor.bouldclockapp.mobile.DashboardState
import at.mentor.bouldclockapp.mobile.axisLabel
import at.mentor.bouldclockapp.mobile.formatDurationShort
import at.mentor.bouldclockapp.mobile.formatKcal
import at.mentor.bouldclockapp.mobile.formatMeters
import at.mentor.bouldclockapp.mobile.formatSessionDate
import at.mentor.bouldclockapp.mobile.formatTimes
import at.mentor.bouldclockapp.mobile.ui.BouldCard
import at.mentor.bouldclockapp.mobile.ui.ChartBar
import at.mentor.bouldclockapp.mobile.ui.ColumnChart
import at.mentor.bouldclockapp.mobile.ui.EmptyState
import at.mentor.bouldclockapp.mobile.ui.SectionHeader
import at.mentor.bouldclockapp.mobile.ui.StatTile

/**
 * Die Startseite.
 *
 * Reihenfolge nach Naehe: ganz oben der letzte Abend, weil man dort noch etwas
 * nachtragen will. Darunter die Hoehenmeter - der Punkt, wegen dem die Uhr
 * ueberhaupt den Luftdruck mitschreibt. Dann der Vergleich zur Vorwoche und
 * der Verlauf.
 */
@Composable
fun DashboardScreen(
    state: DashboardState,
    onOpenSession: (String) -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    if (state.lastSession == null) {
        EmptyState(
            title = "Noch keine Session",
            hint = "Beende eine Session auf der Uhr, dann steht hier dein Abend.",
            modifier = modifier.fillMaxSize().padding(contentPadding),
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            LastSessionCard(
                summary = state.lastSession,
                onClick = { onOpenSession(state.lastSession.sessionId) },
            )
        }

        item { SectionHeader("Höhenmeter") }
        item { HeightCard(state, onOpenHistory) }

        item { SectionHeader("Vergangene Woche") }
        item { LastWeekCard(state, onOpenHistory) }

        item { SectionHeader("Letzte 8 Wochen", trailing = "antippen") }
        item { WeeksCard(state, onOpenHistory) }
    }
}

/**
 * Der letzte Abend, ganz oben und anklickbar.
 *
 * Genau dafuer: was man nachtragen will, traegt man am selben Tag nach.
 */
@Composable
private fun LastSessionCard(summary: SessionSummaryEntity, onClick: () -> Unit) {
    BouldCard(modifier = Modifier.clickable(onClick = onClick)) {
        Text(
            text = "Letzte Session",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = formatSessionDate(summary.startedAt),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatTile(summary.attemptCount.toString(), attemptNoun(summary.attemptCount))
            StatTile(summary.sendCount.toString(), topNoun(summary.sendCount))
            StatTile(
                value = summary.hardestSendValue?.let { Grades.label(it, GradeSystem.FONT) } ?: "–",
                label = "Härtester",
            )
            StatTile(
                value = summary.climbHeightMeters?.let { formatMeters(it) } ?: "–",
                label = "Höhe",
            )
        }
        Text(
            text = listOfNotNull(
                formatDurationShort(summary.totalMs),
                summary.caloriesTotal?.let { formatKcal(it) },
                summary.hrMax?.let { "Puls max $it" },
            ).joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Zum Bearbeiten tippen",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Woche, Monat, Jahr - und was das in Bauwerken heisst. */
@Composable
private fun HeightCard(state: DashboardState, onOpenHistory: () -> Unit) {
    BouldCard(modifier = Modifier.clickable(onClick = onOpenHistory)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatTile(formatMeters(state.heights.week), "Diese Woche")
            StatTile(formatMeters(state.heights.month), "Dieser Monat")
            StatTile(formatMeters(state.heights.year), "Dieses Jahr")
        }
        LandmarkRow(state.landmark)
    }
}

/**
 * Der Bauwerksvergleich.
 *
 * "1247 m" sagt niemandem etwas, "9,1 Mal der Stephansdom" schon. Darunter,
 * wie weit es zum naechsten ist - das ist der Teil, der zum Weiterklettern
 * verleitet.
 */
@Composable
private fun LandmarkRow(comparison: LandmarkComparison) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Insgesamt ${formatMeters(comparison.meters)}",
            style = MaterialTheme.typography.bodyMedium,
        )
        comparison.reached?.let { reached ->
            Text(
                text = "${formatTimes(comparison.times)} ${reached.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        comparison.next?.let { next ->
            LinearProgressIndicator(
                progress = { comparison.progressToNext },
                modifier = Modifier.fillMaxWidth(),
                // Ohne eigene Farben nimmt Material die Sekundaerfarbe fuer die
                // Spur - der Balken sieht dann bei 0 % aus wie bei 100 %.
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
            Text(
                text = "Noch ${formatMeters(comparison.missingMeters)} bis ${next.name}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Die vergangene Woche gegen die laufende. */
@Composable
private fun LastWeekCard(state: DashboardState, onOpenHistory: () -> Unit) {
    val last = state.lastWeek
    BouldCard(modifier = Modifier.clickable(onClick = onOpenHistory)) {
        if (last == null) {
            Text(
                text = "In der Woche davor war nichts los.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatTile(last.sessionCount.toString(), sessionNoun(last.sessionCount))
                StatTile(last.attemptCount.toString(), attemptNoun(last.attemptCount))
                StatTile(last.sendCount.toString(), topNoun(last.sendCount))
                StatTile(formatMeters(last.climbHeightMeters), "Höhe")
            }
            val now = state.thisWeek
            Text(
                text = compareWeeks(now, last),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "Zur Historie",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun WeeksCard(state: DashboardState, onOpenHistory: () -> Unit) {
    var selected by remember { mutableStateOf<Int?>(null) }
    BouldCard(modifier = Modifier.clickable(onClick = onOpenHistory)) {
        ColumnChart(
            bars = state.recentWeeks.map {
                ChartBar(label = axisLabel(it), value = it.climbHeightMeters)
            },
            selectedIndex = selected,
            onSelect = { selected = it },
            valueFormat = { formatMeters(it) },
            emptyHint = "Noch keine Kletterhöhe gemessen. Die kommt aus dem " +
                "Luftdruck während der Versuche - beim ersten echten Abend " +
                "steht hier ein Verlauf.",
        )
    }
}

/** Ein Satz Vergleich statt vier Prozentangaben. */
private fun compareWeeks(
    now: at.mentor.bouldclockapp.core.metrics.PeriodBucket?,
    last: at.mentor.bouldclockapp.core.metrics.PeriodBucket,
): String {
    val current = now?.attemptCount ?: 0
    return when {
        current == 0 -> "Diese Woche noch nichts."
        current > last.attemptCount -> "Diese Woche schon $current Versuche - mehr als die ganze Woche davor."
        current == last.attemptCount -> "Diese Woche gleich viele Versuche."
        else -> "Diese Woche bisher $current Versuche."
    }
}

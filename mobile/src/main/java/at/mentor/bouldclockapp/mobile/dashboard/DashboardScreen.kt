package at.mentor.bouldclockapp.mobile.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.core.metrics.Readiness
import at.mentor.bouldclockapp.core.metrics.ReadinessLevel
import at.mentor.bouldclockapp.core.model.LandmarkComparison
import at.mentor.bouldclockapp.core.text.attemptNoun
import at.mentor.bouldclockapp.core.text.sessionNoun
import at.mentor.bouldclockapp.core.text.topNoun
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import at.mentor.bouldclockapp.mobile.DashboardState
import at.mentor.bouldclockapp.mobile.LocalChartColors
import at.mentor.bouldclockapp.mobile.detail.Metric
import at.mentor.bouldclockapp.mobile.label
import at.mentor.bouldclockapp.mobile.formatBpm
import at.mentor.bouldclockapp.mobile.formatDurationShort
import at.mentor.bouldclockapp.mobile.formatPerWeek
import at.mentor.bouldclockapp.mobile.formatPercent
import at.mentor.bouldclockapp.mobile.ui.StatRow
import at.mentor.bouldclockapp.mobile.formatKcal
import at.mentor.bouldclockapp.mobile.formatMeters
import at.mentor.bouldclockapp.mobile.formatSessionDate
import at.mentor.bouldclockapp.mobile.formatTimes
import at.mentor.bouldclockapp.mobile.ui.BouldCard
import at.mentor.bouldclockapp.mobile.ui.CardPair
import at.mentor.bouldclockapp.mobile.ui.MetricTile
import at.mentor.bouldclockapp.mobile.ui.SparkKind
import at.mentor.bouldclockapp.mobile.ui.Sparkline
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
    readiness: Readiness,
    onOpenSession: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenMetric: (Metric) -> Unit,
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
        item { ReadinessCard(readiness) }

        item {
            LastSessionCard(
                summary = state.lastSession,
                onClick = { onOpenSession(state.lastSession.sessionId) },
            )
        }

        item { SectionHeader("Insgesamt", trailing = "antippen") { onOpenMetric(Metric.VOLUME) } }
        item { RatesCard(state) { onOpenMetric(Metric.VOLUME) } }

        item { SectionHeader("Höhenmeter", trailing = "antippen") { onOpenMetric(Metric.HEIGHT) } }
        item { HeightCard(state) { onOpenMetric(Metric.HEIGHT) } }

        item { SectionHeader("Vergangene Woche") }
        item { LastWeekCard(state, onOpenHistory) }

        item { SectionHeader("Letzte 8 Wochen", trailing = "je Kachel antippen") }
        item { TrendTiles(state, onOpenMetric) }
    }
}

/**
 * Ist heute ein guter Tag zum Bouldern?
 *
 * Kein Punktwert, sondern eine Einschaetzung mit ausgeschriebener Begruendung -
 * eine Zahl ohne Begruendung waere ein Orakel, und ein Orakel glaubt man genau
 * einmal. Was die App nicht sieht - Schlaf, Muskelkater, Lust - weiss nur der
 * Mensch davor.
 */
@Composable
private fun ReadinessCard(readiness: Readiness) {
    if (readiness.level == ReadinessLevel.UNKNOWN) return
    BouldCard {
        Text(
            text = readiness.level.displayName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = when (readiness.level) {
                ReadinessLevel.RESTED -> MaterialTheme.colorScheme.primary
                ReadinessLevel.TIRED -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onSurface
            },
        )
        readiness.reasons.forEach { reason ->
            Text(
                text = reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Quoten und Trainingshäufigkeit über alles - die Zahlen zum Angeben. */
@Composable
private fun RatesCard(state: DashboardState, onOpen: () -> Unit) {
    BouldCard(modifier = Modifier.clickable(onClick = onOpen)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatTile(state.flashRate?.let { formatPercent(it) } ?: "–", "Flashquote")
            StatTile(state.sendRate?.let { formatPercent(it) } ?: "–", "Topquote")
            StatTile(state.sessionCount.toString(), "Sessions")
        }
        state.sessionsPerWeek?.let { StatRow("Häufigkeit", formatPerWeek(it)) }
    }
}

/**
 * Die vier Verlaeufe als Kacheln, zwei nebeneinander.
 *
 * Nebeneinander statt untereinander, weil vier Karten in voller Breite
 * dreimal Scrollen bedeuten und man dann nie zwei Groessen zugleich sieht.
 * Was die Kachel nicht zeigen kann - den Wert jeder einzelnen Woche -, zeigt
 * das eigene Fenster hinter dem Tipper.
 */
@Composable
private fun TrendTiles(state: DashboardState, onOpen: (Metric) -> Unit) {
    val colors = LocalChartColors.current
    val weeks = state.recentWeeks
    if (weeks.isEmpty()) return

    val attempts = weeks.sumOf { it.attemptCount }
    val sends = weeks.sumOf { it.sendCount }
    val pulseWeeks = weeks.mapNotNull { it.hrAvg }
    val peak = weeks.mapNotNull { it.hrMax }.maxOrNull()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CardPair {
            MetricTile(
                title = "Höhenmeter",
                value = formatMeters(weeks.sumOf { it.climbHeightMeters }),
                caption = lastMeasured(weeks, { it.climbHeightMeters }) { formatMeters(it) }
                    ?: "Noch keine Kletterhöhe gemessen.",
                accent = colors.height.base,
                onClick = { onOpen(Metric.HEIGHT) },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Sparkline(weeks.map { it.climbHeightMeters }, colors.height)
            }

            MetricTile(
                title = "Versuche",
                value = attempts.toString(),
                caption = if (attempts > 0) "davon $sends ${topNoun(sends)}" else "In acht Wochen nichts.",
                accent = colors.volume.base,
                onClick = { onOpen(Metric.VOLUME) },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Sparkline(weeks.map { it.attemptCount.toDouble() }, colors.volume)
            }
        }

        CardPair {
            MetricTile(
                title = "Kalorien",
                value = formatKcal(weeks.sumOf { it.caloriesTotal }),
                caption = lastMeasured(weeks, { it.caloriesTotal }) { formatKcal(it) }
                    ?: "Dafür braucht es Puls und Profil.",
                accent = colors.calories.base,
                onClick = { onOpen(Metric.CALORIES) },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Sparkline(weeks.map { it.caloriesTotal }, colors.calories)
            }

            MetricTile(
                title = "Puls",
                value = pulseWeeks.average().takeIf { pulseWeeks.isNotEmpty() }
                    ?.let { formatBpm(it.toInt()) } ?: "–",
                caption = peak?.let { "Spitze ${formatBpm(it)}" } ?: "Noch kein Puls aufgezeichnet.",
                accent = colors.pulse.base,
                onClick = { onOpen(Metric.PULSE) },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Sparkline(
                    values = weeks.map { (it.hrAvg ?: 0).toDouble() },
                    palette = colors.pulse,
                    kind = SparkKind.LINE,
                )
            }
        }
    }
}

/** "KW 38: 17,1 m" - die letzte Woche, in der etwas gemessen wurde. */
private fun lastMeasured(
    weeks: List<at.mentor.bouldclockapp.core.metrics.PeriodBucket>,
    value: (at.mentor.bouldclockapp.core.metrics.PeriodBucket) -> Double,
    format: (Double) -> String,
): String? = weeks.lastOrNull { value(it) > 0.0 }
    ?.let { "${label(it)}: ${format(value(it))}" }

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
                summary.hrMax?.let { "Puls max ${formatBpm(it)}" },
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
private fun HeightCard(state: DashboardState, onOpen: () -> Unit) {
    BouldCard(modifier = Modifier.clickable(onClick = onOpen)) {
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

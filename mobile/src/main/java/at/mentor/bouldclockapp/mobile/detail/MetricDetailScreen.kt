package at.mentor.bouldclockapp.mobile.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilterChip
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
import at.mentor.bouldclockapp.core.metrics.Period
import at.mentor.bouldclockapp.core.metrics.PeriodBucket
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.core.text.attemptLabel
import at.mentor.bouldclockapp.core.text.topNoun
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import at.mentor.bouldclockapp.mobile.axisLabel
import at.mentor.bouldclockapp.mobile.formatBpm
import at.mentor.bouldclockapp.mobile.formatKcal
import at.mentor.bouldclockapp.mobile.formatMeters
import at.mentor.bouldclockapp.mobile.formatShortDate
import at.mentor.bouldclockapp.mobile.formatTime
import at.mentor.bouldclockapp.mobile.label
import at.mentor.bouldclockapp.mobile.ui.BouldCard
import at.mentor.bouldclockapp.mobile.ui.ChartBar
import at.mentor.bouldclockapp.mobile.ui.ChartLegend
import at.mentor.bouldclockapp.mobile.ui.ColumnChart
import at.mentor.bouldclockapp.mobile.ui.EmptyState
import at.mentor.bouldclockapp.mobile.ui.PointLineChart
import at.mentor.bouldclockapp.mobile.ui.SectionHeader
import at.mentor.bouldclockapp.mobile.ui.StatRow

/**
 * Eine Groesse im Detail.
 *
 * Dasselbe Fenster fuer jedes Diagramm: Zeitraum umschalten, einen Punkt
 * auswaehlen, die Sessions dahinter sehen und oeffnen. Dazu die Erklaerung -
 * was gemessen wird, in welcher Einheit, und warum es so gemessen wird.
 *
 * Ein Diagramm auf der Uebersicht ist eine Andeutung; hier steht das Ganze.
 */
@Composable
fun MetricDetailScreen(
    metric: Metric,
    period: Period,
    buckets: List<PeriodBucket>,
    sessionsOf: (PeriodBucket) -> List<SessionSummaryEntity>,
    onSelectPeriod: (Period) -> Unit,
    onOpenSession: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    var selected by remember(metric, period) { mutableStateOf<Int?>(null) }
    val bars = buckets.map { metric.barOf(it) }
    val chosen = buckets.getOrNull(selected ?: buckets.indexOfFirst { it.sessionCount > 0 }
        .takeIf { it >= 0 } ?: -1)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { PeriodPicker(period, onSelectPeriod) }

        if (buckets.none { it.sessionCount > 0 }) {
            item {
                EmptyState(
                    title = "Für diesen Zeitraum nichts",
                    hint = "Wähle einen größeren Zeitraum.",
                )
            }
        } else if (!period.isSingleBucket) {
            item {
                BouldCard {
                    if (metric.isLevel) {
                        PointLineChart(
                            bars = bars,
                            selectedIndex = selected,
                            onSelect = { selected = it },
                            valueFormat = metric.valueFormat,
                            highlightFormat = metric.highlightFormat,
                        )
                    } else {
                        ColumnChart(
                            bars = bars,
                            selectedIndex = selected,
                            onSelect = { selected = it },
                            valueFormat = metric.valueFormat,
                            highlightFormat = metric.highlightFormat,
                        )
                    }
                    metric.legend?.let { (a, b) -> ChartLegend(first = a, second = b) }
                }
            }
        }

        chosen?.let { bucket ->
            item { SectionHeader(label(bucket), trailing = "${bucket.sessionCount} Sessions") }
            item { BucketSummary(metric, bucket) }

            val sessions = sessionsOf(bucket)
            if (sessions.isNotEmpty()) {
                item { SectionHeader("Sessions", trailing = "antippen") }
                sessions.forEach { summary ->
                    item(key = summary.sessionId) {
                        SessionRow(metric, summary) { onOpenSession(summary.sessionId) }
                    }
                }
            }
        }

        item { SectionHeader("Was das ist") }
        item {
            BouldCard {
                StatRow("Einheit", metric.unit)
                Text(
                    text = metric.what,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = metric.why,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun BucketSummary(metric: Metric, bucket: PeriodBucket) {
    BouldCard {
        when (metric) {
            Metric.HEIGHT -> {
                StatRow("Höhe", formatMeters(bucket.climbHeightMeters))
                StatRow("Sessions", bucket.sessionCount.toString())
                if (bucket.sessionCount > 0) {
                    StatRow(
                        label = "Je Session",
                        value = formatMeters(bucket.climbHeightMeters / bucket.sessionCount),
                    )
                }
            }

            Metric.VOLUME -> {
                StatRow("Versuche", attemptLabel(bucket.attemptCount))
                StatRow("Tops", "${bucket.sendCount} · ${bucket.flashCount} Flash")
                bucket.sendRate?.let { StatRow("Erfolgsquote", "${(it * 100).toInt()} %") }
                bucket.hardestSendValue?.let {
                    StatRow("Härtester Top", Grades.label(it, GradeSystem.FONT))
                }
            }

            Metric.CALORIES -> {
                StatRow("Kalorien", formatKcal(bucket.caloriesTotal))
                StatRow("Sessions", bucket.sessionCount.toString())
                if (bucket.sessionCount > 0) {
                    StatRow(
                        label = "Je Session",
                        value = formatKcal(bucket.caloriesTotal / bucket.sessionCount),
                    )
                }
            }

            Metric.PULSE -> {
                bucket.hrAvg?.let { StatRow("Im Schnitt", formatBpm(it)) }
                bucket.hrMax?.let { StatRow("Spitze", formatBpm(it)) }
                StatRow("Sessions", bucket.sessionCount.toString())
            }
        }
    }
}

@Composable
private fun SessionRow(metric: Metric, summary: SessionSummaryEntity, onClick: () -> Unit) {
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
            Text(
                text = metric.sessionValue(summary),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun PeriodPicker(selected: Period, onSelect: (Period) -> Unit) {
    // Scrollbar, weil fuenf Zeitraeume auf einem Telefon nicht nebeneinander
    // passen - "Gesamt" fiel sonst hinten raus.
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Period.entries.forEach { period ->
            FilterChip(
                selected = period == selected,
                onClick = { onSelect(period) },
                label = { Text(period.displayName) },
            )
        }
    }
}

// --- Was die einzelne Groesse aus einem Zeitraum liest ---

private fun Metric.barOf(bucket: PeriodBucket): ChartBar = when (this) {
    Metric.HEIGHT -> ChartBar(axisLabel(bucket), bucket.climbHeightMeters)
    Metric.VOLUME -> ChartBar(
        label = axisLabel(bucket),
        value = bucket.attemptCount.toDouble(),
        highlight = bucket.sendCount.toDouble(),
    )
    Metric.CALORIES -> ChartBar(axisLabel(bucket), bucket.caloriesTotal)
    Metric.PULSE -> ChartBar(
        label = axisLabel(bucket),
        value = (bucket.hrAvg ?: 0).toDouble(),
        highlight = (bucket.hrMax ?: 0).toDouble(),
    )
}

private val Metric.valueFormat: (Double) -> String
    get() = when (this) {
        Metric.HEIGHT -> { v -> formatMeters(v) }
        Metric.VOLUME -> { v -> "${v.toInt()} Versuche" }
        Metric.CALORIES -> { v -> formatKcal(v) }
        Metric.PULSE -> { v -> "${v.toInt()} bpm im Schnitt" }
    }

private val Metric.highlightFormat: ((Double) -> String)?
    get() = when (this) {
        Metric.VOLUME -> { v -> "${v.toInt()} ${topNoun(v.toInt())}" }
        Metric.PULSE -> { v -> "Spitze ${v.toInt()} bpm" }
        else -> null
    }

private val Metric.legend: Pair<String, String>?
    get() = when (this) {
        Metric.VOLUME -> "Versuche" to "davon Tops"
        Metric.PULSE -> "Schnitt" to "Spitze"
        else -> null
    }

private fun Metric.sessionValue(summary: SessionSummaryEntity): String = when (this) {
    Metric.HEIGHT -> summary.climbHeightMeters?.let { formatMeters(it) } ?: "–"
    Metric.VOLUME -> "${summary.attemptCount} / ${summary.sendCount}"
    Metric.CALORIES -> summary.caloriesTotal?.let { formatKcal(it) } ?: "–"
    Metric.PULSE -> summary.hrAvg?.let { formatBpm(it) } ?: "–"
}

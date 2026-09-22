package at.mentor.bouldclockapp.mobile.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import at.mentor.bouldclockapp.core.text.topNoun
import at.mentor.bouldclockapp.data.db.dao.GradeBucket
import at.mentor.bouldclockapp.data.db.dao.Hrr60Point
import at.mentor.bouldclockapp.mobile.axisLabel
import at.mentor.bouldclockapp.mobile.formatDurationShort
import at.mentor.bouldclockapp.mobile.formatKcal
import at.mentor.bouldclockapp.mobile.formatMeters
import at.mentor.bouldclockapp.mobile.label
import at.mentor.bouldclockapp.mobile.ui.BouldCard
import at.mentor.bouldclockapp.mobile.ui.ChartBar
import at.mentor.bouldclockapp.mobile.ui.ChartLegend
import at.mentor.bouldclockapp.mobile.ui.ColumnChart
import at.mentor.bouldclockapp.mobile.ui.EmptyState
import at.mentor.bouldclockapp.mobile.ui.GradePyramid
import at.mentor.bouldclockapp.mobile.ui.SectionHeader
import at.mentor.bouldclockapp.mobile.ui.StatRow
import at.mentor.bouldclockapp.mobile.ui.TrendLine

/**
 * Die Historie.
 *
 * Ein Umschalter oben bestimmt den Zeitraum, darunter zeigen alle Diagramme
 * dasselbe Raster. Ganz unten steht die Liste mit den Zahlen - jedes Diagramm
 * braucht einen Ort, an dem man den genauen Wert nachsieht.
 */
@Composable
fun HistoryScreen(
    period: Period,
    buckets: List<PeriodBucket>,
    grades: List<GradeBucket>,
    hrr60: List<Hrr60Point>,
    onSelectPeriod: (Period) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    val hasData = buckets.any { it.sessionCount > 0 }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { PeriodPicker(period, onSelectPeriod) }

        if (!hasData) {
            item {
                EmptyState(
                    title = "Für diesen Zeitraum nichts",
                    hint = "Wähle einen größeren Zeitraum oder klettere.",
                )
            }
            return@LazyColumn
        }

        item { SectionHeader("Höhenmeter je ${period.singular()}") }
        item { HeightChartCard(buckets) }

        item { SectionHeader("Volumen") }
        item { VolumeChartCard(buckets) }

        if (grades.isNotEmpty()) {
            item { SectionHeader("Gradpyramide", trailing = "alle Sessions") }
            item { PyramidCard(grades) }
        }

        if (hrr60.isNotEmpty()) {
            item { SectionHeader("Erholung nach 60 s", trailing = "höher ist besser") }
            item { Hrr60Card(hrr60) }
        }

        item { SectionHeader("Zahlen") }
        items(buckets)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.items(buckets: List<PeriodBucket>) {
    item {
        BouldCard {
            buckets.filter { it.sessionCount > 0 }
                .sortedByDescending { it.start }
                .forEachIndexed { index, bucket ->
                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    BucketRow(bucket)
                }
        }
    }
}

@Composable
private fun BucketRow(bucket: PeriodBucket) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label(bucket),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        StatRow("Sessions", bucket.sessionCount.toString())
        StatRow(
            label = "Versuche",
            value = "${bucket.attemptCount} · ${bucket.sendCount} ${topNoun(bucket.sendCount)}",
        )
        StatRow("Höhe", formatMeters(bucket.climbHeightMeters))
        StatRow("Wandzeit", formatDurationShort(bucket.workMs))
        if (bucket.caloriesTotal > 0.0) StatRow("Kalorien", formatKcal(bucket.caloriesTotal))
        bucket.hardestSendValue?.let {
            StatRow("Härtester Top", Grades.label(it, GradeSystem.FONT))
        }
    }
}

@Composable
private fun PeriodPicker(selected: Period, onSelect: (Period) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun HeightChartCard(buckets: List<PeriodBucket>) {
    var selected by remember { mutableStateOf<Int?>(null) }
    BouldCard {
        // Eine Reihe, also keine Legende - die Ueberschrift sagt, was gezeigt wird.
        ColumnChart(
            bars = buckets.map { ChartBar(axisLabel(it), it.climbHeightMeters) },
            selectedIndex = selected,
            onSelect = { selected = it },
            valueFormat = { formatMeters(it) },
        )
    }
}

@Composable
private fun VolumeChartCard(buckets: List<PeriodBucket>) {
    var selected by remember { mutableStateOf<Int?>(null) }
    BouldCard {
        ColumnChart(
            bars = buckets.map {
                ChartBar(
                    label = axisLabel(it),
                    value = it.attemptCount.toDouble(),
                    highlight = it.sendCount.toDouble(),
                )
            },
            selectedIndex = selected,
            onSelect = { selected = it },
            valueFormat = { "${it.toInt()} Versuche" },
        )
        ChartLegend(first = "Versuche", second = "davon Tops")
    }
}

/**
 * Die Gradpyramide.
 *
 * Wie viele Versuche je Grad, und wie viele davon durchgingen. Die Form
 * verraet das Trainingsmuster: eine breite Basis heisst Volumen, ein schmaler
 * hoher Zacken heisst Projektieren.
 */
@Composable
private fun PyramidCard(grades: List<GradeBucket>) {
    BouldCard {
        GradePyramid(
            bars = grades.sortedByDescending { it.gradeValue }.map {
                ChartBar(
                    label = Grades.label(it.gradeValue, GradeSystem.FONT),
                    value = it.attemptCount.toDouble(),
                    highlight = it.sendCount.toDouble(),
                )
            },
        )
        ChartLegend(first = "Versuche", second = "davon Tops")
    }
}

/**
 * Der Erholungstrend.
 *
 * Ab zwei Sessions als Verlauf, davor als einzelne Zahl - versteckt wurde die
 * Karte frueher, und dann schien die Messung gar nicht zu existieren.
 */
@Composable
private fun Hrr60Card(points: List<Hrr60Point>) {
    BouldCard {
        if (points.size >= 2) {
            TrendLine(values = points.map { it.hrr60Avg.toDouble() })
        } else {
            Text(
                text = "−${points.last().hrr60Avg}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = "Der Puls fällt in der ersten Minute nach dem Absteigen um " +
                "${points.last().hrr60Avg} Schläge. Steigt diese Zahl über Monate, " +
                "wird die Grundlage besser." +
                if (points.size < 2) " Ab der zweiten Session wird daraus ein Verlauf." else "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun Period.singular(): String = when (this) {
    Period.DAY -> "Tag"
    Period.WEEK -> "Woche"
    Period.MONTH -> "Monat"
    Period.YEAR -> "Jahr"
}

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
import at.mentor.bouldclockapp.core.metrics.HeartRateZone
import at.mentor.bouldclockapp.core.metrics.PeriodBucket
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.core.text.topNoun
import at.mentor.bouldclockapp.data.db.dao.GradeBucket
import at.mentor.bouldclockapp.data.db.dao.Hrr60Point
import at.mentor.bouldclockapp.mobile.axisLabel
import at.mentor.bouldclockapp.mobile.formatDurationShort
import at.mentor.bouldclockapp.mobile.formatKcal
import at.mentor.bouldclockapp.mobile.LocalChartColors
import at.mentor.bouldclockapp.mobile.formatBpm
import at.mentor.bouldclockapp.mobile.formatDurationWithUnit
import at.mentor.bouldclockapp.mobile.formatMeters
import at.mentor.bouldclockapp.mobile.label
import at.mentor.bouldclockapp.mobile.ui.BouldCard
import at.mentor.bouldclockapp.mobile.ui.ChartBar
import at.mentor.bouldclockapp.mobile.ui.ChartLegend
import at.mentor.bouldclockapp.mobile.ui.ColumnChart
import at.mentor.bouldclockapp.mobile.ui.EmptyState
import at.mentor.bouldclockapp.mobile.ui.GradePyramid
import at.mentor.bouldclockapp.mobile.ui.SectionHeader
import at.mentor.bouldclockapp.mobile.ui.StackedShareBar
import at.mentor.bouldclockapp.mobile.ui.StatTile
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
    zones: Map<HeartRateZone, Int>,
    totals: PeriodBucket?,
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

        totals?.let {
            item { SectionHeader("Zusammen", trailing = "${it.sessionCount} Sessions") }
            item { TotalsCard(it) }
        }

        // Bei "Gesamt" gibt es nur einen Eimer - ein einzelner Balken ist kein
        // Diagramm. Dann stehen oben die Kennzahlen und hier nichts.
        if (!period.isSingleBucket) {
            item { SectionHeader("Höhenmeter je ${period.singular()}") }
            item { HeightChartCard(buckets) }

            item { SectionHeader("Volumen") }
            item { VolumeChartCard(buckets) }

            item { SectionHeader("Kalorien") }
            item { CalorieChartCard(buckets) }

            if (buckets.any { it.hrAvg != null }) {
                item { SectionHeader("Puls im Schnitt", trailing = "je ${period.singular()}") }
                item { PulseChartCard(buckets) }
            }
        }

        if (zones.isNotEmpty()) {
            item { SectionHeader("Zeit in Pulszonen") }
            item { ZoneCard(zones) }
        }

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
        StatRow("Wandzeit", formatDurationWithUnit(bucket.workMs))
        if (bucket.caloriesTotal > 0.0) StatRow("Kalorien", formatKcal(bucket.caloriesTotal))
        bucket.hrAvg?.let { avg ->
            StatRow("Puls", bucket.hrMax?.let { max -> "$avg / $max bpm" } ?: formatBpm(avg))
        }
        bucket.hardestSendValue?.let {
            StatRow("Härtester Top", Grades.label(it, GradeSystem.FONT))
        }
    }
}

/** Die Summen des Zeitraums als Kacheln - bei "Gesamt" die einzige Darstellung. */
@Composable
private fun TotalsCard(totals: PeriodBucket) {
    BouldCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatTile(totals.attemptCount.toString(), "Versuche")
            StatTile(totals.sendCount.toString(), "Tops")
            StatTile(formatMeters(totals.climbHeightMeters), "Höhe")
            StatTile(formatKcal(totals.caloriesTotal), "Kalorien")
        }
        StatRow("Sessions", totals.sessionCount.toString())
        StatRow("Wandzeit", formatDurationWithUnit(totals.workMs))
        totals.sendRate?.let { StatRow("Erfolgsquote", "${(it * 100).toInt()} %") }
        totals.hrAvg?.let { avg ->
            StatRow("Puls", totals.hrMax?.let { max -> "$avg / $max bpm" } ?: formatBpm(avg))
        }
        totals.hardestSendValue?.let {
            StatRow("Härtester Top", Grades.label(it, GradeSystem.FONT))
        }
    }
}

/**
 * Zeit je Pulszone.
 *
 * Beim Bouldern liegt der Grossteil in Ruhe und Erholung - das ist kein Fehler,
 * sondern die Sportart. Interessant ist der Anteil oben.
 */
@Composable
private fun ZoneCard(zones: Map<HeartRateZone, Int>) {
    val ramp = LocalChartColors.current.zoneRamp
    BouldCard {
        StackedShareBar(
            parts = HeartRateZone.entries.mapIndexed { index, zone ->
                Triple(zone.displayName, (zones[zone] ?: 0).toLong(), ramp[index])
            },
            valueFormat = { formatDurationWithUnit(it * 1000L) },
        )
        val total = zones.values.sum()
        val hard = (zones[HeartRateZone.THRESHOLD] ?: 0) + (zones[HeartRateZone.MAXIMAL] ?: 0)
        Text(
            text = buildString {
                if (total > 0) {
                    append("${100 * hard / total} % ab Schwelle aufwärts. ")
                }
                append(
                    "Die Grenzen hängen an deinem Ruhe- und Maximalpuls; " +
                        "der Maximalpuls ist der höchste je gemessene, nicht dein echter.",
                )
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CalorieChartCard(buckets: List<PeriodBucket>) {
    var selected by remember { mutableStateOf<Int?>(null) }
    BouldCard {
        ColumnChart(
            bars = buckets.map { ChartBar(axisLabel(it), it.caloriesTotal) },
            selectedIndex = selected,
            onSelect = { selected = it },
            valueFormat = { formatKcal(it) },
            emptyHint = "Noch keine Kalorien berechnet – dafür braucht es Puls und Profil.",
        )
    }
}

@Composable
private fun PulseChartCard(buckets: List<PeriodBucket>) {
    var selected by remember { mutableStateOf<Int?>(null) }
    BouldCard {
        ColumnChart(
            bars = buckets.map {
                ChartBar(axisLabel(it), (it.hrAvg ?: 0).toDouble(), (it.hrMax ?: 0).toDouble())
            },
            selectedIndex = selected,
            onSelect = { selected = it },
            valueFormat = { "${it.toInt()} bpm im Schnitt" },
            highlightFormat = { "Spitze ${it.toInt()} bpm" },
        )
        ChartLegend(first = "Schnitt", second = "Spitze")
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
            highlightFormat = { "${it.toInt()} Tops" },
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
    Period.ALL -> "Gesamt"
    Period.DAY -> "Tag"
    Period.WEEK -> "Woche"
    Period.MONTH -> "Monat"
    Period.YEAR -> "Jahr"
}

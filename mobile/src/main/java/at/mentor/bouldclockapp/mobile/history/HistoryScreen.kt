package at.mentor.bouldclockapp.mobile.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
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
import at.mentor.bouldclockapp.mobile.detail.Metric
import at.mentor.bouldclockapp.mobile.formatKcal
import at.mentor.bouldclockapp.mobile.LocalChartColors
import at.mentor.bouldclockapp.mobile.formatBpm
import at.mentor.bouldclockapp.mobile.formatDurationWithUnit
import at.mentor.bouldclockapp.mobile.formatMeters
import at.mentor.bouldclockapp.mobile.label
import androidx.compose.foundation.layout.fillMaxHeight
import at.mentor.bouldclockapp.mobile.ui.BouldCard
import at.mentor.bouldclockapp.mobile.ui.CardPair
import at.mentor.bouldclockapp.mobile.ui.ChartBar
import at.mentor.bouldclockapp.mobile.ui.ChartCard
import at.mentor.bouldclockapp.mobile.ui.ChartLegend
import at.mentor.bouldclockapp.mobile.ui.ColumnChart
import at.mentor.bouldclockapp.mobile.ui.PointLineChart
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
    zoneBounds: List<Int>,
    totals: PeriodBucket?,
    onSelectPeriod: (Period) -> Unit,
    onOpenMetric: (Metric) -> Unit,
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
            item { SectionHeader("Verläufe", trailing = "je Karte antippen") }
            item { HeightChartCard(buckets, period) { onOpenMetric(Metric.HEIGHT) } }
            item { VolumeChartCard(buckets) { onOpenMetric(Metric.VOLUME) } }
            // Kalorien und Puls nebeneinander: beide beschreiben denselben
            // Abend von zwei Seiten, und in voller Breite kaeme man vor lauter
            // Scrollen nie dazu, sie zusammen zu sehen.
            if (buckets.any { it.hrAvg != null }) {
                item {
                    CardPair {
                        CalorieChartCard(
                            buckets = buckets,
                            compact = true,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        ) { onOpenMetric(Metric.CALORIES) }
                        PulseChartCard(
                            buckets = buckets,
                            compact = true,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        ) { onOpenMetric(Metric.PULSE) }
                    }
                }
            } else {
                item { CalorieChartCard(buckets) { onOpenMetric(Metric.CALORIES) } }
            }
        }

        item { SectionHeader("Auswertung") }

        if (buckets.any { it.fallCount > 0 }) {
            item { FallCard(buckets) { onOpenMetric(Metric.FALLS) } }
        }

        if (zones.isNotEmpty()) {
            item { ZoneCard(zones, zoneBounds) }
        }

        if (grades.isNotEmpty()) {
            item { PyramidCard(grades) }
        }

        if (hrr60.isNotEmpty()) {
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
        if (bucket.fallCount > 0) {
            StatRow("Stürze", "${bucket.fallCount} · ${formatMeters(bucket.fallMeters)}")
        }
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
private fun ZoneCard(zones: Map<HeartRateZone, Int>, bounds: List<Int>) {
    val ramp = LocalChartColors.current.zoneRamp
    ChartCard(title = "Zeit in Pulszonen", accent = ramp.last()) {
        StackedShareBar(
            parts = HeartRateZone.entries.mapIndexed { index, zone ->
                Triple(zoneLabel(zone, index, bounds), (zones[zone] ?: 0).toLong(), ramp[index])
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
private fun CalorieChartCard(
    buckets: List<PeriodBucket>,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onOpen: () -> Unit,
) {
    var selected by remember { mutableStateOf<Int?>(null) }
    ChartCard(
        title = "Kalorien",
        modifier = modifier,
        accent = LocalChartColors.current.calories.base,
        trailing = if (compact) null else "mehr",
        onClick = onOpen,
    ) {
        ColumnChart(
            bars = buckets.map { ChartBar(axisLabel(it), it.caloriesTotal) },
            height = if (compact) 108.dp else 136.dp,
            selectedIndex = selected,
            onSelect = { selected = it },
            valueFormat = { formatKcal(it) },
            palette = LocalChartColors.current.calories,
            maxLabels = if (compact) 4 else 7,
            emptyHint = "Noch keine Kalorien berechnet – dafür braucht es Puls und Profil.",
        )
    }
}

@Composable
private fun PulseChartCard(
    buckets: List<PeriodBucket>,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onOpen: () -> Unit,
) {
    var selected by remember { mutableStateOf<Int?>(null) }
    ChartCard(
        title = if (compact) "Puls" else "Puls im Schnitt",
        modifier = modifier,
        accent = LocalChartColors.current.pulse.base,
        trailing = if (compact) null else "mehr",
        onClick = onOpen,
    ) {
        PointLineChart(
            bars = buckets.map {
                ChartBar(axisLabel(it), (it.hrAvg ?: 0).toDouble(), (it.hrMax ?: 0).toDouble())
            },
            height = if (compact) 108.dp else 136.dp,
            selectedIndex = selected,
            onSelect = { selected = it },
            // Nebeneinander ist kein Platz fuer ausgeschriebene Saetze.
            valueFormat = if (compact) {
                { "${it.toInt()} bpm" }
            } else {
                { "${it.toInt()} bpm im Schnitt" }
            },
            highlightFormat = if (compact) {
                { "max ${it.toInt()}" }
            } else {
                { "Spitze ${it.toInt()} bpm" }
            },
            palette = LocalChartColors.current.pulse,
            maxLabels = if (compact) 4 else 7,
        )
        ChartLegend(
            first = "Schnitt",
            second = "Spitze",
            palette = LocalChartColors.current.pulse,
            vertical = compact,
        )
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

@Composable
private fun HeightChartCard(buckets: List<PeriodBucket>, period: Period, onOpen: () -> Unit) {
    var selected by remember { mutableStateOf<Int?>(null) }
    ChartCard(
        title = "Höhenmeter je ${period.singular()}",
        accent = LocalChartColors.current.height.base,
        trailing = "mehr",
        onClick = onOpen,
    ) {
        // Eine Reihe, also keine Legende - die Ueberschrift sagt, was gezeigt wird.
        ColumnChart(
            bars = buckets.map { ChartBar(axisLabel(it), it.climbHeightMeters) },
            height = 136.dp,
            selectedIndex = selected,
            onSelect = { selected = it },
            valueFormat = { formatMeters(it) },
            palette = LocalChartColors.current.height,
        )
    }
}

@Composable
private fun VolumeChartCard(buckets: List<PeriodBucket>, onOpen: () -> Unit) {
    var selected by remember { mutableStateOf<Int?>(null) }
    ChartCard(
        title = "Volumen",
        accent = LocalChartColors.current.volume.base,
        trailing = "mehr",
        onClick = onOpen,
    ) {
        ColumnChart(
            bars = buckets.map {
                ChartBar(
                    label = axisLabel(it),
                    value = it.attemptCount.toDouble(),
                    highlight = it.sendCount.toDouble(),
                )
            },
            height = 136.dp,
            selectedIndex = selected,
            onSelect = { selected = it },
            valueFormat = { "${it.toInt()} Versuche" },
            highlightFormat = { "${it.toInt()} Tops" },
            palette = LocalChartColors.current.volume,
        )
        ChartLegend(
            first = "Versuche",
            second = "davon Tops",
            palette = LocalChartColors.current.volume,
        )
    }
}

/**
 * Stuerze und die Summe der Fallhoehen.
 *
 * Bei einem Sturz faellt man vom hoechsten Punkt - die gemessene Kletterhoehe
 * *ist* die Fallhoehe.
 */
@Composable
private fun FallCard(buckets: List<PeriodBucket>, onOpen: () -> Unit) {
    val falls = buckets.sumOf { it.fallCount }
    val meters = buckets.sumOf { it.fallMeters }
    ChartCard(
        title = "Stürze",
        accent = LocalChartColors.current.falls.base,
        trailing = "mehr",
        onClick = onOpen,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatTile(falls.toString(), "Stürze")
            StatTile(formatMeters(meters), "gefallen")
            StatTile(
                value = if (falls > 0) formatMeters(meters / falls) else "–",
                label = "im Schnitt",
            )
        }
    }
}

/**
 * Zonenname mit dem Bereich in Schlaegen.
 *
 * Ohne die Zahlen ist "Tempo" ein Wort ohne Bezug - man weiss nicht, ob man
 * gerade drin war.
 */
private fun zoneLabel(zone: HeartRateZone, index: Int, bounds: List<Int>): String {
    if (bounds.size < HeartRateZone.entries.size - 1) return zone.displayName
    val from = if (index == 0) null else bounds[index - 1]
    val to = bounds.getOrNull(index)
    return when {
        from == null && to != null -> "${zone.displayName} (unter $to)"
        from != null && to != null -> "${zone.displayName} ($from–${to - 1})"
        from != null -> "${zone.displayName} (ab $from)"
        else -> zone.displayName
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
    // Zwei Blickwinkel auf dieselben Daten: wie viel probiert, oder wie viel
    // steht. Das Zweite ist die Zahl, die man Freunden sagt.
    var onlySends by remember { mutableStateOf(false) }

    ChartCard(
        title = "Gradpyramide",
        accent = LocalChartColors.current.volume.base,
        trailing = "alle Sessions",
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !onlySends,
                onClick = { onlySends = false },
                label = { Text("Versuche") },
            )
            FilterChip(
                selected = onlySends,
                onClick = { onlySends = true },
                label = { Text("Nur Tops") },
            )
        }

        val relevant = grades
            .filter { if (onlySends) it.sendCount > 0 else it.attemptCount > 0 }
            .sortedByDescending { it.gradeValue }

        GradePyramid(
            bars = relevant.map {
                ChartBar(
                    label = Grades.label(it.gradeValue, GradeSystem.FONT),
                    value = if (onlySends) it.sendCount.toDouble() else it.attemptCount.toDouble(),
                    highlight = if (onlySends) it.flashCount.toDouble() else it.sendCount.toDouble(),
                )
            },
        )
        ChartLegend(
            first = if (onlySends) "Tops" else "Versuche",
            second = if (onlySends) "davon Flash" else "davon Tops",
            palette = LocalChartColors.current.volume,
        )
        Text(
            text = if (onlySends) {
                "${relevant.sumOf { it.sendCount }} Boulder insgesamt durchgestiegen, " +
                    "härtester ${Grades.label(relevant.first().gradeValue, GradeSystem.FONT)}."
            } else {
                "${relevant.sumOf { it.attemptCount }} Versuche über alle Sessions."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
    ChartCard(
        title = "Erholung nach 60 s",
        accent = LocalChartColors.current.pulse.base,
        trailing = "höher ist besser",
    ) {
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

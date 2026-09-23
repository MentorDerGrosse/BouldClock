package at.mentor.bouldclockapp.mobile.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import at.mentor.bouldclockapp.mobile.LocalChartColors
import at.mentor.bouldclockapp.mobile.MetricColors
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

/**
 * Die Diagramme der Handy-App.
 *
 * Nach festen Regeln gezeichnet, nicht nach Gefuehl: duenne Marken, runde Kappe
 * nur oben, 2 dp Luft zwischen benachbarten Balken statt eines Rahmens,
 * haarfeine durchgezogene Hilfslinien, und Werte nur dort beschriftet, wo sie
 * etwas erzaehlen. Eine Zahl an jedem Punkt liest niemand.
 *
 * Die Farben stehen in [at.mentor.bouldclockapp.mobile.ChartColors] und sind auf
 * Unterscheidbarkeit geprueft - hier wird keine dazuerfunden.
 */

/** Ein Balken: Beschriftung, Gesamtwert und der hervorgehobene Anteil daran. */
data class ChartBar(
    val label: String,
    val value: Double,

    /** Teilmenge von [value], in der zweiten Farbe - etwa Tops von Versuchen. */
    val highlight: Double = 0.0,
)

private val BAR_MAX_WIDTH = 24.dp
private val BAR_GAP = 2.dp
private val BAR_CORNER = 6.dp
private val GRID_LINES = 3

/**
 * So viele Achsenbeschriftungen passen nebeneinander.
 *
 * Bei vierzehn Tagen stand frueher "21.9." vierzehnmal nebeneinander und alles
 * ueberlappte. Lieber jede zweite oder dritte beschriften - eine Achse muss
 * den Bereich zeigen, nicht jeden Punkt benennen.
 */
private const val MAX_AXIS_LABELS = 7

/**
 * Leert die Beschriftungen, die nicht mehr hinpassen.
 *
 * Die letzte bleibt immer stehen - das ist die, die man sucht -, und von dort
 * wird rueckwaerts jede n-te behalten.
 */
private fun axisLabels(bars: List<ChartBar>, maxLabels: Int): List<String> {
    if (bars.size <= maxLabels) return bars.map { it.label }
    val step = (bars.size + maxLabels - 1) / maxLabels
    return bars.indices.map { index ->
        if ((bars.lastIndex - index) % step == 0) bars[index].label else ""
    }
}

/**
 * Saeulendiagramm mit optionaler zweiter Reihe.
 *
 * Antippen waehlt eine Saeule aus - das Gegenstueck zum Zeigen mit der Maus.
 * Ohne Auswahl traegt nur der hoechste Balken seinen Wert; alles andere haengt
 * an der Achse.
 */
@Composable
fun ColumnChart(
    bars: List<ChartBar>,
    modifier: Modifier = Modifier,
    height: Dp = 148.dp,
    selectedIndex: Int? = null,
    onSelect: (Int?) -> Unit = {},
    valueFormat: (Double) -> String = { it.toInt().toString() },
    /** Beschriftung des hervorgehobenen Anteils, wenn es einen gibt. */
    highlightFormat: ((Double) -> String)? = null,
    /** Die Farbe der Groesse - jedes Diagramm zeigt genau eine. */
    palette: MetricColors? = null,
    /** Halb so breit heisst halb so viele Beschriftungen - sonst ueberlappen sie. */
    maxLabels: Int = MAX_AXIS_LABELS,
    emptyHint: String = "Für diesen Zeitraum noch nichts gemessen.",
) {
    if (bars.isEmpty()) return

    // Ein Raster ueber lauter Nullen ist kein Diagramm, sondern ein leerer
    // Kasten, den der Leser fuer einen Fehler haelt. Dann lieber ein Satz.
    if (bars.none { it.value > 0.0 }) {
        Text(
            text = emptyHint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }

    val colors = LocalChartColors.current
    val hue = palette ?: colors.volume
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall

    val top = niceCeil(bars.maxOf { it.value })
    val peakIndex = bars.indices.maxByOrNull { bars[it].value } ?: 0
    val labels = axisLabels(bars, maxLabels)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().height(height)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .pointerInput(bars.size) {
                        detectTapGestures { offset ->
                            val slot = size.width.toFloat() / bars.size
                            val index = (offset.x / slot).toInt().coerceIn(0, bars.lastIndex)
                            onSelect(if (index == selectedIndex) null else index)
                        }
                    },
            ) {
                drawGrid(colors.grid)

                val slot = size.width / bars.size
                val barWidth = minOf(slot - BAR_GAP.toPx(), BAR_MAX_WIDTH.toPx())
                val corner = BAR_CORNER.toPx()

                bars.forEachIndexed { index, bar ->
                    if (bar.value <= 0.0) return@forEachIndexed
                    val centerX = slot * index + slot / 2f
                    val left = centerX - barWidth / 2f
                    val barHeight = (bar.value / top).toFloat() * size.height
                    val dimmed = selectedIndex != null && selectedIndex != index

                    drawColumn(
                        left = left,
                        width = barWidth,
                        barHeight = barHeight,
                        corner = corner,
                        color = hue.base.fade(dimmed),
                    )

                    // Der hervorgehobene Anteil sitzt unten auf der Grundlinie,
                    // getrennt durch eine Luecke in der Grundfarbe statt durch
                    // einen Rahmen.
                    if (bar.highlight > 0.0) {
                        val share = (bar.highlight / bar.value).coerceIn(0.0, 1.0)
                        val highlightHeight = (barHeight * share.toFloat() - BAR_GAP.toPx())
                            .coerceAtLeast(0f)
                        if (highlightHeight > 0f) {
                            drawRect(
                                color = hue.light.fade(dimmed),
                                topLeft = Offset(left, size.height - highlightHeight),
                                size = androidx.compose.ui.geometry.Size(barWidth, highlightHeight),
                            )
                        }
                    }
                }
            }
        }

        // Achse: Beschriftungen als Text unter der Zeichenflaeche. Im Bild
        // gezeichneter Text muesste jede Schriftgroesse selbst nachbauen.
        AxisLabels(
            labels = labels,
            highlighted = selectedIndex ?: peakIndex,
            style = labelStyle,
            axisColor = axisColor,
            modifier = Modifier.padding(top = 6.dp),
        )

        // Der ausgewaehlte Balken im Klartext, mit **beiden** Reihen. Ohne das
        // bleibt ein zweifarbiger Balken huebsch und unlesbar: man sieht, dass
        // ein Teil Tops war, aber nicht wie viele.
        val shown = bars.getOrNull(selectedIndex ?: peakIndex)
        if (shown != null && shown.value > 0.0) {
            Text(
                text = buildString {
                    append(shown.label).append(": ").append(valueFormat(shown.value))
                    if (highlightFormat != null && shown.highlight > 0.0) {
                        append(" · ").append(highlightFormat(shown.highlight))
                    }
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

/**
 * Punkte, mit Linien verbunden - fuer Werte, die einen Pegel beschreiben.
 *
 * Der Unterschied zum Balken ist nicht Geschmack: ein Balken behauptet eine
 * Menge ab null, und ein Puls von 126 ist keine Menge. Die Linie zeigt, wie
 * sich ein Pegel bewegt, und darf deshalb auch bei 100 anfangen.
 *
 * Antippen waehlt einen Punkt aus; dann steht der zweite Wert daneben - beim
 * Puls die Spitze der Session.
 */
@Composable
fun PointLineChart(
    bars: List<ChartBar>,
    modifier: Modifier = Modifier,
    height: Dp = 148.dp,
    selectedIndex: Int? = null,
    onSelect: (Int?) -> Unit = {},
    valueFormat: (Double) -> String = { it.toInt().toString() },
    highlightFormat: ((Double) -> String)? = null,
    palette: MetricColors? = null,
    maxLabels: Int = MAX_AXIS_LABELS,
    emptyHint: String = "Für diesen Zeitraum noch nichts gemessen.",
) {
    val measured = bars.filter { it.value > 0.0 }
    if (bars.isEmpty()) return
    if (measured.isEmpty()) {
        Text(
            text = emptyHint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }

    val colors = LocalChartColors.current
    val hue = palette ?: colors.pulse
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall

    // Der Pegel bekommt Luft nach oben und unten, statt am Rand zu kleben.
    val values = measured.flatMap { listOfNotNull(it.value, it.highlight.takeIf { h -> h > 0.0 }) }
    val low = (values.min() - 5).coerceAtLeast(0.0)
    val high = values.max() + 5
    val span = (high - low).coerceAtLeast(1.0)
    val peakIndex = bars.indices.maxByOrNull { bars[it].value } ?: 0
    val labels = axisLabels(bars, maxLabels)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .pointerInput(bars.size) {
                    detectTapGestures { offset ->
                        val slot = size.width.toFloat() / bars.size
                        val index = (offset.x / slot).toInt().coerceIn(0, bars.lastIndex)
                        onSelect(if (index == selectedIndex) null else index)
                    }
                },
        ) {
            drawGrid(colors.grid)
            val slot = size.width / bars.size

            fun pointOf(index: Int, value: Double) = Offset(
                x = slot * index + slot / 2f,
                y = size.height - (((value - low) / span).toFloat() * size.height),
            )

            // Nur zwischen benachbarten gemessenen Punkten verbinden - eine
            // Linie ueber eine Luecke hinweg erfindet einen Verlauf.
            bars.indices.zipWithNext().forEach { (a, b) ->
                if (bars[a].value > 0.0 && bars[b].value > 0.0) {
                    drawLine(
                        color = hue.base,
                        start = pointOf(a, bars[a].value),
                        end = pointOf(b, bars[b].value),
                        strokeWidth = 2.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    )
                }
            }

            bars.forEachIndexed { index, bar ->
                if (bar.value <= 0.0) return@forEachIndexed
                val chosen = index == (selectedIndex ?: peakIndex)

                if (bar.highlight > 0.0) {
                    val top = pointOf(index, bar.highlight)
                    drawCircle(colors.surface, radius = 5.dp.toPx(), center = top)
                    drawCircle(hue.light, radius = 3.dp.toPx(), center = top)
                }

                val point = pointOf(index, bar.value)
                drawCircle(colors.surface, radius = if (chosen) 7.dp.toPx() else 6.dp.toPx(), center = point)
                drawCircle(hue.base, radius = if (chosen) 5.dp.toPx() else 4.dp.toPx(), center = point)
            }
        }

        // Achse: Beschriftungen als Text unter der Zeichenflaeche. Im Bild
        // gezeichneter Text muesste jede Schriftgroesse selbst nachbauen.
        AxisLabels(
            labels = labels,
            highlighted = selectedIndex ?: peakIndex,
            style = labelStyle,
            axisColor = axisColor,
            modifier = Modifier.padding(top = 6.dp),
        )

        val shown = bars.getOrNull(selectedIndex ?: peakIndex)
        if (shown != null && shown.value > 0.0) {
            Text(
                text = buildString {
                    append(shown.label).append(": ").append(valueFormat(shown.value))
                    if (highlightFormat != null && shown.highlight > 0.0) {
                        append(" · ").append(highlightFormat(shown.highlight))
                    }
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

/**
 * Waagrechte Balken je Grad - die Gradpyramide.
 *
 * Waagrecht, weil die Beschriftung ein Grad ist und nicht unter eine Saeule
 * passt, und von unten nach oben schwerer wird: so entsteht die Form, die dem
 * Ding den Namen gibt.
 */
@Composable
fun GradePyramid(
    bars: List<ChartBar>,
    modifier: Modifier = Modifier,
    rowHeight: Dp = 26.dp,
    palette: MetricColors? = null,
) {
    if (bars.none { it.value > 0.0 }) return
    val colors = LocalChartColors.current
    val hue = palette ?: colors.volume
    val top = niceCeil(bars.maxOf { it.value })

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        bars.forEach { bar ->
            Row(
                modifier = Modifier.fillMaxWidth().height(rowHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.size(width = 44.dp, height = rowHeight)
                        .padding(end = 8.dp, top = 4.dp),
                )
                Canvas(modifier = Modifier.weight(1f).height(rowHeight)) {
                    val full = (bar.value / top).toFloat() * size.width
                    val corner = BAR_CORNER.toPx()
                    val barHeight = size.height - 8.dp.toPx()
                    val y = (size.height - barHeight) / 2f

                    drawBarHorizontal(0f, full, y, barHeight, corner, hue.base)

                    if (bar.highlight > 0.0) {
                        val share = (bar.highlight / bar.value).coerceIn(0.0, 1.0)
                        val width = (full * share.toFloat() - BAR_GAP.toPx()).coerceAtLeast(0f)
                        if (width > 0f) {
                            drawRect(
                                color = hue.light,
                                topLeft = Offset(0f, y),
                                size = androidx.compose.ui.geometry.Size(width, barHeight),
                            )
                        }
                    }
                }
                // Beide Zahlen, nicht nur die Gesamtlaenge - sonst muss man den
                // orangen Anteil schaetzen.
                Text(
                    text = if (bar.highlight > 0.0) {
                        "${bar.value.toInt()}/${bar.highlight.toInt()}"
                    } else {
                        bar.value.toInt().toString()
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.size(width = 58.dp, height = rowHeight)
                        .padding(start = 8.dp, top = 4.dp),
                )
            }
        }
    }
}

/**
 * Verlaufslinie, etwa der HRR60-Trend.
 *
 * Eine Reihe, also keine Legende - die Ueberschrift sagt, was gezeigt wird.
 * Der letzte Punkt bekommt einen Ring in der Grundfarbe, damit er auch dort
 * ablesbar bleibt, wo die Linie ihn kreuzt.
 */
@Composable
fun TrendLine(
    values: List<Double>,
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
) {
    if (values.size < 2) return
    val colors = LocalChartColors.current

    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        drawGrid(colors.grid)

        val low = values.min()
        val high = values.max()
        val span = max(high - low, 1.0)
        val stepX = size.width / (values.size - 1)

        fun pointAt(index: Int): Offset {
            val normalized = ((values[index] - low) / span).toFloat()
            return Offset(stepX * index, size.height - normalized * size.height)
        }

        val path = Path().apply {
            moveTo(pointAt(0).x, pointAt(0).y)
            (1..values.lastIndex).forEach { lineTo(pointAt(it).x, pointAt(it).y) }
        }
        drawPath(
            path = path,
            color = colors.series1,
            style = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
        )

        val last = pointAt(values.lastIndex)
        drawCircle(colors.surface, radius = 6.dp.toPx(), center = last)
        drawCircle(colors.series1, radius = 4.dp.toPx(), center = last)
    }
}

/**
 * Anteile einer geordneten Leiter als ein Balken, darunter die Zahlen.
 *
 * Fuer Pulszonen: die Form zeigt auf einen Blick, wie viel vom Abend wirklich
 * hart war. Die Zahlen darunter sind kein Beiwerk - aus einem Balken allein
 * liest niemand Minuten ab.
 */
@Composable
fun StackedShareBar(
    parts: List<Triple<String, Long, Color>>,
    modifier: Modifier = Modifier,
    valueFormat: (Long) -> String,
) {
    val total = parts.sumOf { it.second }
    if (total <= 0L) return
    val colors = LocalChartColors.current

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(18.dp)) {
            var x = 0f
            parts.forEach { (_, value, color) ->
                if (value <= 0L) return@forEach
                val width = size.width * value / total
                drawRect(
                    color = color,
                    topLeft = Offset(x, 0f),
                    size = androidx.compose.ui.geometry.Size(
                        (width - BAR_GAP.toPx()).coerceAtLeast(1f),
                        size.height,
                    ),
                )
                x += width
            }
        }
        parts.filter { it.second > 0L }.forEach { (label, value, color) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "${valueFormat(value)} · ${100 * value / total} %",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/** Legende - Pflicht, sobald zwei Reihen im Bild sind. */
@Composable
fun ChartLegend(
    first: String,
    second: String,
    modifier: Modifier = Modifier,
    palette: MetricColors? = null,
    /** In einer halb breiten Karte passen zwei Eintraege nicht nebeneinander. */
    vertical: Boolean = false,
) {
    val colors = LocalChartColors.current
    val hue = palette ?: colors.volume
    if (vertical) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LegendEntry(hue.base, first)
            LegendEntry(hue.light, second)
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LegendEntry(hue.base, first)
            LegendEntry(hue.light, second)
        }
    }
}

@Composable
private fun LegendEntry(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            // Nie in der Reihenfarbe: Farbe traegt der Punkt daneben.
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Die Achse unter einem Diagramm.
 *
 * Eigenes Layout statt einer Reihe gleich breiter Felder: bei zwoelf Wochen
 * ist ein Feld zehn Punkte breit, und "39" wurde darin zu "3" abgeschnitten.
 * Hier wird jede Beschriftung in ihrer natuerlichen Breite gemessen und ueber
 * der Mitte ihrer Saeule abgesetzt - die leeren Nachbarn geben den Platz her.
 * Am Rand rutscht sie so weit nach innen, dass sie ganz sichtbar bleibt.
 */
@Composable
private fun AxisLabels(
    labels: List<String>,
    highlighted: Int,
    style: androidx.compose.ui.text.TextStyle,
    axisColor: Color,
    modifier: Modifier = Modifier,
) {
    val chosenColor = MaterialTheme.colorScheme.onSurface
    val shown = labels.indices.filter { labels[it].isNotEmpty() }

    Layout(
        modifier = modifier.fillMaxWidth(),
        content = {
            shown.forEach { index ->
                Text(
                    text = labels[index],
                    style = style,
                    color = if (index == highlighted) chosenColor else axisColor,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        },
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(Constraints()) }
        val width = constraints.maxWidth
        val height = placeables.maxOfOrNull { it.height } ?: 0
        val slot = width.toFloat() / labels.size.coerceAtLeast(1)

        layout(width, height) {
            placeables.forEachIndexed { position, placeable ->
                val center = slot * shown[position] + slot / 2f
                val x = (center - placeable.width / 2f).toInt()
                    .coerceIn(0, (width - placeable.width).coerceAtLeast(0))
                placeable.place(x, 0)
            }
        }
    }
}

// --- Zeichenhilfen ---

private fun DrawScope.drawGrid(color: Color) {
    repeat(GRID_LINES) { line ->
        val y = size.height * line / GRID_LINES
        drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
    }
}

private fun DrawScope.drawColumn(
    left: Float,
    width: Float,
    barHeight: Float,
    corner: Float,
    color: Color,
) {
    val radius = minOf(corner, barHeight)
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(left, size.height - barHeight, left + width, size.height),
                topLeft = CornerRadius(radius, radius),
                topRight = CornerRadius(radius, radius),
                bottomRight = CornerRadius.Zero,
                bottomLeft = CornerRadius.Zero,
            ),
        )
    }
    drawPath(path, color)
}

private fun DrawScope.drawBarHorizontal(
    left: Float,
    width: Float,
    top: Float,
    barHeight: Float,
    corner: Float,
    color: Color,
) {
    val radius = minOf(corner, width)
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(left, top, left + width, top + barHeight),
                topLeft = CornerRadius.Zero,
                topRight = CornerRadius(radius, radius),
                bottomRight = CornerRadius(radius, radius),
                bottomLeft = CornerRadius.Zero,
            ),
        )
    }
    drawPath(path, color)
}

/** Abgeblendet, wenn eine andere Saeule ausgewaehlt ist. */
private fun Color.fade(dimmed: Boolean): Color = if (dimmed) copy(alpha = 0.35f) else this

/**
 * Rundet die Achsenobergrenze auf eine glatte Zahl: 1, 2, 5, 10, 20, 50 ...
 *
 * Ohne das steht an der Achse 347,2 statt 400, und der hoechste Balken klebt
 * immer am oberen Rand.
 */
internal fun niceCeil(value: Double): Double {
    if (value <= 0.0 || !value.isFinite()) return 1.0
    val magnitude = 10.0.pow(floorLog10(value))
    val normalized = value / magnitude
    val step = when {
        normalized <= 1.0 -> 1.0
        normalized <= 2.0 -> 2.0
        normalized <= 5.0 -> 5.0
        else -> 10.0
    }
    return step * magnitude
}

private fun floorLog10(value: Double): Int = kotlin.math.floor(log10(abs(value))).toInt()

/** Wie eine Miniatur gezeichnet wird: Saeulen fuer Mengen, Linie fuer Pegel. */
enum class SparkKind { COLUMNS, LINE }

/**
 * Die Miniaturfassung - ohne Achse, ohne Auswahl, ohne Zahlen im Bild.
 *
 * Fuer die Kacheln der Uebersicht: nebeneinander ist kein Platz fuer zwoelf
 * Beschriftungen, und die zwei Zahlen ueber und unter der Miniatur sagen mehr
 * als zwoelf abgeschnittene. Der Verlauf zum Nachlesen steht eine Ebene
 * tiefer im eigenen Fenster.
 *
 * Leere Zeitraeume bekommen einen Strich auf der Grundlinie statt gar nichts -
 * sonst sieht eine Luecke aus wie ein Rand.
 */
@Composable
fun Sparkline(
    values: List<Double>,
    palette: MetricColors,
    modifier: Modifier = Modifier,
    kind: SparkKind = SparkKind.COLUMNS,
    height: Dp = 54.dp,
) {
    if (values.isEmpty()) return
    val colors = LocalChartColors.current

    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        val stub = 2.dp.toPx()

        when (kind) {
            SparkKind.COLUMNS -> {
                val top = niceCeil(values.max())
                val slot = size.width / values.size
                val barWidth = (slot - BAR_GAP.toPx()).coerceAtLeast(2f)
                val corner = (barWidth / 2f).coerceAtMost(BAR_CORNER.toPx())

                values.forEachIndexed { index, value ->
                    val left = slot * index + (slot - barWidth) / 2f
                    if (value <= 0.0) {
                        drawColumn(left, barWidth, stub, stub / 2f, colors.grid)
                    } else {
                        val barHeight = ((value / top).toFloat() * size.height)
                            .coerceAtLeast(stub)
                        drawColumn(left, barWidth, barHeight, corner, palette.base)
                    }
                }
            }

            SparkKind.LINE -> {
                // Der Pegel beginnt nicht bei null - sonst waere eine Kurve
                // zwischen 110 und 140 bpm eine waagrechte Linie.
                val measured = values.filter { it > 0.0 }
                if (measured.isEmpty()) return@Canvas
                val low = measured.min() - 3
                val span = (measured.max() + 3 - low).coerceAtLeast(1.0)
                val slot = size.width / values.size
                val inset = 4.dp.toPx()

                fun pointOf(index: Int, value: Double) = Offset(
                    x = slot * index + slot / 2f,
                    y = inset + (size.height - 2 * inset) *
                        (1f - ((value - low) / span).toFloat()),
                )

                values.indices.zipWithNext().forEach { (a, b) ->
                    if (values[a] > 0.0 && values[b] > 0.0) {
                        drawLine(
                            color = palette.base,
                            start = pointOf(a, values[a]),
                            end = pointOf(b, values[b]),
                            strokeWidth = 2.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        )
                    }
                }

                // Nur der letzte gemessene Punkt bekommt eine Marke: das ist
                // der Wert, der in der Kachel darueber steht.
                val lastIndex = values.indexOfLast { it > 0.0 }
                val point = pointOf(lastIndex, values[lastIndex])
                drawCircle(colors.surface, radius = 5.dp.toPx(), center = point)
                drawCircle(palette.base, radius = 3.5f.dp.toPx(), center = point)
            }
        }
    }
}

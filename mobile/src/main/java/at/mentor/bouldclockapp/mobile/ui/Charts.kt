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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import at.mentor.bouldclockapp.mobile.LocalChartColors
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
private val BAR_CORNER = 4.dp
private val GRID_LINES = 3

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
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall

    val top = niceCeil(bars.maxOf { it.value })
    val peakIndex = bars.indices.maxByOrNull { bars[it].value } ?: 0

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
                        color = colors.series1.fade(dimmed),
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
                                color = colors.series2.fade(dimmed),
                                topLeft = Offset(left, size.height - highlightHeight),
                                size = androidx.compose.ui.geometry.Size(barWidth, highlightHeight),
                            )
                        }
                    }
                }
            }
        }

        // Achse: Beschriftungen als Text unter der Zeichenflaeche, gleich
        // verteilt wie die Saeulen. Im Bild gezeichneter Text muesste jede
        // Schriftgroesse selbst nachbauen.
        Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
            bars.forEachIndexed { index, bar ->
                Text(
                    text = bar.label,
                    style = labelStyle,
                    color = if (index == (selectedIndex ?: peakIndex)) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        axisColor
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Genau ein Wert im Klartext: der ausgewaehlte, sonst der hoechste.
        val shown = bars.getOrNull(selectedIndex ?: peakIndex)
        if (shown != null && shown.value > 0.0) {
            Text(
                text = "${shown.label}: ${valueFormat(shown.value)}",
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
) {
    if (bars.none { it.value > 0.0 }) return
    val colors = LocalChartColors.current
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

                    drawBarHorizontal(0f, full, y, barHeight, corner, colors.series1)

                    if (bar.highlight > 0.0) {
                        val share = (bar.highlight / bar.value).coerceIn(0.0, 1.0)
                        val width = (full * share.toFloat() - BAR_GAP.toPx()).coerceAtLeast(0f)
                        if (width > 0f) {
                            drawRect(
                                color = colors.series2,
                                topLeft = Offset(0f, y),
                                size = androidx.compose.ui.geometry.Size(width, barHeight),
                            )
                        }
                    }
                }
                Text(
                    text = bar.value.toInt().toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.size(width = 36.dp, height = rowHeight)
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

/** Legende - Pflicht, sobald zwei Reihen im Bild sind. */
@Composable
fun ChartLegend(
    first: String,
    second: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalChartColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LegendEntry(colors.series1, first)
        LegendEntry(colors.series2, second)
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

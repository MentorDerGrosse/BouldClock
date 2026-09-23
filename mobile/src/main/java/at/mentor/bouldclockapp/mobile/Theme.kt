package at.mentor.bouldclockapp.mobile

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Dunkles Erscheinungsbild.
 *
 * Fest dunkel und nicht der Systemeinstellung folgend - so gewuenscht.
 *
 * **Kein reines Schwarz.** Der Grund ist ein dunkles Blaugrau, die Karten liegen
 * zwei Stufen darueber. Auf Schwarz wirkt jede Karte wie ein Loch; mit
 * Grauabstufungen entsteht Tiefe, ohne dass es bunt wird. Dazu grosszuegige
 * Rundungen - das ist der Unterschied zwischen "Formular" und "Anwendung".
 */
private val DarkColors = darkColorScheme(
    primary = Color(0xFF5B9DF0),
    onPrimary = Color(0xFF06121F),
    primaryContainer = Color(0xFF1C4372),
    onPrimaryContainer = Color(0xFFD5E5FB),

    // Material faerbt damit *ausgewaehlt* - Reiter, Chips, Regler. Muss zum
    // Akzent passen und darf keine Diagrammfarbe sein, sonst bedeutet dieselbe
    // Farbe an zwei Stellen Verschiedenes.
    secondary = Color(0xFF9DB0C8),
    onSecondary = Color(0xFF10141A),
    secondaryContainer = Color(0xFF283445),
    onSecondaryContainer = Color(0xFFDCE6F4),

    background = Color(0xFF1A1D24),
    onBackground = Color(0xFFECEFF4),

    surface = Color(0xFF1A1D24),
    onSurface = Color(0xFFECEFF4),
    surfaceContainerLowest = Color(0xFF15171C),
    surfaceContainerLow = Color(0xFF20242B),
    surfaceContainer = Color(0xFF272B34),
    surfaceContainerHigh = Color(0xFF2F343E),
    surfaceContainerHighest = Color(0xFF3A404B),

    surfaceVariant = Color(0xFF2F343E),
    onSurfaceVariant = Color(0xFFA8B1BF),
    outline = Color(0xFF444B58),
    outlineVariant = Color(0xFF2F343E),

    error = Color(0xFFE66767),
    onError = Color(0xFF1F0605),
    errorContainer = Color(0xFF5A2020),
    onErrorContainer = Color(0xFFFBD5D5),
)

/** Runder als Material vorgibt - Karten bei 22 dp, Knoepfe und Chips voll gerundet. */
private val RoundShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/**
 * Farben fuer Diagramme.
 *
 * **Eine Farbe je Groesse, und jedes Diagramm zeigt nur eine Groesse.** Die
 * Farbe ist damit ein Wiedererkennungsmerkmal ueber die App hinweg, nicht die
 * Unterscheidung zweier Reihen im selben Bild.
 *
 * Geprueft mit dem Validierer der Diagrammrichtlinie, gegen den Kartengrund
 * #272B34: in der Reihenfolge, in der die Kacheln nebeneinander stehen,
 * bestehen alle benachbarten Paare - der engste ist Puls gegen Kalorien mit
 * 15,8 bei Zielwert 15. Jede Grundfarbe besteht mit ihrer helleren Stufe die
 * Ordinalpruefung, ebenso die sechsstufige Zonenleiter.
 *
 * Der Puls war vorher rot und lag damit bei 7,1 neben dem Kalorienorange -
 * auf der Uebersicht stehen die beiden Kacheln nebeneinander, und dort fielen
 * sie zusammen. Jetzt ist er pink.
 *
 * Der hervorgehobene Anteil ist deshalb **die hellere Stufe derselben Farbe**,
 * nicht eine zweite Farbe: "davon Tops" ist eine Teilmenge von "Versuche", und
 * eine Teilmenge ist keine eigene Kategorie.
 */
data class MetricColors(val base: Color, val light: Color)

data class ChartColors(
    val volume: MetricColors = MetricColors(Color(0xFF3987E5), Color(0xFF86B6EF)),
    val calories: MetricColors = MetricColors(Color(0xFFD95926), Color(0xFFF0906B)),
    val height: MetricColors = MetricColors(Color(0xFF199E70), Color(0xFF5CC3A0)),
    val pulse: MetricColors = MetricColors(Color(0xFFE34F9E), Color(0xFFF096C8)),
    val falls: MetricColors = MetricColors(Color(0xFF9085E9), Color(0xFFBDB6F2)),

    /** Hilfslinien, eine Stufe ueber dem Grund. Nie gestrichelt. */
    val grid: Color = Color(0xFF2F343E),

    /** Der Grund, gegen den Luecken und Ringe gezeichnet werden. */
    val surface: Color = Color(0xFF272B34),

    /**
     * Pulszonen - eine Leiter, keine Auswahl.
     *
     * Eine Farbe in sechs Helligkeiten. Auf dunklem Grund heisst heller lauter,
     * also liegt die Ruhezone hinten und die Maximalzone vorn.
     */
    val zoneRamp: List<Color> = listOf(
        Color(0xFF2160AB),
        Color(0xFF2B73CB),
        Color(0xFF3987E5),
        Color(0xFF6DA7EC),
        Color(0xFF9EC5F4),
        Color(0xFFCDE2FB),
    ),
) {
    /** Voreinstellung fuer Diagramme ohne eigene Groesse. */
    val series1: Color get() = volume.base
    val series2: Color get() = volume.light
}

val LocalChartColors = staticCompositionLocalOf { ChartColors() }

@Composable
fun BouldClockTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, shapes = RoundShapes) {
        CompositionLocalProvider(LocalChartColors provides ChartColors(), content = content)
    }
}

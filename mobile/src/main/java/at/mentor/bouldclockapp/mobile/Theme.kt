package at.mentor.bouldclockapp.mobile

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Dunkles Erscheinungsbild.
 *
 * Fest dunkel und nicht der Systemeinstellung folgend - so gewuenscht. Wenn es
 * spaeter mitgehen soll, ist das ein `if (isSystemInDarkTheme())` an dieser
 * einen Stelle.
 *
 * Gehalten wie ein Messgeraet: fast schwarzer Grund, drei Flaechenstufen und
 * *eine* kraeftige Farbe. Die Zahlen sollen das Lauteste auf dem Schirm sein,
 * nicht die Oberflaeche.
 */
private val DarkColors = darkColorScheme(
    primary = Color(0xFF3987E5),
    onPrimary = Color(0xFF06121F),
    primaryContainer = Color(0xFF16375C),
    onPrimaryContainer = Color(0xFFCFE2FB),

    // Material benutzt die Sekundaerfarbe fuer *ausgewaehlt* - Seitenleiste,
    // Fortschrittsbalken, Chips. Sie muss deshalb zum Akzent passen und darf
    // nicht die Diagrammfarbe sein: sonst faerbt sich die halbe Oberflaeche
    // orange, obwohl Orange "davon Tops" bedeutet.
    secondary = Color(0xFF8FA3BC),
    onSecondary = Color(0xFF0E1013),
    secondaryContainer = Color(0xFF223447),
    onSecondaryContainer = Color(0xFFD5E2F2),

    background = Color(0xFF0E1013),
    onBackground = Color(0xFFE8EAED),

    // Flaechen: Karten heben sich vom Grund ab, ohne Schatten oder Rahmen.
    surface = Color(0xFF0E1013),
    onSurface = Color(0xFFE8EAED),
    surfaceContainerLowest = Color(0xFF0B0D10),
    surfaceContainerLow = Color(0xFF14171C),
    surfaceContainer = Color(0xFF171A1F),
    surfaceContainerHigh = Color(0xFF1F232A),
    surfaceContainerHighest = Color(0xFF272C34),

    surfaceVariant = Color(0xFF1F232A),
    onSurfaceVariant = Color(0xFF9BA1AA),
    outline = Color(0xFF3A404A),
    outlineVariant = Color(0xFF262A31),

    error = Color(0xFFE66767),
    onError = Color(0xFF1F0605),
    errorContainer = Color(0xFF5A1E1E),
    onErrorContainer = Color(0xFFFBD5D5),
)

/**
 * Farben fuer Diagramme.
 *
 * Getrennt vom Material-Schema, weil sie einer anderen Regel folgen: sie muessen
 * auch fuer Farbenblinde unterscheidbar bleiben. Die beiden Reihenfarben sind
 * mit dem Validierer der Diagrammrichtlinie geprueft (alle Paare, dunkler
 * Grund) - Abstand 26,8 unter Protanopie bei einem Zielwert von 8.
 *
 * Wer eine dritte Reihe braucht: erst validieren, dann ergaenzen. Eine
 * dazuerfundene Farbe ist der haeufigste Weg, ein Diagramm unlesbar zu machen.
 */
data class ChartColors(
    /** Menge, Volumen, Hoehe - die Hauptreihe. */
    val series1: Color = Color(0xFF3987E5),

    /** Tops und Flashes - die zweite Reihe. */
    val series2: Color = Color(0xFFD95926),

    /** Hilfslinien, eine Stufe ueber dem Grund. Nie gestrichelt. */
    val grid: Color = Color(0xFF262A31),

    /** Der Grund, gegen den Luecken und Ringe gezeichnet werden. */
    val surface: Color = Color(0xFF171A1F),
)

val LocalChartColors = staticCompositionLocalOf { ChartColors() }

@Composable
fun BouldClockTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors) {
        CompositionLocalProvider(LocalChartColors provides ChartColors(), content = content)
    }
}

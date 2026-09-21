package at.mentor.bouldclockapp.mobile

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Dunkles Erscheinungsbild.
 *
 * Fest dunkel und nicht der Systemeinstellung folgend - so gewuenscht. Wenn es
 * spaeter mitgehen soll, ist das ein `if (isSystemInDarkTheme())` an dieser
 * einen Stelle.
 *
 * Die Farben lehnen sich an die Uhr an, damit beide Apps zusammengehoeren. Am
 * eigentlichen Design arbeiten wir spaeter.
 */
private val DarkColors = darkColorScheme(
    primary = Color(0xFFCFBCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378A),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCBC2DB),
    background = Color(0xFF111015),
    onBackground = Color(0xFFE6E1E9),
    surface = Color(0xFF111015),
    onSurface = Color(0xFFE6E1E9),
    surfaceVariant = Color(0xFF49454E),
    onSurfaceVariant = Color(0xFFCAC4CF),
    error = Color(0xFFFFB4AB),
)

@Composable
fun BouldClockTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}

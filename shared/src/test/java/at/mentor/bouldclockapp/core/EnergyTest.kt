package at.mentor.bouldclockapp.core

import at.mentor.bouldclockapp.core.metrics.BodyProfile
import at.mentor.bouldclockapp.core.metrics.Energy
import at.mentor.bouldclockapp.core.metrics.HeartBeat
import at.mentor.bouldclockapp.core.metrics.WallBlock
import at.mentor.bouldclockapp.core.model.BiologicalSex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnergyTest {

    private val t0 = 1_758_000_000_000L

    private val thomas = BodyProfile(
        weightKg = 73.0,
        ageYears = 21,
        sex = BiologicalSex.MALE,
        heightCm = 180,
        restingHrBpm = 60,
        maxHrBpm = null,
    )

    /** Sekuendliche Pulswerte ueber [seconds] Sekunden. */
    private fun beats(bpm: Int, seconds: Int, start: Long = t0) =
        (0..seconds).map { HeartBeat(start + it * 1000L, bpm) }

    private fun block(startSec: Int, endSec: Int, attempt: Boolean = true) =
        WallBlock(t0 + startSec * 1000L, t0 + endSec * 1000L, isAttempt = attempt)

    private fun estimate(
        beats: List<HeartBeat>,
        blocks: List<WallBlock> = emptyList(),
        seconds: Int,
        profile: BodyProfile = thomas,
    ) = Energy.estimate(beats, blocks, profile, t0, t0 + seconds * 1000L)

    @Test
    fun `ohne Pulswerte bleibt der Grundumsatz`() {
        val result = estimate(emptyList(), seconds = 600)
        assertEquals(Energy.basalKcalPerMinute(thomas) * 10, result.totalKcal, 0.01)
        assertEquals(0.0, result.onWallKcal, 0.001)
        assertEquals(0.0, result.activeKcal, 0.001)
    }

    /** Ein niedriger Puls heisst Ruhe, nicht weniger als Ruhe. */
    @Test
    fun `Ruhepuls ergibt genau den Grundumsatz`() {
        val result = estimate(beats(bpm = 55, seconds = 600), seconds = 600)
        assertEquals(Energy.basalKcalPerMinute(thomas) * 10, result.totalKcal, 0.2)
        assertEquals(0.0, result.activeKcal, 0.2)
    }

    @Test
    fun `an der Wand wird deutlich mehr gerechnet als in Ruhe`() {
        val kletternd = estimate(beats(150, 120), listOf(block(0, 120)), seconds = 120)
        val ruhend = estimate(beats(55, 120), seconds = 120)

        assertTrue("${kletternd.totalKcal} vs ${ruhend.totalKcal}", kletternd.totalKcal > ruhend.totalKcal * 3)
        assertEquals(kletternd.totalKcal, kletternd.onWallKcal, 0.5)
    }

    /**
     * Der Kern des Modells: derselbe erhoehte Puls zaehlt in der Pause weniger,
     * je laenger der letzte Block her ist.
     */
    @Test
    fun `der Ueberschuss klingt nach dem Absteigen ab`() {
        val kurzeSession = estimate(beats(130, 300), listOf(block(0, 20)), seconds = 300)

        // Dieselben 300 Sekunden bei Puls 130, aber ohne vorangehenden Block -
        // dann gibt es nichts, wovon etwas abklingen koennte.
        val ohneAbklingen = estimate(beats(130, 300), seconds = 300)

        assertTrue(
            "${kurzeSession.totalKcal} muss unter ${ohneAbklingen.totalKcal} liegen",
            kurzeSession.totalKcal < ohneAbklingen.totalKcal,
        )
    }

    /**
     * Eine Zugprobe ist Arbeit und zaehlt wie ein Versuch.
     *
     * Verglichen wird gegen denselben Abend ohne sie - nicht gegen "gar kein
     * Block", denn ohne vorangehenden Block klingt nichts ab, und das waere der
     * unrealistische Hoechstwert.
     */
    @Test
    fun `eine Zugprobe zaehlt fuer die Energie`() {
        val versuch = block(0, 20)
        val mitZug = estimate(
            beats(150, 240),
            listOf(versuch, block(40, 100, attempt = false)),
            seconds = 240,
        )
        val ohneZug = estimate(beats(150, 240), listOf(versuch), seconds = 240)

        assertTrue(mitZug.onWallKcal > ohneZug.onWallKcal)
        assertTrue(
            "${mitZug.totalKcal} muss ueber ${ohneZug.totalKcal} liegen",
            mitZug.totalKcal > ohneZug.totalKcal,
        )
    }

    @Test
    fun `Luecken im Puls werden mit dem Grundumsatz gefuellt`() {
        // Zwei Messwerte, dazwischen zehn Minuten nichts.
        val mitLuecke = listOf(HeartBeat(t0, 150), HeartBeat(t0 + 600_000L, 150))
        val result = Energy.estimate(mitLuecke, emptyList(), thomas, t0, t0 + 600_000L)

        val obergrenze = Energy.workingKcalPerMinute(150, thomas, Energy.basalKcalPerMinute(thomas)) * 10
        assertTrue("${result.totalKcal} darf nicht wie Dauerbelastung zaehlen", result.totalKcal < obergrenze / 2)
    }

    // --- Koerperwerte ---

    @Test
    fun `mit Koerpergroesse rechnet Mifflin-St Jeor`() {
        // 10*73 + 6.25*180 - 5*21 + 5 = 1755 kcal/Tag
        assertEquals(1755.0 / 1440.0, Energy.basalKcalPerMinute(thomas), 0.001)
    }

    @Test
    fun `ohne Koerpergroesse ein MET`() {
        val ohne = thomas.copy(heightCm = null)
        assertEquals(3.5 * 73.0 / 1000.0 * 5.0, Energy.basalKcalPerMinute(ohne), 0.001)
    }

    @Test
    fun `beobachteter Maximalpuls schlaegt die Altersformel`() {
        assertEquals(197, Energy.maxHeartRate(thomas))
        assertEquals(185, Energy.maxHeartRate(thomas.copy(maxHrBpm = 185)))
    }

    /** Uth: VO2max aus dem Verhaeltnis von Maximal- zu Ruhepuls. */
    @Test
    fun `VO2max folgt dem Pulsverhaeltnis`() {
        assertEquals(15.3 * 197 / 60.0, Energy.vo2Max(thomas), 0.1)

        // Niedrigerer Ruhepuls heisst mehr Ausdauer - und damit mehr Verbrauch.
        assertTrue(Energy.vo2Max(thomas.copy(restingHrBpm = 45)) > Energy.vo2Max(thomas))
    }

    @Test
    fun `unsinnige Pulswerte ergeben keine unsinnige VO2max`() {
        assertTrue(Energy.vo2Max(thomas.copy(restingHrBpm = 20)) <= 80.0)
        assertTrue(Energy.vo2Max(thomas.copy(restingHrBpm = 150)) >= 25.0)
    }
}

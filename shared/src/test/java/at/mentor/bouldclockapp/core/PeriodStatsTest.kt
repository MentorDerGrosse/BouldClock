package at.mentor.bouldclockapp.core

import at.mentor.bouldclockapp.core.metrics.Period
import at.mentor.bouldclockapp.core.metrics.SessionFact
import at.mentor.bouldclockapp.core.metrics.bucket
import at.mentor.bouldclockapp.core.metrics.fillGaps
import at.mentor.bouldclockapp.core.metrics.heightTotals
import at.mentor.bouldclockapp.core.metrics.startOfPeriod
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PeriodStatsTest {

    private val zone: ZoneId = ZoneId.of("Europe/Vienna")

    private fun factAt(
        date: String,
        time: String = "18:00",
        meters: Double? = 10.0,
        attempts: Int = 5,
        sends: Int = 2,
        hardest: Int? = null,
    ) = SessionFact(
        startedAt = LocalDateTime.parse("${date}T$time")
            .atZone(zone).toInstant().toEpochMilli(),
        attemptCount = attempts,
        sendCount = sends,
        climbHeightMeters = meters,
        caloriesTotal = 100.0,
        workMs = 60_000L,
        totalMs = 600_000L,
        hardestSendValue = hardest,
    )

    @Test
    fun `Wochen beginnen am Montag`() {
        // 2026-09-21 ist ein Montag, der 20. der Sonntag davor.
        assertEquals(
            LocalDate.parse("2026-09-21"),
            startOfPeriod(LocalDate.parse("2026-09-27"), Period.WEEK),
        )
        assertEquals(
            LocalDate.parse("2026-09-14"),
            startOfPeriod(LocalDate.parse("2026-09-20"), Period.WEEK),
        )
    }

    @Test
    fun `Sessions einer Woche werden zusammengezaehlt`() {
        val buckets = bucket(
            listOf(
                factAt("2026-09-21", meters = 12.0, attempts = 4, sends = 1),
                factAt("2026-09-24", meters = 8.0, attempts = 6, sends = 3),
                factAt("2026-10-01", meters = 5.0),
            ),
            Period.WEEK,
            zone,
        )

        assertEquals(2, buckets.size)
        assertEquals(LocalDate.parse("2026-09-21"), buckets[0].start)
        assertEquals(2, buckets[0].sessionCount)
        assertEquals(20.0, buckets[0].climbHeightMeters, 0.001)
        assertEquals(10, buckets[0].attemptCount)
        assertEquals(4, buckets[0].sendCount)
    }

    /**
     * Eine Session, die um 23:40 beginnt, gehoert zu dem Tag, an dem sie
     * angefangen hat - nicht zum naechsten, weil UTC schon weiter ist.
     */
    @Test
    fun `spaete Sessions zaehlen zum Starttag`() {
        val buckets = bucket(listOf(factAt("2026-09-21", time = "23:40")), Period.DAY, zone)
        assertEquals(LocalDate.parse("2026-09-21"), buckets.single().start)
    }

    @Test
    fun `Luecken werden mit Nullen aufgefuellt`() {
        val buckets = bucket(
            listOf(factAt("2026-09-21", meters = 12.0), factAt("2026-10-05", meters = 7.0)),
            Period.WEEK,
            zone,
        )
        val filled = fillGaps(buckets, Period.WEEK, count = 3, until = LocalDate.parse("2026-10-05"))

        assertEquals(3, filled.size)
        assertEquals(LocalDate.parse("2026-09-21"), filled[0].start)
        assertEquals(12.0, filled[0].climbHeightMeters, 0.001)
        // Die Woche dazwischen: vorhanden, aber leer.
        assertEquals(LocalDate.parse("2026-09-28"), filled[1].start)
        assertEquals(0, filled[1].sessionCount)
        assertEquals(7.0, filled[2].climbHeightMeters, 0.001)
    }

    @Test
    fun `haertester Grad ist das Maximum, nicht die Summe`() {
        val buckets = bucket(
            listOf(factAt("2026-09-21", hardest = 11), factAt("2026-09-22", hardest = 14)),
            Period.WEEK,
            zone,
        )
        assertEquals(14, buckets.single().hardestSendValue)
    }

    @Test
    fun `ohne Grad bleibt der haerteste Grad leer`() {
        val buckets = bucket(listOf(factAt("2026-09-21")), Period.WEEK, zone)
        assertNull(buckets.single().hardestSendValue)
    }

    @Test
    fun `Hoehenmeter je Woche Monat und Jahr`() {
        val today = LocalDate.parse("2026-09-23")
        val totals = heightTotals(
            listOf(
                factAt("2026-09-21", meters = 10.0), // diese Woche
                factAt("2026-09-10", meters = 20.0), // dieser Monat
                factAt("2026-03-01", meters = 30.0), // dieses Jahr
                factAt("2025-11-11", meters = 40.0), // frueher
            ),
            today,
            zone,
        )

        assertEquals(10.0, totals.week, 0.001)
        assertEquals(30.0, totals.month, 0.001)
        assertEquals(60.0, totals.year, 0.001)
        assertEquals(100.0, totals.total, 0.001)
    }

    @Test
    fun `Sessions ohne gemessene Hoehe zaehlen als null Meter`() {
        val totals = heightTotals(
            listOf(factAt("2026-09-21", meters = null), factAt("2026-09-22", meters = 5.0)),
            LocalDate.parse("2026-09-23"),
            zone,
        )
        assertEquals(5.0, totals.week, 0.001)
    }
}

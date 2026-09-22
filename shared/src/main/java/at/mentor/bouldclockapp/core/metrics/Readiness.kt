package at.mentor.bouldclockapp.core.metrics

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Wie gut sich heute zum Bouldern anfuehlt - aus den eigenen Daten, ohne Orakel. */
enum class ReadinessLevel(val displayName: String) {
    RESTED("Guter Tag"),
    OKAY("Geht"),
    TIRED("Lieber locker"),
    UNKNOWN("Zu wenig Daten"),
}

/**
 * Einschaetzung samt Begruendung.
 *
 * [reasons] ist der eigentliche Inhalt: eine Zahl ohne Begruendung waere ein
 * Orakel, und ein Orakel glaubt man genau einmal.
 */
data class Readiness(
    val level: ReadinessLevel,
    val reasons: List<String>,
)

/**
 * Ist heute ein guter Tag zum Bouldern?
 *
 * Bewusst eine Handvoll nachvollziehbarer Regeln statt eines gelernten
 * Modells: die Eingaben sind wenige und laut, und der Nutzer soll widersprechen
 * koennen. Was die App sieht, ist Abstand zur letzten Session, das Volumen der
 * letzten Woche gegenueber dem ueblichen, und wie hart die letzte Session war.
 *
 * Was sie **nicht** sieht - Schlaf, Muskelkater, Stress, Lust - weiss nur der
 * Mensch davor. Deshalb ist die Ausgabe eine Einschaetzung und kein Urteil.
 */
fun readiness(
    facts: List<SessionFact>,
    lastRpe: Int?,
    today: LocalDate,
    zone: java.time.ZoneId = java.time.ZoneId.systemDefault(),
): Readiness {
    if (facts.isEmpty()) return Readiness(ReadinessLevel.UNKNOWN, listOf("Noch keine Session."))

    val dates = facts.map {
        java.time.Instant.ofEpochMilli(it.startedAt).atZone(zone).toLocalDate()
    }
    val lastDate = dates.max()
    val daysSince = ChronoUnit.DAYS.between(lastDate, today).toInt()

    val reasons = mutableListOf<String>()
    var score = 0

    when {
        daysSince <= 0 -> {
            reasons += "Heute schon geklettert."
            score -= 2
        }
        daysSince == 1 -> {
            reasons += "Gestern geklettert."
            score -= 1
        }
        daysSince in 2..4 -> {
            reasons += "$daysSince Tage Pause."
            score += 2
        }
        daysSince in 5..10 -> {
            reasons += "$daysSince Tage Pause - gut erholt."
            score += 1
        }
        else -> {
            reasons += "$daysSince Tage nicht geklettert."
            score += 1
        }
    }

    // Volumen der letzten sieben Tage gegen den ueblichen Wochenschnitt.
    val lastWeek = facts.filter {
        val d = java.time.Instant.ofEpochMilli(it.startedAt).atZone(zone).toLocalDate()
        ChronoUnit.DAYS.between(d, today) in 0..6
    }
    val weeklyAttempts = lastWeek.sumOf { it.attemptCount }
    val typical = typicalWeeklyAttempts(facts, today, zone)

    if (typical != null && weeklyAttempts > 0) {
        when {
            weeklyAttempts > typical * 1.5 -> {
                reasons += "Diese Woche $weeklyAttempts Versuche, deutlich über deinem Schnitt."
                score -= 2
            }
            weeklyAttempts < typical * 0.5 -> {
                reasons += "Diese Woche erst $weeklyAttempts Versuche."
                score += 1
            }
            else -> reasons += "Volumen der Woche im üblichen Rahmen."
        }
    }

    lastRpe?.let { rpe ->
        if (rpe >= 9 && daysSince <= 2) {
            reasons += "Letzte Session war mit RPE $rpe sehr hart."
            score -= 1
        }
    }

    val level = when {
        score >= 2 -> ReadinessLevel.RESTED
        score >= 0 -> ReadinessLevel.OKAY
        else -> ReadinessLevel.TIRED
    }
    return Readiness(level, reasons)
}

/** Mittlere Versuche je Woche, ueber die letzten acht Wochen mit Aktivitaet. */
private fun typicalWeeklyAttempts(
    facts: List<SessionFact>,
    today: LocalDate,
    zone: java.time.ZoneId,
): Double? {
    val weeks = bucket(facts, Period.WEEK, zone)
        .filter { ChronoUnit.WEEKS.between(it.start, today) in 0..8 }
        .filter { it.attemptCount > 0 }
    if (weeks.size < 2) return null
    return weeks.sumOf { it.attemptCount }.toDouble() / weeks.size
}

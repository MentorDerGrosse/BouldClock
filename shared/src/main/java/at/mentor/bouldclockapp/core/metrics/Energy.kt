package at.mentor.bouldclockapp.core.metrics

import at.mentor.bouldclockapp.core.model.BiologicalSex
import kotlin.math.exp

/** Koerperdaten, soweit fuer die Energierechnung gebraucht. */
data class BodyProfile(
    val weightKg: Double,
    val ageYears: Int,
    val sex: BiologicalSex,

    /** Optional - ohne sie wird der Grundumsatz genaehert. */
    val heightCm: Int? = null,

    /** Gemessen. Ohne ihn eine Annahme, die spuerbar traegt. */
    val restingHrBpm: Int? = null,

    /** Hoechster je beobachteter Puls. Ohne ihn die Altersformel. */
    val maxHrBpm: Int? = null,
)

/** Ergebnis einer Sessionrechnung. */
data class EnergyEstimate(
    val totalKcal: Double,
    val onWallKcal: Double,
    val basalKcal: Double,
) {
    /** Was ueber dem Ruheverbrauch liegt - das Konto des Boulderns. */
    val activeKcal: Double get() = (totalKcal - basalKcal).coerceAtLeast(0.0)
}

/**
 * Energieverbrauch einer Bouldersession.
 *
 * Warum nicht die Zahl der Uhr: Samsung meldete in einer echten 50-Minuten-
 * Session 34 Mal 0,0 kcal, waehrend die Verfuegbarkeit durchgehend AVAILABLE
 * war. Und selbst wenn sie liefert, ist es eine fremde Blackbox - auf einer
 * Garmin waere es eine andere, und die Historie bekaeme eine Naht.
 *
 * ## Die drei Entscheidungen, die es genauer machen als eine Standardformel
 *
 * **Pulsreserve statt rohem Puls.** Der Anteil zwischen Ruhe- und Maximalpuls
 * entspricht dem Anteil an der Sauerstoffaufnahme. Das passt sich dem
 * einzelnen Herzen an, statt einen Durchschnittsmenschen anzunehmen.
 *
 * **Armarbeit-Korrektur.** Klettern ist Armarbeit, und bei gleichem Puls
 * verbraucht man mit den Armen weniger Sauerstoff als mit den Beinen. Genau
 * deshalb schaetzen generische Uhren beim Klettern zu hoch.
 *
 * **Nachbrennen statt Dauerbrand.** Der Puls bleibt in der Pause erhoeht - aus
 * zwei Gruenden, von denen nur einer Energie ist. Das Nachbrennen (Sauerstoff-
 * schuld, Kreatinphosphat, Laktat) ist echter Verbrauch und haengt am letzten
 * Block an der Wand; die Drift (Waermeabfuhr, Adrenalin) ist keiner und bleibt.
 * Deshalb klingt der Ueberschuss ueber dem Grundumsatz ab, gemessen ab dem
 * Absteigen. Ohne diese Trennung kaeme fuer dieselbe Session 435 statt 269 kcal
 * heraus - das ist der Fehler, den die meisten Apps machen.
 *
 * Fuers Bouldern folgt daraus etwas Unintuitives: **ein Boulder kostet in der
 * Pause mehr als an der Wand.** Zwanzig Sekunden am Limit laufen anaerob,
 * bezahlt wird danach. Die sinnvolle Trennung ist deshalb nicht "Wand gegen
 * Pause", sondern [EnergyEstimate.activeKcal] gegen Grundumsatz.
 *
 * Alle Parameter stehen benannt beieinander - sie sind der Teil, der spaeter
 * kalibriert wird, und die Uebersetzungsvorlage fuer Garmin.
 */
object Energy {

    /** Armarbeit: gleicher Puls, weniger Sauerstoff als bei Beinarbeit. */
    const val ARM_WORK_FACTOR: Double = 0.88

    /**
     * Abklingzeit des Nachbrennens, in Sekunden.
     *
     * Der zweitgroesste Hebel der ganzen Rechnung (±13 % zwischen 1,5 und 4
     * Minuten) und der einzige Wert, der sich nicht aus dem Puls ableiten
     * laesst - er beschreibt ja gerade, wie stark der Puls in der Erholung
     * luegt. Spaeter ueber den Beschleunigungssensor kalibrierbar: steht jemand
     * still und der Puls behauptet Arbeit, ist die Luecke messbar.
     */
    const val EPOC_TAU_SECONDS: Double = 150.0

    /** Energiegehalt eines Liters Sauerstoff. */
    const val KCAL_PER_LITRE_O2: Double = 5.0

    /** Sauerstoffaufnahme in Ruhe, ein MET. */
    const val REST_VO2_ML_PER_KG_MIN: Double = 3.5

    /** Solange der Ruhepuls nicht gemessen ist. */
    const val ASSUMED_RESTING_HR: Int = 60

    /** Groesste Luecke, die noch mit dem gemessenen Puls gefuellt wird. */
    const val MAX_GAP_SECONDS: Double = 5.0

    /** Grenzen, innerhalb derer eine geschaetzte VO2max plausibel bleibt. */
    private val VO2MAX_RANGE = 25.0..80.0

    /**
     * Rechnet eine Session durch.
     *
     * [beats] muss zeitlich sortiert sein. Luecken im Puls werden mit dem
     * Grundumsatz gefuellt statt mit dem letzten Messwert - eine Uhr, die
     * nichts meldet, ist kein Beleg fuer Anstrengung.
     */
    fun estimate(
        beats: List<HeartBeat>,
        blocks: List<WallBlock>,
        profile: BodyProfile,
        sessionStartedAt: Long,
        sessionEndedAt: Long,
    ): EnergyEstimate {
        val minutes = ((sessionEndedAt - sessionStartedAt).coerceAtLeast(0L)) / 60_000.0
        val basalPerMinute = basalKcalPerMinute(profile)
        val basalTotal = basalPerMinute * minutes

        if (beats.isEmpty()) {
            return EnergyEstimate(basalTotal, onWallKcal = 0.0, basalKcal = basalTotal)
        }

        val lastEnds = blocks.map { it.endedAt }.sorted()
        var total = 0.0
        var onWall = 0.0

        // Vor dem ersten und nach dem letzten Messwert: Grundumsatz.
        total += basalPerMinute * gapMinutes(sessionStartedAt, beats.first().timestampMs)
        total += basalPerMinute * gapMinutes(beats.last().timestampMs, sessionEndedAt)

        for (i in 1..beats.lastIndex) {
            val beat = beats[i]
            val gapSeconds = (beat.timestampMs - beats[i - 1].timestampMs) / 1000.0
            val measured = gapSeconds.coerceAtMost(MAX_GAP_SECONDS) / 60.0
            val unmeasured = (gapSeconds - MAX_GAP_SECONDS).coerceAtLeast(0.0) / 60.0

            val working = workingKcalPerMinute(beat.bpm, profile, basalPerMinute)
            val block = blocks.firstOrNull { it.contains(beat.timestampMs) }

            val rate = if (block != null) {
                working
            } else {
                // In der Pause klingt der Ueberschuss ab, gemessen ab dem
                // Absteigen vom letzten Block.
                val since = lastEnds.lastOrNull { it <= beat.timestampMs }
                val decay = since?.let { exp(-((beat.timestampMs - it) / 1000.0) / EPOC_TAU_SECONDS) } ?: 1.0
                basalPerMinute + (working - basalPerMinute) * decay
            }

            total += rate * measured + basalPerMinute * unmeasured
            if (block != null) onWall += rate * measured
        }

        return EnergyEstimate(
            totalKcal = total,
            onWallKcal = onWall,
            basalKcal = basalTotal,
        )
    }

    /**
     * Verbrauch bei diesem Puls, in kcal/min - so, als waere es Dauerbelastung.
     *
     * Nie unter dem Grundumsatz: ein niedriger Puls bedeutet Ruhe, nicht
     * weniger als Ruhe.
     */
    fun workingKcalPerMinute(bpm: Int, profile: BodyProfile, basalPerMinute: Double): Double {
        val restingHr = (profile.restingHrBpm ?: ASSUMED_RESTING_HR).toDouble()
        val maxHr = maxHeartRate(profile).toDouble()
        if (maxHr <= restingHr) return basalPerMinute

        val reserve = ((bpm - restingHr) / (maxHr - restingHr)).coerceIn(0.0, 1.0)
        val vo2 = reserve * (vo2Max(profile) - REST_VO2_ML_PER_KG_MIN) + REST_VO2_ML_PER_KG_MIN
        val kcal = vo2 * profile.weightKg / 1000.0 * KCAL_PER_LITRE_O2 * ARM_WORK_FACTOR
        return maxOf(kcal, basalPerMinute)
    }

    /**
     * Maximalpuls.
     *
     * Der beobachtete schlaegt jede Formel; ohne ihn Nes et al. statt des
     * verbreiteten "220 minus Alter", das im jungen Bereich deutlich danebenliegt.
     */
    fun maxHeartRate(profile: BodyProfile): Int =
        profile.maxHrBpm ?: (211.0 - 0.64 * profile.ageYears).toInt()

    /**
     * Geschaetzte maximale Sauerstoffaufnahme nach Uth et al.
     *
     * Braucht nur Ruhe- und Maximalpuls - beides messen wir selbst. Deshalb
     * geht der Ruhepuls doppelt in die Rechnung ein und ist der wichtigste
     * einzelne Messwert.
     */
    fun vo2Max(profile: BodyProfile): Double {
        val restingHr = (profile.restingHrBpm ?: ASSUMED_RESTING_HR).toDouble()
        if (restingHr <= 0.0) return VO2MAX_RANGE.start
        return (15.3 * maxHeartRate(profile) / restingHr).coerceIn(VO2MAX_RANGE)
    }

    /**
     * Grundumsatz in kcal/min.
     *
     * Nach Mifflin-St Jeor, sobald die Koerpergroesse bekannt ist - sonst ein
     * MET, was nur das Gewicht braucht und etwas hoeher liegt.
     */
    fun basalKcalPerMinute(profile: BodyProfile): Double {
        val height = profile.heightCm
            ?: return REST_VO2_ML_PER_KG_MIN * profile.weightKg / 1000.0 * KCAL_PER_LITRE_O2

        val base = 10.0 * profile.weightKg + 6.25 * height - 5.0 * profile.ageYears
        val perDay = when (profile.sex) {
            BiologicalSex.MALE -> base + 5.0
            BiologicalSex.FEMALE -> base - 161.0
            // Ohne Angabe die Mitte statt einer Behauptung.
            BiologicalSex.UNSPECIFIED -> base - 78.0
        }
        return perDay / (24.0 * 60.0)
    }

    private fun gapMinutes(from: Long, to: Long): Double =
        ((to - from).coerceAtLeast(0L)) / 60_000.0
}

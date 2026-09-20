package at.mentor.bouldclockapp.core.model

/**
 * Waehlbare Bereiche der Koerperdaten.
 *
 * Hier und nicht im Bildschirm, weil die Handy-App beim Bearbeiten dieselben
 * Grenzen braucht - sonst laesst sich dort eintragen, was die Uhr nie anbieten
 * wuerde.
 */
object ProfileRanges {

    val WEIGHT_KG: List<Int> = (30..200).toList()
    const val DEFAULT_WEIGHT_KG: Int = 70

    val AGE_YEARS: List<Int> = (10..99).toList()
    const val DEFAULT_AGE_YEARS: Int = 30

    fun clampWeight(kg: Int): Int = kg.coerceIn(WEIGHT_KG.first(), WEIGHT_KG.last())

    fun clampAge(years: Int): Int = years.coerceIn(AGE_YEARS.first(), AGE_YEARS.last())
}

package at.mentor.bouldclockapp.core.model

import kotlin.math.abs

/**
 * Neigungswinkel eines verstellbaren Boards.
 *
 * Anders als [WallAngle], das eine grobe Einschaetzung einer Hallenwand ist, ist
 * der Boardwinkel eine abgelesene Zahl - am Kilterboard steht sie an der Wand.
 *
 * Gespeichert wird er am *Versuch*, nicht an der Session: an einem Abend stellt
 * man das Board durchaus um.
 */
object BoardAngles {

    /** Verstellbereich des Kilterboards, in Fuenfergrad-Schritten. */
    val DEGREES: List<Int> = (0..70 step 5).toList()

    /** Der gaengigste Winkel - und was vorgeschlagen wird, wenn es keine Vorgeschichte gibt. */
    const val DEFAULT: Int = 40

    fun format(degrees: Int): String = "$degrees°"

    fun clamp(degrees: Int): Int = DEGREES.minBy { abs(it - degrees) }

    fun indexOf(degrees: Int): Int = DEGREES.indexOf(clamp(degrees)).coerceAtLeast(0)
}

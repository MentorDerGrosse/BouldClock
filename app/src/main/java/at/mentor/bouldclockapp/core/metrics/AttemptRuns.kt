package at.mentor.bouldclockapp.core.metrics

/** Ein Versuch, reduziert auf das, was die Zusammenfassung braucht. */
data class AttemptFact(
    val gradeValue: Int?,
    val boardAngleDegrees: Int? = null,
    val startsNewBoulder: Boolean,
    val isSend: Boolean,
    val workMs: Long,
    val hrMax: Int?,
)

/**
 * Aufeinanderfolgende Versuche desselben Grades.
 *
 * Ein Boulder: die Versuche zwischen zwei Grenzen. Wer sechsmal an 7A
 * herumprobiert, war sechsmal am selben Problem.
 */
data class AttemptRun(
    val gradeValue: Int?,
    val boardAngleDegrees: Int? = null,
    val attempts: Int,
    val sends: Int,
    val workMs: Long,
    val hrMax: Int?,
) {
    val isSent: Boolean get() = sends > 0

    /** Erster Versuch, sofort durch. */
    val isFlash: Boolean get() = attempts == 1 && sends == 1
}

/**
 * Fasst die Versuche einer Session zu Bouldern zusammen.
 *
 * Die Eingabe muss in zeitlicher Reihenfolge vorliegen. Getrennt wird an
 * [AttemptFact.startsNewBoulder] - also an dem, was beim Protokollieren
 * festgestellt wurde, nicht an einer Vermutung aus gleichen Graden. Zwei
 * verschiedene 7A hintereinander bleiben dadurch zwei Boulder.
 */
fun groupRuns(attempts: List<AttemptFact>): List<AttemptRun> {
    val runs = mutableListOf<AttemptRun>()
    attempts.forEach { fact ->
        val last = runs.lastOrNull()
        if (last != null && !fact.startsNewBoulder) {
            runs[runs.lastIndex] = last.copy(
                // Der Grad wird beim ersten Versuch mit Angabe festgehalten.
                gradeValue = last.gradeValue ?: fact.gradeValue,
                boardAngleDegrees = last.boardAngleDegrees ?: fact.boardAngleDegrees,
                attempts = last.attempts + 1,
                sends = last.sends + if (fact.isSend) 1 else 0,
                workMs = last.workMs + fact.workMs,
                hrMax = maxOfNullable(last.hrMax, fact.hrMax),
            )
        } else {
            runs += AttemptRun(
                gradeValue = fact.gradeValue,
                boardAngleDegrees = fact.boardAngleDegrees,
                attempts = 1,
                sends = if (fact.isSend) 1 else 0,
                workMs = fact.workMs,
                hrMax = fact.hrMax,
            )
        }
    }
    return runs
}

private fun maxOfNullable(a: Int?, b: Int?): Int? = when {
    a == null -> b
    b == null -> a
    else -> maxOf(a, b)
}

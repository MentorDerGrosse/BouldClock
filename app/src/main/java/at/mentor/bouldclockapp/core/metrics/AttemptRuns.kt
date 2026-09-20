package at.mentor.bouldclockapp.core.metrics

/** Ein Versuch, reduziert auf das, was die Zusammenfassung braucht. */
data class AttemptFact(
    val gradeValue: Int?,
    val isSend: Boolean,
    val workMs: Long,
    val hrMax: Int?,
)

/**
 * Aufeinanderfolgende Versuche desselben Grades.
 *
 * In der Praxis ist das ein Boulder: wer sechsmal hintereinander an 7A
 * herumprobiert, war sechsmal am selben Problem. Wechselt der Grad, faengt eine
 * neue Gruppe an - auch wenn spaeter wieder derselbe Grad kommt.
 */
data class AttemptRun(
    val gradeValue: Int?,
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
 * Fasst die Versuche einer Session zu Boulder-Gruppen zusammen.
 *
 * Die Eingabe muss in zeitlicher Reihenfolge vorliegen.
 *
 * Bewusste Naeherung: zwei verschiedene 7A direkt hintereinander verschmelzen zu
 * einer Gruppe, weil die App den Boulder nicht kennt. Sauber trennen liesse sich
 * das erst, wenn Versuche einem konkreten Problem zugeordnet werden.
 */
fun groupRuns(attempts: List<AttemptFact>): List<AttemptRun> {
    val runs = mutableListOf<AttemptRun>()
    attempts.forEach { fact ->
        val last = runs.lastOrNull()
        if (last != null && last.gradeValue == fact.gradeValue) {
            runs[runs.lastIndex] = last.copy(
                attempts = last.attempts + 1,
                sends = last.sends + if (fact.isSend) 1 else 0,
                workMs = last.workMs + fact.workMs,
                hrMax = maxOfNullable(last.hrMax, fact.hrMax),
            )
        } else {
            runs += AttemptRun(
                gradeValue = fact.gradeValue,
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

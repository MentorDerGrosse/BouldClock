package at.mentor.bouldclockapp.core.session

/**
 * Wo in der Session man gerade steht.
 *
 * Die ganze Bedienung waehrend des Boulderns laeuft ueber genau einen Ausloeser,
 * der zwischen diesen Zustaenden schaltet:
 *
 *     Ready --loesen--> Climbing --loesen--> Resting --loesen--> Climbing --> ...
 *
 * Entscheidend: aus [Resting] fuehrt **kein** automatischer Uebergang heraus.
 * Der Pausen-Timer ist ein Signal, kein Zustandswechsel. Laeuft er ab, vibriert
 * die Uhr und der Zaehler laeuft in die Ueberzeit weiter - sitzen bleiben,
 * nachschrauben, quatschen ist ausdruecklich erlaubt. Der naechste Versuch
 * beginnt ausschliesslich, wenn man selbst ausloest.
 */
sealed interface SessionPhase {

    /** Session laeuft, noch kein Versuch gemacht. */
    data object Ready : SessionPhase

    /** An der Wand. */
    data class Climbing(
        val attemptId: String,
        val startedAt: Long,
    ) : SessionPhase

    /** Zwischen zwei Versuchen. */
    data class Resting(
        val since: Long,
        val targetMs: Long,
        /** Der gerade beendete Versuch - hier wird das Ergebnis nachgetragen. */
        val lastAttemptId: String,
    ) : SessionPhase
}

/** Was der naechste Druck auslaesen wird. Beschriftet den Knopf. */
enum class TriggerAction(val label: String) {
    START_FIRST("Start"),
    END_ATTEMPT("Beenden"),
    START_NEXT("Weiter"),
}

val SessionPhase.nextAction: TriggerAction
    get() = when (this) {
        SessionPhase.Ready -> TriggerAction.START_FIRST
        is SessionPhase.Climbing -> TriggerAction.END_ATTEMPT
        is SessionPhase.Resting -> TriggerAction.START_NEXT
    }

/**
 * Stand der laufenden Pause.
 *
 * [remainingMs] wird bewusst negativ, statt bei null zu klemmen: die Ueberzeit
 * ist eine echte Information. Wer nach einer 5-Minuten-Pause regelmaessig noch
 * vier Minuten sitzt, trainiert etwas anderes, als er glaubt.
 */
data class RestProgress(
    val elapsedMs: Long,
    val targetMs: Long,
) {
    val remainingMs: Long get() = targetMs - elapsedMs

    val isOvertime: Boolean get() = elapsedMs >= targetMs

    val overtimeMs: Long get() = (elapsedMs - targetMs).coerceAtLeast(0L)

    /** Fuer den Fortschrittsring, gekappt - der Ring fuellt sich, die Zeit laeuft weiter. */
    val fraction: Float
        get() = if (targetMs <= 0L) 1f else (elapsedMs.toFloat() / targetMs).coerceIn(0f, 1f)

    companion object {
        fun of(resting: SessionPhase.Resting, now: Long): RestProgress = RestProgress(
            elapsedMs = (now - resting.since).coerceAtLeast(0L),
            targetMs = resting.targetMs,
        )
    }
}

package at.mentor.bouldclockapp.core.session

import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.GradeSystem

/**
 * Wo in der Session man gerade steht.
 *
 * Die ganze Bedienung waehrend des Boulderns laeuft ueber genau einen Ausloeser,
 * der zwischen diesen Zustaenden schaltet:
 *
 *     Ready --> Climbing --> Grading --> Resting --> Climbing --> ...
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

    /**
     * Gradabfrage direkt nach dem Absteigen.
     *
     * Eigener Zustand und kein Beiwerk der Pause: hier steht man noch unter dem
     * Boulder und weiss, was er hatte. Vorgeschlagen wird der zuletzt
     * verwendete Grad - war es derselbe Boulder, tippt man nur weiter.
     *
     * [endedAt] ist der Zeitpunkt des Absteigens. Die Pause laeuft ab da, nicht
     * ab dem Bestaetigen - sonst wuerde die Zeit, die man hier verbringt,
     * unterschlagen, und der Pausenanzeige waere nicht mehr zu trauen.
     */
    data class Grading(
        val attemptId: String,
        val endedAt: Long,
        val gradeValue: Int,
        val gradeSystem: GradeSystem,
    ) : SessionPhase

    /** Zwischen zwei Versuchen. */
    data class Resting(
        val since: Long,
        val targetMs: Long,
        /** Der gerade beendete Versuch - hier wird das Ergebnis nachgetragen. */
        val lastAttemptId: String,
        /**
         * Was fuer diesen Versuch protokolliert wurde, `null` solange nichts.
         *
         * Steht hier und nicht nur in der Datenbank, damit der Bildschirm es
         * anzeigen kann. Ein Tipper, der nichts sichtbar bewirkt, ist auf einer
         * Uhr nicht von einem danebengegangenen zu unterscheiden.
         */
        val loggedOutcome: AttemptOutcome? = null,
    ) : SessionPhase
}

/** Was der naechste Druck auslaesen wird. Beschriftet den Knopf. */
enum class TriggerAction(val label: String) {
    START_FIRST("Start"),
    END_ATTEMPT("Beenden"),

    /** Grad bestaetigen und die Pause anzeigen. */
    CONFIRM_GRADE("Weiter"),

    /** Naechster Versuch. Bewusst dasselbe Wort wie in [START_FIRST]: */
    /** "Start" heisst in der ganzen App "ein Versuch beginnt". */
    START_NEXT("Start"),
}

val SessionPhase.nextAction: TriggerAction
    get() = when (this) {
        SessionPhase.Ready -> TriggerAction.START_FIRST
        is SessionPhase.Climbing -> TriggerAction.END_ATTEMPT
        is SessionPhase.Grading -> TriggerAction.CONFIRM_GRADE
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

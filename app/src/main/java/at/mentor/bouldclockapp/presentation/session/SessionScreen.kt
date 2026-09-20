package at.mentor.bouldclockapp.presentation.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.core.model.RestDurations
import at.mentor.bouldclockapp.core.session.RestProgress
import at.mentor.bouldclockapp.core.session.SessionPhase
import at.mentor.bouldclockapp.core.session.TriggerAction
import at.mentor.bouldclockapp.core.session.nextAction
import at.mentor.bouldclockapp.presentation.components.GradePicker
import at.mentor.bouldclockapp.presentation.theme.BouldClockAppTheme

/**
 * Der Bildschirm waehrend der Session.
 *
 * Zustandslos - die Phase kommt von aussen, damit sie sich in der Vorschau und
 * im Test durchspielen laesst.
 *
 * Zur Bedienflaeche: in [SessionPhase.Ready] und [SessionPhase.Climbing] ist der
 * ganze Bildschirm der Ausloeser. In [SessionPhase.Resting] nicht - dort liegen
 * die Ergebnistasten, und ein danebengegangener Tipper wuerde sonst den naechsten
 * Versuch starten. Weiter geht es dort ueber eine eigene, grosse Taste.
 */
@Composable
fun SessionScreen(
    phase: SessionPhase,
    onTrigger: () -> Unit,
    onOutcome: (AttemptOutcome) -> Unit,
    onGradeChange: (Int) -> Unit,
    onFinishSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val now by rememberNow()

    when (phase) {
        SessionPhase.Ready -> TriggerSurface(
            onTrigger = onTrigger,
            modifier = modifier,
            onFinishSession = onFinishSession,
        ) {
            BigState(
                value = phase.nextAction.label,
                caption = "Tippen startet den Versuch",
                hint = "lang druecken: Session beenden",
            )
        }

        is SessionPhase.Climbing -> TriggerSurface(
            onTrigger = onTrigger,
            modifier = modifier,
            onFinishSession = onFinishSession,
        ) {
            BigState(
                value = RestDurations.format((now - phase.startedAt).coerceAtLeast(0L)),
                // Frueher stand hier "tippen zum Beenden". Das las sich wie
                // "Session beenden" - gemeint ist der Versuch. Wortwahl, die
                // einen Nutzer glauben liess, das Beenden funktioniere nicht.
                caption = "Tippen beendet den Versuch",
            )
        }

        is SessionPhase.Grading -> GradingContent(
            phase = phase,
            onGradeChange = onGradeChange,
            onConfirm = onTrigger,
            modifier = modifier,
        )

        is SessionPhase.Resting -> RestingContent(
            progress = RestProgress.of(phase, now),
            loggedOutcome = phase.loggedOutcome,
            onTrigger = onTrigger,
            onOutcome = onOutcome,
            onFinishSession = onFinishSession,
            modifier = modifier,
        )
    }
}

@Composable
private fun RestingContent(
    progress: RestProgress,
    loggedOutcome: AttemptOutcome?,
    onTrigger: () -> Unit,
    onOutcome: (AttemptOutcome) -> Unit,
    onFinishSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current

    // Einmalig vibrieren, wenn die Soll-Pause um ist. Sonst passiert nichts:
    // der Zustand bleibt Resting, bis der Nutzer selbst ausloest.
    LaunchedEffect(progress.isOvertime) {
        if (progress.isOvertime) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Ausdrueckliche Box: der Ring liegt hinter dem Inhalt. Vorher hing das daran,
    // dass der Eltern-Container zufaellig einer war.
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(progress = { progress.fraction })

        // Knapp bemessen: auf 438 px gehen sich Countdown, Ergebnistasten und
        // Weiter nur aus, wenn nichts Ueberfluessiges dazwischensteht. Das Label
        // erscheint deshalb nur, wenn es etwas sagt.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = if (progress.isOvertime) {
                    "+${RestDurations.format(progress.overtimeMs)}"
                } else {
                    RestDurations.format(progress.remainingMs)
                },
                style = MaterialTheme.typography.numeralMedium,
                color = if (progress.isOvertime) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            // Zweite, unmissverstaendliche Rueckmeldung neben der hervorgehobenen
            // Taste. Die Zeile ist sonst leer, und auf einer Uhr schaut man kurz
            // hin statt genau hin.
            val caption = when {
                loggedOutcome != null -> loggedOutcome.displayName + " notiert"
                progress.isOvertime -> "Pause vorbei"
                else -> null
            }
            if (caption != null) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                LOGGABLE_OUTCOMES.forEach { outcome ->
                    val isChosen = outcome == loggedOutcome
                    CompactButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOutcome(outcome)
                        },
                        modifier = Modifier.semantics { selected = isChosen },
                        colors = if (isChosen) {
                            ButtonDefaults.filledVariantButtonColors()
                        } else {
                            ButtonDefaults.filledTonalButtonColors()
                        },
                        label = {
                            Text(
                                text = outcome.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                            )
                        },
                    )
                }
            }

            // "Start" heisst in der ganzen App "ein Versuch beginnt" - in Ready
            // wie hier. "Weiter" gehoert der Gradabfrage und heisst dort
            // "bestaetigen". Zwei gleiche Woerter fuer Verschiedenes waeren
            // genau die Zweideutigkeit, die schon einmal Verwirrung gestiftet hat.
            // Gleiche Regel wie auf der Ausloeseflaeche: kurz schaltet weiter, lang hoert auf.
            CompactButton(
                onClick = onTrigger,
                onLongClick = onFinishSession,
                onLongClickLabel = "Session beenden",
                label = {
                    Text(
                        text = TriggerAction.START_NEXT.label,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                    )
                },
            )
        }
    }
}

/**
 * Gradabfrage direkt nach dem Absteigen.
 *
 * Bewusst kein Auslesefeld ueber die ganze Flaeche: hier wird gedreht und
 * bestaetigt, ein Fehltipper duerfte nicht die Pause ueberspringen.
 *
 * Vorgeschlagen wird der zuletzt verwendete Grad. War es derselbe Boulder,
 * ist es ein Tipper auf "Weiter" und sonst nichts.
 */
@Composable
private fun GradingContent(
    phase: SessionPhase.Grading,
    onGradeChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Keine Ueberschrift: der obere Rand gehoert der Systemuhrzeit von
    // ScreenScaffold, dort wird alles ueberdeckt. Ein Rad voller Grade braucht
    // ohnehin keine Beschriftung.
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, top = 22.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        GradePicker(
            system = phase.gradeSystem,
            value = phase.gradeValue,
            onValueChange = onGradeChange,
            modifier = Modifier.weight(1f),
        )
        CompactButton(
            onClick = onConfirm,
            label = { Text("Weiter", style = MaterialTheme.typography.labelMedium, maxLines = 1) },
        )
    }
}

/** Die drei Ergebnisse, die waehrend der Session zaehlen. Zone und Abbruch kommen ins Menue. */
private val LOGGABLE_OUTCOMES = listOf(
    AttemptOutcome.FLASH,
    AttemptOutcome.TOP,
    AttemptOutcome.FAIL,
)

@Composable
private fun BigState(value: String, caption: String, hint: String? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(horizontal = 24.dp),
    ) {
        Text(text = value, style = MaterialTheme.typography.numeralMedium)
        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@WearPreviewDevices
@Composable
private fun SessionReadyPreview() {
    BouldClockAppTheme {
        SessionScreen(
            phase = SessionPhase.Ready,
            onTrigger = {},
            onOutcome = {},
            onGradeChange = {},
            onFinishSession = {},
        )
    }
}

@WearPreviewDevices
@Composable
private fun SessionRestingPreview() {
    BouldClockAppTheme {
        SessionScreen(
            phase = SessionPhase.Resting(
                since = System.currentTimeMillis() - 95_000L,
                targetMs = 180_000L,
                lastAttemptId = "preview",
                loggedOutcome = AttemptOutcome.TOP,
            ),
            onTrigger = {},
            onOutcome = {},
            onGradeChange = {},
            onFinishSession = {},
        )
    }
}

@WearPreviewDevices
@Composable
private fun SessionGradingPreview() {
    BouldClockAppTheme {
        SessionScreen(
            phase = SessionPhase.Grading(
                attemptId = "preview",
                endedAt = System.currentTimeMillis(),
                gradeValue = Grades.parse("6C") ?: Grades.DEFAULT_VALUE,
                gradeSystem = GradeSystem.FONT,
            ),
            onTrigger = {},
            onOutcome = {},
            onGradeChange = {},
            onFinishSession = {},
        )
    }
}

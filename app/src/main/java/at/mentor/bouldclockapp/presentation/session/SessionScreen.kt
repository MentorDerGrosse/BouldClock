package at.mentor.bouldclockapp.presentation.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.core.session.RestProgress
import at.mentor.bouldclockapp.core.session.SessionPhase
import at.mentor.bouldclockapp.core.session.TriggerAction
import at.mentor.bouldclockapp.core.session.nextAction
import at.mentor.bouldclockapp.presentation.components.AnglePicker
import at.mentor.bouldclockapp.presentation.components.GradePicker
import at.mentor.bouldclockapp.presentation.theme.BouldClockAppTheme

/**
 * Der Bildschirm waehrend der Session.
 *
 * Zustandslos - die Phase kommt von aussen, damit sie sich in der Vorschau und
 * im Test durchspielen laesst.
 *
 * Zur Bedienflaeche: in [SessionPhase.Ready] und [SessionPhase.Climbing] ist der
 * ganze Bildschirm der Ausloeser. In den anderen Phasen nicht - dort liegen
 * Tasten, und ein danebengegangener Tipper wuerde sonst den naechsten Versuch
 * starten. Langer Druck beendet ueberall die Session.
 */
@Composable
fun SessionScreen(
    phase: SessionPhase,
    type: SessionType,
    onTrigger: () -> Unit,
    onOutcome: (AttemptOutcome) -> Unit,
    onGradeChange: (Int) -> Unit,
    onAngleChange: (Int) -> Unit,
    onNewBoulder: () -> Unit,
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
                hint = "lang drücken: Session beenden",
            )
        }

        is SessionPhase.Climbing -> TriggerSurface(
            onTrigger = onTrigger,
            modifier = modifier,
            onFinishSession = onFinishSession,
        ) {
            BigState(
                value = RestDurations.format((now - phase.startedAt).coerceAtLeast(0L)),
                caption = "Tippen beendet den Versuch",
            )
        }

        is SessionPhase.ChoosingAngle -> PickerScreen(
            onConfirm = onTrigger,
            onFinishSession = onFinishSession,
            modifier = modifier,
        ) {
            AnglePicker(
                degrees = phase.angleDegrees,
                onDegreesChange = onAngleChange,
                modifier = Modifier.weight(1f),
            )
        }

        is SessionPhase.Grading -> GradingContent(
            phase = phase,
            onGradeChange = onGradeChange,
            onConfirm = onTrigger,
            onNewBoulder = onNewBoulder,
            onFinishSession = onFinishSession,
            modifier = modifier,
        )

        is SessionPhase.Resting -> RestingContent(
            progress = RestProgress.of(phase, now),
            loggedOutcome = phase.loggedOutcome,
            isCompetition = type.isCompetition,
            onTrigger = onTrigger,
            onOutcome = onOutcome,
            onFinishSession = onFinishSession,
            modifier = modifier,
        )
    }
}

/**
 * Gradabfrage direkt nach dem Absteigen.
 *
 * Vorgeschlagen wird der zuletzt verwendete Grad. War es derselbe Boulder, ist
 * es ein Tipper auf "Weiter" und sonst nichts.
 *
 * Nur wenn unklar ist, ob ein neuer Boulder beginnt - Sturz, danach derselbe
 * Grad - erscheint "Neu" daneben. Ein eigenes Ja/Nein-Fenster waere beim
 * Projektieren zwanzig zusaetzliche Tipper fuer zwanzigmal dieselbe Antwort.
 */
@Composable
private fun GradingContent(
    phase: SessionPhase.Grading,
    onGradeChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onNewBoulder: () -> Unit,
    onFinishSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PickerScreen(
        onConfirm = onConfirm,
        onFinishSession = onFinishSession,
        modifier = modifier,
        extraButton = if (phase.boulderAmbiguous) {
            {
                CompactButton(
                    onClick = onNewBoulder,
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    label = { Text("Neu", style = MaterialTheme.typography.labelMedium, maxLines = 1) },
                )
            }
        } else {
            null
        },
    ) {
        GradePicker(
            system = phase.gradeSystem,
            value = phase.gradeValue,
            onValueChange = onGradeChange,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Gemeinsames Geruest fuer Winkel- und Gradabfrage: Rad oben, Bestaetigen unten.
 *
 * Keine Ueberschrift - der obere Rand gehoert der Systemuhrzeit von
 * ScreenScaffold, dort wird alles ueberdeckt. Ein Rad voller Zahlen braucht
 * ohnehin keine Beschriftung.
 */
@Composable
private fun PickerScreen(
    onConfirm: () -> Unit,
    onFinishSession: () -> Unit,
    modifier: Modifier = Modifier,
    extraButton: (@Composable () -> Unit)? = null,
    picker: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, top = 22.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        picker()
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CompactButton(
                onClick = onConfirm,
                onLongClick = onFinishSession,
                onLongClickLabel = SESSION_END_LABEL,
                label = { Text("Weiter", style = MaterialTheme.typography.labelMedium, maxLines = 1) },
            )
            extraButton?.invoke()
        }
    }
}

@Composable
private fun RestingContent(
    progress: RestProgress,
    loggedOutcome: AttemptOutcome?,
    isCompetition: Boolean,
    onTrigger: () -> Unit,
    onOutcome: (AttemptOutcome) -> Unit,
    onFinishSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current

    // Einmalig vibrieren, wenn die Soll-Pause um ist. Sonst passiert nichts:
    // der Zustand bleibt Resting, bis der Nutzer selbst ausloest.
    LaunchedEffect(progress.isOvertime, isCompetition) {
        if (progress.isOvertime && !isCompetition) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Im Wettkampf kein Ring: es gibt kein Pausenziel, an dem er sich fuellen
        // koennte. Die Pause laeuft dort einfach hoch.
        if (!isCompetition) {
            CircularProgressIndicator(progress = { progress.fraction })
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = when {
                    isCompetition -> RestDurations.format(progress.elapsedMs)
                    progress.isOvertime -> "+${RestDurations.format(progress.overtimeMs)}"
                    else -> RestDurations.format(progress.remainingMs)
                },
                style = MaterialTheme.typography.numeralMedium,
                color = if (progress.isOvertime && !isCompetition) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )

            val caption = when {
                loggedOutcome != null -> loggedOutcome.displayName + " notiert"
                isCompetition -> "Pause"
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
                val outcomes = if (isCompetition) COMP_OUTCOMES else NORMAL_OUTCOMES
                outcomes.forEach { outcome ->
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
            // "bestaetigen". Kurz schaltet weiter, lang hoert auf.
            CompactButton(
                onClick = onTrigger,
                onLongClick = onFinishSession,
                onLongClickLabel = SESSION_END_LABEL,
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

private const val SESSION_END_LABEL = "Session beenden"

/** Alltag: was man geschafft hat oder eben nicht. */
private val NORMAL_OUTCOMES = listOf(
    AttemptOutcome.FLASH,
    AttemptOutcome.TOP,
    AttemptOutcome.FAIL,
)

/**
 * Wettkampf: die Wertung kennt Zone statt Sturz. Ein Sturz wird nicht getippt,
 * sondern beim Weiterdruecken automatisch eingetragen - im Wettkampf hat man
 * keine Zeit zum Protokollieren.
 */
private val COMP_OUTCOMES = listOf(
    AttemptOutcome.FLASH,
    AttemptOutcome.TOP,
    AttemptOutcome.ZONE,
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
private fun SessionRestingPreview() {
    BouldClockAppTheme {
        SessionScreen(
            phase = SessionPhase.Resting(
                since = System.currentTimeMillis() - 95_000L,
                targetMs = 180_000L,
                lastAttemptId = "preview",
                loggedOutcome = AttemptOutcome.TOP,
            ),
            type = SessionType.FREE,
            onTrigger = {},
            onOutcome = {},
            onGradeChange = {},
            onAngleChange = {},
            onNewBoulder = {},
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
                previousGradeValue = Grades.parse("6C"),
                previousWasSend = false,
            ),
            type = SessionType.FREE,
            onTrigger = {},
            onOutcome = {},
            onGradeChange = {},
            onAngleChange = {},
            onNewBoulder = {},
            onFinishSession = {},
        )
    }
}

@WearPreviewDevices
@Composable
private fun CompetitionRestingPreview() {
    BouldClockAppTheme {
        SessionScreen(
            phase = SessionPhase.Resting(
                since = System.currentTimeMillis() - 42_000L,
                targetMs = 0L,
                lastAttemptId = "preview",
            ),
            type = SessionType.COMPETITION,
            onTrigger = {},
            onOutcome = {},
            onGradeChange = {},
            onAngleChange = {},
            onNewBoulder = {},
            onFinishSession = {},
        )
    }
}

package at.mentor.bouldclockapp.presentation.session

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import at.mentor.bouldclockapp.core.model.AttemptOutcome
import at.mentor.bouldclockapp.core.model.RestDurations
import at.mentor.bouldclockapp.core.session.RestProgress
import at.mentor.bouldclockapp.core.session.SessionPhase
import at.mentor.bouldclockapp.core.session.nextAction
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
                caption = "Tippen fuer den ersten Versuch",
            )
        }

        is SessionPhase.Climbing -> TriggerSurface(
            onTrigger = onTrigger,
            modifier = modifier,
            onFinishSession = onFinishSession,
        ) {
            BigState(
                value = RestDurations.format((now - phase.startedAt).coerceAtLeast(0L)),
                caption = "An der Wand - tippen zum Beenden",
            )
        }

        is SessionPhase.Resting -> RestingContent(
            progress = RestProgress.of(phase, now),
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

    CircularProgressIndicator(progress = { progress.fraction })

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
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
        Text(
            text = if (progress.isOvertime) "Pause vorbei" else "Pause",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            LOGGABLE_OUTCOMES.forEach { outcome ->
                CompactButton(
                    onClick = { onOutcome(outcome) },
                    label = {
                        Text(outcome.displayName, style = MaterialTheme.typography.labelSmall)
                    },
                )
            }
        }

        // Gleiche Regel wie auf der Ausloeseflaeche: kurz schaltet weiter, lang hoert auf.
        Button(
            onClick = onTrigger,
            onLongClick = onFinishSession,
            onLongClickLabel = "Session beenden",
        ) {
            Text("Weiter", style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** Die drei Ergebnisse, die waehrend der Session zaehlen. Zone und Abbruch kommen ins Menue. */
private val LOGGABLE_OUTCOMES = listOf(
    AttemptOutcome.FLASH,
    AttemptOutcome.TOP,
    AttemptOutcome.FAIL,
)

@Composable
private fun BigState(value: String, caption: String) {
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
            ),
            onTrigger = {},
            onOutcome = {},
            onFinishSession = {},
        )
    }
}

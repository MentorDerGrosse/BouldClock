package at.mentor.bouldclockapp.presentation.session

import android.app.Activity
import android.view.KeyEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.wear.input.WearableButtons
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Hardware-Tasten als Ausloeser.
 *
 * Wear OS liefert Apps ausschliesslich [KeyEvent.KEYCODE_STEM_1] bis
 * `KEYCODE_STEM_3` aus. Die Ein/Aus-Taste ist dem System vorbehalten.
 *
 * Auf der Galaxy Watch gibt es keine dieser Tasten: die obere ist Home/Power,
 * die untere ist Zurueck, beide sind Systemtasten und kommen in der App nicht
 * als Stem-Keycode an. [availableKeys] gibt dort eine leere Liste zurueck -
 * darum ist die Flaeche in [TriggerSurface] der eigentliche Ausloeser, und die
 * Tastenunterstuetzung ein Bonus fuer Uhren, die welche haben.
 */
object WearTriggerButtons {

    val STEM_KEYCODES: List<Int> = listOf(
        KeyEvent.KEYCODE_STEM_1,
        KeyEvent.KEYCODE_STEM_2,
        KeyEvent.KEYCODE_STEM_3,
    )

    /** Welche Stem-Tasten diese Uhr tatsaechlich hat. Zur Laufzeit abfragen, nie annehmen. */
    fun availableKeys(activity: Activity): List<Int> =
        STEM_KEYCODES.filter { WearableButtons.getButtonInfo(activity, it) != null }

    fun isTriggerKey(keyCode: Int): Boolean = keyCode in STEM_KEYCODES
}

/**
 * Leitet Tastendruecke von der Activity in die Compose-Welt weiter.
 *
 * In `MainActivity.onKeyDown` aufrufen und das Ergebnis zurueckgeben - true
 * bedeutet, der Druck wurde verbraucht.
 */
class HardwareTriggerBus {

    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun onKeyDown(keyCode: Int): Boolean {
        if (!WearTriggerButtons.isTriggerKey(keyCode)) return false
        return _events.tryEmit(Unit)
    }
}

/**
 * Der ganze Bildschirm ist der Knopf.
 *
 * Auf der Galaxy Watch bleibt das der einzige verlaessliche Ausloeser, und es
 * ist ohnehin das groesste erreichbare Ziel: kein Zielen mit kalkigen Fingern,
 * kein Hinschauen. Haptik statt Ton, weil man in der Halle nichts hoert.
 *
 * Ein anklickbares Kind - etwa die Ergebnistasten - verbraucht den Druck selbst
 * und loest hier nichts aus.
 *
 * Langer Druck beendet die Session. Eine Regel fuer die ganze App: kurz schaltet
 * weiter, lang hoert auf.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TriggerSurface(
    onTrigger: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onFinishSession: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .combinedClickable(
                enabled = enabled,
                onLongClick = onFinishSession?.let {
                    {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        it()
                    }
                },
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onTrigger()
            },
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/** Tickende Uhrzeit fuer laufende Anzeigen. */
@Composable
fun rememberNow(periodMs: Long = 1_000L): State<Long> =
    produceState(initialValue = System.currentTimeMillis(), periodMs) {
        while (true) {
            value = System.currentTimeMillis()
            delay(periodMs)
        }
    }

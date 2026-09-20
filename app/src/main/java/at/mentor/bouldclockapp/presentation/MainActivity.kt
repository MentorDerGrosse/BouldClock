package at.mentor.bouldclockapp.presentation

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.presentation.session.HardwareTriggerBus
import at.mentor.bouldclockapp.presentation.session.SessionScreen
import at.mentor.bouldclockapp.presentation.session.SessionViewModel
import at.mentor.bouldclockapp.presentation.theme.BouldClockAppTheme

class MainActivity : ComponentActivity() {

    private val triggerBus = HardwareTriggerBus()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WearApp(triggerBus) }
    }

    /**
     * Stem-Tasten an die App weiterreichen.
     *
     * Wear OS liefert hier nur KEYCODE_STEM_1..3 aus. Die Tasten der Galaxy Watch
     * sind Systemtasten und kommen nicht an - dort bleibt die Bildschirmflaeche
     * der Ausloeser. Siehe [HardwareTriggerBus].
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean =
        triggerBus.onKeyDown(keyCode) || super.onKeyDown(keyCode, event)
}

@Composable
fun WearApp(triggerBus: HardwareTriggerBus) {
    BouldClockAppTheme {
        AppScaffold {
            ScreenScaffold {
                val viewModel: SessionViewModel = viewModel()
                val session by viewModel.session.collectAsStateWithLifecycle()
                val phase by viewModel.phase.collectAsStateWithLifecycle()

                LaunchedEffect(triggerBus) {
                    triggerBus.events.collect { viewModel.trigger() }
                }

                if (session == null) {
                    StartSession(onStart = viewModel::startSession)
                } else {
                    SessionScreen(
                        phase = phase,
                        onTrigger = viewModel::trigger,
                        onOutcome = viewModel::logOutcome,
                        onFinishSession = viewModel::finishSession,
                    )
                }
            }
        }
    }
}

@Composable
private fun StartSession(onStart: (SessionType) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = "Session starten",
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
        )
        SessionType.entries.forEach { type ->
            Button(onClick = { onStart(type) }) {
                Text(type.displayName, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

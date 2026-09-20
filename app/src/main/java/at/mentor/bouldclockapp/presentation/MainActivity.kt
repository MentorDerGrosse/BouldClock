package at.mentor.bouldclockapp.presentation

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.presentation.session.HardwareTriggerBus
import at.mentor.bouldclockapp.presentation.session.SessionScreen
import at.mentor.bouldclockapp.presentation.session.SessionUiState
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
            val viewModel: SessionViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(triggerBus) {
                triggerBus.events.collect { viewModel.trigger() }
            }

            when (val state = uiState) {
                // Kurz leer statt aufblitzendem Startbildschirm - sonst legt ein
                // schneller Tap eine zweite Session neben der offenen an.
                SessionUiState.Restoring -> Box(Modifier.fillMaxSize())

                SessionUiState.NoSession ->
                    StartSessionScreen(onStart = viewModel::startSession)

                is SessionUiState.Running -> ScreenScaffold {
                    SessionScreen(
                        phase = state.phase,
                        onTrigger = viewModel::trigger,
                        onOutcome = viewModel::logOutcome,
                        onGradeChange = viewModel::previewGrade,
                        onFinishSession = viewModel::finishSession,
                    )
                }
            }
        }
    }
}

/**
 * Sessiontyp waehlen.
 *
 * Scrollbare Liste statt fester Spalte: Kopfzeile plus vier Typen passen auf
 * einem 40-mm-Zifferblatt nicht gleichzeitig auf den Schirm.
 */
@Composable
private fun StartSessionScreen(onStart: (SessionType) -> Unit) {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            item {
                ListHeader(
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                ) {
                    Text("Session starten")
                }
            }
            SessionType.entries.forEach { type ->
                item {
                    Button(
                        onClick = { onStart(type) },
                        modifier = Modifier.fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    ) {
                        Text(type.displayName)
                    }
                }
            }
        }
    }
}

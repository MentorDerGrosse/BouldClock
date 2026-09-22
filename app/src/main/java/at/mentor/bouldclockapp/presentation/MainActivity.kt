package at.mentor.bouldclockapp.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.wear.compose.foundation.pager.VerticalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.VerticalPagerScaffold
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.SessionType
import at.mentor.bouldclockapp.presentation.session.HardwareTriggerBus
import at.mentor.bouldclockapp.presentation.components.RestDurationScreen
import at.mentor.bouldclockapp.presentation.profile.ProfileSetupScreen
import at.mentor.bouldclockapp.presentation.profile.RestingHrScreen
import at.mentor.bouldclockapp.presentation.progress.ProgressScreen
import at.mentor.bouldclockapp.presentation.session.LiveMetricsScreen
import at.mentor.bouldclockapp.presentation.session.ProfileState
import at.mentor.bouldclockapp.presentation.session.SessionScreen
import at.mentor.bouldclockapp.presentation.session.SessionSummaryScreen
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

            RequestRecordingPermissions()

            val gradeSystem by viewModel.gradeSystem.collectAsStateWithLifecycle()
            val profileState by viewModel.profileState.collectAsStateWithLifecycle()

            // Ohne Koerperdaten kann die App keine Kalorien rechnen - und die
            // sind einer der Gruende fuer das Projekt. Deshalb einmal vorab,
            // danach nie wieder.
            if (profileState is ProfileState.Missing) {
                ProfileSetupScreen(onSave = viewModel::saveProfile)
                return@AppScaffold
            }
            if (profileState is ProfileState.Loading) {
                Box(Modifier.fillMaxSize())
                return@AppScaffold
            }

            var showProgress by remember { mutableStateOf(false) }
            var showRestingHr by remember { mutableStateOf(false) }

            when (val state = uiState) {
                // Kurz leer statt aufblitzendem Startbildschirm - sonst legt ein
                // schneller Tap eine zweite Session neben der offenen an.
                SessionUiState.Restoring -> Box(Modifier.fillMaxSize())

                SessionUiState.NoSession -> if (showRestingHr) {
                    val restingState by viewModel.restingHr.collectAsStateWithLifecycle()
                    ScreenScaffold {
                        RestingHrScreen(
                            state = restingState,
                            onStart = viewModel::measureRestingHr,
                            onBack = {
                                viewModel.dismissRestingHr()
                                showRestingHr = false
                            },
                        )
                    }
                } else if (showProgress) {
                    val summaries by viewModel.recentSummaries.collectAsStateWithLifecycle()
                    ProgressScreen(
                        summaries = summaries,
                        gradeSystem = gradeSystem,
                        onBack = { showProgress = false },
                    )
                } else {
                    StartSessionScreen(
                        gradeSystem = gradeSystem,
                        onToggleScale = viewModel::toggleGradeSystem,
                        onStart = viewModel::chooseSession,
                        onShowProgress = { showProgress = true },
                        onMeasureRestingHr = { showRestingHr = true },
                    )
                }

                is SessionUiState.ChoosingRest -> ScreenScaffold {
                    RestDurationScreen(
                        valueMs = state.restTargetMs,
                        onValueChange = viewModel::setCustomRest,
                        onConfirm = viewModel::confirmCustomSession,
                    )
                }

                is SessionUiState.Summary -> SessionSummaryScreen(
                    finished = state.finished,
                    gradeSystem = gradeSystem,
                    onDismiss = viewModel::dismissSummary,
                )

                // Zwei Seiten: bedienen oben, schauen unten. Der Ausloeser bleibt
                // auf der ersten Seite, damit Datengucken keinen Versuch startet.
                is SessionUiState.Running -> {
                    val pagerState = rememberPagerState(pageCount = { 2 })
                    VerticalPagerScaffold(pagerState = pagerState) {
                        VerticalPager(state = pagerState) { page ->
                            ScreenScaffold {
                                if (page == 0) {
                                    SessionScreen(
                                        phase = state.phase,
                                        type = state.type,
                                        onTrigger = viewModel::trigger,
                                        onOutcome = viewModel::logOutcome,
                                        onGradeChange = viewModel::previewGrade,
                                        onAngleChange = viewModel::previewAngle,
                                        onNewBoulder = viewModel::confirmGradeAsNewBoulder,
                                        onFinishSession = viewModel::finishSession,
                                    )
                                } else {
                                    val bpm by viewModel.liveBpm.collectAsStateWithLifecycle()
                                    val hrState by viewModel.liveHeartRateState
                                        .collectAsStateWithLifecycle()
                                    val kcal by viewModel.liveKcal.collectAsStateWithLifecycle()
                                    val height by viewModel.liveClimbHeightMeters
                                        .collectAsStateWithLifecycle()
                                    val attempts by viewModel.liveAttemptCount
                                        .collectAsStateWithLifecycle()
                                    LiveMetricsScreen(
                                        bpm = bpm,
                                        heartRate = hrState,
                                        kcal = kcal,
                                        climbHeightMeters = height,
                                        attemptCount = attempts,
                                    )
                                }
                            }
                        }
                    }
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
/**
 * Fragt die Berechtigungen fuer Puls und Aufzeichnung ab.
 *
 * Wird abgelehnt, laeuft die App weiter - nur eben ohne Puls. Eine Session zu
 * blockieren, weil jemand den Sensor nicht freigeben will, waere die schlechtere
 * Antwort.
 */
@Composable
private fun RequestRecordingPermissions() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { /* Ergebnis egal - ohne Puls zeichnet die App trotzdem auf. */ }

    LaunchedEffect(Unit) {
        val required = buildList {
            add(Manifest.permission.BODY_SENSORS)
            add(Manifest.permission.ACTIVITY_RECOGNITION)
            // Health-Connect-Berechtigungen: ab Android 16 verlangt Health
            // Services diese, BODY_SENSORS allein genuegt nicht mehr. Als
            // Zeichenketten, weil die Konstanten erst in neueren SDKs stehen.
            addAll(HEALTH_PERMISSIONS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (required.isNotEmpty()) launcher.launch(required.toTypedArray())
    }
}

private val HEALTH_PERMISSIONS = listOf(
    "android.permission.health.READ_HEART_RATE",
    "android.permission.health.READ_ACTIVE_CALORIES_BURNED",
    "android.permission.health.READ_TOTAL_CALORIES_BURNED",
    "android.permission.health.READ_ELEVATION_GAINED",
)

@Composable
private fun StartSessionScreen(
    gradeSystem: GradeSystem,
    onToggleScale: () -> Unit,
    onStart: (SessionType) -> Unit,
    onShowProgress: () -> Unit,
    onMeasureRestingHr: () -> Unit,
) {
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

            item {
                Button(
                    onClick = onShowProgress,
                    modifier = Modifier.fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    transformation = SurfaceTransformation(transformationSpec),
                ) {
                    Text("Fortschritt")
                }
            }

            // Kein Sessionstart, deshalb gedaempft: eine Messung, die man
            // einmal macht und dann monatelang nicht mehr.
            item {
                Button(
                    onClick = onMeasureRestingHr,
                    modifier = Modifier.fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    transformation = SurfaceTransformation(transformationSpec),
                ) {
                    Text("Ruhepuls messen")
                }
            }

            // Vor der Session, nicht waehrend ihr: die Skala ist eine
            // Anzeigevorliebe, die man einmal setzt. Gedaempft, damit sie nicht
            // wie eine fuenfte Sessionart aussieht.
            item {
                Button(
                    onClick = onToggleScale,
                    modifier = Modifier.fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    transformation = SurfaceTransformation(transformationSpec),
                    secondaryLabel = { Text(gradeSystem.displayName) },
                    label = { Text("Skala") },
                )
            }
        }
    }
}

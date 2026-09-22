package at.mentor.bouldclockapp.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import at.mentor.bouldclockapp.mobile.boulders.BoulderScreen
import at.mentor.bouldclockapp.mobile.dashboard.DashboardScreen
import at.mentor.bouldclockapp.mobile.gyms.GymScreen
import at.mentor.bouldclockapp.mobile.history.HistoryScreen
import at.mentor.bouldclockapp.mobile.profile.ProfileScreen
import at.mentor.bouldclockapp.mobile.sessions.SessionDetailActions
import at.mentor.bouldclockapp.mobile.sessions.SessionDetailScreen
import at.mentor.bouldclockapp.mobile.sessions.SessionListScreen
import kotlinx.coroutines.launch

/** Die Ziele der Seitenleiste. */
private enum class Destination(val title: String) {
    DASHBOARD("Übersicht"),
    HISTORY("Historie"),
    SESSIONS("Sessions"),
    BOULDERS("Boulder"),
    GYMS("Hallen"),
    PROFILE("Profil"),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BouldClockTheme {
                BouldClockApp()
            }
        }
    }
}

/**
 * Seitenleiste plus ein Detailbildschirm.
 *
 * Bewusst ohne Navigationsbibliothek: sechs Ziele und genau eine Detailebene
 * sind ein `when`, keine Abhaengigkeit. Sobald tiefere Verschachtelung
 * dazukommt - Boulder im Boulder, Halle in der Halle - ist das der Moment,
 * eine einzufuehren.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BouldClockApp() {
    val viewModel: MobileViewModel = viewModel()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var destination by remember { mutableStateOf(Destination.DASHBOARD) }
    var openSessionId by remember { mutableStateOf<String?>(null) }

    fun open(sessionId: String) {
        viewModel.openSession(sessionId)
        openSessionId = sessionId
    }

    fun closeDetail() {
        viewModel.openSession(null)
        openSessionId = null
    }

    fun go(target: Destination) {
        closeDetail()
        destination = target
        scope.launch { drawerState.close() }
    }

    BackHandler(enabled = openSessionId != null || destination != Destination.DASHBOARD) {
        if (openSessionId != null) closeDetail() else destination = Destination.DASHBOARD
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader()
                Destination.entries.forEach { target ->
                    NavigationDrawerItem(
                        label = { Text(target.title) },
                        selected = target == destination && openSessionId == null,
                        onClick = { go(target) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(openSessionId?.let { "Session" } ?: destination.title) },
                    navigationIcon = {
                        if (openSessionId != null) {
                            IconButton(onClick = { closeDetail() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück")
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, "Menü")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            },
        ) { padding ->
            if (openSessionId != null) {
                DetailRoute(viewModel, openSessionId!!, padding, ::closeDetail)
            } else {
                when (destination) {
                    Destination.DASHBOARD -> {
                        val state by viewModel.dashboard.collectAsStateWithLifecycle()
                        DashboardScreen(
                            state = state,
                            onOpenSession = ::open,
                            onOpenHistory = { go(Destination.HISTORY) },
                            modifier = Modifier.padding(padding),
                        )
                    }

                    Destination.HISTORY -> {
                        val period by viewModel.period.collectAsStateWithLifecycle()
                        val buckets by viewModel.historyBuckets.collectAsStateWithLifecycle()
                        val grades by viewModel.gradeHistogram.collectAsStateWithLifecycle()
                        val hrr60 by viewModel.hrr60Trend.collectAsStateWithLifecycle()
                        HistoryScreen(
                            period = period,
                            buckets = buckets,
                            grades = grades,
                            hrr60 = hrr60,
                            onSelectPeriod = viewModel::selectPeriod,
                            modifier = Modifier.padding(padding),
                        )
                    }

                    Destination.SESSIONS -> {
                        val weeks by viewModel.sessionWeeks.collectAsStateWithLifecycle()
                        SessionListScreen(
                            weeks = weeks,
                            onOpenSession = ::open,
                            modifier = Modifier.padding(padding),
                        )
                    }

                    Destination.BOULDERS -> {
                        val problems by viewModel.problems.collectAsStateWithLifecycle()
                        val tallies by viewModel.problemTallies.collectAsStateWithLifecycle()
                        BoulderScreen(
                            problems = problems,
                            tallies = tallies,
                            modifier = Modifier.padding(padding),
                        )
                    }

                    Destination.GYMS -> {
                        val gyms by viewModel.gyms.collectAsStateWithLifecycle()
                        GymScreen(
                            gyms = gyms,
                            onCreate = { viewModel.createGym(it) },
                            onRename = viewModel::renameGym,
                            onMakeDefault = viewModel::makeGymDefault,
                            modifier = Modifier.padding(padding),
                        )
                    }

                    Destination.PROFILE -> {
                        val profile by viewModel.profile.collectAsStateWithLifecycle()
                        ProfileScreen(
                            profile = profile,
                            onSave = { weight, age, sex ->
                                viewModel.saveProfile(weight, age, sex)
                                go(Destination.DASHBOARD)
                            },
                            modifier = Modifier.padding(padding),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRoute(
    viewModel: MobileViewModel,
    sessionId: String,
    padding: androidx.compose.foundation.layout.PaddingValues,
    onDeleted: () -> Unit,
) {
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val gyms by viewModel.gyms.collectAsStateWithLifecycle()
    val problems by viewModel.problems.collectAsStateWithLifecycle()

    SessionDetailScreen(
        detail = detail,
        gyms = gyms,
        problems = problems,
        modifier = Modifier.padding(padding),
        actions = SessionDetailActions(
            onRpeChange = { viewModel.setRpe(sessionId, it) },
            onNoteChange = { viewModel.setNote(sessionId, it) },
            onGymChange = { viewModel.assignGym(sessionId, it) },
            onDeleteSession = {
                viewModel.deleteSession(sessionId)
                onDeleted()
            },
            onOutcomeChange = { id, outcome -> viewModel.setAttemptOutcome(sessionId, id, outcome) },
            onGradeChange = { id, grade -> viewModel.setAttemptGrade(sessionId, id, grade) },
            onAngleChange = { id, degrees -> viewModel.setAttemptAngle(sessionId, id, degrees) },
            onTopMoveChange = { id, move -> viewModel.setAttemptTopMove(sessionId, id, move) },
            onDeleteAttempt = { viewModel.deleteAttempt(sessionId, it) },
            onAssignProblem = { ids, problemId ->
                viewModel.assignProblem(sessionId, ids, problemId)
            },
            onCreateProblem = { ids, label, grade ->
                viewModel.createProblemFor(sessionId, ids, label, grade)
            },
        ),
    )
}

@Composable
private fun DrawerHeader() {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text(
            text = "BouldClock",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Bouldern mitschreiben",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(modifier = Modifier.height(8.dp))
}

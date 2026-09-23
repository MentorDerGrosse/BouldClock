package at.mentor.bouldclockapp.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import at.mentor.bouldclockapp.mobile.boulders.BoulderScreen
import at.mentor.bouldclockapp.mobile.dashboard.DashboardScreen
import at.mentor.bouldclockapp.mobile.detail.Metric
import at.mentor.bouldclockapp.mobile.detail.MetricDetailScreen
import at.mentor.bouldclockapp.mobile.gyms.GymScreen
import at.mentor.bouldclockapp.mobile.history.HistoryScreen
import at.mentor.bouldclockapp.mobile.profile.ProfileScreen
import at.mentor.bouldclockapp.mobile.sessions.SessionDetailActions
import at.mentor.bouldclockapp.mobile.sessions.SessionDetailScreen
import at.mentor.bouldclockapp.mobile.sessions.SessionListScreen

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

    var destination by remember { mutableStateOf(Destination.DASHBOARD) }
    var openSessionId by remember { mutableStateOf<String?>(null) }
    var openMetric by remember { mutableStateOf<Metric?>(null) }

    fun open(sessionId: String) {
        viewModel.openSession(sessionId)
        openSessionId = sessionId
    }

    fun closeDetail() {
        viewModel.openSession(null)
        openSessionId = null
    }

    fun closeMetric() {
        openMetric = null
    }

    fun go(target: Destination) {
        closeDetail()
        closeMetric()
        destination = target
    }

    BackHandler(
        enabled = openSessionId != null || openMetric != null ||
            destination != Destination.DASHBOARD,
    ) {
        when {
            openSessionId != null -> closeDetail()
            openMetric != null -> closeMetric()
            else -> destination = Destination.DASHBOARD
        }
    }

    // Reiter oben statt Schublade an der Seite: sechs Ziele, die man staendig
    // wechselt, gehoeren sichtbar - eine Schublade versteckt sie hinter einem
    // zusaetzlichen Tipper und laesst nie erkennen, wo man gerade ist.
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = when {
                                openSessionId != null -> "Session"
                                openMetric != null -> openMetric!!.title
                                else -> "BouldClock"
                            },
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        if (openSessionId != null || openMetric != null) {
                            IconButton(
                                onClick = { if (openSessionId != null) closeDetail() else closeMetric() },
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )

                // In einem Detailfenster keine Reiter - dort fuehrt der Weg
                // zurueck, nicht zur Seite.
                if (openSessionId == null && openMetric == null) {
                    ScrollableTabRow(
                        selectedTabIndex = destination.ordinal,
                        containerColor = MaterialTheme.colorScheme.background,
                        edgePadding = 12.dp,
                        divider = {},
                    ) {
                        Destination.entries.forEach { target ->
                            Tab(
                                selected = target == destination,
                                onClick = { go(target) },
                                text = {
                                    Text(
                                        text = target.title,
                                        fontWeight = if (target == destination) {
                                            FontWeight.SemiBold
                                        } else {
                                            FontWeight.Normal
                                        },
                                    )
                                },
                            )
                        }
                    }
                }
            }
        },
        ) { padding ->
            if (openSessionId != null) {
                DetailRoute(viewModel, openSessionId!!, padding, ::closeDetail)
            } else if (openMetric != null) {
                val period by viewModel.period.collectAsStateWithLifecycle()
                val buckets by viewModel.historyBuckets.collectAsStateWithLifecycle()
                MetricDetailScreen(
                    metric = openMetric!!,
                    period = period,
                    buckets = buckets,
                    sessionsOf = viewModel::sessionsIn,
                    onSelectPeriod = viewModel::selectPeriod,
                    onOpenSession = { closeMetric(); open(it) },
                    modifier = Modifier.padding(padding),
                )
            } else {
                when (destination) {
                    Destination.DASHBOARD -> {
                        val state by viewModel.dashboard.collectAsStateWithLifecycle()
                        val readiness by viewModel.readiness.collectAsStateWithLifecycle()
                        DashboardScreen(
                            state = state,
                            readiness = readiness,
                            onOpenSession = ::open,
                            onOpenHistory = { go(Destination.HISTORY) },
                            onOpenMetric = { openMetric = it },
                            modifier = Modifier.padding(padding),
                        )
                    }

                    Destination.HISTORY -> {
                        val period by viewModel.period.collectAsStateWithLifecycle()
                        val buckets by viewModel.historyBuckets.collectAsStateWithLifecycle()
                        val grades by viewModel.gradeHistogram.collectAsStateWithLifecycle()
                        val hrr60 by viewModel.hrr60Trend.collectAsStateWithLifecycle()
                        val zones by viewModel.periodZones.collectAsStateWithLifecycle()
                        val zoneBounds by viewModel.zoneBounds.collectAsStateWithLifecycle()
                        val totals by viewModel.periodTotals.collectAsStateWithLifecycle()
                        HistoryScreen(
                            period = period,
                            buckets = buckets,
                            grades = grades,
                            hrr60 = hrr60,
                            zones = zones,
                            zoneBounds = zoneBounds,
                            totals = totals,
                            onSelectPeriod = viewModel::selectPeriod,
                            onOpenMetric = { openMetric = it },
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
                            onSave = { weight, age, sex, height, restingHr ->
                                viewModel.saveProfile(weight, age, sex, height, restingHr)
                                go(Destination.DASHBOARD)
                            },
                            modifier = Modifier.padding(padding),
                        )
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
    val zoneBounds by viewModel.zoneBounds.collectAsStateWithLifecycle()
    val baseline by viewModel.baseline.collectAsStateWithLifecycle()

    SessionDetailScreen(
        detail = detail,
        gyms = gyms,
        problems = problems,
        zoneBounds = zoneBounds,
        baseline = baseline,
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
            onKindChange = { id, kind -> viewModel.setAttemptKind(sessionId, id, kind) },
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

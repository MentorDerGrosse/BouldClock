package at.mentor.bouldclockapp.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/** Welcher Bildschirm gerade offen ist. */
private sealed interface Screen {
    data object List : Screen
    data class Detail(val sessionId: String) : Screen
    data object Profile : Screen
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
 * Drei Bildschirme, ein Zustand.
 *
 * Bewusst ohne Navigationsbibliothek: bei drei Zielen waere sie mehr Aufwand als
 * Nutzen. Wenn Diagramme und Boulderverwaltung dazukommen, wird das der Moment,
 * sie einzufuehren.
 */
@Composable
private fun BouldClockApp() {
    val viewModel: MobileViewModel = viewModel()
    var screen by remember { mutableStateOf<Screen>(Screen.List) }

    BackHandler(enabled = screen !is Screen.List) {
        viewModel.openSession(null)
        screen = Screen.List
    }

    when (val current = screen) {
        Screen.List -> {
            val summaries by viewModel.summaries.collectAsStateWithLifecycle()
            SessionListScreen(
                summaries = summaries,
                onOpenSession = { id ->
                    viewModel.openSession(id)
                    screen = Screen.Detail(id)
                },
                onOpenProfile = { screen = Screen.Profile },
            )
        }

        is Screen.Detail -> {
            val detail by viewModel.detail.collectAsStateWithLifecycle()
            SessionDetailScreen(
                detail = detail,
                onBack = {
                    viewModel.openSession(null)
                    screen = Screen.List
                },
                onRpeChange = { viewModel.setRpe(current.sessionId, it) },
                onNoteChange = { viewModel.setNote(current.sessionId, it) },
                onOutcomeChange = { attemptId, outcome ->
                    viewModel.setAttemptOutcome(current.sessionId, attemptId, outcome)
                },
                onGradeChange = { attemptId, grade ->
                    viewModel.setAttemptGrade(current.sessionId, attemptId, grade)
                },
                onDeleteAttempt = { viewModel.deleteAttempt(current.sessionId, it) },
            )
        }

        Screen.Profile -> {
            val profile by viewModel.profile.collectAsStateWithLifecycle()
            ProfileScreen(
                profile = profile,
                onSave = { weight, age, sex ->
                    viewModel.saveProfile(weight, age, sex)
                    screen = Screen.List
                },
                onBack = { screen = Screen.List },
            )
        }
    }
}

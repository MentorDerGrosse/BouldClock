package at.mentor.bouldclockapp.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.text.attemptLabel
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SessionListScreen()
            }
        }
    }
}

/**
 * Die nackte Liste angekommener Sessions.
 *
 * Bewusst ohne Diagramme und ohne Bearbeitung: erst soll das Rohr stehen und
 * nachweislich Daten liefern. Alles Schoene kommt darauf.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionListScreen() {
    val viewModel: SessionListViewModel = viewModel()
    val summaries by viewModel.summaries.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("BouldClock") })
        },
    ) { padding ->
        if (summaries.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Noch keine Sessions angekommen",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Beende eine Session auf der Uhr.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(summaries, key = { it.sessionId }) { summary ->
                    SummaryCard(summary)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(summary: SessionSummaryEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = DATE_FORMAT.format(Date(summary.startedAt)),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = describe(summary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun describe(summary: SessionSummaryEntity): String = listOfNotNull(
    attemptLabel(summary.attemptCount),
    "${summary.sendCount} Tops",
    summary.hardestSendValue?.let { Grades.label(it, GradeSystem.FONT) },
    summary.caloriesTotal?.let { "${it.roundToInt()} kcal" },
    summary.climbHeightMeters?.let { String.format(Locale.GERMAN, "%.1f m", it) },
).joinToString(" · ")

private val DATE_FORMAT = SimpleDateFormat("EEEE, d. MMMM yyyy, HH:mm", Locale.GERMAN)

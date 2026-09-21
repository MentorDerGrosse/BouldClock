package at.mentor.bouldclockapp.mobile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.core.text.attemptLabel
import at.mentor.bouldclockapp.data.db.entity.SessionSummaryEntity
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionListScreen(
    summaries: List<SessionSummaryEntity>,
    onOpenSession: (String) -> Unit,
    onOpenProfile: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BouldClock") },
                actions = {
                    TextButton(onClick = onOpenProfile) { Text("Profil") }
                },
            )
        },
    ) { padding ->
        if (summaries.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Noch keine Sessions angekommen", style = MaterialTheme.typography.bodyLarge)
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(summaries, key = { it.sessionId }) { summary ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenSession(summary.sessionId) },
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = formatSessionDate(summary.startedAt),
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
            }
        }
    }
}

private fun describe(summary: SessionSummaryEntity): String = listOfNotNull(
    attemptLabel(summary.attemptCount),
    "${summary.sendCount} Tops",
    summary.hardestSendValue?.let { Grades.label(it, GradeSystem.FONT) },
    summary.caloriesTotal?.let { "${it.roundToInt()} kcal" },
    summary.climbHeightMeters?.let { formatMeters(it) },
).joinToString(" · ")

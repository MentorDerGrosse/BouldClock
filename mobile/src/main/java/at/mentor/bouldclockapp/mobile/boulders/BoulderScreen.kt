package at.mentor.bouldclockapp.mobile.boulders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.mentor.bouldclockapp.core.model.GradeSystem
import at.mentor.bouldclockapp.core.model.Grades
import at.mentor.bouldclockapp.core.text.attemptLabel
import at.mentor.bouldclockapp.data.db.dao.ProblemTally
import at.mentor.bouldclockapp.data.db.entity.ProblemEntity
import at.mentor.bouldclockapp.mobile.formatShortDate
import at.mentor.bouldclockapp.mobile.ui.BouldCard
import at.mentor.bouldclockapp.mobile.ui.EmptyState
import at.mentor.bouldclockapp.mobile.ui.SectionHeader

/**
 * Benannte Boulder ueber alle Sessions hinweg.
 *
 * Das Ergebnis des Zusammenfuehrens: wer dieselbe Gruppe in drei Sessions
 * demselben Boulder zugeordnet hat, sieht hier den Projektverlauf - "47
 * Versuche seit dem 3. September, noch nicht durch".
 */
@Composable
fun BoulderScreen(
    problems: List<ProblemEntity>,
    tallies: List<ProblemTally>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    if (problems.isEmpty()) {
        EmptyState(
            title = "Noch keine Boulder benannt",
            hint = "Öffne eine Session und tippe unter „Boulder“ auf eine Gruppe, " +
                "um sie zu benennen. Dieselbe Bezeichnung in mehreren Sessions " +
                "führt sie zu einem Projekt zusammen.",
            modifier = modifier.fillMaxSize().padding(contentPadding),
        )
        return
    }

    val byId = tallies.associateBy { it.problemId }
    val open = problems.filter { byId[it.id]?.sendCount == 0 }
    val done = problems.filter { (byId[it.id]?.sendCount ?: 0) > 0 }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (open.isNotEmpty()) {
            item { SectionHeader("Offene Projekte", trailing = "${open.size}") }
            open.forEach { problem ->
                item(key = problem.id) { ProblemCard(problem, byId[problem.id]) }
            }
        }
        if (done.isNotEmpty()) {
            item { SectionHeader("Geschafft", trailing = "${done.size}") }
            done.forEach { problem ->
                item(key = problem.id) { ProblemCard(problem, byId[problem.id]) }
            }
        }
    }
}

@Composable
private fun ProblemCard(problem: ProblemEntity, tally: ProblemTally?) {
    BouldCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = problem.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = tally?.let {
                        "${attemptLabel(it.attemptCount)} · seit ${formatShortDate(it.firstAt)}"
                    } ?: "noch keine Versuche",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            problem.gradeValue?.let {
                Text(
                    text = Grades.label(it, GradeSystem.FONT),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        tally?.takeIf { it.sendCount > 0 }?.let {
            Text(
                text = "${it.sendCount}× durchgestiegen",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

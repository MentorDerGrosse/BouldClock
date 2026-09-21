package at.mentor.bouldclockapp.mobile.gyms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.mentor.bouldclockapp.data.db.entity.GymEntity
import at.mentor.bouldclockapp.mobile.ui.BouldCard
import at.mentor.bouldclockapp.mobile.ui.SectionHeader

/**
 * Hallen anlegen und verwalten.
 *
 * Warum das ueberhaupt eine eigene Sache ist: Grade sind hallenabhaengig. Ein
 * 6C in Halle A ist kein 6C in Halle B, und nach dem Umschrauben verschiebt
 * sich alles. Ohne diese Dimension zeigt die Historie spaeter "Fortschritt",
 * der in Wahrheit nur ein Hallenwechsel war.
 */
@Composable
fun GymScreen(
    gyms: List<GymEntity>,
    onCreate: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onMakeDefault: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    var newName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { SectionHeader("Neue Halle") }
        item {
            BouldCard {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    enabled = newName.isNotBlank(),
                    onClick = { onCreate(newName); newName = "" },
                ) { Text("Anlegen") }
            }
        }

        if (gyms.isNotEmpty()) {
            item { SectionHeader("Hallen", trailing = "${gyms.size}") }
            gyms.forEach { gym ->
                item(key = gym.id) { GymCard(gym, onRename, onMakeDefault) }
            }
        }
    }
}

@Composable
private fun GymCard(
    gym: GymEntity,
    onRename: (String, String) -> Unit,
    onMakeDefault: (String) -> Unit,
) {
    var editing by remember { mutableStateOf(false) }
    var name by remember(gym.name) { mutableStateOf(gym.name) }

    BouldCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = gym.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                if (gym.isDefault) {
                    Text(
                        text = "Stammhalle",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            TextButton(onClick = { editing = !editing }) {
                Text(if (editing) "Fertig" else "Bearbeiten")
            }
        }

        if (editing) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    enabled = name.isNotBlank() && name != gym.name,
                    onClick = { onRename(gym.id, name); editing = false },
                ) { Text("Umbenennen") }
                if (!gym.isDefault) {
                    TextButton(onClick = { onMakeDefault(gym.id) }) { Text("Als Stammhalle") }
                }
            }
        }
    }
}

package de.himanshu19.docalert.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.himanshu19.docalert.domain.model.remainingDaysOn
import de.himanshu19.docalert.domain.model.remainingDaysText
import de.himanshu19.docalert.domain.model.statusOn
import de.himanshu19.docalert.ui.components.StatusChip
import de.himanshu19.docalert.ui.components.localized
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(state: DetailsUiState, onBack: () -> Unit, onEdit: (Long) -> Unit, onDelete: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    state.errorMessage?.let { LaunchedEffect(it) { snackbar.showSnackbar(it) } }
    LaunchedEffect(state.deleted) { if (state.deleted) onBack() }
    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false }, title = { Text("Delete this item?") },
        text = { Text("This removes the saved item and cancels all of its pending reminders.") },
        confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item details") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") } },
                actions = {
                    state.item?.let { item ->
                        IconButton(onClick = { onEdit(item.id) }) { Icon(Icons.Outlined.Edit, "Edit item") }
                        IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Outlined.Delete, "Delete item") }
                    }
                },
            )
        }, snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        when {
            state.loading -> LinearProgressIndicator(Modifier.padding(padding).fillMaxWidth())
            state.missing -> Column(Modifier.padding(padding).fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
                Text("This item is no longer available.", style = MaterialTheme.typography.headlineSmall)
                Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("Back to home") }
            }
            state.item != null -> {
                val item = state.item
                val today = LocalDate.now()
                Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(item.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatusChip(item.statusOn(today))
                        Text(remainingDaysText(item.remainingDaysOn(today), item.type), modifier = Modifier.padding(top = 10.dp), fontWeight = FontWeight.SemiBold)
                    }
                    InfoCard("Type", item.type.displayName)
                    InfoCard("Category", item.category.displayName)
                    item.owner?.let { InfoCard("Owner / person", it) }
                    item.issueDate?.let { InfoCard("Issue date", it.localized()) }
                    InfoCard("Expiry / due date", item.expiryDate.localized())
                    InfoCard("Reminder schedule", if (item.reminders.isEmpty()) "No reminders" else item.reminders.sortedByDescending { it.daysBefore }.joinToString("\n") { "• ${it.displayName}" })
                    item.notes?.let { InfoCard("Notes", it) }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(label: String, value: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

package de.himanshu19.docalert.ui.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import de.himanshu19.docalert.data.settings.ThemeMode
import de.himanshu19.docalert.domain.model.Category
import de.himanshu19.docalert.domain.model.ItemStatus
import de.himanshu19.docalert.ui.components.ItemCard
import java.time.LocalDate
import java.time.Instant
import de.himanshu19.docalert.domain.model.ItemType
import de.himanshu19.docalert.domain.model.TrackedItem
import de.himanshu19.docalert.ui.theme.DocAlertTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onSearch: (String) -> Unit,
    onCategory: (Category?) -> Unit,
    onStatus: (ItemStatus) -> Unit,
    onItem: (Long) -> Unit,
    onAdd: () -> Unit,
    onSettings: () -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    state.errorMessage?.let { message -> LaunchedEffect(message) { snackbar.showSnackbar(message); onMessageShown() } }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("DocAlert", fontWeight = FontWeight.Bold); Text("Your deadlines, kept private", style = MaterialTheme.typography.labelMedium) } },
                actions = { IconButton(onClick = onSettings) { Icon(Icons.Outlined.Settings, "Settings") } },
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = onAdd) { Icon(Icons.Outlined.Add, "Add document or deadline") } },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (state.loading) {
            LinearProgressIndicator(Modifier.padding(padding).fillMaxWidth())
            return@Scaffold
        }
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        ) {
            item {
                OutlinedTextField(
                    value = state.query.search, onValueChange = onSearch, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search saved items") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true,
                )
            }
            item { SummaryRow(state.summary) }
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ItemStatus.entries.forEach { status ->
                        FilterChip(selected = status in state.query.statuses, onClick = { onStatus(status) }, label = { Text(status.displayName) })
                    }
                }
            }
            item { CategoryFilter(state.query.category, onCategory) }
            if (state.items.isEmpty()) {
                item { EmptyState(filtered = state.query != de.himanshu19.docalert.domain.model.ItemQuery(), onAdd = onAdd) }
            } else {
                items(state.items, key = { it.id }) { item -> ItemCard(item, LocalDate.now(), { onItem(item.id) }) }
            }
        }
    }
}

@Composable
private fun SummaryRow(summary: StatusSummary) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Active" to summary.active, "Expiring" to summary.expiring, "Expired" to summary.expired).forEach { (label, count) ->
            androidx.compose.material3.Card(Modifier.weight(1f)) {
                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(count.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(label, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun CategoryFilter(selected: Category?, onSelect: (Category?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text(selected?.displayName ?: "All categories") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("All categories") }, onClick = { onSelect(null); expanded = false })
            Category.entries.forEach { category -> DropdownMenuItem(text = { Text(category.displayName) }, onClick = { onSelect(category); expanded = false }) }
        }
    }
}

@Composable
private fun EmptyState(filtered: Boolean, onAdd: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 48.dp, horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(Icons.Outlined.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(if (filtered) "No matching items" else "Never miss an important date", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(if (filtered) "Try changing your search or filters." else "Save document expiries and deadlines locally, then choose when you want a reminder.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (!filtered) Button(onClick = onAdd) { Text("Add Document or Deadline") }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun HomeEmptyPreview() {
    DocAlertTheme(ThemeMode.LIGHT) {
        HomeScreen(HomeUiState(loading = false), {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun HomeRecordsPreview() {
    val today = LocalDate.now()
    val previewItems = listOf(
        TrackedItem(1, ItemType.DOCUMENT, "Residence permit", Category.RESIDENCE_PERMIT, "Alex", expiryDate = today.plusDays(18), createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH),
        TrackedItem(2, ItemType.DEADLINE, "School application", Category.SCHOOL, expiryDate = today.plusDays(62), createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH),
    )
    DocAlertTheme(ThemeMode.LIGHT) {
        HomeScreen(HomeUiState(loading = false, items = previewItems), {}, {}, {}, {}, {}, {}, {})
    }
}

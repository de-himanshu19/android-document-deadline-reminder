package de.himanshu19.docalert.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.himanshu19.docalert.domain.model.Category
import de.himanshu19.docalert.domain.model.ItemDraft
import de.himanshu19.docalert.domain.model.ItemType
import de.himanshu19.docalert.domain.model.MAX_NOTES_LENGTH
import de.himanshu19.docalert.domain.model.MAX_TITLE_LENGTH
import de.himanshu19.docalert.domain.model.ReminderInterval
import de.himanshu19.docalert.ui.components.localized
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    state: EditorUiState,
    isEditing: Boolean,
    onUpdate: ((ItemDraft) -> ItemDraft) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    var showDiscard by remember { mutableStateOf(false) }
    val leave = { if (state.isDirty) showDiscard = true else onBack() }
    BackHandler(onBack = leave)
    val snackbar = remember { SnackbarHostState() }
    state.errorMessage?.let { message -> LaunchedEffect(message) { snackbar.showSnackbar(message) } }
    if (showDiscard) AlertDialog(
        onDismissRequest = { showDiscard = false },
        title = { Text("Discard changes?") },
        text = { Text("Your unsaved changes will be lost.") },
        confirmButton = { TextButton(onClick = onBack) { Text("Discard") } },
        dismissButton = { TextButton(onClick = { showDiscard = false }) { Text("Keep editing") } },
    )
    Scaffold(
        topBar = { TopAppBar(title = { Text(if (isEditing) "Edit item" else "Add item") }, navigationIcon = { IconButton(onClick = leave) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") } }) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (state.loading) { LinearProgressIndicator(Modifier.padding(padding).fillMaxWidth()); return@Scaffold }
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).imePadding().navigationBarsPadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("What are you tracking?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ItemType.entries.forEach { type -> FilterChip(selected = state.draft.type == type, onClick = { onUpdate { it.copy(type = type) } }, label = { Text(type.displayName) }) }
            }
            OutlinedTextField(
                value = state.draft.title, onValueChange = { value -> if (value.length <= MAX_TITLE_LENGTH) onUpdate { it.copy(title = value) } },
                label = { Text("Title *") }, supportingText = { Text(state.errors.title ?: "${state.draft.title.length}/$MAX_TITLE_LENGTH") },
                isError = state.errors.title != null, modifier = Modifier.fillMaxWidth(), singleLine = true,
            )
            CategoryField(state.draft.category, state.errors.category) { value -> onUpdate { it.copy(category = value) } }
            OutlinedTextField(
                value = state.draft.owner, onValueChange = { value -> onUpdate { it.copy(owner = value) } },
                label = { Text("Owner / person (optional)") }, supportingText = state.errors.owner?.let { { Text(it) } },
                isError = state.errors.owner != null, modifier = Modifier.fillMaxWidth(), singleLine = true,
            )
            DateField("Issue date (optional)", state.draft.issueDate, state.errors.issueDate, true) { date -> onUpdate { it.copy(issueDate = date) } }
            DateField("Expiry / due date *", state.draft.expiryDate, state.errors.expiryDate, false) { date -> onUpdate { it.copy(expiryDate = date) } }
            OutlinedTextField(
                value = state.draft.notes, onValueChange = { value -> if (value.length <= MAX_NOTES_LENGTH) onUpdate { it.copy(notes = value) } },
                label = { Text("Notes (optional)") }, minLines = 3, maxLines = 7, modifier = Modifier.fillMaxWidth(),
                supportingText = { Text(state.errors.notes ?: "${state.draft.notes.length}/$MAX_NOTES_LENGTH") }, isError = state.errors.notes != null,
            )
            Text("Reminders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Notifications are approximate and are normally requested for 09:00 local time.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReminderInterval.entries.forEach { interval ->
                    FilterChip(
                        selected = interval in state.draft.reminders,
                        onClick = { onUpdate { draft -> draft.copy(reminders = draft.reminders.toMutableSet().apply { if (!add(interval)) remove(interval) }) } },
                        label = { Text(interval.displayName) },
                    )
                }
            }
            Button(onClick = onSave, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text(if (state.saving) "Saving…" else "Save") }
            TextButton(onClick = leave, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryField(selected: Category?, error: String?, onSelect: (Category) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.displayName.orEmpty(), onValueChange = {}, readOnly = true, label = { Text("Category *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            supportingText = error?.let { { Text(it) } }, isError = error != null,
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Category.entries.forEach { category -> DropdownMenuItem(text = { Text(category.displayName) }, onClick = { onSelect(category); expanded = false }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(label: String, date: LocalDate?, error: String?, optional: Boolean, onSelect: (LocalDate?) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Outlined.CalendarMonth, null)
        Text(date?.localized() ?: label, Modifier.padding(horizontal = 8.dp).weight(1f))
        if (optional && date != null) IconButton(onClick = { onSelect(null) }) { Icon(Icons.Outlined.Close, "Clear issue date") }
    }
    error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
    if (showPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = date?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = { TextButton(onClick = {
                pickerState.selectedDateMillis?.let { onSelect(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                showPicker = false
            }) { Text("Choose") } },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } },
        ) { DatePicker(pickerState) }
    }
}

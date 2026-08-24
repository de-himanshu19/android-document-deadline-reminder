package de.himanshu19.docalert.ui.settings

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
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.himanshu19.docalert.data.settings.ThemeMode
import de.himanshu19.docalert.domain.model.ReminderInterval

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    notificationsEnabled: Boolean,
    onBack: () -> Unit,
    onTheme: (ThemeMode) -> Unit,
    onReminder: (ReminderInterval) -> Unit,
    onPrivateContent: (Boolean) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onTestNotification: () -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    state.errorMessage?.let { message -> LaunchedEffect(message) { snackbar.showSnackbar(message); onMessageShown() } }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") } }) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (state.loading) { LinearProgressIndicator(Modifier.padding(padding).fillMaxWidth()); return@Scaffold }
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionTitle("Theme")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode -> FilterChip(selected = state.settings.themeMode == mode, onClick = { onTheme(mode) }, label = { Text(mode.displayName) }) }
            }
            HorizontalDivider()
            SectionTitle("Defaults for new items")
            Text("These choices apply only to records created after you change them.", style = MaterialTheme.typography.bodySmall)
            ReminderInterval.entries.forEach { interval ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(interval.displayName, Modifier.weight(1f))
                    Switch(checked = interval in state.settings.defaultReminders, onCheckedChange = { onReminder(interval) })
                }
            }
            HorizontalDivider()
            SectionTitle("Notifications")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.Notifications, contentDescription = null)
                Column(Modifier.weight(1f)) {
                    Text(if (notificationsEnabled) "Notifications enabled" else "Notifications not enabled", fontWeight = FontWeight.SemiBold)
                    Text("Android controls final delivery timing and may delay background work.", style = MaterialTheme.typography.bodySmall)
                }
            }
            if (!notificationsEnabled) OutlinedButton(onClick = onOpenNotificationSettings) { Text("Open notification settings") }
            Button(onClick = onTestNotification, modifier = Modifier.fillMaxWidth()) { Text("Send test notification") }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Private notification content", fontWeight = FontWeight.SemiBold)
                    Text("Hide saved titles in lock-screen notification text.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = state.settings.privateNotificationContent, onCheckedChange = onPrivateContent)
            }
            HorizontalDivider()
            SectionTitle("Privacy")
            Text("Your saved metadata remains on this device. DocAlert has no account, analytics, advertising, cloud service, or internet permission. Clearing app storage or uninstalling removes your records.")
            Text("Avoid placing unnecessary sensitive information in notes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider()
            SectionTitle("About")
            Text("DocAlert 1.0.0")
            Text("Offline document expiry and deadline reminders.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable private fun SectionTitle(text: String) = Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

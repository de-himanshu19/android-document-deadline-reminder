package de.himanshu19.docalert.navigation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.himanshu19.docalert.AppContainer
import de.himanshu19.docalert.notifications.NotificationContent
import de.himanshu19.docalert.ui.details.DetailsScreen
import de.himanshu19.docalert.ui.details.DetailsViewModel
import de.himanshu19.docalert.ui.editor.EditorScreen
import de.himanshu19.docalert.ui.editor.EditorViewModel
import de.himanshu19.docalert.ui.home.HomeScreen
import de.himanshu19.docalert.ui.home.HomeViewModel
import de.himanshu19.docalert.ui.settings.SettingsScreen
import de.himanshu19.docalert.ui.settings.SettingsViewModel

private const val HOME = "home"
private const val SETTINGS = "settings"
private const val DETAILS = "details/{id}"
private const val EDITOR = "editor?id={id}"

@Composable
fun DocAlertNavHost(container: AppContainer, initialItemId: Long?) {
    val nav = rememberNavController()
    LaunchedEffect(initialItemId) { if (initialItemId != null && initialItemId > 0) nav.navigate("details/$initialItemId") }
    NavHost(navController = nav, startDestination = HOME) {
        composable(HOME) {
            val vm: HomeViewModel = viewModel(factory = viewModelFactory { initializer { HomeViewModel(container.items) } })
            val state by vm.uiState.collectAsStateWithLifecycle()
            HomeScreen(state, vm::setSearch, vm::setCategory, vm::toggleStatus, { nav.navigate("details/$it") }, { nav.navigate("editor?id=-1") }, { nav.navigate(SETTINGS) }, vm::clearMessage)
        }
        composable(DETAILS, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            val id = requireNotNull(entry.arguments).getLong("id")
            val vm: DetailsViewModel = viewModel(key = "details-$id", factory = viewModelFactory { initializer { DetailsViewModel(id, container.items, container.reminders) } })
            val state by vm.uiState.collectAsStateWithLifecycle()
            DetailsScreen(state, { nav.popBackStack() }, { nav.navigate("editor?id=$it") }, vm::delete)
        }
        composable(EDITOR, arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })) { entry ->
            val rawId = requireNotNull(entry.arguments).getLong("id")
            val id = rawId.takeIf { it > 0 }
            val vm: EditorViewModel = viewModel(
                key = "editor-${id ?: "new"}",
                factory = viewModelFactory { initializer { EditorViewModel(id, container.items, container.settings, container.reminders, createSavedStateHandle()) } },
            )
            val state by vm.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current
            var explainPermission by remember { mutableStateOf(false) }
            var destination by remember { mutableStateOf<Long?>(null) }
            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                destination?.let { saved -> nav.navigate("details/$saved") { popUpTo(HOME) } }
                destination = null
            }
            state.savedId?.let { savedId ->
                LaunchedEffect(savedId) {
                    vm.consumeSaved()
                    val needsPermission = state.draft.reminders.isNotEmpty() && Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    if (needsPermission) { destination = savedId; explainPermission = true }
                    else nav.navigate("details/$savedId") { popUpTo(HOME) }
                }
            }
            if (explainPermission) AlertDialog(
                onDismissRequest = {}, title = { Text("Allow reminder notifications?") },
                text = { Text("DocAlert needs notification permission to show the reminder times you selected. You can change this later in Settings.") },
                confirmButton = { TextButton(onClick = { explainPermission = false; launcher.launch(NOTIFICATION_PERMISSION) }) { Text("Continue") } },
                dismissButton = { TextButton(onClick = { explainPermission = false; destination?.let { nav.navigate("details/$it") { popUpTo(HOME) } }; destination = null }) { Text("Not now") } },
            )
            EditorScreen(state, id != null, vm::update, vm::save) { nav.popBackStack() }
        }
        composable(SETTINGS) {
            val vm: SettingsViewModel = viewModel(factory = viewModelFactory { initializer { SettingsViewModel(container.settings) } })
            val state by vm.uiState.collectAsStateWithLifecycle()
            SettingsRoute(container, state, vm, onBack = { nav.popBackStack() })
        }
    }
}

@Composable
private fun SettingsRoute(container: AppContainer, state: de.himanshu19.docalert.ui.settings.SettingsUiState, vm: SettingsViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    var explain by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        feedback = if (granted && container.notifications.send(TEST_ID, NotificationContent("DocAlert test", "Notifications are ready."))) "Test notification sent." else "Notification permission is not enabled."
    }
    feedback?.let { message -> AlertDialog(onDismissRequest = { feedback = null }, confirmButton = { TextButton(onClick = { feedback = null }) { Text("OK") } }, text = { Text(message) }) }
    if (explain) AlertDialog(
        onDismissRequest = { explain = false }, title = { Text("Allow notifications?") },
        text = { Text("Permission is needed only to deliver the reminders and test notification you request.") },
        confirmButton = { TextButton(onClick = { explain = false; launcher.launch(NOTIFICATION_PERMISSION) }) { Text("Continue") } },
        dismissButton = { TextButton(onClick = { explain = false }) { Text("Cancel") } },
    )
    val enabled = container.notifications.canNotify()
    SettingsScreen(
        state, enabled, onBack, vm::setTheme, vm::toggleReminder, vm::setPrivateContent,
        onOpenNotificationSettings = {
            context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply { putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName) })
        },
        onTestNotification = {
            if (enabled) feedback = if (container.notifications.send(TEST_ID, NotificationContent("DocAlert test", "Notifications are ready."))) "Test notification sent." else "Notification could not be sent."
            else if (Build.VERSION.SDK_INT >= 33 && ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.POST_NOTIFICATIONS)) explain = true
            else if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) explain = true
            else feedback = "Enable notifications in Android settings first."
        },
        onMessageShown = vm::clearMessage,
    )
}

private const val TEST_ID = 2_147_000_001L
private const val NOTIFICATION_PERMISSION = "android.permission.POST_NOTIFICATIONS"

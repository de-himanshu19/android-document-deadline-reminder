package de.himanshu19.docalert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import de.himanshu19.docalert.data.settings.AppSettings
import de.himanshu19.docalert.navigation.DocAlertNavHost
import de.himanshu19.docalert.ui.theme.DocAlertTheme
import kotlinx.coroutines.flow.catch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val container = (application as DocAlertApplication).container
        val initialItemId = intent.takeIf { it.action == ACTION_OPEN_ITEM }?.getLongExtra(EXTRA_ITEM_ID, -1)?.takeIf { it > 0 }
        setContent {
            val settingsFlow = remember(container) { container.settings.settings.catch { emit(AppSettings()) } }
            val settings by settingsFlow
                .collectAsStateWithLifecycle(initialValue = AppSettings())
            DocAlertTheme(settings.themeMode) { DocAlertNavHost(container, initialItemId) }
        }
    }

    companion object {
        const val ACTION_OPEN_ITEM = "de.himanshu19.docalert.OPEN_ITEM"
        const val EXTRA_ITEM_ID = "item_id"
    }
}

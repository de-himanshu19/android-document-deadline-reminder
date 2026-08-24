package de.himanshu19.docalert.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import de.himanshu19.docalert.data.settings.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF245A4A), onPrimary = Color.White,
    primaryContainer = Color(0xFFC7EBDD), onPrimaryContainer = Color(0xFF062019),
    secondary = Color(0xFF4D635B), tertiary = Color(0xFF59658A),
    background = Color(0xFFF7FAF8), surface = Color(0xFFF7FAF8),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9BD1BE), onPrimary = Color(0xFF00382B),
    primaryContainer = Color(0xFF0B503F), onPrimaryContainer = Color(0xFFB7EEDA),
    secondary = Color(0xFFB4CCC2), tertiary = Color(0xFFC0C6F3),
    background = Color(0xFF101412), surface = Color(0xFF101412),
    error = Color(0xFFFFB4AB),
)

@Composable
fun DocAlertTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dark -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (context as Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
    }
    MaterialTheme(colorScheme = colors, content = content)
}


package de.himanshu19.docalert.data.settings

import de.himanshu19.docalert.domain.model.ReminderInterval
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setTheme(themeMode: ThemeMode)
    suspend fun setDefaultReminders(reminders: Set<ReminderInterval>)
    suspend fun setPrivateNotificationContent(enabled: Boolean)
}


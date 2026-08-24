package de.himanshu19.docalert.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.himanshu19.docalert.domain.model.ReminderInterval
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.docAlertDataStore by preferencesDataStore(name = "docalert_settings")

class DataStoreSettingsRepository(context: Context) : SettingsRepository {
    private val dataStore = context.applicationContext.docAlertDataStore

    override val settings: Flow<AppSettings> = dataStore.data
        .catch { error -> if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw error }
        .map { preferences ->
            AppSettings(
                themeMode = preferences[THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                    ?: ThemeMode.SYSTEM,
                defaultReminders = decodeReminders(preferences[DEFAULT_REMINDERS])
                    ?: AppSettings().defaultReminders,
                privateNotificationContent = preferences[PRIVATE_CONTENT] ?: true,
            )
        }

    override suspend fun setTheme(themeMode: ThemeMode) {
        dataStore.edit { it[THEME] = themeMode.name }
    }

    override suspend fun setDefaultReminders(reminders: Set<ReminderInterval>) {
        dataStore.edit { it[DEFAULT_REMINDERS] = encodeReminders(reminders) }
    }

    override suspend fun setPrivateNotificationContent(enabled: Boolean) {
        dataStore.edit { it[PRIVATE_CONTENT] = enabled }
    }

    private fun encodeReminders(reminders: Set<ReminderInterval>): String =
        reminders.sortedByDescending { it.daysBefore }.joinToString(",") { it.name }

    private fun decodeReminders(value: String?): Set<ReminderInterval>? = value?.let {
        runCatching {
            it.split(',').filter(String::isNotBlank).map(ReminderInterval::valueOf).toSet()
        }.getOrNull()
    }

    private companion object {
        val THEME = stringPreferencesKey("theme")
        val DEFAULT_REMINDERS = stringPreferencesKey("default_reminders")
        val PRIVATE_CONTENT = booleanPreferencesKey("private_notification_content")
    }
}


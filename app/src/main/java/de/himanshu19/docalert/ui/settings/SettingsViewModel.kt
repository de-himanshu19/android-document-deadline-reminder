package de.himanshu19.docalert.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.himanshu19.docalert.data.settings.AppSettings
import de.himanshu19.docalert.data.settings.SettingsRepository
import de.himanshu19.docalert.data.settings.ThemeMode
import de.himanshu19.docalert.domain.model.ReminderInterval
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val loading: Boolean = true,
    val settings: AppSettings = AppSettings(),
    val errorMessage: String? = null,
)

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    private val error = MutableStateFlow<String?>(null)
    val uiState: StateFlow<SettingsUiState> = combine(
        repository.settings.catch {
            error.value = "Settings could not be loaded. Defaults are shown."
            emit(AppSettings())
        }, error,
    ) { settings, message -> SettingsUiState(false, settings, message) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setTheme(value: ThemeMode) = write { repository.setTheme(value) }
    fun toggleReminder(value: ReminderInterval) = write {
        val current = uiState.value.settings.defaultReminders.toMutableSet().apply { if (!add(value)) remove(value) }
        repository.setDefaultReminders(current)
    }
    fun setPrivateContent(value: Boolean) = write { repository.setPrivateNotificationContent(value) }
    fun clearMessage() { error.value = null }

    private fun write(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }.onFailure { error.value = "The setting could not be saved. Try again." }
        }
    }
}


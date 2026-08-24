package de.himanshu19.docalert.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.himanshu19.docalert.data.repository.TrackedItemRepository
import de.himanshu19.docalert.domain.model.TrackedItem
import de.himanshu19.docalert.notifications.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailsUiState(
    val loading: Boolean = true,
    val item: TrackedItem? = null,
    val missing: Boolean = false,
    val errorMessage: String? = null,
    val deleted: Boolean = false,
)

class DetailsViewModel(
    private val itemId: Long,
    private val repository: TrackedItemRepository,
    private val reminders: ReminderScheduler,
) : ViewModel() {
    private val deletion = kotlinx.coroutines.flow.MutableStateFlow(false)
    private val error = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    val uiState: StateFlow<DetailsUiState> = kotlinx.coroutines.flow.combine(
        repository.observeItem(itemId)
            .map { DetailsUiState(loading = false, item = it, missing = it == null) }
            .catch { emit(DetailsUiState(loading = false, errorMessage = "This item could not be loaded.")) },
        deletion,
        error,
    ) { base, deleted, message -> base.copy(deleted = deleted, errorMessage = message ?: base.errorMessage) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailsUiState())

    fun delete() {
        viewModelScope.launch {
            runCatching {
                check(repository.delete(itemId)) { "Record no longer exists" }
                reminders.cancel(itemId)
            }.onSuccess { deletion.value = true }
                .onFailure { error.value = "The item could not be deleted. Try again." }
        }
    }
}

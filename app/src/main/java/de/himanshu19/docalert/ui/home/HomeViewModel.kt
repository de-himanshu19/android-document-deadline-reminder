package de.himanshu19.docalert.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.himanshu19.docalert.data.repository.TrackedItemRepository
import de.himanshu19.docalert.domain.model.Category
import de.himanshu19.docalert.domain.model.ItemQuery
import de.himanshu19.docalert.domain.model.ItemStatus
import de.himanshu19.docalert.domain.model.TrackedItem
import de.himanshu19.docalert.domain.model.statusOn
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class StatusSummary(val active: Int = 0, val expiring: Int = 0, val expired: Int = 0)

data class HomeUiState(
    val loading: Boolean = true,
    val items: List<TrackedItem> = emptyList(),
    val query: ItemQuery = ItemQuery(),
    val summary: StatusSummary = StatusSummary(),
    val errorMessage: String? = null,
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HomeViewModel(
    repository: TrackedItemRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    private val query = MutableStateFlow(ItemQuery())
    private val error = MutableStateFlow<String?>(null)
    private val today get() = LocalDate.now(clock)

    private val filtered = query.flatMapLatest { current ->
        repository.observeItems(current, today).catch {
            error.value = "Saved items could not be loaded."
            emit(emptyList())
        }
    }
    private val all = repository.observeItems(ItemQuery(), today).catch { emit(emptyList()) }

    val uiState: StateFlow<HomeUiState> = combine(query, filtered, all, error) { current, items, allItems, message ->
        HomeUiState(
            loading = false,
            items = items,
            query = current,
            summary = StatusSummary(
                active = allItems.count { it.statusOn(today) == ItemStatus.ACTIVE },
                expiring = allItems.count { it.statusOn(today) in setOf(ItemStatus.DUE_TODAY, ItemStatus.URGENT, ItemStatus.EXPIRING_SOON) },
                expired = allItems.count { it.statusOn(today) == ItemStatus.EXPIRED },
            ),
            errorMessage = message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun setSearch(value: String) { query.value = query.value.copy(search = value) }
    fun setCategory(value: Category?) { query.value = query.value.copy(category = value) }
    fun toggleStatus(value: ItemStatus) {
        val statuses = query.value.statuses.toMutableSet().apply { if (!add(value)) remove(value) }
        query.value = query.value.copy(statuses = statuses)
    }
    fun clearMessage() { error.value = null }
}

package de.himanshu19.docalert

import de.himanshu19.docalert.data.repository.TrackedItemRepository
import de.himanshu19.docalert.data.settings.AppSettings
import de.himanshu19.docalert.data.settings.SettingsRepository
import de.himanshu19.docalert.data.settings.ThemeMode
import de.himanshu19.docalert.domain.model.ItemQuery
import de.himanshu19.docalert.domain.model.ReminderInterval
import de.himanshu19.docalert.domain.model.TrackedItem
import de.himanshu19.docalert.domain.model.filterAndSortItems
import de.himanshu19.docalert.notifications.ReminderScheduler
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeItemRepository(initial: List<TrackedItem> = emptyList()) : TrackedItemRepository {
    val records = MutableStateFlow(initial)
    var failWrites = false
    override fun observeItems(query: ItemQuery, today: LocalDate): Flow<List<TrackedItem>> = records.map { filterAndSortItems(it, query, today) }
    override fun observeItem(id: Long): Flow<TrackedItem?> = records.map { list -> list.find { it.id == id } }
    override suspend fun getItem(id: Long) = records.value.find { it.id == id }
    override suspend fun save(item: TrackedItem): Long {
        if (failWrites) error("write failed")
        val id = item.id.takeIf { it > 0 } ?: ((records.value.maxOfOrNull { it.id } ?: 0) + 1)
        records.value = records.value.filterNot { it.id == id } + item.copy(id = id)
        return id
    }
    override suspend fun delete(id: Long): Boolean {
        if (failWrites) error("delete failed")
        val had = records.value.any { it.id == id }
        records.value = records.value.filterNot { it.id == id }
        return had
    }
}

class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {
    private val values = MutableStateFlow(initial)
    override val settings: Flow<AppSettings> = values
    override suspend fun setTheme(themeMode: ThemeMode) { values.value = values.value.copy(themeMode = themeMode) }
    override suspend fun setDefaultReminders(reminders: Set<ReminderInterval>) { values.value = values.value.copy(defaultReminders = reminders) }
    override suspend fun setPrivateNotificationContent(enabled: Boolean) { values.value = values.value.copy(privateNotificationContent = enabled) }
}

class FakeReminderScheduler : ReminderScheduler {
    val rescheduled = mutableListOf<TrackedItem>()
    val cancelled = mutableListOf<Long>()
    override fun reschedule(item: TrackedItem) { rescheduled += item }
    override fun cancel(itemId: Long) { cancelled += itemId }
}


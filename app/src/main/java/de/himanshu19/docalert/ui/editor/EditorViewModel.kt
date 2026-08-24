package de.himanshu19.docalert.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.himanshu19.docalert.data.repository.TrackedItemRepository
import de.himanshu19.docalert.data.settings.SettingsRepository
import de.himanshu19.docalert.domain.model.Category
import de.himanshu19.docalert.domain.model.ItemDraft
import de.himanshu19.docalert.domain.model.ItemType
import de.himanshu19.docalert.domain.model.ReminderInterval
import de.himanshu19.docalert.domain.model.TrackedItem
import de.himanshu19.docalert.domain.model.ValidationErrors
import de.himanshu19.docalert.domain.model.toDraft
import de.himanshu19.docalert.domain.model.toTrackedItem
import de.himanshu19.docalert.domain.model.validateDraft
import de.himanshu19.docalert.notifications.ReminderScheduler
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class EditorUiState(
    val loading: Boolean = true,
    val draft: ItemDraft = ItemDraft(),
    val errors: ValidationErrors = ValidationErrors(),
    val isDirty: Boolean = false,
    val saving: Boolean = false,
    val savedId: Long? = null,
    val errorMessage: String? = null,
)

class EditorViewModel(
    private val itemId: Long?,
    private val repository: TrackedItemRepository,
    private val settings: SettingsRepository,
    private val reminders: ReminderScheduler,
    private val savedStateHandle: SavedStateHandle,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()
    private var existing: TrackedItem? = null

    init { viewModelScope.launch { load() } }

    private suspend fun load() {
        val existingId = itemId
        if (existingId != null) {
            existing = runCatching { repository.getItem(existingId) }.getOrNull()
            if (existing == null) {
                _uiState.value = EditorUiState(loading = false, errorMessage = "This item is no longer available.")
                return
            }
        }
        val restored = savedStateHandle.get<ItemDraftSnapshot>(DRAFT_KEY)?.toDraft()
        if (restored != null) {
            _uiState.value = EditorUiState(loading = false, draft = restored, isDirty = true)
            return
        }
        runCatching {
            existing?.toDraft() ?: ItemDraft(reminders = settings.settings.first().defaultReminders)
        }.onSuccess { _uiState.value = EditorUiState(loading = false, draft = it) }
            .onFailure { _uiState.value = EditorUiState(loading = false, errorMessage = "This item is no longer available.") }
    }

    fun update(transform: (ItemDraft) -> ItemDraft) {
        val next = transform(_uiState.value.draft)
        savedStateHandle[DRAFT_KEY] = ItemDraftSnapshot.from(next)
        _uiState.value = _uiState.value.copy(draft = next, errors = ValidationErrors(), isDirty = true, errorMessage = null)
    }

    fun save() {
        val current = _uiState.value.draft
        val errors = validateDraft(current)
        if (errors.hasErrors) {
            _uiState.value = _uiState.value.copy(errors = errors, errorMessage = "Check the highlighted fields.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saving = true, errorMessage = null)
            runCatching {
                val item = current.toTrackedItem(existing, Instant.now(clock))
                val id = repository.save(item)
                val saved = item.copy(id = id)
                reminders.reschedule(saved)
                savedStateHandle.remove<ItemDraftSnapshot>(DRAFT_KEY)
                id
            }.onSuccess { id -> _uiState.value = _uiState.value.copy(saving = false, savedId = id, isDirty = false) }
                .onFailure { _uiState.value = _uiState.value.copy(saving = false, errorMessage = "The item could not be saved. Try again.") }
        }
    }

    fun consumeSaved() { _uiState.value = _uiState.value.copy(savedId = null) }

    data class ItemDraftSnapshot(
        val type: String,
        val title: String,
        val category: String?,
        val owner: String,
        val issueDate: String?,
        val expiryDate: String?,
        val notes: String,
        val reminders: Array<String>,
    ) : android.os.Parcelable {
        constructor(parcel: android.os.Parcel) : this(
            parcel.readString().orEmpty(), parcel.readString().orEmpty(), parcel.readString(), parcel.readString().orEmpty(),
            parcel.readString(), parcel.readString(), parcel.readString().orEmpty(), parcel.createStringArray()?.map { it }?.toTypedArray() ?: emptyArray(),
        )
        override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
            parcel.writeString(type); parcel.writeString(title); parcel.writeString(category); parcel.writeString(owner)
            parcel.writeString(issueDate); parcel.writeString(expiryDate); parcel.writeString(notes); parcel.writeStringArray(reminders)
        }
        override fun describeContents() = 0
        override fun toString() = title
        fun toDraft() = ItemDraft(
            type = runCatching { ItemType.valueOf(type) }.getOrDefault(ItemType.DOCUMENT), title = title,
            category = category?.let { runCatching { Category.valueOf(it) }.getOrNull() }, owner = owner,
            issueDate = issueDate?.let(LocalDate::parse), expiryDate = expiryDate?.let(LocalDate::parse), notes = notes,
            reminders = reminders.mapNotNull { runCatching { ReminderInterval.valueOf(it) }.getOrNull() }.toSet(),
        )
        companion object CREATOR : android.os.Parcelable.Creator<ItemDraftSnapshot> {
            override fun createFromParcel(parcel: android.os.Parcel) = ItemDraftSnapshot(parcel)
            override fun newArray(size: Int): Array<ItemDraftSnapshot?> = arrayOfNulls(size)
            fun from(draft: ItemDraft) = ItemDraftSnapshot(
                draft.type.name, draft.title, draft.category?.name, draft.owner, draft.issueDate?.toString(),
                draft.expiryDate?.toString(), draft.notes, draft.reminders.map { it.name }.toTypedArray(),
            )
        }
    }

    private companion object { const val DRAFT_KEY = "editor_draft" }
}

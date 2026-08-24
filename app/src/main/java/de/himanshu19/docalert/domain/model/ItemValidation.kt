package de.himanshu19.docalert.domain.model

import java.time.LocalDate

const val MAX_TITLE_LENGTH = 80
const val MAX_OWNER_LENGTH = 80
const val MAX_NOTES_LENGTH = 500

data class ItemDraft(
    val type: ItemType = ItemType.DOCUMENT,
    val title: String = "",
    val category: Category? = null,
    val owner: String = "",
    val issueDate: LocalDate? = null,
    val expiryDate: LocalDate? = null,
    val notes: String = "",
    val reminders: Set<ReminderInterval> = emptySet(),
)

data class ValidationErrors(
    val title: String? = null,
    val category: String? = null,
    val owner: String? = null,
    val issueDate: String? = null,
    val expiryDate: String? = null,
    val notes: String? = null,
) {
    val hasErrors: Boolean
        get() = listOf(title, category, owner, issueDate, expiryDate, notes).any { it != null }
}

fun validateDraft(draft: ItemDraft): ValidationErrors {
    val title = draft.title.trim()
    val owner = draft.owner.trim()
    val notes = draft.notes.trim()
    return ValidationErrors(
        title = when {
            title.isEmpty() -> "Enter a title."
            title.length > MAX_TITLE_LENGTH -> "Use $MAX_TITLE_LENGTH characters or fewer."
            else -> null
        },
        category = if (draft.category == null) "Choose a category." else null,
        owner = if (owner.length > MAX_OWNER_LENGTH) "Use $MAX_OWNER_LENGTH characters or fewer." else null,
        issueDate = if (draft.issueDate != null && draft.expiryDate != null && draft.issueDate > draft.expiryDate) {
            "Issue date cannot be after the expiry or due date."
        } else null,
        expiryDate = if (draft.expiryDate == null) "Choose an expiry or due date." else null,
        notes = if (notes.length > MAX_NOTES_LENGTH) "Use $MAX_NOTES_LENGTH characters or fewer." else null,
    )
}

fun ItemDraft.toTrackedItem(existing: TrackedItem? = null, now: java.time.Instant): TrackedItem {
    check(!validateDraft(this).hasErrors)
    return TrackedItem(
        id = existing?.id ?: 0,
        type = type,
        title = title.trim(),
        category = requireNotNull(category),
        owner = owner.trim().ifBlank { null },
        issueDate = issueDate,
        expiryDate = requireNotNull(expiryDate),
        notes = notes.trim().ifBlank { null },
        reminders = reminders,
        createdAt = existing?.createdAt ?: now,
        updatedAt = now,
    )
}

fun TrackedItem.toDraft(): ItemDraft = ItemDraft(
    type = type,
    title = title,
    category = category,
    owner = owner.orEmpty(),
    issueDate = issueDate,
    expiryDate = expiryDate,
    notes = notes.orEmpty(),
    reminders = reminders,
)


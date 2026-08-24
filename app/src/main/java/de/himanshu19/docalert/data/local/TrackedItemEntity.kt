package de.himanshu19.docalert.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.himanshu19.docalert.domain.model.Category
import de.himanshu19.docalert.domain.model.ItemType
import de.himanshu19.docalert.domain.model.ReminderInterval
import de.himanshu19.docalert.domain.model.TrackedItem
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "tracked_items")
data class TrackedItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: ItemType,
    val title: String,
    val category: Category,
    val owner: String?,
    val issueDate: LocalDate?,
    val expiryDate: LocalDate,
    val notes: String?,
    val reminders: Set<ReminderInterval>,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

fun TrackedItemEntity.toDomain(): TrackedItem = TrackedItem(
    id = id,
    type = type,
    title = title,
    category = category,
    owner = owner,
    issueDate = issueDate,
    expiryDate = expiryDate,
    notes = notes,
    reminders = reminders,
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
)

fun TrackedItem.toEntity(): TrackedItemEntity = TrackedItemEntity(
    id = id,
    type = type,
    title = title,
    category = category,
    owner = owner,
    issueDate = issueDate,
    expiryDate = expiryDate,
    notes = notes,
    reminders = reminders,
    createdAtEpochMillis = createdAt.toEpochMilli(),
    updatedAtEpochMillis = updatedAt.toEpochMilli(),
)


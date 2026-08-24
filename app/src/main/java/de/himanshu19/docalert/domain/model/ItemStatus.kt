package de.himanshu19.docalert.domain.model

import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class ItemStatus(val displayName: String) {
    EXPIRED("Expired"),
    DUE_TODAY("Due today"),
    URGENT("Urgent"),
    EXPIRING_SOON("Expiring soon"),
    ACTIVE("Active"),
}

fun TrackedItem.status(clock: Clock): ItemStatus = statusOn(LocalDate.now(clock))

fun TrackedItem.statusOn(today: LocalDate): ItemStatus {
    val days = ChronoUnit.DAYS.between(today, expiryDate)
    return when {
        days < 0 -> ItemStatus.EXPIRED
        days == 0L -> ItemStatus.DUE_TODAY
        days <= 30 -> ItemStatus.URGENT
        days <= 90 -> ItemStatus.EXPIRING_SOON
        else -> ItemStatus.ACTIVE
    }
}

fun TrackedItem.remainingDaysOn(today: LocalDate): Long =
    ChronoUnit.DAYS.between(today, expiryDate)

fun remainingDaysText(days: Long, type: ItemType = ItemType.DOCUMENT): String = when {
    days == 0L -> if (type == ItemType.DEADLINE) "Due today" else "Expires today"
    days == 1L -> "1 day remaining"
    days > 1L -> "$days days remaining"
    days == -1L -> "Expired 1 day ago"
    else -> "Expired ${-days} days ago"
}


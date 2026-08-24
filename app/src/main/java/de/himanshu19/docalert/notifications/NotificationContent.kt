package de.himanshu19.docalert.notifications

import de.himanshu19.docalert.domain.model.ItemType
import de.himanshu19.docalert.domain.model.TrackedItem

data class NotificationContent(val title: String, val text: String)

fun notificationContent(item: TrackedItem, privateContent: Boolean, daysBefore: Long): NotificationContent {
    if (privateContent) return NotificationContent(
        title = "Document reminder",
        text = "One of your saved items needs attention.",
    )
    val verb = if (item.type == ItemType.DEADLINE) "is due" else "expires"
    val timing = when (daysBefore) {
        0L -> "today"
        1L -> "in 1 day"
        else -> "in $daysBefore days"
    }
    return NotificationContent(item.title, "${item.title} $verb $timing.")
}


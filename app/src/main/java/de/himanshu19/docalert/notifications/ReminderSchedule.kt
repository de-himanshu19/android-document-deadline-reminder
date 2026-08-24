package de.himanshu19.docalert.notifications

import de.himanshu19.docalert.domain.model.ReminderInterval
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

private val DELIVERY_TIME: LocalTime = LocalTime.of(9, 0)

fun reminderWorkName(itemId: Long, interval: ReminderInterval): String =
    "docalert-reminder-$itemId-${interval.daysBefore}"

fun reminderItemTag(itemId: Long): String = "docalert-item-$itemId"

fun reminderRunAt(
    expiryDate: LocalDate,
    interval: ReminderInterval,
    clock: Clock,
): ZonedDateTime? {
    val now = ZonedDateTime.now(clock)
    val reminderDate = expiryDate.minusDays(interval.daysBefore)
    if (reminderDate.isBefore(now.toLocalDate())) return null
    val scheduled = reminderDate.atTime(DELIVERY_TIME).atZone(now.zone)
    return if (scheduled.isBefore(now)) now else scheduled
}

fun reminderDelay(
    expiryDate: LocalDate,
    interval: ReminderInterval,
    clock: Clock,
): Duration? = reminderRunAt(expiryDate, interval, clock)?.let {
    Duration.between(ZonedDateTime.now(clock), it).coerceAtLeast(Duration.ZERO)
}


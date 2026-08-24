package de.himanshu19.docalert.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class ItemStatusTest {
    private val today = LocalDate.of(2028, 2, 28)

    private fun item(date: LocalDate, type: ItemType = ItemType.DOCUMENT) = TrackedItem(
        type = type,
        title = "Test",
        category = Category.OTHER,
        expiryDate = date,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
    )

    @Test fun `status boundaries and leap day are deterministic`() {
        assertEquals(ItemStatus.EXPIRED, item(today.minusDays(1)).statusOn(today))
        assertEquals(ItemStatus.DUE_TODAY, item(today).statusOn(today))
        assertEquals(ItemStatus.URGENT, item(today.plusDays(1)).statusOn(today))
        assertEquals(ItemStatus.URGENT, item(today.plusDays(30)).statusOn(today))
        assertEquals(ItemStatus.EXPIRING_SOON, item(today.plusDays(31)).statusOn(today))
        assertEquals(ItemStatus.EXPIRING_SOON, item(today.plusDays(90)).statusOn(today))
        assertEquals(ItemStatus.ACTIVE, item(today.plusDays(91)).statusOn(today))
        assertEquals(ItemStatus.URGENT, item(LocalDate.of(2028, 2, 29)).statusOn(today))
    }

    @Test fun `remaining wording handles singular plural and type`() {
        assertEquals("Expires today", remainingDaysText(0))
        assertEquals("Due today", remainingDaysText(0, ItemType.DEADLINE))
        assertEquals("1 day remaining", remainingDaysText(1))
        assertEquals("28 days remaining", remainingDaysText(28))
        assertEquals("Expired 1 day ago", remainingDaysText(-1))
        assertEquals("Expired 3 days ago", remainingDaysText(-3))
    }
}


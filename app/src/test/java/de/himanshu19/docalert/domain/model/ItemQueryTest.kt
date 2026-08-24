package de.himanshu19.docalert.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class ItemQueryTest {
    private val today = LocalDate.of(2026, 8, 24)
    private fun item(id: Long, title: String, days: Long, category: Category, owner: String? = null) = TrackedItem(
        id = id, type = ItemType.DOCUMENT, title = title, category = category, owner = owner,
        expiryDate = today.plusDays(days), createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH,
    )

    @Test fun `search category and status filters combine case insensitively`() {
        val items = listOf(
            item(1, "Residence Permit", 20, Category.RESIDENCE_PERMIT, "Himanshu"),
            item(2, "Car", 20, Category.VEHICLE_TUV, "Alex"),
            item(3, "Old permit", -2, Category.RESIDENCE_PERMIT, "Himanshu"),
        )
        val result = filterAndSortItems(
            items,
            ItemQuery("HIMAN", setOf(ItemStatus.URGENT), Category.RESIDENCE_PERMIT),
            today,
        )
        assertEquals(listOf(1L), result.map { it.id })
    }

    @Test fun `default order is today then upcoming ascending then expired most recent`() {
        val result = filterAndSortItems(
            listOf(
                item(1, "Old", -10, Category.OTHER), item(2, "Later", 30, Category.OTHER),
                item(3, "Today", 0, Category.OTHER), item(4, "Soon", 2, Category.OTHER),
                item(5, "Recent expired", -1, Category.OTHER),
            ), ItemQuery(), today,
        )
        assertEquals(listOf(3L, 4L, 2L, 5L, 1L), result.map { it.id })
    }
}


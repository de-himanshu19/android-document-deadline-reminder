package de.himanshu19.docalert.notifications

import de.himanshu19.docalert.domain.model.Category
import de.himanshu19.docalert.domain.model.ItemType
import de.himanshu19.docalert.domain.model.TrackedItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class NotificationContentTest {
    private val item = TrackedItem(
        id = 9, type = ItemType.DOCUMENT, title = "Residence Permit",
        category = Category.RESIDENCE_PERMIT, expiryDate = LocalDate.of(2027, 1, 1),
        createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH,
    )

    @Test fun `private content does not reveal item title`() {
        val content = notificationContent(item, privateContent = true, daysBefore = 30)
        assertFalse(content.title.contains(item.title))
        assertFalse(content.text.contains(item.title))
    }

    @Test fun `descriptive content includes title and timing`() {
        val content = notificationContent(item, privateContent = false, daysBefore = 30)
        assertEquals("Residence Permit", content.title)
        assertEquals("Residence Permit expires in 30 days.", content.text)
    }
}


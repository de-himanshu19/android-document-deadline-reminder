package de.himanshu19.docalert.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class ItemValidationTest {
    @Test fun `blank title and missing required fields fail without changing draft`() {
        val draft = ItemDraft(title = "   ")
        val errors = validateDraft(draft)
        assertEquals("   ", draft.title)
        assertNotNull(errors.title)
        assertNotNull(errors.category)
        assertNotNull(errors.expiryDate)
    }

    @Test fun `issue date after expiry fails`() {
        val draft = ItemDraft(
            title = "Passport",
            category = Category.PASSPORT,
            issueDate = LocalDate.of(2027, 1, 2),
            expiryDate = LocalDate.of(2027, 1, 1),
        )
        assertNotNull(validateDraft(draft).issueDate)
    }

    @Test fun `valid conversion trims values and removes empty optional fields`() {
        val draft = ItemDraft(
            title = "  Passport  ",
            category = Category.PASSPORT,
            owner = "  ",
            expiryDate = LocalDate.of(2027, 1, 1),
            notes = " notes ",
        )
        val errors = validateDraft(draft)
        assertFalse(errors.hasErrors)
        val item = draft.toTrackedItem(now = Instant.EPOCH)
        assertEquals("Passport", item.title)
        assertNull(item.owner)
        assertEquals("notes", item.notes)
    }
}


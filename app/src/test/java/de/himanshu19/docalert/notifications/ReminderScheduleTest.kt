package de.himanshu19.docalert.notifications

import de.himanshu19.docalert.domain.model.ReminderInterval
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ReminderScheduleTest {
    private val zone = ZoneId.of("Europe/Berlin")

    @Test fun `future reminder targets local nine oclock`() {
        val clock = Clock.fixed(Instant.parse("2026-08-24T08:00:00Z"), zone)
        val runAt = reminderRunAt(LocalDate.of(2026, 9, 30), ReminderInterval.DAYS_30, clock)
        assertEquals(LocalDate.of(2026, 8, 31), runAt?.toLocalDate())
        assertEquals(9, runAt?.hour)
    }

    @Test fun `today after nine runs without negative delay`() {
        val clock = Clock.fixed(Instant.parse("2026-08-24T12:00:00Z"), zone)
        val delay = reminderDelay(LocalDate.of(2026, 8, 24), ReminderInterval.DUE_DATE, clock)
        assertTrue(requireNotNull(delay).isZero)
    }

    @Test fun `past reminder is skipped`() {
        val clock = Clock.fixed(Instant.parse("2026-08-24T08:00:00Z"), zone)
        assertNull(reminderDelay(LocalDate.of(2026, 8, 20), ReminderInterval.DAY_1, clock))
    }

    @Test fun `work names are stable and interval specific`() {
        assertEquals("docalert-reminder-42-30", reminderWorkName(42, ReminderInterval.DAYS_30))
        assertEquals("docalert-item-42", reminderItemTag(42))
    }
}


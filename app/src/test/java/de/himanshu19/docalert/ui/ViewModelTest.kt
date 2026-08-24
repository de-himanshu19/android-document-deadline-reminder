package de.himanshu19.docalert.ui

import androidx.lifecycle.SavedStateHandle
import de.himanshu19.docalert.FakeItemRepository
import de.himanshu19.docalert.FakeReminderScheduler
import de.himanshu19.docalert.FakeSettingsRepository
import de.himanshu19.docalert.MainDispatcherRule
import de.himanshu19.docalert.domain.model.Category
import de.himanshu19.docalert.domain.model.ItemType
import de.himanshu19.docalert.domain.model.TrackedItem
import de.himanshu19.docalert.ui.details.DetailsViewModel
import de.himanshu19.docalert.ui.editor.EditorViewModel
import de.himanshu19.docalert.ui.home.HomeViewModel
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val clock = Clock.fixed(Instant.parse("2026-08-24T10:00:00Z"), ZoneOffset.UTC)
    private fun item(id: Long = 1, title: String = "Passport") = TrackedItem(
        id = id, type = ItemType.DOCUMENT, title = title, category = Category.PASSPORT,
        expiryDate = LocalDate.of(2027, 1, 1), createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH,
    )

    @Test fun `home exposes initial empty and successful load`() = runTest {
        val repository = FakeItemRepository()
        val vm = HomeViewModel(repository, clock)
        val empty = vm.uiState.first { !it.loading }
        assertTrue(empty.items.isEmpty())
        repository.records.value = listOf(item())
        assertEquals("Passport", vm.uiState.first { it.items.isNotEmpty() }.items.single().title)
    }

    @Test fun `editor retains validation input and saves valid record with reminders`() = runTest {
        val repository = FakeItemRepository()
        val scheduler = FakeReminderScheduler()
        val vm = EditorViewModel(null, repository, FakeSettingsRepository(), scheduler, SavedStateHandle(), clock)
        advanceUntilIdle()
        vm.update { it.copy(title = "   ") }
        vm.save()
        assertNotNull(vm.uiState.value.errors.title)
        assertEquals("   ", vm.uiState.value.draft.title)
        vm.update { it.copy(title = "Passport", category = Category.PASSPORT, expiryDate = LocalDate.of(2027, 1, 1)) }
        vm.save()
        advanceUntilIdle()
        assertEquals(1L, vm.uiState.value.savedId)
        assertEquals(1, scheduler.rescheduled.size)
    }

    @Test fun `editor loads an item for editing and reports missing record`() = runTest {
        val repository = FakeItemRepository(listOf(item()))
        val edit = EditorViewModel(1, repository, FakeSettingsRepository(), FakeReminderScheduler(), SavedStateHandle(), clock)
        val missing = EditorViewModel(99, repository, FakeSettingsRepository(), FakeReminderScheduler(), SavedStateHandle(), clock)
        advanceUntilIdle()
        assertEquals("Passport", edit.uiState.value.draft.title)
        assertFalse(edit.uiState.value.loading)
        assertNotNull(missing.uiState.value.errorMessage)
    }

    @Test fun `restored edit updates original instead of inserting duplicate`() = runTest {
        val repository = FakeItemRepository(listOf(item()))
        val handle = SavedStateHandle()
        val first = EditorViewModel(1, repository, FakeSettingsRepository(), FakeReminderScheduler(), handle, clock)
        advanceUntilIdle()
        first.update { it.copy(title = "Restored title") }
        val recreated = EditorViewModel(1, repository, FakeSettingsRepository(), FakeReminderScheduler(), handle, clock)
        advanceUntilIdle()
        assertEquals("Restored title", recreated.uiState.value.draft.title)
        recreated.save()
        advanceUntilIdle()
        assertEquals(1, repository.records.value.size)
        assertEquals(1L, repository.records.value.single().id)
        assertEquals("Restored title", repository.records.value.single().title)
    }

    @Test fun `details missing state and deletion cancel reminder work`() = runTest {
        val repository = FakeItemRepository(listOf(item()))
        val scheduler = FakeReminderScheduler()
        val vm = DetailsViewModel(1, repository, scheduler)
        assertNotNull(vm.uiState.first { !it.loading }.item)
        vm.delete()
        advanceUntilIdle()
        assertEquals(listOf(1L), scheduler.cancelled)
        assertTrue(vm.uiState.first { it.deleted }.deleted)
        assertTrue(repository.records.value.isEmpty())
        val missing = DetailsViewModel(99, repository, scheduler)
        assertTrue(missing.uiState.first { !it.loading }.missing)
    }
}

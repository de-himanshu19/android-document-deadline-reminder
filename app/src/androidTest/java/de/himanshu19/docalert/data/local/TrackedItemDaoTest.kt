package de.himanshu19.docalert.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.himanshu19.docalert.domain.model.Category
import de.himanshu19.docalert.domain.model.ItemType
import de.himanshu19.docalert.domain.model.ItemQuery
import de.himanshu19.docalert.domain.model.ItemStatus
import de.himanshu19.docalert.domain.model.ReminderInterval
import de.himanshu19.docalert.data.repository.RoomTrackedItemRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrackedItemDaoTest {
    private lateinit var database: DocAlertDatabase
    private lateinit var dao: TrackedItemDao

    @Before fun setup() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), DocAlertDatabase::class.java)
            .allowMainThreadQueries().build()
        dao = database.trackedItemDao()
    }
    @After fun close() = database.close()

    private fun entity(title: String = "Passport") = TrackedItemEntity(
        type = ItemType.DOCUMENT, title = title, category = Category.PASSPORT, owner = "Alex",
        issueDate = LocalDate.of(2020, 1, 1), expiryDate = LocalDate.of(2030, 1, 1), notes = "Renew early",
        reminders = setOf(ReminderInterval.DAYS_30, ReminderInterval.DAY_1), createdAtEpochMillis = 10, updatedAtEpochMillis = 20,
    )

    @Test fun insertUpdateObserveDeleteAndConverters() = runTest {
        val id = dao.insert(entity())
        val inserted = dao.observeById(id).first()
        assertEquals(LocalDate.of(2030, 1, 1), inserted?.expiryDate)
        assertEquals(setOf(ReminderInterval.DAYS_30, ReminderInterval.DAY_1), inserted?.reminders)
        dao.update(requireNotNull(inserted).copy(title = "Updated"))
        assertEquals("Updated", dao.getById(id)?.title)
        assertEquals(listOf("Updated"), dao.observeAll().first().map { it.title })
        dao.deleteById(id)
        assertNull(dao.getById(id))
    }

    @Test fun roomBackedRepositoryCombinesSearchCategoryStatusAndOrdering() = runTest {
        val today = LocalDate.of(2026, 8, 24)
        dao.insert(entity("Permit soon").copy(category = Category.RESIDENCE_PERMIT, owner = "Alex", expiryDate = today.plusDays(20)))
        dao.insert(entity("Permit later").copy(category = Category.RESIDENCE_PERMIT, owner = "Alex", expiryDate = today.plusDays(10)))
        dao.insert(entity("Other").copy(category = Category.PASSPORT, owner = "Alex", expiryDate = today.plusDays(5)))
        dao.insert(entity("Old permit").copy(category = Category.RESIDENCE_PERMIT, owner = "Alex", expiryDate = today.minusDays(1)))
        val repository = RoomTrackedItemRepository(database)
        val items = repository.observeItems(
            ItemQuery(search = "alex", statuses = setOf(ItemStatus.URGENT), category = Category.RESIDENCE_PERMIT),
            today,
        ).first()
        assertEquals(listOf("Permit later", "Permit soon"), items.map { it.title })
    }
}

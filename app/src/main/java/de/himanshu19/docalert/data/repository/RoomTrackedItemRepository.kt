package de.himanshu19.docalert.data.repository

import androidx.room.withTransaction
import de.himanshu19.docalert.data.local.DocAlertDatabase
import de.himanshu19.docalert.data.local.toDomain
import de.himanshu19.docalert.data.local.toEntity
import de.himanshu19.docalert.domain.model.ItemQuery
import de.himanshu19.docalert.domain.model.TrackedItem
import de.himanshu19.docalert.domain.model.filterAndSortItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class RoomTrackedItemRepository(
    private val database: DocAlertDatabase,
) : TrackedItemRepository {
    private val dao = database.trackedItemDao()

    override fun observeItems(query: ItemQuery, today: LocalDate): Flow<List<TrackedItem>> =
        dao.observeAll().map { entities ->
            filterAndSortItems(entities.map { it.toDomain() }, query, today)
        }

    override fun observeItem(id: Long): Flow<TrackedItem?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun getItem(id: Long): TrackedItem? = dao.getById(id)?.toDomain()

    override suspend fun save(item: TrackedItem): Long = database.withTransaction {
        if (item.id == 0L) dao.insert(item.toEntity())
        else {
            checkNotNull(dao.getById(item.id)) { "Record no longer exists" }
            dao.update(item.toEntity())
            item.id
        }
    }

    override suspend fun delete(id: Long): Boolean = dao.deleteById(id) > 0
}


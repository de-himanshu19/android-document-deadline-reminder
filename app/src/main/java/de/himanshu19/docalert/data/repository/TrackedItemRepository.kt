package de.himanshu19.docalert.data.repository

import de.himanshu19.docalert.domain.model.ItemQuery
import de.himanshu19.docalert.domain.model.TrackedItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TrackedItemRepository {
    fun observeItems(query: ItemQuery, today: LocalDate): Flow<List<TrackedItem>>
    fun observeItem(id: Long): Flow<TrackedItem?>
    suspend fun getItem(id: Long): TrackedItem?
    suspend fun save(item: TrackedItem): Long
    suspend fun delete(id: Long): Boolean
}


package de.himanshu19.docalert.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedItemDao {
    @Query("SELECT * FROM tracked_items")
    fun observeAll(): Flow<List<TrackedItemEntity>>

    @Query("SELECT * FROM tracked_items WHERE id = :id")
    fun observeById(id: Long): Flow<TrackedItemEntity?>

    @Query("SELECT * FROM tracked_items WHERE id = :id")
    suspend fun getById(id: Long): TrackedItemEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: TrackedItemEntity): Long

    @Update
    suspend fun update(item: TrackedItemEntity)

    @Delete
    suspend fun delete(item: TrackedItemEntity)

    @Query("DELETE FROM tracked_items WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}


package de.himanshu19.docalert.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [TrackedItemEntity::class], version = 1, exportSchema = true)
@TypeConverters(RoomConverters::class)
abstract class DocAlertDatabase : RoomDatabase() {
    abstract fun trackedItemDao(): TrackedItemDao

    companion object {
        @Volatile private var instance: DocAlertDatabase? = null

        fun getInstance(context: Context): DocAlertDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                DocAlertDatabase::class.java,
                "docalert.db",
            ).build().also { instance = it }
        }
    }
}


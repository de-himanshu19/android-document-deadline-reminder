package de.himanshu19.docalert.data.local

import androidx.room.TypeConverter
import de.himanshu19.docalert.domain.model.Category
import de.himanshu19.docalert.domain.model.ItemType
import de.himanshu19.docalert.domain.model.ReminderInterval
import java.time.LocalDate

class RoomConverters {
    @TypeConverter fun fromLocalDate(value: LocalDate?): String? = value?.toString()
    @TypeConverter fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)
    @TypeConverter fun fromItemType(value: ItemType): String = value.name
    @TypeConverter fun toItemType(value: String): ItemType = ItemType.valueOf(value)
    @TypeConverter fun fromCategory(value: Category): String = value.name
    @TypeConverter fun toCategory(value: String): Category = Category.valueOf(value)
    @TypeConverter fun fromReminders(value: Set<ReminderInterval>): String =
        value.sortedBy { it.daysBefore }.joinToString(",") { it.name }
    @TypeConverter fun toReminders(value: String): Set<ReminderInterval> =
        value.split(',').filter { it.isNotBlank() }.map(ReminderInterval::valueOf).toSet()
}


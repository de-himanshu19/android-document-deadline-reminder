package de.himanshu19.docalert

import android.content.Context
import de.himanshu19.docalert.data.local.DocAlertDatabase
import de.himanshu19.docalert.data.repository.RoomTrackedItemRepository
import de.himanshu19.docalert.data.repository.TrackedItemRepository
import de.himanshu19.docalert.data.settings.DataStoreSettingsRepository
import de.himanshu19.docalert.data.settings.SettingsRepository
import de.himanshu19.docalert.notifications.NotificationService
import de.himanshu19.docalert.notifications.ReminderScheduler
import de.himanshu19.docalert.notifications.WorkManagerReminderScheduler

class AppContainer(context: Context) {
    private val database = DocAlertDatabase.getInstance(context)
    val items: TrackedItemRepository = RoomTrackedItemRepository(database)
    val settings: SettingsRepository = DataStoreSettingsRepository(context)
    val reminders: ReminderScheduler = WorkManagerReminderScheduler(context)
    val notifications = NotificationService(context)
}


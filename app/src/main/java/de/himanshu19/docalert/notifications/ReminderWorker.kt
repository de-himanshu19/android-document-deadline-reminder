package de.himanshu19.docalert.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.himanshu19.docalert.DocAlertApplication
import de.himanshu19.docalert.domain.model.ReminderInterval
import kotlinx.coroutines.flow.first

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val id = inputData.getLong(KEY_ITEM_ID, -1)
        val interval = inputData.getString(KEY_INTERVAL)?.let {
            runCatching { ReminderInterval.valueOf(it) }.getOrNull()
        } ?: return Result.failure()
        if (id <= 0) return Result.failure()

        val container = (applicationContext as DocAlertApplication).container
        val item = container.items.getItem(id) ?: return Result.success()
        val privateContent = container.settings.settings.first().privateNotificationContent
        container.notifications.send(id, notificationContent(item, privateContent, interval.daysBefore))
        return Result.success()
    }

    companion object {
        const val KEY_ITEM_ID = "item_id"
        const val KEY_INTERVAL = "interval"
    }
}


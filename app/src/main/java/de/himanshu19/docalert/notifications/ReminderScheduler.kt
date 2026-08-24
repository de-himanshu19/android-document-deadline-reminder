package de.himanshu19.docalert.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import de.himanshu19.docalert.domain.model.ReminderInterval
import de.himanshu19.docalert.domain.model.TrackedItem
import java.time.Clock
import java.util.concurrent.TimeUnit

interface ReminderScheduler {
    fun reschedule(item: TrackedItem)
    fun cancel(itemId: Long)
}

class WorkManagerReminderScheduler(
    context: Context,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ReminderScheduler {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    override fun reschedule(item: TrackedItem) {
        ReminderInterval.entries.forEach { interval ->
            workManager.cancelUniqueWork(reminderWorkName(item.id, interval))
        }
        item.reminders.forEach { interval ->
            val delay = reminderDelay(item.expiryDate, interval, clock) ?: return@forEach
            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
                .setInputData(
                    Data.Builder()
                        .putLong(ReminderWorker.KEY_ITEM_ID, item.id)
                        .putString(ReminderWorker.KEY_INTERVAL, interval.name)
                        .build(),
                )
                .addTag(reminderItemTag(item.id))
                .build()
            workManager.enqueueUniqueWork(
                reminderWorkName(item.id, interval),
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }

    override fun cancel(itemId: Long) {
        ReminderInterval.entries.forEach { interval ->
            workManager.cancelUniqueWork(reminderWorkName(itemId, interval))
        }
    }
}

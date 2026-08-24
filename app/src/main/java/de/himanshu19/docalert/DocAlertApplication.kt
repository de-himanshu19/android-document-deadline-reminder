package de.himanshu19.docalert

import android.app.Application
import de.himanshu19.docalert.domain.model.ItemQuery
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DocAlertApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.notifications.createChannel()
        applicationScope.launch {
            runCatching {
                container.items.observeItems(ItemQuery(), LocalDate.now()).first()
                    .forEach(container.reminders::reschedule)
            }
        }
    }
}


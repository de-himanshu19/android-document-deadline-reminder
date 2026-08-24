package de.himanshu19.docalert.data.settings

import de.himanshu19.docalert.domain.model.ReminderInterval

enum class ThemeMode(val displayName: String) {
    SYSTEM("System default"),
    LIGHT("Light"),
    DARK("Dark"),
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultReminders: Set<ReminderInterval> = setOf(
        ReminderInterval.DAYS_30,
        ReminderInterval.DAYS_7,
        ReminderInterval.DAY_1,
    ),
    val privateNotificationContent: Boolean = true,
)


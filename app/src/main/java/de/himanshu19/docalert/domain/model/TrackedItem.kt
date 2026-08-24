package de.himanshu19.docalert.domain.model

import java.time.Instant
import java.time.LocalDate

enum class ItemType(val displayName: String) {
    DOCUMENT("Document"),
    DEADLINE("Deadline"),
}

enum class Category(val displayName: String) {
    PASSPORT("Passport"),
    RESIDENCE_PERMIT("Residence Permit"),
    DRIVING_LICENCE("Driving Licence"),
    INSURANCE("Insurance"),
    VEHICLE_TUV("Vehicle / TÜV"),
    WARRANTY("Warranty"),
    MEDICINE("Medicine"),
    SCHOOL("School"),
    JOB_APPLICATION("Job / Application"),
    CONTRACT("Contract"),
    OTHER("Other"),
}

enum class ReminderInterval(val daysBefore: Long, val displayName: String) {
    DAYS_90(90, "90 days before"),
    DAYS_30(30, "30 days before"),
    DAYS_7(7, "7 days before"),
    DAY_1(1, "1 day before"),
    DUE_DATE(0, "On the due date"),
}

data class TrackedItem(
    val id: Long = 0,
    val type: ItemType,
    val title: String,
    val category: Category,
    val owner: String? = null,
    val issueDate: LocalDate? = null,
    val expiryDate: LocalDate,
    val notes: String? = null,
    val reminders: Set<ReminderInterval> = emptySet(),
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)


package de.himanshu19.docalert.domain.model

import java.time.LocalDate

data class ItemQuery(
    val search: String = "",
    val statuses: Set<ItemStatus> = emptySet(),
    val category: Category? = null,
)

fun filterAndSortItems(
    items: List<TrackedItem>,
    query: ItemQuery,
    today: LocalDate,
): List<TrackedItem> {
    val needle = query.search.trim()
    return items.asSequence()
        .filter { item ->
            needle.isBlank() || listOf(item.title, item.owner.orEmpty(), item.category.displayName, item.type.displayName)
                .any { it.contains(needle, ignoreCase = true) }
        }
        .filter { query.category == null || it.category == query.category }
        .filter { query.statuses.isEmpty() || it.statusOn(today) in query.statuses }
        .sortedWith(
            compareBy<TrackedItem> {
                when (it.statusOn(today)) {
                    ItemStatus.DUE_TODAY -> 0
                    ItemStatus.URGENT, ItemStatus.EXPIRING_SOON, ItemStatus.ACTIVE -> 1
                    ItemStatus.EXPIRED -> 2
                }
            }.thenBy { item ->
                if (item.expiryDate >= today) item.expiryDate.toEpochDay() else -item.expiryDate.toEpochDay()
            }.thenBy { it.title.lowercase() },
        )
        .toList()
}


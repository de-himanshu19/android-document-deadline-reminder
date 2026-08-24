package de.himanshu19.docalert.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.himanshu19.docalert.domain.model.ItemStatus
import de.himanshu19.docalert.domain.model.TrackedItem
import de.himanshu19.docalert.domain.model.remainingDaysOn
import de.himanshu19.docalert.domain.model.remainingDaysText
import de.himanshu19.docalert.domain.model.statusOn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun LocalDate.localized(): String = format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

@Composable
fun StatusChip(status: ItemStatus, modifier: Modifier = Modifier) {
    val colors = when (status) {
        ItemStatus.ACTIVE -> Color(0xFF1B6D4A) to Color(0xFFD7F8E6)
        ItemStatus.EXPIRING_SOON -> Color(0xFF835500) to Color(0xFFFFDEA4)
        ItemStatus.URGENT, ItemStatus.DUE_TODAY -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.errorContainer
        ItemStatus.EXPIRED -> MaterialTheme.colorScheme.onSurfaceVariant to MaterialTheme.colorScheme.surfaceVariant
    }
    val icon = when (status) {
        ItemStatus.ACTIVE -> Icons.Outlined.CheckCircle
        ItemStatus.EXPIRING_SOON -> Icons.Outlined.Schedule
        ItemStatus.URGENT, ItemStatus.DUE_TODAY, ItemStatus.EXPIRED -> Icons.Outlined.ErrorOutline
    }
    Surface(
        color = colors.second,
        contentColor = colors.first,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.semantics { contentDescription = "Status: ${status.displayName}" },
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null)
            Text(status.displayName, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun ItemCard(item: TrackedItem, today: LocalDate, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val status = item.statusOn(today)
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().semantics {
            contentDescription = "${item.title}, ${status.displayName}, ${remainingDaysText(item.remainingDaysOn(today), item.type)}"
        },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("${item.type.displayName} • ${item.category.displayName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    item.owner?.let { Text("For $it", style = MaterialTheme.typography.bodyMedium) }
                }
                StatusChip(status, Modifier.padding(start = 8.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text(item.expiryDate.localized(), style = MaterialTheme.typography.bodyMedium)
                    Text(remainingDaysText(item.remainingDaysOn(today), item.type), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

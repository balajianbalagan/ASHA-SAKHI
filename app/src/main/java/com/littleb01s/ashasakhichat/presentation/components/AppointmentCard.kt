package com.littleb01s.ashasakhichat.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.littleb01s.ashasakhichat.data.local.entity.Appointment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppointmentCard(
    appointment: Appointment,
    onViewDetails: (Appointment) -> Unit = {},
    onEditAppointment: (Appointment) -> Unit = {},
    onMarkInProgress: (Appointment) -> Unit = {},
    onMarkCompleted:  (Appointment) -> Unit = {},
    onMarkCancelled:  (Appointment) -> Unit = {},
    onShare: (Appointment) -> Unit = {},
    onDelete: @Composable (Appointment) -> Unit = {}
) {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    var showMenu by remember { mutableStateOf(false) }
    
    // Check if appointment is overdue
    val isOverdue = appointment.appointmentDate.before(Date()) && 
                   appointment.appointmentStatus.lowercase() != "completed" &&
                   appointment.appointmentStatus.lowercase() != "cancelled"
    
    // Determine card styling based on priority
    val cardElevation = when {
        appointment.appointmentPriority != null && appointment.appointmentPriority >= 8 -> 8.dp
        appointment.appointmentPriority != null && appointment.appointmentPriority >= 5 -> 4.dp
        else -> 2.dp
    }
    
    val cardBorderColor = when {
        appointment.appointmentPriority != null && appointment.appointmentPriority >= 8 -> MaterialTheme.colorScheme.error
        appointment.appointmentPriority != null && appointment.appointmentPriority >= 5 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with date/time and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatter.format(appointment.appointmentDate),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeFormatter.format(appointment.appointmentDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Overdue indicator
                    if (isOverdue) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                "Overdue",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Overdue",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppointmentStatusChip(status = appointment.appointmentStatus)
                    
                    // Menu button
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("View Details") },
                                onClick = {
                                    onViewDetails(appointment)
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Visibility, "View")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share") },
                                onClick = {
                                    onShare(appointment)
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Share, "Share")
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Appointment name if available
            appointment.appointmentName?.let { name ->
                if (name.isNotBlank()) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
            
            // Bottom row with appointment type, priority, and quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Appointment type and priority
                Column {
                    // Appointment type chip
                    AppointmentTypeChip(appointmentType = appointment.appointmentType ?: "Regular")
                    
                    // Priority indicator
                    Spacer(modifier = Modifier.height(4.dp))
                    PriorityIndicator(priority = appointment.appointmentPriority ?: 0)
                }
                
                // Right side: Quick action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mark as In Progress button (for Scheduled appointments)
                    if (appointment.appointmentStatus.lowercase() == "scheduled") {
                        Button(
                            onClick = { onMarkInProgress(appointment) },
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                "Start Appointment",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Start",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                        
                        // Cancel button (for Scheduled appointments)
                        Button(
                            onClick = { onMarkCancelled(appointment) },
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                "Cancel Appointment",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onError
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Cancel",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                    
                    // Mark as Completed button (for In Progress appointments)
                    if (appointment.appointmentStatus.lowercase() == "in progress") {
                        Button(
                            onClick = { onMarkCompleted(appointment) },
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                "Mark Completed",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onTertiary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Complete",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiary
                            )
                        }
                    }
                    
                    // No action buttons for Completed appointments - only view details available
                }
            }
            
            // Sync pending indicator
            if (appointment.needsUpload) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CloudUpload,
                        "Pending Sync to Server",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pending Sync to Server",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun PriorityIndicator(priority: Int) {
    val priorityConfig = when {
        priority >= 8 -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "High Priority"
        )
        priority >= 5 -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Medium Priority"
        )
        priority >= 1 -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Low Priority"
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "No Priority"
        )
    }
    
    val backgroundColor = priorityConfig.first
    val textColor = priorityConfig.second
    val priorityText = priorityConfig.third
    
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = when {
                    priority >= 8 -> Icons.Default.Warning
                    priority >= 5 -> Icons.Default.Info
                    priority >= 1 -> Icons.Default.Schedule
                    else -> Icons.Default.Info
                },
                contentDescription = "Priority",
                modifier = Modifier.size(12.dp),
                tint = textColor
            )
            Text(
                text = "$priority/10",
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
}

@Composable
private fun AppointmentTypeChip(appointmentType: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = appointmentType,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun AppointmentStatusChip(status: String) {
    val statusConfig = when (status.lowercase()) {
        "scheduled" -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.Schedule
        )
        "in progress" -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            Icons.Default.PlayArrow
        )
        "completed" -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            Icons.Default.CheckCircle
        )
        "cancelled" -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.Cancel
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Info
        )
    }
    
    val backgroundColor = statusConfig.first
    val textColor = statusConfig.second
    val icon = statusConfig.third
    
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = status,
                modifier = Modifier.size(12.dp),
                tint = textColor
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
} 
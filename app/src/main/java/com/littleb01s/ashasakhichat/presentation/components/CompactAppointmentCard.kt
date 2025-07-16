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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littleb01s.ashasakhichat.data.local.entity.Appointment
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CompactAppointmentCard(
    appointment: Appointment,
    patientName: String? = null,
    onViewDetails: (Appointment) -> Unit = {},
    onMarkInProgress: (Appointment) -> Unit = {},
    onMarkCompleted: (Appointment) -> Unit = {},
    onMarkCancelled: (Appointment) -> Unit = {},
    onSendReminder: (Appointment) -> Unit = {},
    onAddCheckup: (Appointment) -> Unit = {}
) {
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    
    // Check if appointment is overdue
    val isOverdue = appointment.appointmentDate.before(Date()) && 
                   appointment.appointmentStatus.lowercase() != "completed" &&
                   appointment.appointmentStatus.lowercase() != "cancelled"
    
    // Determine card styling based on priority
    val cardElevation = when {
        appointment.appointmentPriority != null && appointment.appointmentPriority >= 8 -> 6.dp
        appointment.appointmentPriority != null && appointment.appointmentPriority >= 5 -> 3.dp
        else -> 1.dp
    }
    
    val cardBorderColor = when {
        appointment.appointmentPriority != null && appointment.appointmentPriority >= 8 -> MaterialTheme.colorScheme.error
        appointment.appointmentPriority != null && appointment.appointmentPriority >= 5 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header with time, status, and patient name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Time and Patient Name
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = timeFormatter.format(appointment.appointmentDate),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = patientName ?: "Patient ${appointment.patientId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Right side: Status chip
                CompactAppointmentStatusChip(status = appointment.appointmentStatus)
            }
            
            // Appointment name if available
            appointment.appointmentName?.let { name ->
                if (name.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Bottom row with appointment type, priority, and quick actions
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Appointment type and priority
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompactAppointmentTypeChip(appointmentType = appointment.appointmentType ?: "Regular")
                    CompactPriorityIndicator(priority = appointment.appointmentPriority ?: 0)
                }
                
                // Right side: Quick action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Mark as In Progress button (for Scheduled appointments)
                    if (appointment.appointmentStatus.lowercase() == "scheduled") {
                        IconButton(
                            onClick = { onMarkInProgress(appointment) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                "Start Appointment",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        
                        // Cancel button (for Scheduled appointments)
                        IconButton(
                            onClick = { onMarkCancelled(appointment) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                "Cancel Appointment",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    // Mark as Completed button (for In Progress appointments)
                    if (appointment.appointmentStatus.lowercase() == "in progress") {
                        IconButton(
                            onClick = { onMarkCompleted(appointment) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                "Mark Completed",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    
                    // Add Checkup button (for In Progress and Completed appointments)
                    if (appointment.appointmentStatus.lowercase() == "in progress" || 
                        appointment.appointmentStatus.lowercase() == "completed") {
                        IconButton(
                            onClick = { onAddCheckup(appointment) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                "Add Checkup",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // View Details button (always available)
                    IconButton(
                        onClick = { onViewDetails(appointment) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            "View Details",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Overdue indicator
            if (isOverdue) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        "Overdue",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Overdue",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Sync pending indicator
            if (appointment.needsUpload) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CloudUpload,
                        "Pending Sync",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Pending Sync",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactAppointmentStatusChip(status: String) {
    val (backgroundColor, textColor, icon) = when (status.lowercase()) {
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
    
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = status,
                modifier = Modifier.size(10.dp),
                tint = textColor
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun CompactAppointmentTypeChip(appointmentType: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = appointmentType,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun CompactPriorityIndicator(priority: Int) {
    val (backgroundColor, textColor) = when {
        priority >= 8 -> Pair(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        priority >= 5 -> Pair(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        priority >= 1 -> Pair(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        else -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = when {
                    priority >= 8 -> Icons.Default.Warning
                    priority >= 5 -> Icons.Default.Info
                    priority >= 1 -> Icons.Default.Schedule
                    else -> Icons.Default.Info
                },
                contentDescription = "Priority",
                modifier = Modifier.size(8.dp),
                tint = textColor
            )
            Text(
                text = "$priority",
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontSize = 10.sp
            )
        }
    }
} 
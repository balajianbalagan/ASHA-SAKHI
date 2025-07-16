package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.littleb01s.ashasakhichat.data.local.entity.Appointment
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailsScreen(
    appointment: Appointment,
    onNavigateBack: () -> Unit,
    onEditAppointment: (Appointment) -> Unit = {},
    onMarkInProgress: (Appointment) -> Unit = {},
    onMarkCompleted: (Appointment) -> Unit = {},
    onMarkCancelled: (Appointment) -> Unit = {},
    onShare: (Appointment) -> Unit = {}
) {
    val formatter = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    // Check if appointment is overdue
    val isOverdue = appointment.appointmentDate.before(Date()) && 
                   appointment.appointmentStatus.lowercase() != "completed" &&
                   appointment.appointmentStatus.lowercase() != "cancelled"
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointment Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onShare(appointment) }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Card with Status
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Status and Priority Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppointmentStatusChip(status = appointment.appointmentStatus)
                        PriorityIndicator(priority = appointment.appointmentPriority ?: 0)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Date and Time
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            "Date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = formatter.format(appointment.appointmentDate),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = timeFormatter.format(appointment.appointmentDate),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Overdue warning
                    if (isOverdue) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                "Overdue",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "This appointment is overdue",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Appointment Name
                    appointment.appointmentName?.let { name ->
                        if (name.isNotBlank()) {
                            DetailSection(
                                title = "Appointment Name",
                                content = name,
                                icon = Icons.Default.EventNote
                            )
                        }
                    }
                    
                    // Appointment Type
                    DetailSection(
                        title = "Type",
                        content = appointment.appointmentType ?: "Regular",
                        icon = Icons.Default.Category
                    )
                    
                    // Description
                    appointment.appointmentDescription?.let { description ->
                        if (description.isNotBlank()) {
                            DetailSection(
                                title = "Description",
                                content = description,
                                icon = Icons.Default.Description,
                                isMultiline = true
                            )
                        }
                    }
                    
                    // Priority
                    DetailSection(
                        title = "Priority Level",
                        content = "${appointment.appointmentPriority ?: 0}/10",
                        icon = Icons.Default.PriorityHigh
                    )
                    
                    // Worker and Patient IDs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DetailSection(
                            title = "Worker ID",
                            content = appointment.workerId.toString(),
                            icon = Icons.Default.Person,
                            modifier = Modifier.weight(1f)
                        )
                        DetailSection(
                            title = "Patient ID",
                            content = appointment.patientId.toString(),
                            icon = Icons.Default.Person,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Sync Status
                    if (appointment.needsUpload) {
                        DetailSection(
                            title = "Sync Status",
                            content = "Pending upload to server",
                            icon = Icons.Default.CloudUpload,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    } else {
                        DetailSection(
                            title = "Sync Status",
                            content = "Synced with server",
                            icon = Icons.Default.CloudDone,
                            contentColor = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    
                    // Created/Updated timestamps
                    appointment.createdAt?.let { createdAt ->
                        DetailSection(
                            title = "Created",
                            content = dateTimeFormatter.format(createdAt),
                            icon = Icons.Default.Create
                        )
                    }
                    
                    appointment.updatedAt?.let { updatedAt ->
                        DetailSection(
                            title = "Last Updated",
                            content = dateTimeFormatter.format(updatedAt),
                            icon = Icons.Default.Update
                        )
                    }
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit Button
                OutlinedButton(
                    onClick = { onEditAppointment(appointment) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                
                // Start Appointment (Mark as In Progress)
                if (appointment.appointmentStatus.lowercase() == "scheduled") {
                    Button(
                        onClick = { onMarkInProgress(appointment) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, "Start", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start")
                    }
                }
                
                // Mark as Completed
                if (appointment.appointmentStatus.lowercase() == "in progress") {
                    Button(
                        onClick = { onMarkCompleted(appointment) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, "Complete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Complete")
                    }
                }
                
                // Mark as Cancelled
                if (appointment.appointmentStatus.lowercase() != "cancelled" && 
                    appointment.appointmentStatus.lowercase() != "completed") {
                    Button(
                        onClick = { onMarkCancelled(appointment) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Cancel, "Cancel", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isMultiline: Boolean = false,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            modifier = if (isMultiline) Modifier.fillMaxWidth() else Modifier
        )
    }
}

@Composable
private fun PriorityIndicator(priority: Int) {
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
private fun AppointmentStatusChip(status: String) {
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
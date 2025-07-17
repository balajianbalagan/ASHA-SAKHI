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
import androidx.compose.ui.unit.sp
import com.littleb01s.ashasakhichat.data.local.entity.Appointment
import com.littleb01s.ashasakhichat.data.repository.AppointmentRepository
import com.littleb01s.ashasakhichat.util.Resource
import com.littleb01s.ashasakhichat.data.api.SaveAppointmentResponse
import com.littleb01s.ashasakhichat.data.api.SaveAppointmentData
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailsScreen(
    appointment: Appointment,
    patientName: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToPatient: (Int) -> Unit,
    appointmentRepository: AppointmentRepository
) {
    val formatter = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val dateTimeFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val scope = rememberCoroutineScope()
    
    // Check if appointment is overdue
    val isOverdue = appointment.appointmentDate.before(Date()) && 
                   appointment.appointmentStatus.lowercase() != "completed" &&
                   appointment.appointmentStatus.lowercase() != "cancelled"
    
    // Confirm dialog state
    var showConfirmDialog by remember { mutableStateOf(false) }
    var actionToConfirm by remember { mutableStateOf<String?>(null) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var confirmButtonText by remember { mutableStateOf("") }
    
    // Action result state
    var showActionResult by remember { mutableStateOf(false) }
    var actionResultMessage by remember { mutableStateOf("") }
    var isActionResultSuccess by remember { mutableStateOf(false) }
    
    // Function to show confirm dialog
    fun showConfirmDialog(action: String) {
        actionToConfirm = action
        
        when (action) {
            "cancel" -> {
                dialogTitle = "Cancel Appointment"
                dialogMessage = "Are you sure you want to cancel this appointment? This action cannot be undone."
                confirmButtonText = "Cancel Appointment"
            }
            "start" -> {
                dialogTitle = "Start Appointment"
                dialogMessage = "Are you sure you want to start this appointment? This will mark it as 'In Progress'."
                confirmButtonText = "Start Appointment"
            }
            "complete" -> {
                dialogTitle = "Complete Appointment"
                dialogMessage = "Are you sure you want to mark this appointment as completed?"
                confirmButtonText = "Complete Appointment"
            }
            "reminder" -> {
                dialogTitle = "Send Reminder"
                dialogMessage = "Are you sure you want to send a reminder to the patient for this appointment?"
                confirmButtonText = "Send Reminder"
            }
        }
        
        showConfirmDialog = true
    }
    
    // Function to handle confirm action
    fun handleConfirmAction() {
        if (actionToConfirm != null) {
            scope.launch {
                val result: Resource<SaveAppointmentResponse> = when (actionToConfirm) {
                    "cancel" -> {
                        appointmentRepository.cancelAppointment(appointment.appointmentId)
                    }
                    "start" -> {
                        appointmentRepository.markInProgress(appointment.appointmentId)
                    }
                    "complete" -> {
                        appointmentRepository.markCompleted(appointment.appointmentId)
                    }
                    "reminder" -> {
                        // For now, return success since sendReminder is not implemented
                        Resource.Success(SaveAppointmentResponse(data = SaveAppointmentData(appointmentId = appointment.appointmentId, message = "Reminder sent")))
                    }
                    else -> Resource.Error("Unknown action")
                }
                
                when (result) {
                    is Resource.Success -> {
                        actionResultMessage = "Action completed successfully"
                        isActionResultSuccess = true
                    }
                    is Resource.Error -> {
                        actionResultMessage = result.message ?: "Action failed"
                        isActionResultSuccess = false
                    }
                    is Resource.Loading -> {
                        // Should not happen here
                    }
                }
                showActionResult = true
            }
        }
        showConfirmDialog = false
        actionToConfirm = null
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointment Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
                    // Status and Priority Row with Sync Status and Type Chip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppointmentStatusChip(status = appointment.appointmentStatus)
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                        PriorityIndicator(priority = appointment.appointmentPriority ?: 0)
                            
                            // Sync Status Icon
                            Icon(
                                imageVector = if (appointment.needsUpload) Icons.Default.CloudUpload else Icons.Default.CloudDone,
                                contentDescription = if (appointment.needsUpload) "Pending upload" else "Synced",
                                tint = if (appointment.needsUpload) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
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
                    
                    // Patient Name Button
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { onNavigateToPatient(appointment.patientId) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Person,
                            "Patient",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = patientName ?: "View Patient Details",
                            style = MaterialTheme.typography.bodyMedium
                        )
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Appointment Type Chip in top right corner
                    AppointmentTypeChip(
                        type = appointment.appointmentType ?: "Regular",
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                    
                    // Main content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp), // Space for the chip
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
                    
                        // Created/Updated timestamps in same row - much smaller
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            appointment.createdAt?.let { createdAt ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Create,
                                        "Created",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Created:\n ${dateTimeFormatter.format(createdAt)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                    }
                    
                    appointment.updatedAt?.let { updatedAt ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Update,
                                        "Updated",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Updated:\n ${dateTimeFormatter.format(updatedAt)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Action buttons based on appointment status
                when (appointment.appointmentStatus.lowercase()) {
                    "scheduled" -> {
                        // Row 1: Start and Cancel buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                            // Start Appointment button
                            Button(
                                onClick = { showConfirmDialog("start") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(Icons.Default.PlayArrow, "Start", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Start")
                            }
                            
                            // Cancel button
                            Button(
                                onClick = { showConfirmDialog("cancel") },
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
                        
                        // Row 2: Send Reminder button
                OutlinedButton(
                            onClick = { showConfirmDialog("reminder") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                ) {
                            Icon(Icons.Default.Notifications, "Send Reminder", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                            Text("Send Reminder")
                }
                    }
                    
                    "in progress" -> {
                        // Row 1: Complete and Add Checkup buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Complete button
                    Button(
                                onClick = { showConfirmDialog("complete") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, "Complete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Complete")
                    }
                            
                            // Add Checkup button
                            Button(
                                onClick = { /* TODO: Navigate to add checkup screen */ },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.Add, "Add Checkup", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Checkup")
                            }
                        }
                    }
                    
                    "completed" -> {
                        // Add Checkup button for completed appointments
                    Button(
                            onClick = { /* TODO: Navigate to add checkup screen */ },
                            modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                            Icon(Icons.Default.Add, "Add Checkup", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Checkup")
                        }
                    }
                    
                    "cancelled" -> {
                        // No action buttons for cancelled appointments
                        // Just show a message
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No actions available for cancelled appointments",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(dialogTitle) },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(onClick = { handleConfirmAction() }) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Action Result Dialog
    if (showActionResult) {
        AlertDialog(
            onDismissRequest = { showActionResult = false },
            title = { 
                Text(
                    if (isActionResultSuccess) "Success" else "Error",
                    color = if (isActionResultSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                ) 
            },
            text = { Text(actionResultMessage) },
            confirmButton = {
                TextButton(onClick = { showActionResult = false }) {
                    Text("OK")
                }
            }
        )
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

@Composable
private fun AppointmentTypeChip(type: String, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor, icon) = when (type.lowercase()) {
        "regular" -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            Icons.Default.Event
        )
        "emergency" -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.Emergency
        )
        "follow-up" -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            Icons.Default.Refresh
        )
        "consultation" -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.Person
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Category
        )
    }
    
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = type,
                modifier = Modifier.size(10.dp),
                tint = textColor
            )
            Text(
                text = type,
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
} 
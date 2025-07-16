package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.data.local.entity.Appointment
import com.littleb01s.ashasakhichat.presentation.components.AppointmentCard
import com.littleb01s.ashasakhichat.presentation.viewmodel.AppointmentViewModel
import com.littleb01s.ashasakhichat.util.Resource
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    patientId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToAddAppointment: () -> Unit,
    onViewAppointmentDetails: (Appointment) -> Unit = {},
    onEditAppointment: (Appointment) -> Unit = {},
    onMarkInProgress: (Appointment) -> Unit = {},
    onMarkCompleted: (Appointment) -> Unit = {},
    onMarkCancelled: (Appointment) -> Unit = {},
    onSendReminder: (Appointment) -> Unit = {},
    onDelete: (Appointment) -> Unit = {},
    viewModel: AppointmentViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.fetchAppointmentsForPatient(patientId)
    }
    
    val appointmentsState by viewModel.appointments.collectAsState()
    val cancelAppointmentState by viewModel.cancelAppointmentState.collectAsState()
    val markInProgressState by viewModel.markInProgressState.collectAsState()
    val markCompletedState by viewModel.markCompletedState.collectAsState()
    
    // Confirm dialog state
    var showConfirmDialog by remember { mutableStateOf(false) }
    var appointmentToConfirm by remember { mutableStateOf<Appointment?>(null) }
    var actionToConfirm by remember { mutableStateOf<String?>(null) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var confirmButtonText by remember { mutableStateOf("") }
    var confirmButtonColor by remember { mutableStateOf(Color.Unspecified) }
    
    // Handle appointment status update results
    LaunchedEffect(cancelAppointmentState, markInProgressState, markCompletedState) {
        when {
            cancelAppointmentState is Resource.Success -> {
                viewModel.resetCancelAppointmentState()
            }
            cancelAppointmentState is Resource.Error -> {
                viewModel.resetCancelAppointmentState()
            }
            markInProgressState is Resource.Success -> {
                viewModel.resetMarkInProgressState()
            }
            markInProgressState is Resource.Error -> {
                viewModel.resetMarkInProgressState()
            }
            markCompletedState is Resource.Success -> {
                viewModel.resetMarkCompletedState()
            }
            markCompletedState is Resource.Error -> {
                viewModel.resetMarkCompletedState()
            }
        }
    }
    
    // Filter state
    val statusFilters = remember {
        mutableStateSetOf("Scheduled", "In Progress") // Default to show Scheduled and In Progress
    }
    
    // Function to show confirm dialog
    fun showConfirmDialog(appointment: Appointment, action: String) {
        appointmentToConfirm = appointment
        actionToConfirm = action
        
        when (action) {
            "cancel" -> {
                dialogTitle = "Cancel Appointment"
                dialogMessage = "Are you sure you want to cancel this appointment? This action cannot be undone."
                confirmButtonText = "Cancel Appointment"
                // Color will be set in the dialog
            }
            "start" -> {
                dialogTitle = "Start Appointment"
                dialogMessage = "Are you sure you want to start this appointment? This will mark it as 'In Progress'."
                confirmButtonText = "Start Appointment"
                // Color will be set in the dialog
            }
            "complete" -> {
                dialogTitle = "Complete Appointment"
                dialogMessage = "Are you sure you want to mark this appointment as completed?"
                confirmButtonText = "Complete Appointment"
                // Color will be set in the dialog
            }
            "reminder" -> {
                dialogTitle = "Send Reminder"
                dialogMessage = "Are you sure you want to send a reminder to the patient for this appointment?"
                confirmButtonText = "Send Reminder"
                // Color will be set in the dialog
            }
            "delete" -> {
                dialogTitle = "Delete Appointment"
                dialogMessage = "Are you sure you want to delete this appointment? This action cannot be undone."
                confirmButtonText = "Delete Appointment"
                // Color will be set in the dialog
            }
        }
        
        showConfirmDialog = true
    }
    
    // Function to handle confirm action
    fun handleConfirmAction() {
        if (appointmentToConfirm != null && actionToConfirm != null) {
            when (actionToConfirm) {
                "cancel" -> {
                    viewModel.cancelAppointment(appointmentToConfirm!!.appointmentId)
                    onMarkCancelled(appointmentToConfirm!!)
                }
                "start" -> {
                    viewModel.markInProgress(appointmentToConfirm!!.appointmentId)
                    onMarkInProgress(appointmentToConfirm!!)
                }
                "complete" -> {
                    viewModel.markCompleted(appointmentToConfirm!!.appointmentId)
                    onMarkCompleted(appointmentToConfirm!!)
                }
                "reminder" -> {
                    onSendReminder(appointmentToConfirm!!)
                }
                "delete" -> {
                    onDelete(appointmentToConfirm!!)
                }
            }
        }
        showConfirmDialog = false
        appointmentToConfirm = null
        actionToConfirm = null
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointments") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddAppointment,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Add Appointment")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter section
            FilterSection(
                statusFilters = statusFilters,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Appointments list
            when (val state = appointmentsState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message ?: "Error loading appointments",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is Resource.Success -> {
                    val allAppointments = state.data!!.appointments
                    val filteredAppointments = allAppointments
                        .filter { appointment ->
                            statusFilters.contains(appointment.appointmentStatus)
                        }
                        .sortedWith(
                            compareBy<Appointment> { appointment ->
                                // Priority order: In Progress (1), Scheduled (2), Completed (3), Cancelled (4)
                                when (appointment.appointmentStatus.lowercase()) {
                                    "in progress" -> 1
                                    "completed" -> 2
                                    "scheduled" -> 3
                                    "cancelled" -> 4
                                    else -> 5
                                }
                            }.thenBy { appointment ->
                                // Within each status group, sort by date (earliest first)
                                appointment.appointmentDate
                            }
                        )
                    
                    if (filteredAppointments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (allAppointments.isEmpty()) {
                                        "No appointments scheduled"
                                    } else {
                                        "No appointments match the selected filters"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (allAppointments.isEmpty()) {
                                        "Appointments will appear here once created"
                                    } else {
                                        "Try adjusting your filters"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp) // Add padding for FAB
                        ) {
                            items(filteredAppointments) { appointment ->
                                AppointmentCard(
                                    appointment = appointment,
                                    onViewDetails = onViewAppointmentDetails,
                                    onEditAppointment = onEditAppointment,
                                    onMarkInProgress = { appointment ->
                                        showConfirmDialog(appointment, "start")
                                    },
                                    onMarkCompleted = { appointment ->
                                        showConfirmDialog(appointment, "complete")
                                    },
                                    onMarkCancelled = { appointment ->
                                        showConfirmDialog(appointment, "cancel")
                                    },
                                    onSendReminder = onSendReminder,
                                    onDelete = { appointment ->
                                        showConfirmDialog(appointment, "delete")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Show confirm dialog
    if (showConfirmDialog) {
        val confirmColor = when (actionToConfirm) {
            "cancel" -> MaterialTheme.colorScheme.error
            "start" -> MaterialTheme.colorScheme.secondary
            "complete" -> MaterialTheme.colorScheme.tertiary
            "reminder" -> MaterialTheme.colorScheme.primary
            "delete" -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.primary
        }
        
        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
                appointmentToConfirm = null
                actionToConfirm = null
            },
            title = { Text(dialogTitle) },
            text = { Text(dialogMessage) },
            confirmButton = {
                Button(
                    onClick = { handleConfirmAction() },
                    colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
                ) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    appointmentToConfirm = null
                    actionToConfirm = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun FilterSection(
    statusFilters: MutableSet<String>,
    modifier: Modifier = Modifier
) {
    val availableStatuses = listOf("Scheduled", "In Progress", "Completed", "Cancelled")
    
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.FilterList,
                contentDescription = "Filter",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Filter by Status",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableStatuses) { status ->
                FilterChip(
                    selected = statusFilters.contains(status),
                    onClick = {
                        if (statusFilters.contains(status)) {
                            statusFilters.remove(status)
                        } else {
                            statusFilters.add(status)
                        }
                    },
                    label = {
                        Text(status)
                    },
                    leadingIcon = if (statusFilters.contains(status)) {
                        {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else null
                )
            }
        }
    }
} 
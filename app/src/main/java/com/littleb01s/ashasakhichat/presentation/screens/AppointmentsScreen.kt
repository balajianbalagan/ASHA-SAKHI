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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    patientId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToAddAppointment: () -> Unit,
    onViewAppointmentDetails: (Appointment) -> Unit = {},
    onEditAppointment: (Appointment) -> Unit = {},
    onMarkCompleted: (Appointment) -> Unit = {},
    onMarkCancelled: (Appointment) -> Unit = {},
    onShare: (Appointment) -> Unit = {},
    onDelete: (Appointment) -> Unit = {},
    viewModel: AppointmentViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.fetchAppointmentsForPatient(patientId)
    }
    
    val appointmentsState by viewModel.appointments.collectAsState()
    
    // Filter state
    val statusFilters = remember {
        mutableStateSetOf("Scheduled") // Default to show only scheduled
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
                        .sortedBy { appointment ->
                            appointment.appointmentDate
                        }
                    
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
                                    onMarkCompleted = onMarkCompleted,
                                    onMarkCancelled = onMarkCancelled,
                                    onShare = onShare,
                                    onDelete = onDelete
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    statusFilters: MutableSet<String>,
    modifier: Modifier = Modifier
) {
    val availableStatuses = listOf("Scheduled", "Completed", "Cancelled")
    
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
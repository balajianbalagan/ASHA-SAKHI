package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.data.model.Appointment
import com.littleb01s.ashasakhichat.presentation.viewmodel.AppointmentViewModel
import com.littleb01s.ashasakhichat.util.Resource
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentScreen(
    patientId: Int,
    onNavigateBack: () -> Unit,
    viewModel: AppointmentViewModel = hiltViewModel()
) {
    var selectedDate by remember { mutableStateOf(Date()) }
    var status by remember { mutableStateOf("Scheduled") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val createAppointmentState by viewModel.createAppointmentState.collectAsState()
    
    LaunchedEffect(createAppointmentState) {
        when (createAppointmentState) {
            is Resource.Success -> {
                onNavigateBack()
            }
            is Resource.Error -> {
                showError = true
                errorMessage = (createAppointmentState as Resource.Error).message ?: "Failed to create appointment"
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Appointment") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Picker
            OutlinedTextField(
                value = selectedDate.toString(),
                onValueChange = { /* Read-only */ },
                label = { Text("Appointment Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
            
            // Status Dropdown
            OutlinedTextField(
                value = status,
                onValueChange = { status = it },
                label = { Text("Status") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Error Message
            if (showError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Save Button
            Button(
                onClick = {
                    val workerId = viewModel.getWorkerId() ?: run {
                        showError = true
                        errorMessage = "Worker ID not found"
                        return@Button
                    }
                    
                    val appointment = Appointment(
                        workerId = workerId,
                        patientId = patientId,
                        appointmentDate = selectedDate,
                        appointmentType = "Regular",
                        appointmentStatus = status
                    )
                    viewModel.createAppointment(appointment)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Appointment")
            }
        }
    }
} 
package com.littleb01s.ashasakhichat.presentation.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.data.local.entity.Appointment
import com.littleb01s.ashasakhichat.presentation.viewmodel.AppointmentViewModel
import com.littleb01s.ashasakhichat.util.Resource
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DisplayMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentScreen(
    patientId: Int,
    onNavigateBack: () -> Unit,
    viewModel: AppointmentViewModel = hiltViewModel()
) {
    // Form state
    var selectedDate by remember { mutableStateOf(Date()) }
    var selectedTime by remember { mutableStateOf(Calendar.getInstance().apply { 
        set(Calendar.HOUR_OF_DAY, 9)
        set(Calendar.MINUTE, 0)
    }.time) }
    var appointmentType by remember { mutableStateOf("Regular") }
    var appointmentStatus by remember { mutableStateOf("Scheduled") }
    var notes by remember { mutableStateOf("") }
    
    // UI state
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    
    val createAppointmentState by viewModel.createAppointmentState.collectAsState()
    
    LaunchedEffect(createAppointmentState) {
        when (createAppointmentState) {
            is Resource.Success -> {
                isSubmitting = false
                showSuccessDialog = true
            }
            is Resource.Error -> {
                showErrorDialog = true
                errorMessage = (createAppointmentState as Resource.Error).message ?: "Failed to create appointment"
                isSubmitting = false
            }
            is Resource.Loading -> {
                // Keep isSubmitting = true when loading
            }
        }
    }
    
    // Date and time formatters
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    
    // Function to generate random future date and time
    fun generateRandomFutureDateTime(): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.timeInMillis
        
        // Generate random future date (1-30 days from now)
        val randomDays = (1..30).random()
        calendar.add(Calendar.DAY_OF_MONTH, randomDays)
        
        // Generate random time (9 AM to 5 PM) with rounded minutes (:00 or :30)
        val randomHour = (9..17).random()
        val randomMinute = listOf(0, 30).random() // Only :00 or :30
        calendar.set(Calendar.HOUR_OF_DAY, randomHour)
        calendar.set(Calendar.MINUTE, randomMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val futureDate = calendar.time
        
        // Create time component
        val timeCalendar = Calendar.getInstance()
        timeCalendar.set(Calendar.HOUR_OF_DAY, randomHour)
        timeCalendar.set(Calendar.MINUTE, randomMinute)
        timeCalendar.set(Calendar.SECOND, 0)
        timeCalendar.set(Calendar.MILLISECOND, 0)
        val futureTime = timeCalendar.time
        
        return Pair(futureDate, futureTime)
    }
    
    // Function to shuffle form values
    fun shuffleFormValues() {
        val appointmentTypes = listOf("Regular", "Emergency", "Follow-up", "Check-up", "Vaccination")
        val sampleNotes = listOf(
            "Patient requested follow-up",
            "Routine check-up appointment",
            "Emergency consultation needed",
            "Vaccination appointment",
            "Regular monitoring visit"
        )
        
        val (randomDate, randomTime) = generateRandomFutureDateTime()
        selectedDate = randomDate
        selectedTime = randomTime
        appointmentType = appointmentTypes.random()
        // Status remains "Scheduled" - no need to randomize
        notes = sampleNotes.random()
    }
    
    // Combine date and time
    val combinedDateTime = remember(selectedDate, selectedTime) {
        Calendar.getInstance().apply {
            time = selectedDate
            val timeCalendar = Calendar.getInstance().apply { time = selectedTime }
            set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Appointment") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { shuffleFormValues() }) {
                        Icon(Icons.Default.Shuffle, "Shuffle")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Date Selection
            Text(
                text = "Date & Time",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Date Button
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarToday, "Date", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(dateFormatter.format(selectedDate))
                }
                
                // Time Button
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarToday, "Time", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(timeFormatter.format(selectedTime))
                }
            }
            
            // Appointment Type
            val appointmentTypes = listOf("Regular", "Emergency", "Follow-up", "Check-up", "Vaccination")
            var expanded by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = appointmentType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Appointment Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    appointmentTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                appointmentType = type
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // Status (Fixed to Scheduled)
            OutlinedTextField(
                value = appointmentStatus,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Status") },
                modifier = Modifier.fillMaxWidth()
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                placeholder = { Text("Add any notes or special instructions") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )



            // Save Button
            Button(
                onClick = {
                    val workerId = viewModel.getWorkerId() ?: run {
                        showErrorDialog = true
                        errorMessage = "Worker ID not found"
                        return@Button
                    }

                    // Validate date is not in the past
                    if (combinedDateTime.before(Date())) {
                        showErrorDialog = true
                        errorMessage = "Appointment date cannot be in the past"
                        return@Button
                    }

                    isSubmitting = true
                    
                    val appointment = Appointment(
                        workerId = workerId,
                        patientId = patientId,
                        appointmentDate = combinedDateTime,
                        appointmentType = appointmentType,
                        appointmentStatus = appointmentStatus
                    )
                    viewModel.createAppointment(appointment)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSubmitting) "Creating..." else "Create Appointment")
            }

            // Info Text
            Text(
                text = "Note: Appointments will be saved locally and synced when online.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }

    val context = LocalContext.current
    
    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                onNavigateBack()
            },
            title = { Text("Success") },
            text = { Text("Appointment created successfully!") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showSuccessDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
    
    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(
                    onClick = { showErrorDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.time
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = millis
                            // Preserve the time part from selectedTime
                            val timeCal = Calendar.getInstance().apply { time = selectedTime }
                            cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                            cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.MILLISECOND, 0)
                            selectedDate = cal.time
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Show Time Picker
    if (showTimePicker) {
        val calendar = Calendar.getInstance().apply { time = selectedTime }
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val cal = Calendar.getInstance()
                cal.time = selectedTime
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                selectedTime = cal.time
                showTimePicker = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false // is24HourView
        ).show()
        // Reset the flag so it doesn't show repeatedly
        showTimePicker = false
    }
}
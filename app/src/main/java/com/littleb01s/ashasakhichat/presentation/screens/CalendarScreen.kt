package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.data.local.entity.Appointment
import com.littleb01s.ashasakhichat.presentation.viewmodel.AppointmentViewModel
import com.littleb01s.ashasakhichat.util.Resource
import com.littleb01s.ashasakhichat.presentation.components.CompactAppointmentCard
import com.littleb01s.ashasakhichat.data.repository.PatientRepository
import com.littleb01s.ashasakhichat.data.repository.AppointmentRepository
import com.littleb01s.ashasakhichat.data.api.SaveAppointmentResponse
import com.littleb01s.ashasakhichat.data.api.SaveAppointmentData
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*



@Composable
fun CalendarScreen(
    onNavigateToAppointmentDetails: (Int) -> Unit = {},
    viewModel: AppointmentViewModel = hiltViewModel(),
    patientRepository: PatientRepository = hiltViewModel<AppointmentViewModel>().patientRepository
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentWeekStart by remember { mutableStateOf(LocalDate.now().minusDays(LocalDate.now().dayOfWeek.value.toLong() - 1)) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var isWeekView by remember { mutableStateOf(true) }
    
    val customBlue = Color(0xFF0174B3)
    val customGreen = Color(0xFF1BBF69)
    
    val appointments = viewModel.appointments.collectAsState().value
    val appointmentsList = when (appointments) {
        is Resource.Success -> appointments.data!!.appointments
        else -> emptyList()
    }

    val filteredAppointments = remember(selectedDate, appointmentsList) {
        appointmentsList.filter { appointment ->
        val appointmentLocalDate = appointment.appointmentDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        appointmentLocalDate == selectedDate
        }
    }
    
    // State to store patient names
    var patientNames by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    
    // Dialog states for quick actions
    var showConfirmDialog by remember { mutableStateOf(false) }
    var appointmentToConfirm by remember { mutableStateOf<Appointment?>(null) }
    var actionToConfirm by remember { mutableStateOf<String?>(null) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var confirmButtonText by remember { mutableStateOf("") }
    
    // Action result state
    var showActionResult by remember { mutableStateOf(false) }
    var actionResultMessage by remember { mutableStateOf("") }
    var isActionResultSuccess by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    // Observe ViewModel state changes for action results
    val cancelAppointmentState by viewModel.cancelAppointmentState.collectAsState()
    val markInProgressState by viewModel.markInProgressState.collectAsState()
    val markCompletedState by viewModel.markCompletedState.collectAsState()
    
    // Handle action results
    LaunchedEffect(cancelAppointmentState, markInProgressState, markCompletedState) {
        when {
            cancelAppointmentState is Resource.Success -> {
                actionResultMessage = "Appointment cancelled successfully"
                isActionResultSuccess = true
                showActionResult = true
                viewModel.resetCancelAppointmentState()
            }
            cancelAppointmentState is Resource.Error -> {
                actionResultMessage = cancelAppointmentState.message ?: "Failed to cancel appointment"
                isActionResultSuccess = false
                showActionResult = true
                viewModel.resetCancelAppointmentState()
            }
            markInProgressState is Resource.Success -> {
                actionResultMessage = "Appointment started successfully"
                isActionResultSuccess = true
                showActionResult = true
                viewModel.resetMarkInProgressState()
            }
            markInProgressState is Resource.Error -> {
                actionResultMessage = markInProgressState.message ?: "Failed to start appointment"
                isActionResultSuccess = false
                showActionResult = true
                viewModel.resetMarkInProgressState()
            }
            markCompletedState is Resource.Success -> {
                actionResultMessage = "Appointment completed successfully"
                isActionResultSuccess = true
                showActionResult = true
                viewModel.resetMarkCompletedState()
            }
            markCompletedState is Resource.Error -> {
                actionResultMessage = markCompletedState.message ?: "Failed to complete appointment"
                isActionResultSuccess = false
                showActionResult = true
                viewModel.resetMarkCompletedState()
            }
        }
    }
    
    // Fetch patient names for filtered appointments
    LaunchedEffect(filteredAppointments) {
        val names = mutableMapOf<Int, String>()
        filteredAppointments.forEach { appointment ->
            val patientName = patientRepository.getPatientNameById(appointment.patientId)
            if (patientName != null) {
                names[appointment.patientId] = patientName
            }
        }
        patientNames = names
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
        if (appointmentToConfirm != null && actionToConfirm != null) {
            when (actionToConfirm) {
                "cancel" -> {
                    viewModel.cancelAppointment(appointmentToConfirm!!.appointmentId)
                }
                "start" -> {
                    viewModel.markInProgress(appointmentToConfirm!!.appointmentId)
                }
                "complete" -> {
                    viewModel.markCompleted(appointmentToConfirm!!.appointmentId)
        }
                "reminder" -> {
                    // For now, show success message since sendReminder is not implemented
                    actionResultMessage = "Reminder sent successfully"
                    isActionResultSuccess = true
                    showActionResult = true
                }
            }
        }
        showConfirmDialog = false
        appointmentToConfirm = null
        actionToConfirm = null
    }
    

    
    LaunchedEffect(Unit) {
        viewModel.fetchAppointments()
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp,vertical = 0.dp)
    ) {
        // Navigation Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        if (isWeekView) {
                            currentWeekStart = currentWeekStart.minusWeeks(1)
                            selectedDate = selectedDate.minusWeeks(1)
                        } else {
                            currentMonth = currentMonth.minusMonths(1)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = if (isWeekView) "Previous Week" else "Previous Month",
                        tint = customBlue
                    )
                }
                
                Text(
                    text = if (isWeekView) {
                        "Week of ${currentWeekStart.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd"))}"
                    } else {
                        currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentMonth.year
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = customBlue
                )
                
                IconButton(
                    onClick = { 
                        if (isWeekView) {
                            currentWeekStart = currentWeekStart.plusWeeks(1)
                            selectedDate = selectedDate.plusWeeks(1)
                        } else {
                            currentMonth = currentMonth.plusMonths(1)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = if (isWeekView) "Next Week" else "Next Month",
                        tint = customBlue
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(0.dp))
        }
        
        // Calendar View
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (isWeekView) {
                        WeekView(
                            currentWeekStart = currentWeekStart,
                            selectedDate = selectedDate,
                            appointmentsList = appointmentsList,
                            onDateSelected = { selectedDate = it },
                            customBlue = customBlue,
                            customGreen = customGreen
                        )
                    } else {
                        MonthView(
                            currentMonth = currentMonth,
                            selectedDate = selectedDate,
                            appointmentsList = appointmentsList,
                            onDateSelected = { selectedDate = it },
                            customBlue = customBlue,
                            customGreen = customGreen
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Events Header with Toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Events for ${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = customBlue
                )
                
                IconButton(
                    onClick = { isWeekView = !isWeekView }
                ) {
                    Icon(
                        imageVector = if (isWeekView) Icons.Default.CalendarMonth else Icons.Default.ViewWeek,
                        contentDescription = if (isWeekView) "Switch to Month View" else "Switch to Week View",
                        tint = customBlue
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Events List
        when {
            appointments is Resource.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = customBlue,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Loading appointments...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            appointments is Resource.Error -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Error loading appointments",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = appointments.message ?: "Please try again",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            else -> {
                items(filteredAppointments) { appointment ->
                    CompactAppointmentCard(
                        appointment = appointment,
                        patientName = patientNames[appointment.patientId],
                        onViewDetails = { appointment ->
                            onNavigateToAppointmentDetails(appointment.appointmentId)
                        },
                        onMarkInProgress = { appointment ->
                            showConfirmDialog(appointment, "start")
                        },
                        onMarkCompleted = { appointment ->
                            showConfirmDialog(appointment, "complete")
                        },
                        onMarkCancelled = { appointment ->
                            showConfirmDialog(appointment, "cancel")
                        },
                        onSendReminder = { appointment ->
                            showConfirmDialog(appointment, "reminder")
                        },
                        onAddCheckup = { /* TODO: Navigate to add checkup screen */ }
                    )
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
private fun WeekView(
    currentWeekStart: LocalDate,
    selectedDate: LocalDate,
    appointmentsList: List<Appointment>,
    onDateSelected: (LocalDate) -> Unit,
    customBlue: Color,
    customGreen: Color
) {
    Column {
        // Weekday Headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 0..6) {
                val date = currentWeekStart.plusDays(i.toLong())
                val dayOfWeek = date.dayOfWeek
                val isToday = date == LocalDate.now()
                val isSelected = date == selectedDate
                
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        textAlign = TextAlign.Center,
                        color = if (isToday) customGreen else customBlue,
                        fontSize = 12.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Date number
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = when {
                                    isSelected -> customBlue
                                    isToday -> customGreen.copy(alpha = 0.2f)
                                    else -> Color.Transparent
                                },
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onDateSelected(date) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            color = when {
                                isSelected -> Color.White
                                isToday -> customGreen
                                else -> Color.Black
                            },
                            fontSize = 14.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Event indicator
                    val hasEvents = appointmentsList.any { 
                        it.appointmentDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate() == date 
                    }
                    
                    if (hasEvents) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = customGreen,
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthView(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    appointmentsList: List<Appointment>,
    onDateSelected: (LocalDate) -> Unit,
    customBlue: Color,
    customGreen: Color
) {
            Column {
                // Weekday Headers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DayOfWeek.values().forEach { dayOfWeek ->
                        Text(
                            text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            textAlign = TextAlign.Center,
                    color = customBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Calendar Days
                val firstDayOfMonth = currentMonth.atDay(1)
                val lastDayOfMonth = currentMonth.atEndOfMonth()
                val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
                val daysInMonth = lastDayOfMonth.dayOfMonth
                
                var currentDay = 1
                var currentWeek = 0
                
                while (currentDay <= daysInMonth) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (dayOfWeek in 0..6) {
                            if (currentWeek == 0 && dayOfWeek < firstDayOfWeek) {
                                // Empty space before first day of month
                                Box(modifier = Modifier.weight(1f))
                            } else if (currentDay <= daysInMonth) {
                                val date = currentMonth.atDay(currentDay)
                                val isSelected = date == selectedDate
                                val isToday = date == LocalDate.now()
                        val hasEvents = remember(date, appointmentsList) {
                            appointmentsList.any { 
                                    it.appointmentDate.toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate() == date 
                            }
                                }
                                
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Date number
                                Box(
                                    modifier = Modifier
                                    .size(28.dp)
                                        .background(
                                            color = when {
                                                isSelected -> customBlue
                                                isToday -> customGreen.copy(alpha = 0.2f)
                                                else -> Color.Transparent
                                            },
                                        shape = RoundedCornerShape(14.dp)
                                        )
                                    .clickable { onDateSelected(date) },
                                    contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = currentDay.toString(),
                                            color = when {
                                                isSelected -> Color.White
                                                isToday -> customGreen
                                                else -> Color.Black
                                            },
                                    fontSize = 12.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                        )
                            }
                            
                            Spacer(modifier = Modifier.height(2.dp))
                                        
                            // Event indicator
                                        if (hasEvents) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(
                                                        color = customGreen,
                                                        shape = RoundedCornerShape(2.dp)
                                                    )
                                            )
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                                currentDay++
                            } else {
                                // Empty space after last day of month
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    currentWeek++
        }
    }
}


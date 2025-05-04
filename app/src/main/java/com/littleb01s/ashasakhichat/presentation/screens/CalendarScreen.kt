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
import com.littleb01s.ashasakhichat.data.model.Appointment
import com.littleb01s.ashasakhichat.presentation.viewmodel.AppointmentViewModel
import com.littleb01s.ashasakhichat.util.Resource
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

data class CalendarEvent(
    val id: String,
    val title: String,
    val date: LocalDate,
    val time: LocalTime,
    val description: String? = null
)

@Composable
fun CalendarScreen(
    viewModel: AppointmentViewModel = hiltViewModel()
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    
    val customBlue = Color(0xFF0174B3)
    val customGreen = Color(0xFF1BBF69)
    
    val appointments = viewModel.appointments.collectAsState().value
    val appointmentsList = when (appointments) {
        is Resource.Success -> appointments.data!!.appointments
        else -> emptyList()
    }

    val filteredAppointments = appointmentsList.filter { appointment ->
        val appointmentLocalDate = appointment.appointmentDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        appointmentLocalDate == selectedDate
    }
    
    val eventsForSelectedDate = remember(filteredAppointments) {
        filteredAppointments.map { appointment ->
            CalendarEvent(
                id = appointment.appointmentId.toString(),
                title = "Appointment with Patient ${appointment.patientId}",
                date = appointment.appointmentDate.toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate(),
                time = appointment.appointmentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime(),
                description = "Status: ${appointment.appointmentStatus}"
            )
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.fetchAppointments()
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Month Navigation
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { currentMonth = currentMonth.minusMonths(1) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Month",
                        tint = customBlue
                    )
                }
                
                Text(
                    text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentMonth.year,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = customBlue
                )
                
                IconButton(
                    onClick = { currentMonth = currentMonth.plusMonths(1) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Month",
                        tint = customBlue
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Calendar Grid
        item {
            Column {
                // Weekday Headers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DayOfWeek.values().forEach { dayOfWeek ->
                        Text(
                            text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = customBlue
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
                                val hasEvents = appointmentsList.any { 
                                    it.appointmentDate.toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate() == date 
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(4.dp)
                                        .background(
                                            color = when {
                                                isSelected -> customBlue
                                                isToday -> customGreen.copy(alpha = 0.2f)
                                                else -> Color.Transparent
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedDate = date },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = currentDay.toString(),
                                            color = when {
                                                isSelected -> Color.White
                                                isToday -> customGreen
                                                else -> Color.Black
                                            },
                                            fontSize = 16.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                        )
                                        
                                        // Event indicator dots
                                        if (hasEvents) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(
                                                        color = customGreen,
                                                        shape = RoundedCornerShape(2.dp)
                                                    )
                                            )
                                        }
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
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Events Header
        item {
            Text(
                text = "Events for ${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = customBlue
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Events List
        items(eventsForSelectedDate) { event ->
            EventCard(
                event = event,
                onClick = { /* Handle event click */ }
            )
        }
    }
}

@Composable
private fun EventCard(
    event: CalendarEvent,
    onClick: () -> Unit
) {
    val customGreen = Color(0xFF1BBF69)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = customGreen.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = customGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.time.toString(),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                event.description?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
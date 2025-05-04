package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.littleb01s.R
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarScreen() {
    val currentMonth = remember { YearMonth.now() }
    val startDate = remember { currentMonth.atStartOfMonth() }
    val endDate = remember { currentMonth.plusMonths(12).atEndOfMonth() }
    
    val weekCalendarState = rememberWeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstVisibleWeekDate = startDate,
    )

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    
    val customBlue = Color(0xFF0174B3)
    val customGreen = Color(0xFF1BBF69)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.Calendar),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = customBlue
            )
            
            IconButton(
                onClick = { showAddEventDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Event",
                    tint = customGreen
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Week Calendar
        WeekCalendar(
            state = weekCalendarState,
            dayContent = { day ->
                Day(day, selectedDate, customBlue, customGreen) { date ->
                    selectedDate = date
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Events List
        Text(
            text = "Events for ${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = customBlue
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // TODO: Replace with actual events from database
        LazyColumn {
            items(3) { index ->
                EventCard(
                    title = "Sample Event $index",
                    time = "10:00 AM",
                    color = customGreen
                )
            }
        }
    }
    
    if (showAddEventDialog) {
        AddEventDialog(
            onDismiss = { showAddEventDialog = false },
            onAddEvent = { title, time ->
                // TODO: Save event to database
                showAddEventDialog = false
            }
        )
    }
}

@Composable
private fun Day(
    day: WeekDay,
    selectedDate: LocalDate,
    customBlue: Color,
    customGreen: Color,
    onDateSelected: (LocalDate) -> Unit
) {
    val isSelected = day.date == selectedDate
    val isToday = day.date == LocalDate.now()
    
    Box(
        modifier = Modifier
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
            .clickable { onDateSelected(day.date) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = when {
                    isSelected -> Color.White
                    isToday -> customGreen
                    else -> Color.Black
                },
                fontSize = 16.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
            
            // Event indicator dots
            if (day.date.dayOfMonth % 3 == 0) { // TODO: Replace with actual event check
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
}

@Composable
private fun EventCard(
    title: String,
    time: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
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
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = time,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun AddEventDialog(
    onDismiss: () -> Unit,
    onAddEvent: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    
    val customBlue = Color(0xFF0174B3)
    val customGreen = Color(0xFF1BBF69)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Event",
                color = customBlue,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (e.g., 10:00 AM)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && time.isNotBlank()) {
                        onAddEvent(title, time)
                    }
                }
            ) {
                Text(
                    text = "Add",
                    color = customGreen
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = customBlue
                )
            }
        }
    )
} 
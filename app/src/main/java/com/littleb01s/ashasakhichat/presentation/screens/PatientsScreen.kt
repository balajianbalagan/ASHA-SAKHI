package com.littleb01s.ashasakhichat.presentation.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.data.local.entity.Patient
import com.littleb01s.ashasakhichat.presentation.PatientsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

// Define colors at the top level to match PatientDetailsScreen
private val CustomBlue = Color(0xFF0174B3)
private val CustomGreen = Color(0xFF1BBF69)
private val CustomOrange = Color(0xFFFF5151)
private val BackgroundColor = Color(0xFFFFF5EE)
private val GradientBrush = Brush.horizontalGradient(colors = listOf(CustomBlue, CustomGreen))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(
    onPatientClick: (Patient) -> Unit,
    onAddNewPatient: () -> Unit,
    viewModel: PatientsViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val patients by viewModel.filteredPatients(searchQuery).collectAsState(initial = emptyList())
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Sync Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = CustomOrange,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    Scaffold(
        containerColor = BackgroundColor,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewPatient,
                containerColor = CustomGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Patient")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 1.dp
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search by name") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = CustomBlue
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CustomBlue,
                            focusedLabelColor = CustomBlue,
                            cursorColor = CustomBlue
                        ),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            scope.launch {
                                try {
                                    isRefreshing = true
                                    viewModel.syncPatients()
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Failed to sync patients"
                                    showErrorDialog = true
                                } finally {
                                    isRefreshing = false
                                }
                            }
                        },
                        enabled = !isRefreshing
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = CustomBlue
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Sync",
                                tint = CustomBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Patients List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(patients) { patient ->
                    PatientCard(patient = patient, onClick = { onPatientClick(patient) })
                }
            }
        }
    }
}

@Composable
fun PatientCard(patient: Patient, onClick: () -> Unit) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left section with patient info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${patient.firstName} ${patient.lastName ?: ""}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CustomBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (patient.needsUpload) {
                        Surface(
                            color = CustomOrange.copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Pending Sync",
                                tint = CustomOrange,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(2.dp)
                            )
                        }
                    }
                }

                // Patient details in a compact grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left column
                    Column(modifier = Modifier.weight(1f)) {
                        PatientInfoRow(
                            icon = Icons.Filled.DateRange,
                            mainText = "Age",
                            text = "${calculateAge(patient.dateOfBirth)}y",
                            color = Color.Gray
                        )
                        PatientInfoRow(
                            icon = Icons.Default.LocationOn,
                            text = patient.city ?: "-",
                            mainText = "Place",
                            color = Color.Gray
                        )
                    }
                    
                    // Right column
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        PatientInfoRow(
                            icon = Icons.Filled.Person,
                            mainText = "Trimester",
                            text = calculateTrimester(patient.lmp),
                            color = CustomGreen
                        )
                        PatientInfoRow(
                            icon = Icons.Default.DateRange,
                            mainText = "EDD",
                            text = formatDate(patient.deliveryDate ?: calculateEDD(patient.lmp)),
                            color = CustomBlue
                        )
                    }
                }
            }

            // Call button
            IconButton(
                onClick = { handlePhoneCall(context, patient.mobileNumber) },
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(36.dp)
                    .background(
                        color = CustomGreen.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    )
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = "Call Patient",
                    tint = CustomGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PatientInfoRow(
    icon: ImageVector,
    mainText: String,
    text: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = mainText,
            fontSize = 14.sp,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun handlePhoneCall(context: android.content.Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle exception (could show a toast or snackbar)
        e.printStackTrace()
    }
}

private fun calculateAge(dateOfBirth: Date): Int {
    val today = Calendar.getInstance()
    val birthDate = Calendar.getInstance().apply { time = dateOfBirth }
    var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
        age--
    }
    return age
}

private fun calculateTrimester(lmp: Date?): String {
    if (lmp == null) return "-"
    val today = Calendar.getInstance().timeInMillis
    val lmpTime = lmp.time
    val weeksDiff = TimeUnit.MILLISECONDS.toDays(today - lmpTime) / 7

    return when {
        weeksDiff < 0 -> "-"
        weeksDiff <= 13 -> "1st Tri"
        weeksDiff <= 26 -> "2nd Tri"
        weeksDiff <= 40 -> "3rd Tri"
        else -> "Post"
    }
}

private fun calculateEDD(lmp: Date?): Date {
    if (lmp == null) return Date()
    return Calendar.getInstance().apply {
        time = lmp
        add(Calendar.WEEK_OF_YEAR, 40)
    }.time
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
    return formatter.format(date)
} 
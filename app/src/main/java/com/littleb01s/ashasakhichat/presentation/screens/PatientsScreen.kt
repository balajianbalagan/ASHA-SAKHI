package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.data.local.entity.Patient
import com.littleb01s.ashasakhichat.presentation.PatientsViewModel
import kotlinx.coroutines.launch

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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${patient.firstName} ${patient.lastName ?: ""}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CustomBlue
                )
                if (patient.needsUpload) {
                    Surface(
                        color = CustomOrange.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Pending Sync",
                                tint = CustomOrange,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                "Pending Sync",
                                color = CustomOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Text(
                text = "Mobile: ${patient.mobileNumber}",
                color = Color.Gray
            )
            Text(
                text = "City: ${patient.city ?: "-"}",
                color = Color.Gray
            )
        }
    }
} 
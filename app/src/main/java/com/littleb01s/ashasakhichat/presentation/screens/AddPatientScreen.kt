package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.presentation.PatientsViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientScreen(
    onNavigateBack: () -> Unit,
    viewModel: PatientsViewModel = hiltViewModel()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var employmentStatus by remember { mutableStateOf("") }
    var religion by remember { mutableStateOf("") }
    var caste by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Patient") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Information
            Text("Basic Information", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name*") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                label = { Text("Mobile Number*") },
                modifier = Modifier.fillMaxWidth()
            )

            // Location Information
            Text("Location", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = state,
                onValueChange = { state = it },
                label = { Text("State*") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City*") },
                modifier = Modifier.fillMaxWidth()
            )

            // Personal Information
            Text("Personal Information", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it },
                label = { Text("Date of Birth (DD/MM/YYYY)*") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = bloodGroup,
                onValueChange = { bloodGroup = it },
                label = { Text("Blood Group") },
                modifier = Modifier.fillMaxWidth()
            )

            // Social Information
            Text("Social Information", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = education,
                onValueChange = { education = it },
                label = { Text("Education") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = employmentStatus,
                onValueChange = { employmentStatus = it },
                label = { Text("Employment Status") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = religion,
                onValueChange = { religion = it },
                label = { Text("Religion") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = caste,
                onValueChange = { caste = it },
                label = { Text("Caste") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // TODO: Validate and save patient data
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Patient")
            }
        }
    }
} 
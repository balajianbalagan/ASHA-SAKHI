package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.presentation.AddCheckupViewModel
import com.littleb01s.ashasakhichat.presentation.CheckupFormState
import com.littleb01s.ashasakhichat.presentation.DetailScaffold

// Define colors at the top level
private val CustomBlue = Color(0xFF0174B3)
private val CustomGreen = Color(0xFF1BBF69)
private val CustomOrange = Color(0xFFFF5151)
private val BackgroundColor = Color(0xFFFFF5EE)
private val GradientBrush = Brush.horizontalGradient(colors = listOf(CustomBlue, CustomGreen))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientRecordScreen(
    patientId: Int,
    onNavigateBack: () -> Unit,
    viewModel: AddCheckupViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    var showTypeDialog by remember { mutableStateOf(false) }

    if (showTypeDialog) {
        AlertDialog(
            containerColor = Color.White,
            titleContentColor = CustomBlue,
            textContentColor = CustomBlue,
            onDismissRequest = { showTypeDialog = false },
            title = { Text("Select Record Type", color = CustomBlue) },
            text = {
                Column {
                    AddCheckupViewModel.CHECKUP_TYPES.forEach { type ->
                        TextButton(
                            onClick = {
                                viewModel.updateCheckupType(type)
                                showTypeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = CustomBlue
                            )
                        ) {
                            Text(type, color = CustomBlue)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showTypeDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = CustomOrange)
                ) {
                    Text("Cancel", color = CustomOrange)
                }
            }
        )
    }

    DetailScaffold(
        title = "Add Patient Record",
        onNavigateBack = onNavigateBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Record Type Selection
            OutlinedButton(
                onClick = { showTypeDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = CustomBlue
                )
            ) {
                Text(formState.checkupType.ifEmpty { "Select Record Type" })
            }

            // Dynamic form fields based on record type
            when (formState.checkupType) {
                "VITALS" -> VitalsForm(viewModel, formState)
                "SYMPTOMS" -> SymptomsForm(viewModel, formState)
                "NOTES" -> NotesForm(viewModel, formState)
                "TEST_RESULTS" -> TestResultsForm(viewModel, formState)
                "ANC_VISIT" -> ANCVisitForm(viewModel, formState)
            }

            if (formState.error != null) {
                Text(
                    text = formState.error!!,
                    color = CustomOrange,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Button(
                onClick = { viewModel.saveCheckup(patientId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = formState.checkupType.isNotEmpty() && !formState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CustomGreen
                )
            ) {
                if (formState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Save Record")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalsForm(viewModel: AddCheckupViewModel, state: CheckupFormState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.bloodPressure,
            onValueChange = { viewModel.updateBloodPressure(it) },
            label = { Text("Blood Pressure (mmHg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = state.oxygen,
            onValueChange = { viewModel.updateOxygen(it) },
            label = { Text("Oxygen Level (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = state.weight,
            onValueChange = { viewModel.updateWeight(it) },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = state.temperature,
            onValueChange = { viewModel.updateTemperature(it) },
            label = { Text("Temperature (°C)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = state.sugarLevel,
            onValueChange = { viewModel.updateSugarLevel(it) },
            label = { Text("Blood Sugar Level") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = state.bmi,
            onValueChange = { viewModel.updateBMI(it) },
            label = { Text("BMI") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = state.haemoglobin,
            onValueChange = { viewModel.updateHaemoglobin(it) },
            label = { Text("Haemoglobin") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomsForm(viewModel: AddCheckupViewModel, state: CheckupFormState) {
    OutlinedTextField(
        value = state.checkupData,
        onValueChange = { viewModel.updateCheckupData(it) },
        label = { Text("Symptoms Description") },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        maxLines = 5,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CustomBlue,
            unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesForm(viewModel: AddCheckupViewModel, state: CheckupFormState) {
    OutlinedTextField(
        value = state.checkupData,
        onValueChange = { viewModel.updateCheckupData(it) },
        label = { Text("Notes") },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        maxLines = 5,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CustomBlue,
            unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestResultsForm(viewModel: AddCheckupViewModel, state: CheckupFormState) {
    OutlinedTextField(
        value = state.checkupData,
        onValueChange = { viewModel.updateCheckupData(it) },
        label = { Text("Test Results") },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        maxLines = 5,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CustomBlue,
            unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ANCVisitForm(viewModel: AddCheckupViewModel, state: CheckupFormState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.checkupData,
            onValueChange = { viewModel.updateCheckupData(it) },
            label = { Text("ANC Visit Notes") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = state.pregnancyStage,
            onValueChange = { viewModel.updatePregnancyStage(it) },
            label = { Text("Pregnancy Stage") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
    }
} 
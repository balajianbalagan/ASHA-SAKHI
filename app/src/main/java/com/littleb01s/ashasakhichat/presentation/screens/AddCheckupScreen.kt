package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.R
import com.littleb01s.ashasakhichat.presentation.AddCheckupViewModel
import com.littleb01s.ashasakhichat.presentation.CheckupFormState
import com.littleb01s.ashasakhichat.presentation.DetailScaffold
import com.littleb01s.ashasakhichat.presentation.screens.forms.*

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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                when (type) {
                                    "VITALS" -> Icon(
                                        painter = painterResource(id = R.drawable.ic_vitals),
                                        contentDescription = "Vitals",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    "SYMPTOMS" -> Icon(
                                        painter = painterResource(id = R.drawable.ic_symptoms),
                                        contentDescription = "Symptoms",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    "NOTES" -> Icon(
                                        painter = painterResource(id = R.drawable.ic_notes),
                                        contentDescription = "Notes",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    "TEST_RESULTS" -> Icon(
                                        painter = painterResource(id = R.drawable.ic_test_results),
                                        contentDescription = "Test Results",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    "ANC_VISIT" -> Icon(
                                        painter = painterResource(id = R.drawable.ic_anc_visit),
                                        contentDescription = "ANC Visit",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    "VACCINATION" -> Icon(
                                        painter = painterResource(id = R.drawable.ic_vaccination),
                                        contentDescription = "Vaccination",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    "MEDICAL_REPORT" -> Icon(
                                        painter = painterResource(id = R.drawable.ic_medical_report),
                                        contentDescription = "Medical Report",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            Text(type, color = CustomBlue)
                            }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showTypeDialog = true },
                    modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = CustomBlue
                )
            ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (formState.checkupType) {
                            "VITALS" -> Icon(
                                painter = painterResource(id = R.drawable.ic_vitals),
                                contentDescription = "Vitals",
                                modifier = Modifier.size(24.dp)
                            )
                            "SYMPTOMS" -> Icon(
                                painter = painterResource(id = R.drawable.ic_symptoms),
                                contentDescription = "Symptoms",
                                modifier = Modifier.size(24.dp)
                            )
                            "NOTES" -> Icon(
                                painter = painterResource(id = R.drawable.ic_notes),
                                contentDescription = "Notes",
                                modifier = Modifier.size(24.dp)
                            )
                            "TEST_RESULTS" -> Icon(
                                painter = painterResource(id = R.drawable.ic_test_results),
                                contentDescription = "Test Results",
                                modifier = Modifier.size(24.dp)
                            )
                            "ANC_VISIT" -> Icon(
                                painter = painterResource(id = R.drawable.ic_anc_visit),
                                contentDescription = "ANC Visit",
                                modifier = Modifier.size(24.dp)
                            )
                            "VACCINATION" -> Icon(
                                painter = painterResource(id = R.drawable.ic_vaccination),
                                contentDescription = "Vaccination",
                                modifier = Modifier.size(24.dp)
                            )
                            "MEDICAL_REPORT" -> Icon(
                                painter = painterResource(id = R.drawable.ic_medical_report),
                                contentDescription = "Medical Report",
                                modifier = Modifier.size(24.dp)
                            )
                            else -> Icon(
                                Icons.Default.Add,
                                contentDescription = "Select Type",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                Text(formState.checkupType.ifEmpty { "Select Record Type" })
                    }
                }

                if (formState.checkupType.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            when (formState.checkupType) {
                                "VITALS" -> VitalsForm.onActionButtonClick(viewModel, formState)
                                "SYMPTOMS" -> SymptomsForm.onActionButtonClick(viewModel, formState)
                                "NOTES" -> NotesForm.onActionButtonClick(viewModel, formState)
                                "TEST_RESULTS" -> TestResultsForm.onActionButtonClick(viewModel, formState)
                                "ANC_VISIT" -> ANCVisitForm.onActionButtonClick(viewModel, formState)
                                "VACCINATION" -> VaccinationForm.onActionButtonClick(viewModel, formState)
                                "MEDICAL_REPORT" -> MedicalReportForm.onActionButtonClick(viewModel, formState)
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = CustomBlue
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_action),
                            contentDescription = "Form Action",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Dynamic form fields based on record type
            when (formState.checkupType) {
                "VITALS" -> VitalsForm(viewModel, formState)
                "SYMPTOMS" -> SymptomsForm(viewModel, formState)
                "NOTES" -> NotesForm(viewModel, formState)
                "TEST_RESULTS" -> TestResultsForm(viewModel, formState)
                "ANC_VISIT" -> ANCVisitForm(viewModel, formState)
                "VACCINATION" -> VaccinationForm(viewModel, formState)
                "MEDICAL_REPORT" -> MedicalReportForm(viewModel, formState)
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
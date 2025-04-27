package com.littleb01s.ashasakhichat.presentation.screens.riskanalysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.presentation.DetailScaffold
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PregnancyRiskAssessmentScreen(
    onNavigateBack: () -> Unit,
    viewModel: PregnancyRiskViewModel = hiltViewModel()
) {
    var age by remember { mutableStateOf("") }
    var systolicBP by remember { mutableStateOf("") }
    var diastolicBP by remember { mutableStateOf("") }
    var bloodSugar by remember { mutableStateOf("") }
    var bodyTemp by remember { mutableStateOf("") }
    var heartRate by remember { mutableStateOf("") }
    
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Collect state from ViewModel
    val riskLevel by viewModel.riskLevel.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    DetailScaffold(
        title = "Pregnancy Risk Assessment",
        onNavigateBack = onNavigateBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Risk Level Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when(riskLevel?.lowercase()) {
                        "high risk" -> MaterialTheme.colorScheme.errorContainer
                        "mid risk" -> MaterialTheme.colorScheme.tertiaryContainer
                        "low risk" -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Risk Level",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = riskLevel?.replaceFirstChar { it.uppercase() } ?: "Not Assessed",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(riskLevel?.lowercase()) {
                            "high risk" -> MaterialTheme.colorScheme.error
                            "mid risk" -> MaterialTheme.colorScheme.tertiary
                            "low risk" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            if (showError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            if (error != null) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Input Fields
            OutlinedTextField(
                value = age,
                onValueChange = { 
                    if (it.isEmpty() || it.toFloatOrNull() != null) {
                        age = it
                    }
                },
                label = { Text("Age (years)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = showError && age.isEmpty(),
                enabled = !isLoading
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = systolicBP,
                    onValueChange = { 
                        if (it.isEmpty() || it.toFloatOrNull() != null) {
                            systolicBP = it
                        }
                    },
                    label = { Text("Systolic BP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = showError && systolicBP.isEmpty(),
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = diastolicBP,
                    onValueChange = { 
                        if (it.isEmpty() || it.toFloatOrNull() != null) {
                            diastolicBP = it
                        }
                    },
                    label = { Text("Diastolic BP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = showError && diastolicBP.isEmpty(),
                    enabled = !isLoading
                )
            }

            OutlinedTextField(
                value = bloodSugar,
                onValueChange = { 
                    if (it.isEmpty() || it.toFloatOrNull() != null) {
                        bloodSugar = it
                    }
                },
                label = { Text("Blood Sugar (mg/dL)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = showError && bloodSugar.isEmpty(),
                enabled = !isLoading
            )

            OutlinedTextField(
                value = bodyTemp,
                onValueChange = { 
                    if (it.isEmpty() || it.toFloatOrNull() != null) {
                        bodyTemp = it
                    }
                },
                label = { Text("Body Temperature (°C)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = showError && bodyTemp.isEmpty(),
                enabled = !isLoading
            )

            OutlinedTextField(
                value = heartRate,
                onValueChange = { 
                    if (it.isEmpty() || it.toFloatOrNull() != null) {
                        heartRate = it
                    }
                },
                label = { Text("Heart Rate (bpm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = showError && heartRate.isEmpty(),
                enabled = !isLoading
            )

            Button(
                onClick = {
                    if (validateInputs(age, systolicBP, diastolicBP, bloodSugar, bodyTemp, heartRate)) {
                        viewModel.predictRisk(
                            age = age.toFloat(),
                            systolicBP = systolicBP.toFloat(),
                            diastolicBP = diastolicBP.toFloat(),
                            bloodSugar = bloodSugar.toFloat(),
                            bodyTemp = bodyTemp.toFloat(),
                            heartRate = heartRate.toFloat()
                        )
                        showError = false
                    } else {
                        showError = true
                        errorMessage = "Please fill in all fields with valid values"
                    }
                },
                modifier = Modifier

                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Assess Risk", fontSize = 16.sp)
                }
            }
        }
    }
}

private fun validateInputs(
    age: String,
    systolicBP: String,
    diastolicBP: String,
    bloodSugar: String,
    bodyTemp: String,
    heartRate: String
): Boolean {
    return age.isNotEmpty() && 
           systolicBP.isNotEmpty() && 
           diastolicBP.isNotEmpty() && 
           bloodSugar.isNotEmpty() && 
           bodyTemp.isNotEmpty() && 
           heartRate.isNotEmpty()
} 
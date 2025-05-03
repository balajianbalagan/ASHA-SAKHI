package com.littleb01s.ashasakhichat.presentation.screens.riskanalysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.presentation.DetailScaffold

@Composable
fun PregnancyRiskAssessmentScreen(
    patientId: Int? = null,
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
    val observations by viewModel.observations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Create a scroll state that we can programmatically control
    val scrollState = rememberScrollState()

    DetailScaffold(
        title = "Pregnancy Risk Assessment",
        onNavigateBack = onNavigateBack
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Display Patient ID if available
                patientId?.let { id ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "Patient ID: $id",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Risk Level Speedometer
                RiskSpeedometer(
                    riskLevel = riskLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )

                // Observations Cards
                if (observations.isNotEmpty()) {
                    Text(
                        text = "Detailed Observations",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp)
                    )
                    
                    observations.forEach { (parameter, observation) ->
                        // Determine risk level based on observation text
                        val (textColor, cardColor) = when {
                            // Normal/Optimal conditions - subtle gray
                            observation.contains("Normal range") || 
                            observation.contains("Ideal") || 
                            observation.contains("Optimal") -> 
                                Pair(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                )
                            
                            // Mild concerns - yellow/orange warning
                            observation.contains("Pre-hypertension") || 
                            observation.contains("Slight elevation") || 
                            observation.contains("Impaired glucose") || 
                            observation.contains("Mild tachycardia") ||
                            observation.contains("Below normal range") ||
                            (observation.contains("above the") && !observation.contains("above the optimal range")) -> 
                                Pair(
                                    MaterialTheme.colorScheme.tertiary,
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                                )
                            
                            // Serious concerns - bright red
                            observation.contains("High risk") || 
                            observation.contains("Risk of pre-eclampsia") ||
                            observation.contains("Hypoglycemia") || 
                            observation.contains("Hypothermia") || 
                            observation.contains("Bradycardia") ||
                            observation.contains("Gestational diabetes") -> 
                                Pair(
                                    MaterialTheme.colorScheme.error,
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                )
                                
                            // Very high risk - darker red
                            observation.contains("medical attention needed") ||
                            observation.contains("organ damage") ||
                            (observation.contains("mmHg above") && observation.contains("20")) ||
                            (observation.contains("mmol/L above") && observation.contains("4")) -> 
                                Pair(
                                    MaterialTheme.colorScheme.error,
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                )
                            
                            // Default - neutral
                            else -> Pair(
                                MaterialTheme.colorScheme.onSurface,
                                MaterialTheme.colorScheme.surface
                            )
                        }
                        
                        // Extract the value and range information for highlighting
                        val valuePattern = """\((.*?)\)""".toRegex()
                        val rangePattern = """\(([\d.-]+)-([\d.-]+).*?\)""".toRegex()
                        
                        val valueMatch = valuePattern.find(parameter)
                        val rangeMatch = rangePattern.find(observation)
                        
                        val valueText = valueMatch?.groupValues?.get(1) ?: ""
                        val normalRangeText = if (rangeMatch != null) {
                            "Normal range: ${rangeMatch.groupValues[1]}-${rangeMatch.groupValues[2]}"
                        } else ""
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = cardColor
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Parameter name row with value highlight
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = parameter.replace(valuePattern, "").trim(),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    if (valueText.isNotEmpty()) {
                                        Text(
                                            text = " $valueText",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                    }
                                }
                                
                                // Normal range indicator
                                if (normalRangeText.isNotEmpty() && !observation.contains("Normal range")) {
                                    Text(
                                        text = normalRangeText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                                    )
                                }
                                
                                // Observation text
                                Text(
                                    text = observation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textColor
                                )
                            }
                        }
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
                        label = { Text("Systolic BP (mmHg)") },
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
                        label = { Text("Diastolic BP (mmHg)") },
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
                    label = { Text("Blood Sugar (mmol/L)") },
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
                    label = { Text("Body Temperature (°F)") },
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
                            viewModel.assessRisk(
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
                        .height(56.dp)
                        .padding(bottom = 8.dp),
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
                
                // Add extra padding at the bottom to ensure content isn't hidden behind keyboard
                Spacer(modifier = Modifier.height(100.dp))
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
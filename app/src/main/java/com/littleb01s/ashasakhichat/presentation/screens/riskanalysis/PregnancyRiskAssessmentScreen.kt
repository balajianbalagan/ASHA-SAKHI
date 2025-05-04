package com.littleb01s.ashasakhichat.presentation.screens.riskanalysis

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.data.local.entity.CheckupType
import com.littleb01s.ashasakhichat.presentation.DetailScaffold
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar
import java.util.Date
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import com.littleb01s.ashasakhichat.data.local.dao.RiskAnalysisDao
import java.text.SimpleDateFormat
import java.util.Locale

private val CustomBlue = Color(0xFF0174B3)
private val CustomGreen = Color(0xFF1BBF69)
private val CustomOrange = Color(0xFFFF5151)
private val BackgroundColor = Color(0xFFFFF5EE)

@Composable
fun  PregnancyRiskAssessmentScreen(
    patientId: Int? = null,
    onNavigateBack: () -> Unit,
    onNavigateToAddCheckup: (Int) -> Unit,
    viewModel: PregnancyRiskViewModel = hiltViewModel()
) {
    // Initialize vitals check when screen loads
    LaunchedEffect(patientId) {
        patientId?.let { id ->
            Log.d("PregnancyRiskAssessmentScreen", "Checking for recent vitals for patient $id")
            viewModel.checkRecentVitals(id)
            viewModel.loadPatientData(id)
            viewModel.loadLatestRiskAnalysis(id)
        }
    }

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
    val hasRecentVitals by viewModel.hasRecentVitals.collectAsState()
    val formData by viewModel.formData.collectAsState()
    val patientData by viewModel.patientData.collectAsState()
    val latestRiskAnalysis by viewModel.latestRiskAnalysis.collectAsState()
    
    // Date formatter for risk analysis timestamp
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    // Update form fields when formData changes
    LaunchedEffect(formData) {
        age = formData["age"] ?: ""
        systolicBP = formData["systolicBP"] ?: ""
        diastolicBP = formData["diastolicBP"] ?: ""
        bloodSugar = formData["bloodSugar"] ?: ""
        bodyTemp = formData["bodyTemp"] ?: ""
        heartRate = formData["heartRate"] ?: ""
    }

    // Create a scroll state that we can programmatically control
    val scrollState = rememberScrollState()

    DetailScaffold(
        title = "Pregnancy Risk Assessment",
        onNavigateBack = onNavigateBack
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp,vertical = 4.dp)
        ) {
            if (hasRecentVitals) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Patient Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = CustomBlue.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            patientData?.let { patient ->
                                Text(
                                    text = "Name: ${patient.firstName} ${patient.lastName ?: ""}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = CustomBlue
                                )
                                Text(
                                    text = "Age: ${age} years",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CustomBlue
                                )
                                
                                // Risk Analysis Status
                                Text(
                                    text = if (latestRiskAnalysis != null) {
                                        "Risk Analysis last done on: ${dateFormatter.format(latestRiskAnalysis!!.createdAt)}"
                                    } else {
                                        "No Risk Analysis done for this patient"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (latestRiskAnalysis != null) CustomGreen else CustomOrange
                                )
                            }
                        }
                    }

                    // Risk Level Speedometer
                    RiskSpeedometerView(
                        riskLevel = riskLevel,
                        modifier = Modifier
                            .fillMaxWidth(fraction = 0.90f)
                            .padding(bottom = 8.dp)
                            .height(200.dp)
                    )

                    // Observations Cards
                    if (observations.isNotEmpty()) {
                        Text(
                            text = "Detailed Observations",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CustomBlue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        )
                        
                        observations.forEach { (parameter, observation) ->
                            // Determine risk level based on observation text
                            val (textColor, cardColor) = when {
                                // Normal/Optimal conditions - CustomGreen
                                observation.contains("Normal range") || 
                                observation.contains("Ideal") || 
                                observation.contains("Optimal") -> 
                                    Pair(
                                        CustomGreen,
                                        CustomGreen.copy(alpha = 0.1f)
                                    )
                                
                                // Mild concerns - CustomBlue
                                observation.contains("Pre-hypertension") || 
                                observation.contains("Slight elevation") || 
                                observation.contains("Impaired glucose") || 
                                observation.contains("Mild tachycardia") ||
                                observation.contains("Below normal range") ||
                                (observation.contains("above the") && !observation.contains("above the optimal range")) -> 
                                    Pair(
                                        CustomBlue,
                                        CustomBlue.copy(alpha = 0.1f)
                                    )
                                
                                // Serious concerns - CustomOrange
                                observation.contains("High risk") || 
                                observation.contains("Risk of pre-eclampsia") ||
                                observation.contains("Hypoglycemia") || 
                                observation.contains("Hypothermia") || 
                                observation.contains("Bradycardia") ||
                                observation.contains("Gestational diabetes") -> 
                                    Pair(
                                        CustomOrange,
                                        CustomOrange.copy(alpha = 0.1f)
                                    )
                                    
                                // Very high risk - darker orange
                                observation.contains("medical attention needed") ||
                                observation.contains("organ damage") ||
                                (observation.contains("mmHg above") && observation.contains("20")) ||
                                (observation.contains("mmol/L above") && observation.contains("4")) -> 
                                    Pair(
                                        CustomOrange,
                                        CustomOrange.copy(alpha = 0.2f)
                                    )
                                
                                // Default - CustomBlue
                                else -> Pair(
                                    CustomBlue,
                                    CustomBlue.copy(alpha = 0.1f)
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

                    // Input Fields Section
                    var isVitalsExpanded by remember { mutableStateOf(false) }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = CustomGreen.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Vitals Information",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CustomGreen
                                )
                                IconButton(
                                    onClick = { isVitalsExpanded = !isVitalsExpanded }
                                ) {
                                    Icon(
                                        imageVector = if (isVitalsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isVitalsExpanded) "Collapse" else "Expand",
                                        tint = CustomGreen
                                    )
                                }
                            }

                            if (isVitalsExpanded) {
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
                            }
                        }
                    }

                    // Assess Risk Button
                    Button(
                        onClick = {
                            if (validateInputs(age, systolicBP, diastolicBP, bloodSugar, bodyTemp, heartRate)) {
                                patientData?.let {
                                    viewModel.assessRisk(
                                        patientId= it.patientId,
                                        age = age.toFloat(),
                                        systolicBP = systolicBP.toFloat(),
                                        diastolicBP = diastolicBP.toFloat(),
                                        bloodSugar = bloodSugar.toFloat(),
                                        bodyTemp = bodyTemp.toFloat(),
                                        heartRate = heartRate.toFloat()
                                    )
                                }
                                showError = false
                            } else {
                                showError = true
                                errorMessage = "Please fill in all fields with valid values"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CustomGreen
                        )
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
                    
                    // Remove extra padding at bottom
                    Spacer(modifier = Modifier.height(32.dp))
                }
            } else {
                // New UI for when no recent vitals are available
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "No Recent Vitals Data",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "There are no VITALS details recorded for this patient in the past month. Please update with the latest checkup details to perform risk assessment.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            if(patientId != null) {
                                Button(
                                    onClick = { 
                                        Log.d("PregnancyRiskAssessmentScreen", "Trying to nav to add checkupscreen for patient $patientId")
                                        onNavigateToAddCheckup(patientId) 
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Add Vitals Checkup")
                                }
                            } else {
                                Button(
                                    onClick = { onNavigateBack() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("No Patient Details Found! Go Back.")
                                }
                            }
                        }
                    }
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
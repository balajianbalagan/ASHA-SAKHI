package com.littleb01s.ashasakhichat.presentation.screens.forms

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.littleb01s.ashasakhichat.presentation.AddCheckupViewModel
import com.littleb01s.ashasakhichat.presentation.CheckupFormState
import java.util.Date
import kotlin.random.Random

private val CustomBlue = Color(0xFF0174B3)
private val CustomOrange = Color(0xFFFF5151)

// Test information
private val testInfo = mapOf(
    "Hemoglobin" to Triple("g/dL", "12.0-15.5", { Random.nextDouble(11.0, 16.0) }),
    "Blood Sugar" to Triple("mg/dL", "70-140", { Random.nextDouble(65.0, 145.0) }),
    "Urine Test" to Triple("", "Negative", { listOf("Negative", "Trace", "1+", "2+", "3+").random() }),
    "Ultrasound" to Triple("", "Normal", { listOf("Normal", "Mild", "Moderate", "Severe").random() })
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestResultsForm(viewModel: AddCheckupViewModel, state: CheckupFormState) {
    var testName by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var referenceRange by remember { mutableStateOf("") }
    var testDate by remember { mutableStateOf<Date?>(null) }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = testName,
            onValueChange = { 
                testName = it
                viewModel.updateTestName(it)
            },
            label = { Text("Test Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = result,
            onValueChange = { 
                result = it
                viewModel.updateTestResult(it)
            },
            label = { Text("Result") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = unit,
            onValueChange = { 
                unit = it
                viewModel.updateTestUnit(it)
            },
            label = { Text("Unit") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = referenceRange,
            onValueChange = { 
                referenceRange = it
                viewModel.updateTestReferenceRange(it)
            },
            label = { Text("Reference Range") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = testDate?.toString() ?: "",
            onValueChange = { },
            label = { Text("Test Date") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { /* TODO: Implement date picker */ }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select date")
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { 
                notes = it
                viewModel.updateNotes(it)
            },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        if (error != null) {
            Text(error!!, color = CustomOrange)
        }
    }
}

object TestResultsForm {
    fun validateForm(state: CheckupFormState): Boolean {
        if (state.testName.isEmpty()) {
            println("Validation Error: Test name cannot be empty")
            return false
        }
        if (state.testResult.isEmpty()) {
            println("Validation Error: Result cannot be empty")
            return false
        }
        return true
    }

    fun onActionButtonClick(viewModel: AddCheckupViewModel, state: CheckupFormState) {
        // Generate random test data
        val testInfo = listOf(
            mapOf(
                "name" to "Blood Pressure",
                "result" to "${Random.nextInt(90, 140)}/${Random.nextInt(60, 90)}",
                "unit" to "mmHg",
                "range" to "90-140/60-90 mmHg"
            ),
            mapOf(
                "name" to "Blood Sugar",
                "result" to "${Random.nextInt(70, 140)}",
                "unit" to "mg/dL",
                "range" to "70-140 mg/dL"
            ),
            mapOf(
                "name" to "Hemoglobin",
                "result" to "${Random.nextInt(11, 16)}",
                "unit" to "g/dL",
                "range" to "11-16 g/dL"
            )
        ).random()
        
        // Update the form fields
        viewModel.updateTestName(testInfo["name"]!!)
        viewModel.updateTestResult(testInfo["result"]!!)
        viewModel.updateTestUnit(testInfo["unit"]!!)
        viewModel.updateTestReferenceRange(testInfo["range"]!!)
        
        // Format the data for display
        val formattedData = """
            Test Name: ${testInfo["name"]}
            Result: ${testInfo["result"]} ${testInfo["unit"]}
            Reference Range: ${testInfo["range"]}
        """.trimIndent()
        
        viewModel.updateCheckupData(formattedData)
    }
} 
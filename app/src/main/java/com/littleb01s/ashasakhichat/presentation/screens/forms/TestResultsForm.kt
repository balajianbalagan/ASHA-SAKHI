package com.littleb01s.ashasakhichat.presentation.screens.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.littleb01s.ashasakhichat.presentation.AddCheckupViewModel
import com.littleb01s.ashasakhichat.presentation.CheckupFormState
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
    val testNames = listOf("Hemoglobin", "Blood Sugar", "Urine Test", "Ultrasound")
    var selectedTest by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var referenceRange by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedTest ?: "Select Test Name",
                onValueChange = {},
                readOnly = true,
                label = { Text("Test Name") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CustomBlue,
                    unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                testNames.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedTest = option
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        OutlinedTextField(
            value = result,
            onValueChange = { result = it },
            label = { Text("Result") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = unit,
            onValueChange = { unit = it },
            label = { Text("Unit") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = referenceRange,
            onValueChange = { referenceRange = it },
            label = { Text("Reference Range") },
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
    fun onActionButtonClick(viewModel: AddCheckupViewModel, state: CheckupFormState) {
        // Select a random test
        val testName = testInfo.keys.random()
        val (unit, refRange, resultGenerator) = testInfo[testName]!!
        
        // Generate random result
        val result = resultGenerator()
        
        // Format the test result
        val formattedResult = """
            Test: $testName
            Result: $result ${if (unit.isNotEmpty()) unit else ""}
            Reference Range: $refRange
            Status: ${if (result.toString().toDoubleOrNull() != null) {
                val resultValue = result.toString().toDouble()
                val (min, max) = refRange.split("-").map { it.toDouble() }
                when {
                    resultValue < min -> "Below Normal"
                    resultValue > max -> "Above Normal"
                    else -> "Normal"
                }
            } else {
                if (result == "Negative" || result == "Normal") "Normal" else "Abnormal"
            }}
        """.trimIndent()
        
        // Update the form state
        viewModel.updateCheckupData(formattedResult)
        viewModel.updateTestName(testName)
        viewModel.updateTestResult(result.toString())
        viewModel.updateTestUnit(unit)
        viewModel.updateTestReferenceRange(refRange)
    }
} 
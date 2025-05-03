package com.littleb01s.ashasakhichat.presentation.screens.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.littleb01s.ashasakhichat.presentation.AddCheckupViewModel
import com.littleb01s.ashasakhichat.presentation.CheckupFormState
import java.util.Date
import kotlin.random.Random

private val CustomBlue = Color(0xFF0174B3)
private val CustomOrange = Color(0xFFFF5151)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomsForm(viewModel: AddCheckupViewModel, state: CheckupFormState) {
    var symptoms by remember { mutableStateOf(listOf<String>()) }
    var onsetDate by remember { mutableStateOf<Date?>(null) }
    var severity by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Symptoms selection
        val commonSymptoms = listOf(
            "Fever", "Headache", "Cough", "Fatigue", "Nausea",
            "Dizziness", "Shortness of breath", "Chest pain",
            "Abdominal pain", "Muscle pain"
        )
        
        Text("Select Symptoms", style = MaterialTheme.typography.titleMedium)
        commonSymptoms.forEach { symptom ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = symptoms.contains(symptom),
                    onCheckedChange = { checked ->
                        symptoms = if (checked) {
                            symptoms + symptom
                        } else {
                            symptoms - symptom
                        }
                        viewModel.updateSymptoms(symptoms)
                    }
                )
                Text(symptom)
            }
        }

        // Onset date picker
        OutlinedTextField(
            value = onsetDate?.toString() ?: "",
            onValueChange = { },
            label = { Text("Onset Date") },
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

        // Severity selection
        val severityOptions = listOf("Mild", "Moderate", "Severe")
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = severity,
                onValueChange = {},
                readOnly = true,
                label = { Text("Severity") },
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
                severityOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            severity = option
                            viewModel.updateSeverity(option)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        // Notes
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

object SymptomsForm {
    fun validateForm(state: CheckupFormState): Boolean {
        if (state.symptoms.isEmpty()) {
            println("Validation Error: At least one symptom must be selected")
            return false
        }
        return true
    }

    fun onActionButtonClick(viewModel: AddCheckupViewModel, state: CheckupFormState) {
        // Generate random symptoms (2-4 symptoms)
        val commonSymptoms = listOf(
            "Fever", "Headache", "Cough", "Fatigue", "Nausea",
            "Dizziness", "Shortness of breath", "Chest pain",
            "Abdominal pain", "Muscle pain"
        )
        val numSymptoms = Random.nextInt(2, 5)
        val selectedSymptoms = commonSymptoms.shuffled().take(numSymptoms)
        
        // Generate random severity
        val severity = listOf("Mild", "Moderate", "Severe").random()
        
        // Update the form fields
        viewModel.updateSymptoms(selectedSymptoms)
        viewModel.updateSeverity(severity)
        
        // Format the data for display
        val formattedData = """
            Symptoms:
            ${selectedSymptoms.joinToString("\n• ", "• ")}
            
            Severity: $severity
        """.trimIndent()
        
        viewModel.updateCheckupData(formattedData)
    }
} 
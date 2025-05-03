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

// List of common symptoms
private val commonSymptoms = listOf(
    "Fever", "Headache", "Cough", "Sore throat", "Fatigue",
    "Body aches", "Nausea", "Vomiting", "Diarrhea", "Shortness of breath",
    "Chest pain", "Loss of taste", "Loss of smell", "Runny nose",
    "Muscle pain", "Joint pain", "Dizziness", "Rash", "Itching",
    "Swelling", "Abdominal pain", "Constipation", "Heartburn",
    "Insomnia", "Anxiety", "Depression", "Memory problems",
    "Vision problems", "Hearing problems", "Weight loss"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomsForm(viewModel: AddCheckupViewModel, state: CheckupFormState) {
    val severityOptions = listOf("Mild", "Moderate", "Severe")
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.checkupData,
            onValueChange = {
                viewModel.updateCheckupData(it)
                if (it.isNotBlank()) error = null
            },
            label = { Text("Symptoms (comma separated)") },
            modifier = Modifier.fillMaxWidth(),
            isError = error != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f),
                errorBorderColor = CustomOrange
            )
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = state.severity.ifEmpty { "Select Severity" },
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
                            viewModel.updateSeverity(option)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        if (error != null) {
            Text(error!!, color = CustomOrange)
        }
    }
}

object SymptomsForm {
    fun onActionButtonClick(viewModel: AddCheckupViewModel, state: CheckupFormState) {
        // Validate that symptoms text is not empty
        if (state.checkupData.isBlank()) {
            // Set error in the UI (handled by the composable)
            // You may want to use a shared error state or a callback for better UX
            return
        }
        // Generate random number of symptoms (between 2 and 5)
        val numSymptoms = Random.nextInt(2, 6)
        val selectedSymptoms = commonSymptoms.shuffled().take(numSymptoms)
        val severity = listOf("Mild", "Moderate", "Severe").random()
        val formattedSymptoms = selectedSymptoms.joinToString(", ")
        viewModel.updateCheckupData(formattedSymptoms)
        viewModel.updateSeverity(severity)
    }
} 
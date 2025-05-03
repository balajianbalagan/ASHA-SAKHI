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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val CustomBlue = Color(0xFF0174B3)
private val CustomOrange = Color(0xFFFF5151)

// Vaccine information
private val vaccineInfo = mapOf(
    "TT" to mapOf(
        "doses" to 5,
        "schedule" to listOf("First dose", "Second dose", "Third dose", "Fourth dose", "Fifth dose"),
        "interval" to listOf(4, 4, 6, 6, 12) // weeks between doses
    ),
    "Hepatitis B" to mapOf(
        "doses" to 3,
        "schedule" to listOf("First dose", "Second dose", "Third dose"),
        "interval" to listOf(4, 8, 0) // weeks between doses
    ),
    "COVID-19" to mapOf(
        "doses" to 2,
        "schedule" to listOf("First dose", "Second dose"),
        "interval" to listOf(4, 0) // weeks between doses
    ),
    "Influenza" to mapOf(
        "doses" to 1,
        "schedule" to listOf("Annual dose"),
        "interval" to listOf(52) // weeks between doses
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationForm(viewModel: AddCheckupViewModel, state: CheckupFormState) {
    val vaccineNames = listOf("TT", "Hepatitis B", "COVID-19", "Influenza")
    var selectedVaccine by remember { mutableStateOf(state.checkupData) }
    var expanded by remember { mutableStateOf(false) }
    var doseNumber by remember { mutableStateOf("") }
    var batchNumber by remember { mutableStateOf("") }
    var administeredBy by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedVaccine ?: "Select Vaccine Name",
                onValueChange = {},
                readOnly = true,
                label = { Text("Vaccine Name") },
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
                vaccineNames.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedVaccine = option
                            viewModel.updateCheckupData(option)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        OutlinedTextField(
            value = doseNumber,
            onValueChange = { doseNumber = it },
            label = { Text("Dose Number") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = batchNumber,
            onValueChange = { batchNumber = it },
            label = { Text("Batch Number") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = administeredBy,
            onValueChange = { administeredBy = it },
            label = { Text("Administered By") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
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

object VaccinationForm {
    fun onActionButtonClick(viewModel: AddCheckupViewModel, state: CheckupFormState) {
        // Select a random vaccine
        val vaccineName = vaccineInfo.keys.random()
        val vaccineDetails = vaccineInfo[vaccineName]!!
        
        // Generate random dose number (1 to max doses)
        val maxDoses = vaccineDetails["doses"] as Int
        val doseNumber = Random.nextInt(1, maxDoses + 1)
        
        // Generate random batch number
        val batchNumber = "B${Random.nextInt(1000, 9999)}"
        
        // Select random healthcare provider
        val administeredBy = listOf("Dr. Smith", "Nurse Johnson", "Dr. Patel", "Dr. Kumar").random()
        
        // Calculate next dose date
        val intervals = vaccineDetails["interval"] as List<Int>
        val nextDoseWeeks = intervals[doseNumber - 1]
        val nextDoseDate = if (nextDoseWeeks > 0) {
            LocalDate.now().plusWeeks(nextDoseWeeks.toLong())
                .format(DateTimeFormatter.ISO_LOCAL_DATE)
        } else {
            "No further doses required"
        }
        
        // Format the vaccination data
        val formattedData = """
            Vaccine: $vaccineName
            Dose: ${vaccineDetails["schedule"] as List<String>}[doseNumber - 1]
            Batch Number: $batchNumber
            Administered By: $administeredBy
            Date: ${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)}
            Next Dose: $nextDoseDate
        """.trimIndent()
        
        // Update the form state
        viewModel.updateCheckupData(formattedData)
        viewModel.updateVaccineName(vaccineName)
        viewModel.updateDoseNumber(doseNumber.toString())
        viewModel.updateBatchNumber(batchNumber)
        viewModel.updateAdministeredBy(administeredBy)
        viewModel.updateNextDoseDate(nextDoseDate)
    }
} 
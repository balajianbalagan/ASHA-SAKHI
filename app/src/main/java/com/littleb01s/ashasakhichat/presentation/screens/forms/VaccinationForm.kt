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
import kotlin.random.Random
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

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
    var vaccineName by remember { mutableStateOf("") }
    var doseNumber by remember { mutableStateOf("") }
    var administrationDate by remember { mutableStateOf<Date?>(null) }
    var batchNumber by remember { mutableStateOf("") }
    var administeredBy by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = vaccineName,
            onValueChange = { 
                vaccineName = it
                viewModel.updateVaccineName(it)
            },
            label = { Text("Vaccine Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = doseNumber,
            onValueChange = { 
                doseNumber = it
                viewModel.updateDoseNumber(it)
            },
            label = { Text("Dose Number") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = administrationDate?.toString() ?: "",
            onValueChange = { },
            label = { Text("Administration Date") },
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
            value = batchNumber,
            onValueChange = { 
                batchNumber = it
                viewModel.updateBatchNumber(it)
            },
            label = { Text("Batch Number") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = administeredBy,
            onValueChange = { 
                administeredBy = it
                viewModel.updateAdministeredBy(it)
            },
            label = { Text("Administered By") },
            modifier = Modifier.fillMaxWidth(),
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

object VaccinationForm {
    fun validateForm(state: CheckupFormState): Boolean {
        if (state.vaccineName.isEmpty()) {
            println("Validation Error: Vaccine name cannot be empty")
            return false
        }
        if (state.doseNumber.isEmpty()) {
            println("Validation Error: Dose number cannot be empty")
            return false
        }
        return true
    }

    fun onActionButtonClick(viewModel: AddCheckupViewModel, state: CheckupFormState) {
        // Generate random vaccination data
        val vaccineInfo = listOf(
            mapOf(
                "name" to "COVID-19 Vaccine",
                "dose" to "1",
                "batch" to "BATCH-${Random.nextInt(1000, 9999)}",
                "administeredBy" to "Dr. Smith"
            ),
            mapOf(
                "name" to "Tetanus Toxoid",
                "dose" to "2",
                "batch" to "BATCH-${Random.nextInt(1000, 9999)}",
                "administeredBy" to "Nurse Johnson"
            ),
            mapOf(
                "name" to "Hepatitis B",
                "dose" to "3",
                "batch" to "BATCH-${Random.nextInt(1000, 9999)}",
                "administeredBy" to "Dr. Patel"
            )
        ).random()
        
        // Update the form fields
        viewModel.updateVaccineName(vaccineInfo["name"]!!)
        viewModel.updateDoseNumber(vaccineInfo["dose"]!!)
        viewModel.updateBatchNumber(vaccineInfo["batch"]!!)
        viewModel.updateAdministeredBy(vaccineInfo["administeredBy"]!!)
        
        // Format the data for display
        val formattedData = """
            Vaccine Name: ${vaccineInfo["name"]}
            Dose Number: ${vaccineInfo["dose"]}
            Batch Number: ${vaccineInfo["batch"]}
            Administered By: ${vaccineInfo["administeredBy"]}
        """.trimIndent()
        
        viewModel.updateCheckupData(formattedData)
    }
} 
package com.littleb01s.ashasakhichat.presentation.screens.forms

import android.util.Log
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

// Common findings and interventions
private val commonFindings = listOf(
    "Normal fetal heart rate",
    "Adequate weight gain",
    "Normal blood pressure",
    "No signs of anemia",
    "Normal urine test results",
    "Fetal movement normal",
    "Fundal height appropriate for gestational age",
    "No signs of preeclampsia",
    "Normal blood sugar levels",
    "Adequate iron levels"
)

private val commonInterventions = listOf(
    "Continue current medication",
    "Increase iron supplements",
    "Schedule next ultrasound",
    "Monitor blood pressure",
    "Follow up in 2 weeks",
    "Start calcium supplements",
    "Begin folic acid supplements",
    "Schedule glucose tolerance test",
    "Start protein-rich diet",
    "Begin light exercise routine"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ANCVisitForm(viewModel: AddCheckupViewModel, state: CheckupFormState) {
    var visitNumber by remember { mutableStateOf("") }
    var pregnancyStage by remember { mutableStateOf(state.pregnancyStage ?: "") }
    var findings by remember { mutableStateOf("") }
    var interventions by remember { mutableStateOf("") }
    var nextVisitDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = visitNumber,
            onValueChange = { visitNumber = it },
            label = { Text("Visit Number") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = pregnancyStage,
            onValueChange = {
                pregnancyStage = it
                viewModel.updatePregnancyStage(it)
            },
            label = { Text("Pregnancy Stage") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = findings,
            onValueChange = { findings = it },
            label = { Text("Findings") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = interventions,
            onValueChange = { interventions = it },
            label = { Text("Interventions") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = nextVisitDate,
            onValueChange = { nextVisitDate = it },
            label = { Text("Next Visit Date (yyyy-MM-dd)") },
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

object ANCVisitForm {
    private val commonComplaints = listOf(
        "Nausea and vomiting",
        "Back pain",
        "Fatigue",
        "Swelling in feet",
        "Heartburn",
        "Frequent urination",
        "Shortness of breath",
        "Headaches",
        "Leg cramps",
        "Constipation"
    )

    private val commonAdvice = listOf(
        "Take prescribed iron supplements",
        "Maintain regular exercise routine",
        "Follow balanced diet",
        "Get adequate rest",
        "Stay hydrated",
        "Monitor blood pressure",
        "Attend all scheduled appointments",
        "Practice relaxation techniques",
        "Wear comfortable clothing",
        "Avoid strenuous activities"
    )

    fun validateForm(state: CheckupFormState): Boolean {
        if (state.visitNumber.isEmpty()) {
            println("Validation Error: Visit number cannot be empty")
            return false
        }
        if (state.findings.isEmpty()) {
            println("Validation Error: Findings cannot be empty")
            return false
        }
        if (state.interventions.isEmpty()) {
            println("Validation Error: Interventions cannot be empty")
            return false
        }
        return true
    }

    fun onActionButtonClick(viewModel: AddCheckupViewModel, state: CheckupFormState) {
        // Generate random visit number (between 1 and 8)
        val visitNumber = Random.nextInt(1, 9).toString()
        
        // Generate random number of complaints (between 2 and 4)
        val numComplaints = Random.nextInt(2, 5)
        val selectedComplaints = commonComplaints.shuffled().take(numComplaints)
        
        // Generate random number of advice items (between 2 and 4)
        val numAdvice = Random.nextInt(2, 5)
        val selectedAdvice = commonAdvice.shuffled().take(numAdvice)
        
        // Format the complaints and advice
        val formattedComplaints = selectedComplaints.joinToString(", ")
        val formattedAdvice = selectedAdvice.joinToString(", ")
        
        // Update the form fields
        viewModel.updateVisitNumber(visitNumber)
        viewModel.updateFindings(formattedComplaints)
        viewModel.updateInterventions(formattedAdvice)
        
        // Format the data for display
        val formattedData = """
            Visit Number: $visitNumber
            Findings: $formattedComplaints
            Interventions: $formattedAdvice
        """.trimIndent()
        
        viewModel.updateCheckupData(formattedData)
    }
} 
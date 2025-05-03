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
    fun onActionButtonClick(viewModel: AddCheckupViewModel, state: CheckupFormState) {
        // Generate random visit number (1-8)
        val visitNumber = Random.nextInt(1, 9)
        
        // Generate random pregnancy stage (in weeks)
        val pregnancyStage = Random.nextInt(8, 40)
        
        // Generate random findings (2-4)
        val numFindings = Random.nextInt(2, 5)
        val selectedFindings = commonFindings.shuffled().take(numFindings)
        
        // Generate random interventions (1-3)
        val numInterventions = Random.nextInt(1, 4)
        val selectedInterventions = commonInterventions.shuffled().take(numInterventions)
        
        // Calculate next visit date (2-4 weeks from today)
        val nextVisitWeeks = Random.nextInt(2, 5)
        val nextVisitDate = LocalDate.now().plusWeeks(nextVisitWeeks.toLong())
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
        
        // Format the ANC visit data
        val formattedData = """
            Visit Number: $visitNumber
            Pregnancy Stage: $pregnancyStage weeks
            
            Findings:
            ${selectedFindings.joinToString("\n• ")}
            
            Interventions:
            ${selectedInterventions.joinToString("\n• ")}
            
            Next Visit: $nextVisitDate
        """.trimIndent()
        
        // Update the form state
        viewModel.updateCheckupData(formattedData)
        viewModel.updatePregnancyStage(pregnancyStage.toString())
        viewModel.updateVisitNumber(visitNumber.toString())
        viewModel.updateFindings(selectedFindings.joinToString("\n"))
        viewModel.updateInterventions(selectedInterventions.joinToString("\n"))
        viewModel.updateNextVisitDate(nextVisitDate)
    }
} 
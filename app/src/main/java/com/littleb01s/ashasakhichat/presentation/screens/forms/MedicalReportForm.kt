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

// Report information
private val reportInfo = mapOf(
    "Ultrasound" to mapOf(
        "summary" to listOf(
            "Normal fetal development observed",
            "Adequate amniotic fluid levels",
            "Normal placental position",
            "Fetal measurements within normal range",
            "No structural abnormalities detected"
        ),
        "fileUrl" to "ultrasound_${LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)}.pdf"
    ),
    "Blood Test" to mapOf(
        "summary" to listOf(
            "Complete blood count within normal range",
            "Normal electrolyte levels",
            "Liver function tests normal",
            "Kidney function tests normal",
            "Blood sugar levels normal"
        ),
        "fileUrl" to "blood_test_${LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)}.pdf"
    ),
    "X-Ray" to mapOf(
        "summary" to listOf(
            "No fractures or dislocations observed",
            "Normal bone density",
            "No signs of infection",
            "Normal joint spaces",
            "No abnormal masses detected"
        ),
        "fileUrl" to "xray_${LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)}.pdf"
    ),
    "ECG" to mapOf(
        "summary" to listOf(
            "Normal sinus rhythm",
            "No signs of ischemia",
            "Normal PR interval",
            "Normal QRS complex",
            "No arrhythmias detected"
        ),
        "fileUrl" to "ecg_${LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)}.pdf"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalReportForm(viewModel: AddCheckupViewModel, state: CheckupFormState) {
    val reportTypes = listOf("Ultrasound", "Blood Test", "X-Ray", "ECG")
    var selectedReportType by remember { mutableStateOf(state.checkupData) }
    var expanded by remember { mutableStateOf(false) }
    var summary by remember { mutableStateOf("") }
    var fileUrl by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedReportType ?: "Select Report Type",
                onValueChange = {},
                readOnly = true,
                label = { Text("Report Type") },
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
                reportTypes.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedReportType = option
                            viewModel.updateCheckupData(option)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        OutlinedTextField(
            value = summary,
            onValueChange = { summary = it },
            label = { Text("Summary") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = fileUrl,
            onValueChange = { fileUrl = it },
            label = { Text("File URL") },
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

object MedicalReportForm {
    fun onActionButtonClick(viewModel: AddCheckupViewModel, state: CheckupFormState) {
        // Select a random report type
        val reportType = reportInfo.keys.random()
        val reportDetails = reportInfo[reportType]!!
        
        // Generate random number of summary points (2-4)
        val numPoints = Random.nextInt(2, 5)
        val selectedSummary = (reportDetails["summary"] as List<String>).shuffled().take(numPoints)
        
        // Generate random notes
        val notes = listOf(
            "Follow-up recommended in 3 months",
            "No immediate intervention required",
            "Results discussed with patient",
            "Patient advised to maintain current treatment",
            "Further tests may be required"
        ).random()
        
        // Format the medical report
        val formattedReport = """
            Report Type: $reportType
            Date: ${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)}
            
            Summary:
            ${selectedSummary.joinToString("\n• ")}
            
            File URL: ${reportDetails["fileUrl"]}
            
            Notes: $notes
        """.trimIndent()
        
        // Update the form state
        viewModel.updateCheckupData(formattedReport)
        viewModel.updateReportType(reportType)
        viewModel.updateSummary(selectedSummary.joinToString("\n"))
        viewModel.updateFileUrl(reportDetails["fileUrl"] as String)
        viewModel.updateNotes(notes)
    }
} 
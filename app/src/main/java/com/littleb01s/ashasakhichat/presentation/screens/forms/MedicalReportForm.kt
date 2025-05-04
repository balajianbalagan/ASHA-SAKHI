package com.littleb01s.ashasakhichat.presentation.screens.forms

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    var selectedReportType by remember { mutableStateOf(state.reportType) }
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
                            viewModel.updateReportType(option)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        OutlinedTextField(
            value = summary,
            onValueChange = { 
                summary = it
                viewModel.updateSummary(it)
            },
            label = { Text("Summary") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        // File picker UI (replace fileUrl text field)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                // TODO: Launch file picker intent
                // On file selected: check size, copy to app private storage, update fileUrl
            }) {
                Text("Select File")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (fileUrl.isNotBlank()) fileUrl.substringAfterLast('/') else "No file selected",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        // Max file size: 5MB (enforced in file picker logic)
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

object MedicalReportForm {
    fun validateForm(state: CheckupFormState): Boolean {
        if (state.reportType.isEmpty()) {
            println("Validation Error: Report type cannot be empty")
            return false
        }
        if (state.summary.isEmpty()) {
            println("Validation Error: Summary cannot be empty")
            return false
        }
        return true
    }

    fun onActionButtonClick(viewModel: AddCheckupViewModel, state: CheckupFormState) {
        // Select a random report type
        val reportTypes = listOf("Ultrasound", "Blood Test", "X-Ray", "ECG")
        val reportType = reportTypes.random()
        
        // Get corresponding summary and file URL
        val reportInfo = reportInfo[reportType]!!
        val summary = (reportInfo["summary"] as List<String>).random()
        val fileUrl = reportInfo["fileUrl"]!!
        
        // Update the form fields
        viewModel.updateReportType(reportType)
        viewModel.updateSummary(summary)
        viewModel.updateFileUrl(fileUrl.toString())
        
        // Format the data for display
        val formattedData = """
            Report Type: $reportType
            Summary: $summary
            File URL: $fileUrl
        """.trimIndent()
        
        viewModel.updateCheckupData(formattedData)
    }
} 
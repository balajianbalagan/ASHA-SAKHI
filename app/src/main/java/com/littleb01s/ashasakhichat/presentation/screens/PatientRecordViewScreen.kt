package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.gson.Gson
import com.littleb01s.ashasakhichat.data.local.entity.*
import com.littleb01s.ashasakhichat.presentation.MedicalHistoryViewModel
import com.littleb01s.ashasakhichat.presentation.DetailScaffold
import java.text.SimpleDateFormat
import java.util.*
import com.littleb01s.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRecordViewScreen(
    checkupId: Int,
    onNavigateBack: () -> Unit,
    viewModel: MedicalHistoryViewModel = hiltViewModel()
) {
    var checkup by remember { mutableStateOf<Checkup?>(null) }
    LaunchedEffect(checkupId) {
        checkup = viewModel.getCheckupById(checkupId)
    }

    DetailScaffold(
        title = "Patient Record View",
        onNavigateBack = onNavigateBack
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (checkup == null) {
                CircularProgressIndicator()
            } else {
                val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
                val gson = remember { Gson() }
                val type = checkup!!.checkupType ?: ""
                val data = checkup!!.checkupData ?: ""
                val cardModifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                when (type) {
                    "SYMPTOMS" -> {
                        val record = try { gson.fromJson(data, SymptomsRecord::class.java) } catch (_: Exception) { null }
                        RecordCard(
                            iconRes = R.drawable.ic_symptoms,
                            title = "Symptoms",
                            content = {
                                if (record != null) {
                                    Text("Symptoms: ${record.symptoms.joinToString(", ")}", maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    record.severity?.let { Text("Severity: $it") }
                                    record.onsetDate?.let { Text("Onset: ${dateFormat.format(it)}") }
                                    record.notes?.let { Text("Notes: $it") }
                                } else {
                                    Text("Could not parse symptoms record.")
                                }
                            },
                            modifier = cardModifier
                        )
                    }
                    "NOTES" -> {
                        val record = try { gson.fromJson(data, NotesRecord::class.java) } catch (_: Exception) { null }
                        RecordCard(
                            iconRes = R.drawable.ic_notes,
                            title = "Notes",
                            content = {
                                if (record != null) {
                                    Text("Note: ${record.note}", maxLines = 3, overflow = TextOverflow.Ellipsis)
                                    record.author?.let { Text("Author: $it") }
                                    Text("Timestamp: ${dateFormat.format(record.timestamp)}")
                                } else {
                                    Text("Could not parse notes record.")
                                }
                            },
                            modifier = cardModifier
                        )
                    }
                    "TEST_RESULTS" -> {
                        val record = try { gson.fromJson(data, TestResultsRecord::class.java) } catch (_: Exception) { null }
                        RecordCard(
                            iconRes = R.drawable.ic_test_results,
                            title = "Test Results",
                            content = {
                                if (record != null) {
                                    Text("Test: ${record.testName}")
                                    Text("Result: ${record.result} ${record.unit ?: ""}")
                                    record.referenceRange?.let { Text("Reference: $it") }
                                    record.testDate?.let { Text("Test Date: ${dateFormat.format(it)}") }
                                    record.notes?.let { Text("Notes: $it") }
                                } else {
                                    Text("Could not parse test results record.")
                                }
                            },
                            modifier = cardModifier
                        )
                    }
                    "ANC_VISIT" -> {
                        val record = try { gson.fromJson(data, ANCVisitRecord::class.java) } catch (_: Exception) { null }
                        RecordCard(
                            iconRes = R.drawable.ic_anc_visit,
                            title = "ANC Visit",
                            content = {
                                if (record != null) {
                                    record.visitNumber?.let { Text("Visit #: $it") }
                                    record.pregnancyStage?.let { Text("Pregnancy Stage: $it") }
                                    record.findings?.let { Text("Findings: $it", maxLines = 2, overflow = TextOverflow.Ellipsis) }
                                    record.interventions?.let { Text("Interventions: $it", maxLines = 2, overflow = TextOverflow.Ellipsis) }
                                    record.visitDate.let { Text("Visit Date: ${dateFormat.format(it)}") }
                                    record.nextVisitDate?.let { Text("Next Visit: ${dateFormat.format(it)}") }
                                    record.notes?.let { Text("Notes: $it") }
                                } else {
                                    Text("Could not parse ANC visit record.")
                                }
                            },
                            modifier = cardModifier
                        )
                    }
                    "VACCINATION" -> {
                        val record = try { gson.fromJson(data, VaccinationRecord::class.java) } catch (_: Exception) { null }
                        RecordCard(
                            iconRes = R.drawable.ic_vaccination,
                            title = "Vaccination",
                            content = {
                                if (record != null) {
                                    Text("Vaccine: ${record.vaccineName}")
                                    record.doseNumber?.let { Text("Dose #: $it") }
                                    record.batchNumber?.let { Text("Batch: $it") }
                                    record.administeredBy?.let { Text("Administered By: $it") }
                                    Text("Date: ${dateFormat.format(record.administrationDate)}")
                                    record.notes?.let { Text("Notes: $it") }
                                } else {
                                    Text("Could not parse vaccination record.")
                                }
                            },
                            modifier = cardModifier
                        )
                    }
                    "MEDICAL_REPORT" -> {
                        val record = try { gson.fromJson(data, MedicalReportRecord::class.java) } catch (_: Exception) { null }
                        RecordCard(
                            iconRes = R.drawable.ic_medical_report,
                            title = "Medical Report",
                            content = {
                                if (record != null) {
                                    Text("Type: ${record.reportType}")
                                    Text("Date: ${dateFormat.format(record.reportDate)}")
                                    record.summary?.let { Text("Summary: $it", maxLines = 3, overflow = TextOverflow.Ellipsis) }
                                    record.fileUrl?.let { Text("File: $it") }
                                    record.notes?.let { Text("Notes: $it") }
                                } else {
                                    Text("Could not parse medical report record.")
                                }
                            },
                            modifier = cardModifier
                        )
                    }
                    else -> {
                        RecordCard(
                            iconRes = R.drawable.ic_placeholder,
                            title = "Unknown Record Type",
                            content = {
                                Text("Raw Data: $data", maxLines = 5, overflow = TextOverflow.Ellipsis)
                            },
                            modifier = cardModifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecordCard(
    iconRes: Int,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F6FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Image(painter = painterResource(id = iconRes), contentDescription = title, modifier = Modifier.size(36.dp), colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF0174B3)))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0174B3))
                Spacer(modifier = Modifier.height(4.dp))
                content()
            }
        }
    }
} 
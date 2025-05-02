package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.presentation.DetailScaffold
import com.littleb01s.ashasakhichat.presentation.MedicalHistoryViewModel
import com.littleb01s.ashasakhichat.data.local.entity.Checkup

@Composable
fun MedicalHistoryScreen(
    patientId: Int,
    onNavigateBack: () -> Unit,
    onAddCheckup: (Int) -> Unit,
    viewModel: MedicalHistoryViewModel = hiltViewModel()
) {
    val checkups by viewModel.checkups.collectAsState()
    val patientInfo by viewModel.patientInfo.collectAsState()

    LaunchedEffect(patientId) {
        viewModel.loadCheckups(patientId)
        viewModel.loadPatientInfo(patientId)
    }

    DetailScaffold(
        title = "Medical History",
        onNavigateBack = onNavigateBack
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Patient Info Card
                item {
                    patientInfo?.let { info ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF98DBC2)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "${info.name}'s\nMedical History",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color(0xFF1B365D)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Age: ${info.age}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF1B365D)
                                )
                                Text(
                                    text = "Trimester: ${info.trimester}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF1B365D)
                                )
                                Text(
                                    text = "EDD: ${info.edd}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF1B365D)
                                )
                            }
                        }
                    }
                }

                // Checkup History Cards
                items(checkups) { checkup ->
                    CheckupCard(checkup = checkup)
                }
            }

            // FAB for adding new checkup
            FloatingActionButton(
                onClick = { onAddCheckup(patientId) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = Color(0xFFFF6B6B)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Checkup")
            }
        }
    }
}

@Composable
fun CheckupCard(checkup: Checkup) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            when (checkup.checkupType) {
                "SYMPTOMS" -> SymptomsSection(checkup)
                "VITALS" -> VitalsSection(checkup)
                "NOTES" -> NotesSection(checkup)
                "TEST_RESULTS" -> TestResultsSection(checkup)
                "ANC_VISIT" -> ANCVisitSection(checkup)
                else -> DefaultSection(checkup)
            }
        }
    }
}

@Composable
fun SymptomsSection(checkup: Checkup) {
    Column {
        Text(
            text = "Symptoms Report",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF4A3B8B)
        )
        Text(
            text = checkup.checkupData ?: "No symptoms recorded",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF4A3B8B)
        )
        Text(
            text = "Today",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4A3B8B),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun VitalsSection(checkup: Checkup) {
    Column {
        Text(
            text = "Vitals & Tests",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF4A3B8B)
        )
        Text(
            text = "BP: ${checkup.bloodPressure} mmHg",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF4A3B8B)
        )
        Text(
            text = "Weight: ${checkup.weight} kg",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF4A3B8B)
        )
        if (checkup.sugarLevel != null) {
            Text(
                text = "Blood Sugar (RBS): ${checkup.sugarLevel}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF4A3B8B)
            )
        }
        Text(
            text = "Yesterday",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4A3B8B),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun NotesSection(checkup: Checkup) {
    Column {
        Text(
            text = "Notes by ASHA",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF4A3B8B)
        )
        Text(
            text = checkup.checkupData ?: "No notes recorded",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF4A3B8B)
        )
        Text(
            text = "Apr 2nd, 2025",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4A3B8B),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TestResultsSection(checkup: Checkup) {
    Column {
        Text(
            text = "Medical Test Results",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF4A3B8B)
        )
        Text(
            text = checkup.checkupData ?: "No test results recorded",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF4A3B8B)
        )
        Text(
            text = "Mar 21st, 2025",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4A3B8B),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ANCVisitSection(checkup: Checkup) {
    Column {
        Text(
            text = "ANC Visit Summary",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF4A3B8B)
        )
        Text(
            text = checkup.checkupData ?: "No ANC visit data recorded",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF4A3B8B)
        )
        Text(
            text = "Mar 2nd, 2025",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4A3B8B),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DefaultSection(checkup: Checkup) {
    Column {
        Text(
            text = "Checkup Record",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF4A3B8B)
        )
        Text(
            text = checkup.checkupData ?: "No data recorded",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF4A3B8B)
        )
    }
} 
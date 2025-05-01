package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.data.local.entity.Patient
import com.littleb01s.ashasakhichat.presentation.PatientsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsScreen(
    patientId: Int,
    onNavigateBack: () -> Unit,
    viewModel: PatientsViewModel = hiltViewModel()
) {
    var isEditMode by remember { mutableStateOf(false) }
    val patient by viewModel.getPatientDetails(patientId).collectAsState(initial = null)
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Section expansion states
    var isBasicInfoExpanded by remember { mutableStateOf(true) }
    var isContactInfoExpanded by remember { mutableStateOf(false) }
    var isMedicalInfoExpanded by remember { mutableStateOf(false) }
    var isSocialInfoExpanded by remember { mutableStateOf(false) }
    var isPregnancyInfoExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isEditMode) {
                                // TODO: Handle save
                            }
                            isEditMode = !isEditMode
                        }
                    ) {
                        Icon(
                            if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditMode) "Save" else "Edit"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (patient == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Basic Info Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${patient!!.firstName} ${patient!!.lastName ?: ""}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ID: ${patient!!.patientId}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (patient!!.needsUpload) {
                            Text(
                                text = "Pending Sync",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Basic Information Section
                ExpandableSection(
                    title = "Basic Information",
                    isExpanded = isBasicInfoExpanded,
                    onExpandChange = { isBasicInfoExpanded = it }
                ) {
                    EditableField(
                        label = "First Name",
                        value = patient!!.firstName,
                        isEditMode = isEditMode
                    )
                    EditableField(
                        label = "Last Name",
                        value = patient!!.lastName ?: "-",
                        isEditMode = isEditMode
                    )
                    EditableField(
                        label = "Date of Birth",
                        value = dateFormat.format(patient!!.dateOfBirth),
                        isEditMode = isEditMode
                    )
                    EditableField(
                        label = "Blood Group",
                        value = patient!!.bloodGroup ?: "-",
                        isEditMode = isEditMode
                    )
                }

                // Contact Information Section
                ExpandableSection(
                    title = "Contact Information",
                    isExpanded = isContactInfoExpanded,
                    onExpandChange = { isContactInfoExpanded = it }
                ) {
                    EditableField(
                        label = "Mobile Number",
                        value = patient!!.mobileNumber,
                        isEditMode = isEditMode
                    )
                    EditableField(
                        label = "State",
                        value = patient!!.state ?: "-",
                        isEditMode = isEditMode
                    )
                    EditableField(
                        label = "City",
                        value = patient!!.city ?: "-",
                        isEditMode = isEditMode
                    )
                    EditableField(
                        label = "Language Preference",
                        value = patient!!.languagePreference ?: "-",
                        isEditMode = isEditMode
                    )
                }

                // Medical Information Section
                ExpandableSection(
                    title = "Medical Information",
                    isExpanded = isMedicalInfoExpanded,
                    onExpandChange = { isMedicalInfoExpanded = it }
                ) {
                    EditableField(
                        label = "Previous Illness",
                        value = patient!!.previousIllness ?: "None",
                        isEditMode = isEditMode
                    )
                }

                // Social Information Section
                ExpandableSection(
                    title = "Social Information",
                    isExpanded = isSocialInfoExpanded,
                    onExpandChange = { isSocialInfoExpanded = it }
                ) {
                    EditableField(
                        label = "Education",
                        value = patient!!.education ?: "-",
                        isEditMode = isEditMode
                    )
                    EditableField(
                        label = "Employment Status",
                        value = patient!!.employmentStatus ?: "-",
                        isEditMode = isEditMode
                    )
                    EditableField(
                        label = "Religion",
                        value = patient!!.religion ?: "-",
                        isEditMode = isEditMode
                    )
                    EditableField(
                        label = "Caste",
                        value = patient!!.caste ?: "-",
                        isEditMode = isEditMode
                    )
                }

                // Pregnancy Information Section
                ExpandableSection(
                    title = "Pregnancy Information",
                    isExpanded = isPregnancyInfoExpanded,
                    onExpandChange = { isPregnancyInfoExpanded = it }
                ) {
                    EditableField(
                        label = "LMP",
                        value = patient!!.lmp?.let { dateFormat.format(it) } ?: "-",
                        isEditMode = isEditMode
                    )
                    EditableField(
                        label = "Expected Delivery Date",
                        value = patient!!.deliveryDate?.let { dateFormat.format(it) } ?: "-",
                        isEditMode = isEditMode
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandableSection(
    title: String,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandChange(!isExpanded) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableField(
    label: String,
    value: String,
    isEditMode: Boolean
) {
    if (isEditMode) {
        OutlinedTextField(
            value = value,
            onValueChange = { /* TODO: Handle value change */ },
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
} 
package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.data.local.entity.Patient
import com.littleb01s.ashasakhichat.presentation.PatientsViewModel
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.navigation.NavController
import com.littleb01s.ashasakhichat.presentation.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

// Define colors at the top level
private val CustomBlue = Color(0xFF0174B3)
private val CustomGreen = Color(0xFF1BBF69)
private val CustomOrange = Color(0xFFFF5151)
private val BackgroundColor = Color(0xFFFFF5EE)
private val GradientBrush = Brush.horizontalGradient(colors = listOf(CustomBlue, CustomGreen))

// Sample base64 encoded image (replace with actual image later)
private const val SAMPLE_BASE64_IMAGE = "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"
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsScreen(
    patientId: Int,
    navController: NavController,
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

    // Convert base64 to ImageBitmap
    val profileImage = remember {
        val imageBytes = Base64.decode(SAMPLE_BASE64_IMAGE, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size).asImageBitmap()
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Patient Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                CircularProgressIndicator(color = CustomBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Profile Header Section with gradient background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GradientBrush)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Image
                        Box(
                            modifier = Modifier
                                .size(75.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color.White, CircleShape)
                        ) {
                            Image(
                                bitmap = profileImage,
                                contentDescription = "Patient Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(24.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "${patient!!.firstName} ${patient!!.lastName ?: ""}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "ID: ${patient!!.patientId}",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            if (patient!!.needsUpload) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = CustomOrange.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 3.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "Sync Status",
                                            tint = CustomOrange,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Pending Sync",
                                            color = CustomOrange,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons Section - Moved here between header and Basic Information
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { navController.navigate("${Screen.MedicalHistory.route}/${patientId}") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CustomGreen
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = "Medical History",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Medical History")
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { navController.navigate("${Screen.PregnancyRiskAssessment.route}/$patientId") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CustomGreen
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Risk Analysis",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Risk Analysis")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { navController.navigate("${Screen.DietSuggestions.route}/${patientId}/${patient!!.mobileNumber}") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CustomGreen
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.List,
                                contentDescription = "Suggest Diet",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Suggest Diet")
                        }
                    }
                }

                // Information Sections
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Basic Information Section
                    ExpandableSection(
                        title = "Basic Information",
                        icon = Icons.Default.Person,
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Contact Information Section
                    ExpandableSection(
                        title = "Contact Information",
                        icon = Icons.Default.Phone,
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Medical Information Section
                    ExpandableSection(
                        title = "Medical Information",
                        icon = Icons.Default.Favorite,
                        isExpanded = isMedicalInfoExpanded,
                        onExpandChange = { isMedicalInfoExpanded = it }
                    ) {
                        EditableField(
                            label = "Previous Illness",
                            value = patient!!.previousIllness ?: "None",
                            isEditMode = isEditMode
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Social Information Section
                    ExpandableSection(
                        title = "Social Information",
                        icon = Icons.Default.Person,
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pregnancy Information Section
                    ExpandableSection(
                        title = "Pregnancy Information",
                        icon = Icons.Default.Favorite,
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
}

@Composable
fun ExpandableSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = CustomBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CustomBlue
                    )
                }
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = CustomBlue
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
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
                .padding(vertical = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                focusedLabelColor = CustomBlue,
                cursorColor = CustomBlue
            )
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                modifier = Modifier.padding(top = 4.dp)
            )
            Divider(
                modifier = Modifier.padding(top = 8.dp),
                color = Color.LightGray
            )
        }
    }
} 
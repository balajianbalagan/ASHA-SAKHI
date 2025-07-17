package com.littleb01s.ashasakhichat.presentation.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.R
import com.littleb01s.ashasakhichat.data.local.entity.Patient
import com.littleb01s.ashasakhichat.data.local.dao.RiskAnalysisDao
import com.littleb01s.ashasakhichat.presentation.PatientsViewModel
import com.littleb01s.ashasakhichat.presentation.viewmodel.AppointmentViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import android.util.Base64
import androidx.compose.material.icons.filled.Refresh
import androidx.core.graphics.createBitmap
import androidx.compose.material.icons.filled.KeyboardArrowDown

// Define colors at the top level to match PatientDetailsScreen
private val CustomBlue = Color(0xFF0174B3)
private val CustomGreen = Color(0xFF1BBF69)
private val CustomOrange = Color(0xFFFF8C00) // Changed to darker orange to distinguish from red
private val CustomRed = Color(0xFFFF5151) // Keep original red for high risk
private val BackgroundColor = Color(0xFFFFF5EE)
private val GradientBrush = Brush.horizontalGradient(colors = listOf(CustomBlue, CustomGreen))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(
    onPatientClick: (Patient) -> Unit,
    onAddNewPatient: () -> Unit,
    viewModel: PatientsViewModel = hiltViewModel(),
    appointmentViewModel: AppointmentViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf("EDD") }
    val patients by viewModel.filteredPatients(searchQuery).collectAsState(initial = emptyList())
    val context = LocalContext.current
    val db = remember(context) { com.littleb01s.ashasakhichat.data.local.AshaSakhiDatabase.getInstance(context) }
    val riskAnalysisDao = remember(db) { db.riskAnalysisDao() }
    val riskStatusMap = remember { mutableStateMapOf<Int, String>() }
    val coroutineScope = rememberCoroutineScope()

    // Fetch risk status for each patient
    LaunchedEffect(patients) {
        patients.forEach { patient ->
            coroutineScope.launch {
                val latest = riskAnalysisDao.getLatestAnalysisForPatient(patient.patientId)
                riskStatusMap[patient.patientId] = latest?.riskValue ?: "Risk not assessed"
            }
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewPatient,
                containerColor = CustomGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Patient")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {
            // Compact Search Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search by name") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = CustomBlue
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CustomBlue,
                            focusedLabelColor = CustomBlue,
                            cursorColor = CustomBlue
                        ),
                        singleLine = true
                    )
                }
            }
            // Sort Option Dropdown and Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Sort dropdown
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sort by:", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Button(
                            onClick = { expanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CustomBlue.copy(alpha = 0.08f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(sortOption, fontSize = 13.sp, color = CustomBlue)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = CustomBlue)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("EDD", fontSize = 13.sp) },
                                onClick = {
                                    sortOption = "EDD"
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Priority", fontSize = 13.sp) },
                                onClick = {
                                    sortOption = "Priority"
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Color Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PatientRiskLegendItem("High", CustomRed)
                    PatientRiskLegendItem("Medium", CustomOrange)
                    PatientRiskLegendItem("Low", CustomGreen)
                }
            }
            // Patients List
            val sortedPatients = when (sortOption) {
                "Priority" -> patients.sortedWith(compareBy(
                    { patient ->
                        when (riskStatusMap[patient.patientId]?.lowercase()) {
                            "high risk" -> 0
                            "medium risk", "mid risk" -> 1
                            "low risk" -> 2
                            else -> 3 // not assessed
                        }
                    },
                    { patient -> patient.deliveryDate ?: Date(Long.MAX_VALUE) }
                ))
                else -> patients.sortedBy { it.deliveryDate ?: Date(Long.MAX_VALUE) }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedPatients) { patient ->
                    val riskStatus = riskStatusMap[patient.patientId] ?: "Risk not assessed"
                    PatientCard(
                        patient = patient, 
                        riskStatus = riskStatus,
                        onClick = { onPatientClick(patient) }
                    )
                }
            }
        }
    }
}

@Composable
fun PatientCard(
    patient: Patient, 
    riskStatus: String = "Risk not assessed",
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val profileImage = getProfileImage(patient.profilePhoto)
    
    // Get risk color based on status
    val riskColor = when (riskStatus.lowercase()) {
        "high risk" -> CustomRed
        "medium risk", "mid risk" -> CustomOrange
        "low risk" -> CustomGreen
        else -> Color.Gray
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .border(
                width = 4.dp,
                color = riskColor,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, CustomBlue.copy(alpha = 0.3f), CircleShape)
                    .padding(2.dp)
            ) {
                Image(
                    bitmap = profileImage,
                    contentDescription = "Patient Profile",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))

            // Left section with patient info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${patient.firstName} ${patient.lastName ?: ""}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CustomBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (patient.needsUpload) {
                        Row(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .background(
                                    color = CustomOrange.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pending Sync",
                                fontSize = 10.sp,
                                color = CustomOrange,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 2.dp)
                            )
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Pending Sync",
                                tint = CustomOrange,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Patient details in a compact grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left column
                    Column(modifier = Modifier.weight(1f)) {
                        PatientInfoRow(
                            icon = Icons.Filled.DateRange,
                            mainText = "Age",
                            text = "${calculateAge(patient.dateOfBirth)}y",
                            color = Color.Gray
                        )
                        PatientInfoRow(
                            icon = Icons.Default.LocationOn,
                            text = patient.city ?: "-",
                            mainText = "Place",
                            color = Color.Gray
                        )
                    }
                    
                    // Right column
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        PatientInfoRow(
                            icon = null,
                            mainText = "Trimester",
                            text = calculateTrimester(patient.lmp),
                            color = CustomGreen
                        )
                        PatientInfoRow(
                            icon = Icons.Default.DateRange,
                            mainText = "EDD",
                            text = formatDate(patient.deliveryDate ?: calculateEDD(patient.lmp)),
                            color = CustomBlue
                        )
                    }
                }
            }

            // Call button
            IconButton(
                onClick = { handlePhoneCall(context, patient.mobileNumber) },
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(36.dp)
                    .background(
                        color = CustomGreen.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    )
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = "Call Patient",
                    tint = CustomGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PatientInfoRow(
    icon: ImageVector?,
    mainText: String,
    text: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = mainText,
            fontSize = 14.sp,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = text,
            fontSize = 12.sp,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun handlePhoneCall(context: android.content.Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle exception (could show a toast or snackbar)
        e.printStackTrace()
    }
}

private fun calculateAge(dateOfBirth: Date): Int {
    val today = Calendar.getInstance()
    val birthDate = Calendar.getInstance().apply { time = dateOfBirth }
    var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
        age--
    }
    return age
}

private fun calculateTrimester(lmp: Date?): String {
    if (lmp == null) return "-"
    val today = Calendar.getInstance().timeInMillis
    val lmpTime = lmp.time
    val weeksDiff = TimeUnit.MILLISECONDS.toDays(today - lmpTime) / 7

    return when {
        weeksDiff < 0 -> "-"
        weeksDiff <= 13 -> "1st"
        weeksDiff <= 26 -> "2nd"
        weeksDiff <= 40 -> "3rd"
        else -> "Post"
    }
}

private fun calculateEDD(lmp: Date?): Date {
    if (lmp == null) return Date()
    return Calendar.getInstance().apply {
        time = lmp
        add(Calendar.WEEK_OF_YEAR, 40)
    }.time
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
    return formatter.format(date)
}

private fun decodeBase64Image(base64String: String?): ImageBitmap? {
    if (base64String.isNullOrEmpty()) return null

    return try {
        val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
        if (imageBytes.isEmpty()) return null

        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

@Composable
fun PatientRiskLegendItem(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = text,
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@SuppressLint("UseKtx")
@Composable
private fun getProfileImage(profilePhoto: String?): ImageBitmap {
    val context = LocalContext.current
    val placeholderBitmap = remember {
        val drawable = ContextCompat.getDrawable(context, R.drawable.profile_placeholder)
        val bitmap = drawable?.let {
            createBitmap(it.intrinsicWidth, it.intrinsicHeight).also { bmp ->
                val canvas = Canvas(bmp)
                it.setBounds(0, 0, canvas.width, canvas.height)
                it.draw(canvas)
            }
        }
        bitmap?.asImageBitmap()
    }

    return remember(profilePhoto) {
        decodeBase64Image(profilePhoto) ?: placeholderBitmap!!
    }
}
package com.littleb01s.ashasakhichat.presentation.screens

import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.data.local.entity.Patient
import com.littleb01s.ashasakhichat.presentation.PatientsViewModel
import com.littleb01s.ashasakhichat.data.local.dao.RiskAnalysisDao
import com.littleb01s.ashasakhichat.data.local.entity.RiskAnalysisResult
import com.littleb01s.ashasakhichat.presentation.DetailScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.painterResource
import com.littleb01s.R
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap

private val CustomBlue = Color(0xFF0174B3)
private val CustomGreen = Color(0xFF1BBF69)
private val CustomOrange = Color(0xFFFF5151)
private val BackgroundColor = Color(0xFFFFF5EE)

@Composable
fun RiskAnalysisScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPregnancyRisk: (Int) -> Unit,
    patientId: Int? = null,
    viewModel: PatientsViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val patients by viewModel.filteredPatients(searchQuery).collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    val riskStatusMap = remember { mutableStateMapOf<Int, String>() }
    val eddFormatter = remember { SimpleDateFormat("MMM d, yy", Locale.getDefault()) }
    val context = LocalContext.current
    val db = remember(context) { com.littleb01s.ashasakhichat.data.local.AshaSakhiDatabase.getInstance(context) }
    val riskAnalysisDao = remember(db) { db.riskAnalysisDao() }

    // Fetch latest risk for each patient
    LaunchedEffect(patients) {
        patients.forEach { patient ->
            coroutineScope.launch(Dispatchers.IO) {
                val latest = riskAnalysisDao.getLatestAnalysisForPatient(patient.patientId)
                riskStatusMap[patient.patientId] = latest?.riskLevel ?: "Risk not assessed"
            }
        }
    }

    // Sort patients by risk and EDD
    val sortedPatients = patients.sortedWith(compareBy(
        { patient ->
            when (riskStatusMap[patient.patientId]?.lowercase()) {
                "high risk" -> 0
                "medium risk" -> 1
                "low risk" -> 2
                else -> 3 // not assessed
            }
        },
        { patient -> patient.deliveryDate ?: Date(Long.MAX_VALUE) }
    ))

    DetailScaffold(
        title = "Risk Analysis",
        onNavigateBack = onNavigateBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(0.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedPatients) { patient ->
                    RiskPatientCard(
                        patient = patient,
                        riskStatus = riskStatusMap[patient.patientId] ?: "Risk not assessed",
                        eddFormatter = eddFormatter,
                        onClick = { onNavigateToPregnancyRisk(patient.patientId) }
                    )
                }
            }
        }
    }
}

@Composable
fun RiskPatientCard(
    patient: Patient,
    riskStatus: String,
    eddFormatter: SimpleDateFormat,
    onClick: () -> Unit
) {
    val profileImage = getProfileImage(patient.profilePhoto)
    val riskColor = when (riskStatus.lowercase()) {
        "high risk" -> CustomOrange
        "low risk" -> CustomGreen
        "medium risk" -> Color(0xFFFFC107) // Amber
        else -> Color.Gray
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CustomBlue.copy(alpha = 0.1f))
            ) {
                Image(
                    bitmap = profileImage,
                    contentDescription = "Patient Profile",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${patient.firstName} ${patient.lastName ?: ""}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CustomBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "EDD: ${patient.deliveryDate?.let { eddFormatter.format(it) } ?: "-"}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = riskStatus.replaceFirstChar { it.uppercase() },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = riskColor,
                textAlign = TextAlign.End,
                modifier = Modifier.widthIn(min = 90.dp)
            )
        }
    }
}

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

private fun decodeBase64Image(base64String: String?): ImageBitmap? {
    if (base64String.isNullOrEmpty()) return null
    return try {
        val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
        if (imageBytes.isEmpty()) return null
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}   
package com.littleb01s.ashasakhichat.presentation.screens

import android.annotation.SuppressLint
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
import com.littleb01s.ashasakhichat.presentation.DetailScaffold
import com.littleb01s.ashasakhichat.presentation.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.compose.material.icons.filled.Call
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import com.littleb01s.R
import androidx.core.graphics.createBitmap

// Define colors at the top level
private val CustomBlue = Color(0xFF0174B3)
private val CustomGreen = Color(0xFF1BBF69)
private val CustomOrange = Color(0xFFFF5151)
private val BackgroundColor = Color(0xFFFFF5EE)
private val GradientBrush = Brush.horizontalGradient(colors = listOf(CustomBlue, CustomGreen))

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

    // Get profile image
    val profileImage = getProfileImage(patient?.profilePhoto)

    DetailScaffold(
        title = "Patient Details",
        onNavigateBack = onNavigateBack
    ) {
        if (patient == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CustomBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
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
                            modifier = Modifier.weight(1f),
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

                        // Call Button
                        CallButton(patient!!.mobileNumber)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { navController.navigate("${Screen.DietSuggestions.route}/${patientId}/${patient!!.mobileNumber}") },
                            modifier = Modifier.weight(1f),
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { navController.navigate(Screen.Appointments.createRoute(patientId)) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CustomGreen
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "Appointments",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Appointments")
                            }
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

@Composable
fun ExpandableSection(
    title: String,
    icon: ImageVector,
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

@Composable
fun CallButton(phoneNumber: String) {
    val context = LocalContext.current
    IconButton(
        onClick = {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            context.startActivity(intent)
        },
        modifier = Modifier
            .size(48.dp)
            .background(Color.White.copy(alpha = 0.2f), CircleShape)
    ) {
        Icon(
            Icons.Default.Call,
            contentDescription = "Call Patient",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
} 
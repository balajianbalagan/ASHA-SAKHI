package com.littleb01s.ashasakhichat.presentation.screens
import com.littleb01s.R
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.littleb01s.ashasakhichat.presentation.DetailScaffold
import kotlinx.coroutines.launch
import com.littleb01s.ashasakhichat.data.model.DayMeals
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.presentation.viewmodels.DietSuggestionsViewModel
import kotlin.math.min

private const val TAG = "DietSuggestionsScreen"
private const val SMS_PERMISSION_REQUEST = 123
private const val SMS_SENT = "SMS_SENT"
private const val TIMEOUT_DURATION = 600000L // 60 seconds timeout
private const val SMS_LENGTH_LIMIT = 160

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DietSuggestionsScreen(
    patientId: Int,
    phoneNumber: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: DietSuggestionsViewModel = hiltViewModel()
    
    val dietPlan by viewModel.dietPlan.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // SMS Permission handling
    val activity = context as? Activity
    val hasPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    fun requestSmsPermission() {
        activity?.let {
            ActivityCompat.requestPermissions(
                it,
                arrayOf(Manifest.permission.SEND_SMS),
                SMS_PERMISSION_REQUEST
            )
        }
    }

    fun sendSMSInChunks(phoneNumber: String, dietPlan: Map<String, DayMeals>) {
        try {
            val smsManager = SmsManager.getDefault()
//            Log.d("DietSuggestionsScreen","Your 7-Day Diet Plan from ASHA Sakhi:\n(Message 1/${3 * 7})");
            // Send introduction message
            smsManager.sendTextMessage(
                phoneNumber,
                null,
                "Your 7-Day Diet Plan from ASHA Sakhi:\n(Message 1/${3 * 7})",
                null,
                null
            )

            // For each day, send 3 messages: morning meals, afternoon meals, and dinner
            dietPlan.forEach { (day, meals) ->
                // Morning meals (breakfast and morning snack)
                val morningMessage = buildString {
                    append("$day - Part 1/3:\n")
                    append("Breakfast: ${meals.breakfast}\n")
                    append("Morning Snack: ${meals.morning_snack}")
                }
//                Log.d("DietSuggestionsScreen",morningMessage)
                smsManager.sendTextMessage(phoneNumber, null, morningMessage, null, null)

                // Afternoon meals (lunch and evening snack)
                val afternoonMessage = buildString {
                    append("$day - Part 2/3:\n")
                    append("Lunch: ${meals.lunch}\n")
                    append("Evening Snack: ${meals.evening_snack}")
                }
                smsManager.sendTextMessage(phoneNumber, null, afternoonMessage, null, null)
//                Log.d("DietSuggestionsScreen",afternoonMessage)
                // Dinner
                val dinnerMessage = buildString {
                    append("$day - Part 3/3:\n")
                    append("Dinner: ${meals.dinner}")
                }
                smsManager.sendTextMessage(phoneNumber, null, dinnerMessage, null, null)
//                Log.d("DietSuggestionsScreen",dinnerMessage)
//                 Add a small delay between messages to prevent flooding
                Thread.sleep(300)
            }

            Toast.makeText(context, "Diet plan sent successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(patientId) {
        viewModel.loadDietPlan(patientId = patientId, isOnlineMode = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diet Suggestions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator()
                    }
                    error != null -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
            Text(
                                text = error ?: "Unknown error occurred",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
            Button(
                onClick = {
                                    viewModel.loadDietPlan(patientId = patientId, isOnlineMode = true)
                                },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                    dietPlan != null -> {
                        Column {
                            // Header
                            Text(
                                text = "7-Day Diet Plan",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            val pagerState = rememberPagerState(pageCount = { 7 })

                            // Day selector tabs
                            ScrollableTabRow(
                                selectedTabIndex = pagerState.currentPage,
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                dietPlan?.diet_plan?.keys?.forEachIndexed { index, _ ->
                                    Tab(
                                        selected = pagerState.currentPage == index,
                                        onClick = {
                                            scope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        },
                                        text = { Text("Day ${index + 1}") }
                                    )
                                }
                            }

                            // Day content pager
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) { page ->
                                val dayKey = "day${page + 1}"
                                val dayMeals = dietPlan?.diet_plan?.get(dayKey)
                                if (dayMeals != null) {
                                    DayMealsContent(dayMeals = dayMeals)
                                }
                            }

                            // Send to Patient Button
                            Button(
                                onClick = { 
                                    if (!hasPermission.value) {
                                        requestSmsPermission()
                                    } else {
                                        dietPlan?.let { plan ->
                                            sendSMSInChunks(phoneNumber, plan.diet_plan)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Send to Patient")
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = "No diet plan available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayMealsContent(dayMeals: DayMeals) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        item {
            MealCard(
                title = "Breakfast",
                meal = dayMeals.breakfast,
                iconRes = R.drawable.breakfast
            )
        }
        item {
            MealCard(
                title = "Morning Snack",
                meal = dayMeals.morning_snack,
                iconRes = R.drawable.morning_snack
            )
        }
        item {
            MealCard(
                title = "Lunch",
                meal = dayMeals.lunch,
                iconRes = R.drawable.lunch
            )
        }
        item {
            MealCard(
                title = "Evening Snack",
                meal = dayMeals.evening_snack,
                iconRes = R.drawable.evening_snack
            )
        }
        item {
            MealCard(
                title = "Dinner",
                meal = dayMeals.dinner,
                iconRes = R.drawable.dinner
            )
        }
    }
}

@Composable
fun MealCard(
    title: String,
    meal: String,
    iconRes: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F6FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0174B3)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = meal,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
} 
package com.littleb01s.ashasakhichat.presentation.screens

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.littleb01s.ashasakhichat.presentation.DetailScaffold
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "DietSuggestionsScreen"
private const val SMS_SENT = "SMS_SENT"
private const val TIMEOUT_DURATION = 120000L // 60 seconds timeout

@Composable
fun DietSuggestionsScreen(
    patientId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var isOnlineMode by remember { mutableStateOf(true) }
    var hasSmsSendPermission by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var buttonText by remember { mutableStateOf("Get Diet Suggestions") }
    val coroutineScope = rememberCoroutineScope()

    val ASHA_ADMIN_PH = "+917305746710"

    // Permission launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasSmsSendPermission = isGranted
        if (isGranted) {
            Log.d(TAG, "SMS permission granted")
        } else {
            Log.w(TAG, "SMS permission denied")
        }
    }

    // SMS sent receiver
    val smsSentReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Log.d(TAG, "SMS sent successfully")
                        isLoading = false
                        buttonText = "Get Diet Suggestions"
                        Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Log.e(TAG, "Failed to send SMS, result code: $resultCode")
                        isLoading = false
                        buttonText = "Get Diet Suggestions"
                        Toast.makeText(context, "Failed to send SMS", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Register SMS sent receiver
    DisposableEffect(Unit) {
        val filter = IntentFilter(SMS_SENT)
        ContextCompat.registerReceiver(
            context,
            smsSentReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            try {
                context.unregisterReceiver(smsSentReceiver)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering receiver", e)
            }
        }
    }

    // Check SMS permission
    LaunchedEffect(Unit) {
        hasSmsSendPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "SMS permission status: $hasSmsSendPermission")
    }

    DetailScaffold(
        title = "Diet Suggestions",
        onNavigateBack = onNavigateBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Online/Offline Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (isOnlineMode) "Online Mode" else "Offline Mode")
                Switch(
                    checked = isOnlineMode,
                    onCheckedChange = { newMode ->
                        isOnlineMode = newMode
                        buttonText = if (newMode) "Get Diet Suggestions" else "Send SMS Request"
                        Log.d(TAG, "Mode switched to: ${if (newMode) "Online" else "Offline"}")
                    }
                )
            }

            Text(
                text = "Diet Suggestions for Patient ID: $patientId",
                style = MaterialTheme.typography.headlineMedium
            )

            Button(
                onClick = {
                    if (!isOnlineMode) {
                        if (!hasSmsSendPermission) {
                            Log.d(TAG, "Requesting SMS permission")
                            requestPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                        } else {
                            coroutineScope.launch {
                                try {
                                    isLoading = true
                                    buttonText = "Diet plan is being prepared..."
                                    Log.d(TAG, "Preparing to send SMS for patient $patientId")

                                    val smsManager = SmsManager.getDefault()
                                    val payload = Gson().toJson(mapOf("patientId" to patientId))
                                    val message = "ASHASAKHI DIET 1 0 $payload"

                                    // Create PendingIntent for SMS sent
                                    val sentIntent = PendingIntent.getBroadcast(
                                        context, 0,
                                        Intent(SMS_SENT),
                                        PendingIntent.FLAG_IMMUTABLE
                                    )

                                    // Send SMS
                                    Log.d(TAG, "Sending SMS to $ASHA_ADMIN_PH: $message")
                                    smsManager.sendTextMessage(
                                        ASHA_ADMIN_PH,
                                        null,
                                        message,
                                        sentIntent,
                                        null
                                    )

                                    // Start timeout timer
                                    launch {
                                        delay(TIMEOUT_DURATION)
                                        if (isLoading) {
                                            isLoading = false
                                            buttonText = if (isOnlineMode) "Get Diet Suggestions" else "Send SMS Request"
                                            Log.w(TAG, "SMS sending timed out")
                                            Toast.makeText(
                                                context,
                                                "Request timed out. Please try again.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error sending SMS", e)
                                    isLoading = false
                                    buttonText = if (isOnlineMode) "Get Diet Suggestions" else "Send SMS Request"
                                    Toast.makeText(
                                        context,
                                        "Error: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } else {
                        // Online mode logic here
                        // TODO: Implement API call
                        Log.d(TAG, "Online mode selected - API call not implemented yet")
                        Toast.makeText(context, "Online mode not implemented yet", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isLoading
            ) {
                Text(text = buttonText)
            }

            // Display permission status if in offline mode
            if (!isOnlineMode && !hasSmsSendPermission) {
                Text(
                    text = "SMS permission is required to send requests in offline mode",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
} 
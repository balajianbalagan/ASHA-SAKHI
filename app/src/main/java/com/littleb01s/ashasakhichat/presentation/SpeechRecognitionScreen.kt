package com.littleb01s.ashasakhichat.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import com.littleb01s.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.PermissionStatus
import android.Manifest
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.ui.draw.alpha

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SpeechRecognitionScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateBack: () -> Boolean
) {
    val context = LocalContext.current
    val isSpeechRecognitionActive by viewModel.isSpeechRecognitionActive.collectAsState()
    var recognizedText by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    // Request microphone permission
    val micPermissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, can start speech recognition
            Toast.makeText(context, "Microphone permission granted", Toast.LENGTH_SHORT).show()
            // Initialize speech recognition after permission is granted
            initializeSpeechRecognition(viewModel, context) { isInitialized = it }
        } else {
            // Permission denied
            Toast.makeText(context, "Microphone permission is required for speech recognition", Toast.LENGTH_SHORT).show()
        }
    }

    // Initialize speech recognition on launch and when language changes
    LaunchedEffect(Unit) {
        if (micPermissionState.status == PermissionStatus.Granted) {
            initializeSpeechRecognition(viewModel, context) { isInitialized = it }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.alpha(if (isInitialized) 1f else 0.5f)
        ) {
            FloatingActionButton(
                onClick = {
                    when {
                        !isInitialized -> {
                            Toast.makeText(context, "Speech recognition is initializing...", Toast.LENGTH_SHORT).show()
                        }
                        micPermissionState.status is PermissionStatus.Granted -> {
                            viewModel.toggleSpeechRecognition()
                        }
                        else -> {
                            micPermissionState.launchPermissionRequest()
                        }
                    }
                },
                shape = CircleShape,
                containerColor = if (isSpeechRecognitionActive) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    imageVector = if (isSpeechRecognitionActive) Icons.Default.Clear else Icons.Default.Call,
                    contentDescription = if (isSpeechRecognitionActive) 
                        stringResource(R.string.stop_recording) 
                    else 
                        stringResource(R.string.start_recording),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Status Text
        Text(
            text = when {
                !isInitialized -> stringResource(R.string.initializing)
                isSpeechRecognitionActive -> stringResource(R.string.listening)
                else -> stringResource(R.string.tap_mic_to_start)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Recognized Text Display
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = recognizedText.ifEmpty { stringResource(R.string.recognized_text_placeholder) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = if (recognizedText.isEmpty()) 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                else 
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }

    // Listen for speech recognition results
    DisposableEffect(viewModel) {
        val listener = object : SpeechRecognitionListener {
            override fun onResult(text: String) {
                recognizedText = text
            }
        }
        
        viewModel.setSpeechRecognitionListener(listener)
        
        onDispose {
            viewModel.removeSpeechRecognitionListener()
        }
    }
}

private fun initializeSpeechRecognition(
    viewModel: ChatViewModel,
    context: android.content.Context,
    onInitialized: (Boolean) -> Unit
) {
    viewModel.initSpeechRecognition(
        onSuccess = {
            onInitialized(true)
        },
        onError = { error ->
            Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
            onInitialized(false)
        }
    )
}

interface SpeechRecognitionListener {
    fun onResult(text: String)
} 
@file:OptIn(ExperimentalPermissionsApi::class)

package com.littleb01s.ashasakhichat.presentation

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.sharp.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.R
import com.littleb01s.ashasakhichat.presentation.components.MarkdownRenderer
import com.littleb01s.ashasakhichat.presentation.components.ModelDownloadDialog
import com.littleb01s.ashasakhichat.presentation.components.StatusChip
import com.littleb01s.ashasakhichat.ui.theme.AshaTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

private const val StartChatKey = "StartChat"

@Serializable
object Chat

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val isProcessing by viewModel.isProcessing.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val hasShownWelcome by viewModel.hasShownWelcome.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English") }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var chatInputText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main chat UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            ChatHeader(
                onSettingsClick = { showSettings = true }
            )
            
            if (messages.isEmpty() && !hasShownWelcome) {
                WelcomeScreen()
            } else {
                val lazyListState = rememberLazyListState()
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(key1 = messages.size) {
                    lazyListState.animateScrollToItem(index = messages.lastIndex)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f)
                        .padding(horizontal = 16.dp),
                    state = lazyListState
                ) {
                    var currentDate = ""
                    val items = messages.toList()
                    
                    items.forEachIndexed { index, message ->
                        if (message.formattedDate != currentDate) {
                            currentDate = message.formattedDate
                            item {
                                DateHeader(date = currentDate)
                            }
                        }
                        
                        item {
                            ChatItem(
                                message = message,
                                onRetry = { viewModel.retryLastMessage() },
                                onShare = { text ->
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, text)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                                }
                            )
                        }
                    }
                }

                val focusManager = LocalFocusManager.current

                ChatBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    onTextFieldClicked = {
                        coroutineScope.launch {
                            lazyListState.scrollToItem(index = messages.lastIndex)
                        }
                    },
                    onSendMessageClicked = { message ->
                        viewModel.sendMessage(message)
                    },
                    isProcessing = isProcessing,
                    initialText = chatInputText
                )
            }
        }
    }

    // Settings Dialog
    if (showSettings) {
        ChatSettingsDialog(
            selectedLanguage = selectedLanguage,
            onLanguageChange = { selectedLanguage = it },
            onAskRandomQuestion = {
                val questions = getPresetQuestions(selectedLanguage)
                val question = questions[currentQuestionIndex]
                chatInputText = question
                currentQuestionIndex = (currentQuestionIndex + 1) % questions.size
                showSettings = false
            },
            onDismiss = { showSettings = false }
        )
    }
    
    // Clear chatInputText after it's been used
    LaunchedEffect(chatInputText) {
        if (chatInputText.isNotEmpty()) {
            delay(100) // Small delay to ensure the text is set
            chatInputText = ""
        }
    }

    // Only start chat if not shown before
    LaunchedEffect(key1 = hasShownWelcome) {
        if (!hasShownWelcome) {
            viewModel.startChat()
        }
    }
}

@Composable
fun ChatHeader(
    onSettingsClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = painterResource(id = R.drawable.chat_bot_icon),
                contentDescription = stringResource(R.string.chat_bot_logo),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.chat_bot_title),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings),
                tint = Color.White
            )
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ShareMessage(text: String) {
    val context = LocalContext.current
    val shareViaText = stringResource(R.string.share_via)
    
    IconButton(
        onClick = {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, shareViaText))
        },
        modifier = Modifier.size(24.dp)
    ) {
        Icon(
            Icons.Default.Share,
            "Share message",
            tint = Color.Black
        )
    }
}

@Composable
fun ChatItem(
    message: Message,
    onRetry: () -> Unit,
    onShare: (String) -> Unit
) {
    val viewModel: ChatViewModel = hiltViewModel()
    val shareViaText = stringResource(R.string.share_via)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
        ) {
            // TTS Speak button for all messages (only for non-empty text)

            if (!message.isFromMe) {
                Image(
                    painter = painterResource(id = R.drawable.chat_bot_icon),
                    contentDescription = stringResource(R.string.chat_bot_logo),
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 8.dp)
                )
            }
            
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16f,
                            topEnd = 16f,
                            bottomStart = if (message.isFromMe) 16f else 0f,
                            bottomEnd = if (message.isFromMe) 0f else 16f
                        )
                    )
                    .background(
                        if (message.isError) MaterialTheme.colorScheme.error
                        else if (message.isFromMe) Color(0xFF006BE5)
                        else Color(0xFFF2F8FF)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    if (message.text.isNotEmpty() || message.isLoading) {
                        if (message.isLoading) {
                            LoadingAnimation()
                        } else {
                            // Use MarkdownRenderer for non-user messages
                            if (!message.isFromMe) {
                                MarkdownRenderer(
                                    text = message.text,
                                    textColor = Color.Black
                                )
                            } else {
                                Text(
                                    text = message.text,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    // Add StatusChip for non-user messages
                    if (!message.isFromMe && message.status != MessageStatus.NONE) {
                        Spacer(modifier = Modifier.height(8.dp))
                        StatusChip(
                            status = message.status,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                    
                    if (!message.isFromMe && !message.isLoading) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            val clipboardManager = LocalClipboardManager.current
                            if (message.text.isNotBlank()) {
                                // You can change br = true to speak in Brazilian Portuguese
                                IconButton(
                                    onClick = { viewModel.speak(message.text) }, // br = true for Portuguese
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Speak message",
                                        tint = Color.Black
                                    )
                                }
                            }
                            IconButton(
                                onClick = { clipboardManager.setText(AnnotatedString(message.text)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.copy),
                                    contentDescription = "Copy message",
                                    tint = Color.Black
                                )
                            }
                            ShareMessage(message.text)
                            if (message.isError) {
                                Row(
                                    modifier = Modifier.padding(start = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = onRetry,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = stringResource(R.string.retry),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = stringResource(R.string.retry),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Text(
            text = message.formattedTime,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier
                .align(if (message.isFromMe) Alignment.End else Alignment.Start)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun LoadingAnimation() {
    Text(
        text = stringResource(R.string.loading),
        color = Color.Black,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun ChatBox(
    modifier: Modifier,
    onSendMessageClicked: (String) -> Unit,
    onTextFieldClicked: () -> Unit,
    isProcessing: Boolean,
    initialText: String = ""
) {
    var chatBoxValue by remember { mutableStateOf(TextFieldValue(initialText)) }
    
    // Update chatBoxValue when initialText changes
    LaunchedEffect(initialText) {
        if (initialText.isNotEmpty()) {
            chatBoxValue = TextFieldValue(initialText)
        }
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val context = LocalContext.current
    val viewModel: ChatViewModel = hiltViewModel()
    var isSpeechRecognitionActive by remember { mutableStateOf(false) }

    if (isPressed) {
        onTextFieldClicked()
    }

    // Speech recognition listener
    DisposableEffect(viewModel) {
        val listener = object : SpeechRecognitionListener {
            override fun onResult(text: String) {
                if (isSpeechRecognitionActive) {
                    // Just update with the latest partial
                    chatBoxValue = TextFieldValue(text)
                } else {
                    // This is a final result
                    chatBoxValue = TextFieldValue(text)
                }
            }
        }
        viewModel.setSpeechRecognitionListener(listener)
        onDispose {
            viewModel.removeSpeechRecognitionListener()
        }
    }

    // Initialize speech recognition
    LaunchedEffect(Unit) {
        viewModel.initSpeechRecognition(
            onSuccess = { /* Ready to use */ },
            onError = { error ->
                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom // Changed to Bottom to align with multiline TextField
    ) {
        // Microphone Button
        IconButton(
            onClick = {
                if (!isSpeechRecognitionActive) {
                    viewModel.startSpeechRecognition()
                } else {
                    viewModel.stopSpeechRecognition()
                }
                isSpeechRecognitionActive = !isSpeechRecognitionActive
            },
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    if (isSpeechRecognitionActive)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.secondary
                )
        ) {
            Icon(
                painter = painterResource(
                    id = if (isSpeechRecognitionActive)
                        R.drawable.ic_stop_recording
                    else
                        R.drawable.ic_mic
                ),
                contentDescription = if (isSpeechRecognitionActive)
                    stringResource(R.string.stop_recording)
                else
                    stringResource(R.string.start_recording),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        TextField(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp, max = 160.dp) // Set maximum height for 5 lines
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp)
                ),
            value = chatBoxValue,
            onValueChange = { newText ->
                chatBoxValue = newText
            },
            placeholder = {
                Text(
                    text = if (isSpeechRecognitionActive)
                        stringResource(R.string.listening)
                    else
                        stringResource(R.string.type_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            interactionSource = interactionSource,
            enabled = !isProcessing,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodyLarge,
            maxLines = 5,
            minLines = 1
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = {
                if (chatBoxValue.text.isNotBlank()) {
                    onSendMessageClicked(chatBoxValue.text)
                    chatBoxValue = TextFieldValue("")
                }
            },
            enabled = !isProcessing && chatBoxValue.text.isNotBlank(),
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    if (!isProcessing && chatBoxValue.text.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.send),
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun WelcomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "ASHA Sakhi Logo",
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Welcome to ASHA Sakhi",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your AI Healthcare Assistant",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Preview(showBackground = true)
@Composable
fun ChatPreview() {
    AshaTheme {
        ChatScreen(hiltViewModel<ChatViewModel>())
    }
}

@Composable
fun ChatSettingsDialog(
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
    onAskRandomQuestion: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Language Selection
                Column {
                    Text(
                        text = stringResource(R.string.select_language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("English", "हिंदी", "தமிழ்").forEach { language ->
                            val isSelected = selectedLanguage == language
                            Button(
                                onClick = { onLanguageChange(language) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.surface,
                                    contentColor = if (isSelected) 
                                        Color.White 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                ),
                                border = if (!isSelected) {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                } else null
                            ) {
                                Text(
                                    text = language,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Random Question Button
                Column {
                    Text(
                        text = stringResource(R.string.preset_questions),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onAskRandomQuestion,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.ask_random_question),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.dismiss))
            }
        }
    )
}

fun getPresetQuestions(language: String): List<String> {
    return when (language) {
        "हिंदी" -> listOf(
            "प्रसव के लक्षण क्या हैं?",
            "गर्भावस्था के दौरान मुझे कितना वजन बढ़ाना चाहिए?",
            "गर्भावस्था के दौरान मुझे कौन से खाद्य पदार्थों से बचना चाहिए?",
            "मुझे कितनी बार डॉक्टर से मिलना चाहिए?",
            "गर्भावस्था के दौरान कौन से व्यायाम सुरक्षित हैं?",
            "मुझे कौन से चेतावनी संकेतों पर ध्यान देना चाहिए?",
            "मैं स्तनपान के लिए कैसे तैयारी कर सकती हूं?",
            "मुझे अस्पताल के लिए क्या पैक करना चाहिए?",
            "मैं मॉर्निंग सिकनेस का प्रबंधन कैसे कर सकती हूं?",
            "प्रसव पूर्व विटामिन के क्या लाभ हैं?"
        )
        "தமிழ்" -> listOf(
            "பிரசவ அறிகுறிகள் என்ன?",
            "கர்ப்ப காலத்தில் நான் எவ்வளவு எடை கூட்ட வேண்டும்?",
            "கர்ப்ப காலத்தில் நான் எந்த உணவுகளைத் தவிர்க்க வேண்டும்?",
            "நான் எத்தனை முறை மருத்துவரை சந்திக்க வேண்டும்?",
            "கர்ப்ப காலத்தில் எந்த பயிற்சிகள் பாதுகாப்பானவை?",
            "நான் கவனிக்க வேண்டிய எச்சரிக்கை அறிகுறிகள் என்ன?",
            "நான் மார்பக ஊட்டத்திற்கு எப்படி தயாராகலாம்?",
            "மருத்துவமனைக்கு நான் என்ன பொதிய வேண்டும்?",
            "காலை நோயை நான் எப்படி நிர்வகிக்க முடியும்?",
            "கர்ப்ப முன் வைட்டமின்களின் நன்மைகள் என்ன?"
        )
        else -> listOf(
            "What are the signs of labor?",
            "How much weight should I gain during pregnancy?",
            "What foods should I avoid during pregnancy?",
            "How often should I visit the doctor?",
            "What exercises are safe during pregnancy?",
            "What are the warning signs I should watch for?",
            "How can I prepare for breastfeeding?",
            "What should I pack for the hospital?",
            "How can I manage morning sickness?",
            "What are the benefits of prenatal vitamins?"
        )
    }
}

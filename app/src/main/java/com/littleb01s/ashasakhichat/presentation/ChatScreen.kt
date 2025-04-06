@file:OptIn(ExperimentalPermissionsApi::class)

package com.littleb01s.ashasakhichat.presentation

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.R
import com.littleb01s.ashasakhichat.ui.theme.AshaTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

private const val StartChatKey = "StartChat"

@Serializable
object Chat

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val context = LocalContext.current
    val isProcessing by viewModel.isProcessing.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header
        ChatHeader()
        
        val messages by viewModel.messages.collectAsState()

        if (messages.isEmpty()) {
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
                    .imePadding()
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
                isProcessing = isProcessing
            )
        }
    }

    LaunchedEffect(key1 = StartChatKey) {
        viewModel.startChat()
    }
}

@Composable
fun ChatHeader() {
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
                contentDescription = "ASHA Sakhi Logo",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ASHA Sakhi Bot",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
        IconButton(onClick = { /* TODO: Implement settings */ }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
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
fun ChatItem(
    message: Message,
    onRetry: () -> Unit,
    onShare: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
        ) {
            if (!message.isFromMe) {
                Image(
                    painter = painterResource(id = R.drawable.chat_bot_icon),
                    contentDescription = "ASHA Sakhi Logo",
                    modifier = Modifier
                        .size(24.dp)
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
                    if (message.text.isNotEmpty()) {
                        if (message.isLoading) {
                            LoadingAnimation()
                        } else {
                            Text(
                                text = message.text,
                                color = if (message.isFromMe) Color.White else Color.Black
                            )
                        }
                    }
                    
                    if (!message.isFromMe && !message.isLoading) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            val clipboardManager = LocalClipboardManager.current
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
                            IconButton(
                                onClick = { onShare(message.text) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    "Share message",
                                    tint = Color.Black
                                )
                            }
                            if (message.isError) {
                                IconButton(
                                    onClick = onRetry,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        "Retry message",
                                        tint = Color.Black
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
    var dots by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        while (true) {
            dots = when (dots.length) {
                0 -> "."
                1 -> ".."
                2 -> "..."
                else -> ""
            }
            kotlinx.coroutines.delay(500)
        }
    }
    
    Text(text = "ASHA Sakhi is typing$dots")
}

@Composable
fun ChatBox(
    modifier: Modifier,
    onSendMessageClicked: (String) -> Unit,
    onTextFieldClicked: () -> Unit,
    isProcessing: Boolean
) {
    var chatBoxValue by remember { mutableStateOf(TextFieldValue("")) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    if (isPressed) {
        onTextFieldClicked()
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            modifier = Modifier
                .weight(1f)
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
                    text = "Ask ASHA Sakhi...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
            singleLine = true
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

@Preview(showBackground = true)
@Composable
fun ChatPreview() {
    AshaTheme {
        ChatScreen(hiltViewModel<ChatViewModel>())
    }
}

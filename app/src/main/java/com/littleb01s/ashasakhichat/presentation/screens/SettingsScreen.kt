package com.littleb01s.ashasakhichat.presentation.screens

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import com.littleb01s.ashasakhichat.presentation.SettingsViewModel

sealed class DownloadState {
    object Idle : DownloadState()
    object Downloading : DownloadState()
    data class Failed(val message: String) : DownloadState()
    object Completed : DownloadState()
    object Cancelled : DownloadState()
}

@Composable
fun SettingsScreen(
    onNavigateToHome: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val customBlue = Color(0xFF0174B3)
    val customGreen = Color(0xFF1BBF69)

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()

    // Get SharedPreferences instance
    val sharedPrefs = remember {
        context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
    }
    var showConfirmation by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var showDownloadError by remember { mutableStateOf(false) }
    var downloadError by remember { mutableStateOf("") }

    val downloadProgressMap = remember { mutableStateMapOf<String, Int>() }
    val downloadStateMap = remember { mutableStateMapOf<String, DownloadState>() }
    var currentFile by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember {
        mutableStateOf(sharedPrefs.getString("selected_language", configuration.locales[0].language) ?: "en")
    }

    // Update configuration when language changes
    LaunchedEffect(selectedLanguage) {
        // Save to SharedPreferences
        sharedPrefs.edit().putString("selected_language", selectedLanguage).apply()

        val locale = Locale(selectedLanguage)
        Locale.setDefault(locale)
        val config = Configuration(configuration)
        config.setLocale(locale)
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    // Navigate to home after showing confirmation
    LaunchedEffect(showConfirmation) {
        if (showConfirmation) {
            delay(1000) // Wait for 1 second to show the confirmation
            onNavigateToHome()
        }
    }

    // Handle the downloading, retry, and cancel logic
    LaunchedEffect(downloadProgressMap) {
        downloadStateMap.clear() // Reset the states when the screen is loaded
    }

    val filesToDownload = listOf(
        "Gecko_1024_quant.tflite" to "https://asha-sakhi-cdn.b-cdn.net/Gecko_1024_quant.tflite",
        "sentencepiece.model" to "https://asha-sakhi-cdn.b-cdn.net/sentencepiece.model",
        "asha-kb.pdf" to "https://asha-sakhi-cdn.b-cdn.net/asha-kb.pdf",
        "gemma3-1b-it-int4.task" to "https://asha-sakhi-cdn.b-cdn.net/gemma3-1b-it-int4.task",
        "asha_guidelines.json" to "https://asha-sakhi-cdn.b-cdn.net/asha_guidelines.json"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Language Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Section Header with Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_language_24),
                        contentDescription = stringResource(R.string.language),
                        tint = customBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.language_settings),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = customBlue
                    )
                }

                // Language Selector
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = customBlue
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (selectedLanguage) {
                                    "en" -> stringResource(R.string.english)
                                    "hi" -> stringResource(R.string.hindi)
                                    "ta" -> stringResource(R.string.tamil)
                                    "bn" -> stringResource(R.string.bengali)
                                    else -> stringResource(R.string.english)
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = customBlue
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Language",
                                tint = customBlue
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.english),
                                    color = customBlue
                                )
                            },
                            onClick = {
                                selectedLanguage = "en"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.hindi),
                                    color = customBlue
                                )
                            },
                            onClick = {
                                selectedLanguage = "hi"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.tamil),
                                    color = customBlue
                                )
                            },
                            onClick = {
                                selectedLanguage = "ta"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.bengali),
                                    color = customBlue
                                )
                            },
                            onClick = {
                                selectedLanguage = "bn"
                                expanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Apply Button with Gradient
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        showConfirmation = true
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(customBlue, customGreen)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.apply_changes),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Downloads Section Title
        Text(
            text = stringResource(R.string.downloads_section_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = customBlue,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        filesToDownload.forEach { (filename, url) ->
            val downloadState = downloadStateMap[filename] ?: DownloadState.Idle
            val progress = downloadProgressMap[filename] ?: 0
            val fileDir = context.getExternalFilesDir(null)
            val file = java.io.File(fileDir, "llm/$filename")
            val fileExists = file.exists() && file.length() > 0
            val fileSizeMB = if (fileExists) String.format("%.2f", file.length() / (1024.0 * 1024.0)) else null

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = filename,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = customBlue,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        when (downloadState) {
                            is DownloadState.Idle -> {
                                IconButton(
                                    onClick = {
                                        downloadStateMap[filename] = DownloadState.Downloading
                                        viewModel.downloadModels(
                                            onProgress = { file, prog ->
                                                downloadProgressMap[file] = prog
                                            },
                                            onComplete = { },
                                            onError = { error ->
                                                downloadStateMap[filename] = DownloadState.Failed(error)
                                            },
                                            onFileStart = { file ->
                                                downloadStateMap[file] = DownloadState.Downloading
                                            }
                                        )
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Download,
                                        contentDescription = stringResource(R.string.download),
                                        tint = customGreen
                                    )
                                }
                            }

                            is DownloadState.Downloading -> {
                                Box(
                                    modifier = Modifier.size(32.dp),
                                    contentAlignment = Alignment.Center
                                ) 
                                {
                                    CircularProgressIndicator(
                                        progress = progress / 100f,
                                        color = customBlue,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    if (progress in 1..99) {
                                        Text(
                                            "$progress%",
                                            fontSize = 10.sp,
                                            color = customBlue,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            downloadStateMap[filename] = DownloadState.Cancelled
                                            viewModel.cancelDownload(filename)
                                        },
                                        modifier = Modifier.size(32.dp).align(Alignment.TopEnd)
                                    ) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = stringResource(R.string.cancel),
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }

                            is DownloadState.Failed -> {
                                IconButton(
                                    onClick = {
                                        downloadStateMap[filename] = DownloadState.Downloading
                                        viewModel.retryDownload(
                                            filename,
                                            onProgress = { file, prog ->
                                                downloadProgressMap[file] = prog
                                            },
                                            onError = { error ->
                                                downloadStateMap[filename] = DownloadState.Failed(error)
                                            },
                                            onFileStart = { file ->
                                                downloadStateMap[file] = DownloadState.Downloading
                                            }
                                        )
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Download,
                                        contentDescription = stringResource(R.string.retry),
                                        tint = Color.Red
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.download_failed),
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }

                            is DownloadState.Completed -> {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = stringResource(R.string.download_complete),
                                    tint = customGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            is DownloadState.Cancelled -> {
                                Icon(
                                    imageVector = Icons.Filled.Cancel,
                                    contentDescription = stringResource(R.string.cancelled),
                                    tint = Color.Gray,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                    // Download status below filename
                    if (fileExists) {
                        Text(
                            text = "File already downloaded • $fileSizeMB MB",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp, start = 2.dp)
                        )
                    }
                }
            }
        }
    }

    if (showConfirmation) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text(stringResource(R.string.dismiss), color = Color.White)
                }
            }
        ) {
            Text(stringResource(R.string.settings_updated))
        }
    }

    if (showDownloadError) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { showDownloadError = false }) {
                    Text(stringResource(R.string.dismiss), color = Color.White)
                }
            }
        ) {
            Text(downloadError)
        }
    }
}

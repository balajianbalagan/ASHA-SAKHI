package com.littleb01s.ashasakhichat.presentation.screens

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Download
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
import java.io.File
import com.littleb01s.ashasakhichat.data.api.ModelDownloadService
import com.littleb01s.ashasakhichat.presentation.SettingsViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    
    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { 
        mutableStateOf(sharedPrefs.getString("selected_language", configuration.locales[0].language) ?: "en")
    }
    
    var showConfirmation by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var showDownloadError by remember { mutableStateOf(false) }
    var downloadError by remember { mutableStateOf("") }

    // Map to store progress per file
    val downloadProgressMap = remember { mutableStateMapOf<String, Int>() }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Header
        Text(
            text = stringResource(R.string.settings),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = customBlue,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Language Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
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

        // Download Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
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
                        imageVector = Icons.Default.Download,
                        contentDescription = stringResource(R.string.download),
                        tint = customBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.download_models),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = customBlue
                    )
                }

                // Download Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        if (!isDownloading) {
                            scope.launch {
                                isDownloading = true
                                downloadProgressMap.clear()

                                try {
                                    viewModel.downloadModels(
                                        onProgress = { filename, progress ->
                                            downloadProgressMap[filename] = progress
                                        },
                                        onComplete = {
                                            showConfirmation = true
                                        },
                                        onError = { error ->
                                            downloadError = error
                                            showDownloadError = true
                                        }
                                    )
                                } finally {
                                    isDownloading = false
                                }
                            }
                        }
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
                        if (isDownloading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.download_models),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }

                if (downloadProgressMap.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    downloadProgressMap.forEach { (filename, progress) ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(
                                text = "$filename: $progress%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = customBlue
                            )
                            LinearProgressIndicator(
                                progress = progress / 100f,
                                modifier = Modifier.fillMaxWidth(),
                                color = customBlue
                            )
                        }
                    }
                }
            }
        }
    }

    // Show confirmation snackbar
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

    // Show download error snackbar
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
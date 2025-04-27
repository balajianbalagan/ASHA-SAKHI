package com.littleb01s.ashasakhichat.presentation

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littleb01s.R
import com.littleb01s.ashasakhichat.ui.theme.AshaTheme
import kotlinx.serialization.Serializable
import java.util.Locale
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface

@Serializable
object Welcome

private data class LanguageTitle(
    val stringResId: Int,
    val languageCode: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    
    // Get SharedPreferences instance
    val sharedPrefs = remember { context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE) }
    
    // Track both selected language and current configuration, initialize from SharedPreferences
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

    // List of language titles to cycle through
    val languageTitles = remember {
        listOf(
            LanguageTitle(R.string.select_language_en, "en"),
            LanguageTitle(R.string.select_language_hi, "hi"),
            LanguageTitle(R.string.select_language_mr, "mr"),
            LanguageTitle(R.string.select_language_gu, "gu"),
            LanguageTitle(R.string.select_language_bn, "bn")
        )
    }
    
    // State for the current language title index
    var currentTitleIndex by remember { mutableStateOf(0) }
    
    // Animation effect to cycle through languages
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000) // 3 seconds delay
            currentTitleIndex = (currentTitleIndex + 1) % languageTitles.size
        }
    }

    val customBlue = Color(0xFF0174B3)
    val customGreen = Color(0xFF1BBF69)
    val customOrange = Color(0xFFFF5151)
    
    val gradient = Brush.horizontalGradient(
        colors = listOf(customBlue, customGreen)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF5EE))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Logo Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_home),
                contentDescription = "Asha Sakhi Logo",
                modifier = Modifier
                    .size(250.dp)
                    .padding(16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ASHA ",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 64.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = customGreen
                )
                Text(
                    text = "सखी",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 64.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = customOrange
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "आपका AI-Powered सखी for\nSafer Motherhood!",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 24.sp
                ),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }

        // Bottom Section with Language Selector and Get Started Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            // Animated Language Selection Title
            AnimatedContent(
                targetState = currentTitleIndex,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) with
                    fadeOut(animationSpec = tween(600))
                }
            ) { index ->
                Text(
                    text = stringResource(languageTitles[index].stringResId),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = customBlue
                )
            }

            // Language Selector Dropdown
            Box {
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
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Get Started Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                onClick = onGetStarted,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(gradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.get_started),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    AshaTheme {
        WelcomeScreen(
            onGetStarted = { }
        )
    }
}

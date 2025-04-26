package com.littleb01s.ashasakhichat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.darrylbayliss.simonsays.domain.PlayViewModel
import com.darrylbayliss.simonsays.presentation.Instructions
import com.darrylbayliss.simonsays.presentation.InstructionsScreen
import com.darrylbayliss.simonsays.presentation.Play
import com.darrylbayliss.simonsays.presentation.PlayScreen
import com.darrylbayliss.simonsays.presentation.Welcome
import com.darrylbayliss.simonsays.presentation.WelcomeScreen
import com.darrylbayliss.simonsays.ui.theme.SimonSaysTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.ui.unit.LayoutDirection
import org.intellij.lang.annotations.Language

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val configuration = LocalConfiguration.current
            val locale = configuration.locales[0]
            
            CompositionLocalProvider(
                LocalLayoutDirection provides if (locale.language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                AshaTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        NavHost(
                            navController = navController,
                            startDestination = "welcome"
                        ) {
                            composable("welcome") {
                                WelcomeScreen(
                                    onGetStarted = {
                                        navController.navigate("home") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("home") {
                                HomeScreen(
                                    onNavigateToPatients = { /* TODO */ },
                                    onNavigateToTraining = { /* TODO */ },
                                    onNavigateToRiskAnalysis = { /* TODO */ },
                                    onNavigateToChat = { navController.navigate("chat") },
                                    onNavigateToMap = { /* TODO */ }
                                )
                            }
                            composable("chat") { 
                                ChatScreen(hiltViewModel()) 
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AshaSakhiChatApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Welcome) {
        composable<Welcome> {
            WelcomeScreen(
                onGetStarted = {
                    navController.navigate(Home)
                })
        }
        composable<Home> {
            HomeScreen(
                onNavigateToPatients = { /* TODO */ },
                onNavigateToTraining = { /* TODO */ },
                onNavigateToRiskAnalysis = { /* TODO */ },
                onNavigateToChat = { navController.navigate(Chat) },
                onNavigateToMap = { /* TODO */ }
            )
        }
        composable<Chat> { ChatScreen(hiltViewModel()) }
        composable<Instructions> { InstructionsScreen() }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AshaTheme {
        WelcomeScreen(
            onGetStarted = { },
        )
    }
}


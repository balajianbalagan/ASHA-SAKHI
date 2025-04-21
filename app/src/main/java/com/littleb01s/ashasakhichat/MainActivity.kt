package com.littleb01s.ashasakhichat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AshaTheme {
                AshaSakhiChatApp()
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
        composable<Chat> { ChatScreen(hiltViewModel<ChatViewModel>()) }
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


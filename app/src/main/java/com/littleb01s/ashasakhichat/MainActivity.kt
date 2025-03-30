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
import com.littleb01s.ashasakhichat.presentation.Instructions
import com.littleb01s.ashasakhichat.presentation.PlayViewModel
import com.littleb01s.ashasakhichat.presentation.InstructionsScreen
import com.littleb01s.ashasakhichat.presentation.Play
import com.littleb01s.ashasakhichat.presentation.PlayScreen
import com.littleb01s.ashasakhichat.presentation.Welcome
import com.littleb01s.ashasakhichat.presentation.WelcomeScreen
import com.littleb01s.ashasakhichat.ui.theme.AshaTheme
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
                onNavigateToPlay = {
                    navController.navigate(Play)
                },
                onNavigateToInstructions = {
                    navController.navigate(Instructions)
                })
        }
        composable<Play> { PlayScreen(hiltViewModel<PlayViewModel>()) }
        composable<Instructions> { InstructionsScreen() }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AshaTheme {
        WelcomeScreen(
            onNavigateToPlay = { },
            onNavigateToInstructions = { }
        )
    }
}


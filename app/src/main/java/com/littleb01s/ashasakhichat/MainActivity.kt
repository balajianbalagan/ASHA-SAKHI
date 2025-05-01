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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.unit.LayoutDirection
import com.littleb01s.ashasakhichat.presentation.ChatScreen
import org.intellij.lang.annotations.Language
import com.littleb01s.ashasakhichat.presentation.navigation.Screen
import com.littleb01s.ashasakhichat.presentation.screens.*
import com.littleb01s.ashasakhichat.presentation.HomeContent
import com.littleb01s.ashasakhichat.presentation.LoginScreen
import com.littleb01s.ashasakhichat.presentation.MainScaffold
import com.littleb01s.ashasakhichat.presentation.MainViewModel
import com.littleb01s.ashasakhichat.presentation.WelcomeScreen
import com.littleb01s.ashasakhichat.presentation.screens.riskanalysis.PregnancyRiskAssessmentScreen
import com.littleb01s.ashasakhichat.ui.theme.AshaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            AshaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Chat.route
                    ) {
                        composable(Screen.Chat.route) {
                            ChatScreen(hiltViewModel())
                        }
                        // ... existing code ...
                    }
                }
            }
        }
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


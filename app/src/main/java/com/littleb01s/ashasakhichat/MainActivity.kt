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
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val configuration = LocalConfiguration.current
            val locale = configuration.locales[0]
            val navController = rememberNavController()
            val mainViewModel: MainViewModel = hiltViewModel()
            
            // Determine start destination based on login status
            val startDestination = if (mainViewModel.isUserLoggedIn()) {
                Screen.Home.route
            } else {
                Screen.Welcome.route
            }
            
            CompositionLocalProvider(
                LocalLayoutDirection provides if (locale.language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                AshaTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = startDestination
                        ) {
                            composable(Screen.Welcome.route) {
                                WelcomeScreen(
                                    onGetStarted = {
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(Screen.Welcome.route) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(Screen.Login.route) {
                                LoginScreen(
                                    onLoginSuccess = {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // Bottom Navigation Screens
                            composable(Screen.Home.route) {
                                MainScaffold(
                                    currentRoute = Screen.Home.route,
                                    onNavigate = { route -> navController.navigate(route) },
                                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                                ) {
                                    HomeContent(
                                        onNavigateToTraining = { navController.navigate(Screen.Training.route) },
                                        onNavigateToRiskAnalysis = { navController.navigate(Screen.RiskAnalysis.route) },
                                        onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                                        onNavigateToMap = { navController.navigate(Screen.RegionalMap.route) },
                                        onNavigateToPatients = { navController.navigate(Screen.Patients.route) }
                                    )
                                }
                            }
                            composable(Screen.Patients.route) {
                                MainScaffold(
                                    currentRoute = Screen.Patients.route,
                                    onNavigate = { route -> navController.navigate(route) },
                                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                                ) {
                                    PatientsScreen(
                                        onPatientClick = { patient ->
                                            navController.navigate(Screen.PatientDetails.createRoute(patient.patientId))
                                        },
                                        onAddNewPatient = {
                                            navController.navigate(Screen.AddPatient.route)
                                        }
                                    )
                                }
                            }
                            composable(Screen.AddPatient.route) {
                                AddPatientScreen(
                                    onNavigateBack = { navController.navigateUp() }
                                )
                            }
                            composable(
                                route = Screen.PatientDetails.route,
                                arguments = listOf(
                                    navArgument("patientId") { type = NavType.IntType }
                                )
                            ) {
                                PatientDetailsScreen(
                                    onNavigateBack = { navController.navigateUp() }
                                )
                            }
                            composable(Screen.Notifications.route) {
                                MainScaffold(
                                    currentRoute = Screen.Notifications.route,
                                    onNavigate = { route -> navController.navigate(route) },
                                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                                ) {
                                    NotificationsScreen()
                                }
                            }
                            composable(Screen.Settings.route) {
                                MainScaffold(
                                    currentRoute = Screen.Settings.route,
                                    onNavigate = { route -> navController.navigate(route) },
                                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                                ) {
                                    SettingsScreen(
                                        onNavigateToHome = {
                                            navController.navigate(Screen.Home.route) {
                                                popUpTo(Screen.Home.route) { inclusive = true }
                                            }
                                        }
                                    )
                                }
                            }

                            // Full Screen Routes
                            composable(Screen.Training.route) {
                                TrainingScreen(
                                    onNavigateBack = { navController.navigateUp() }
                                )
                            }
                            composable(Screen.RiskAnalysis.route) {
                                RiskAnalysisScreen(
                                    onNavigateBack = { navController.navigateUp() },
                                    onNavigateToPregnancyRisk = { navController.navigate(Screen.PregnancyRiskAssessment.route) }
                                )
                            }
                            composable(Screen.PregnancyRiskAssessment.route) {
                                PregnancyRiskAssessmentScreen(
                                    onNavigateBack = { navController.navigateUp() }
                                )
                            }
                            composable(Screen.Chat.route) { 
                                ChatScreen(hiltViewModel()) 
                            }
                            composable(Screen.RegionalMap.route) {
                                RegionalMapScreen(
                                    onNavigateBack = { navController.navigateUp() }
                                )
                            }
                            composable(Screen.Profile.route) {
                                ProfileScreen(
                                    onNavigateBack = { navController.navigateUp() },
                                    onSignOut = {
                                        // Navigate to Welcome screen and clear backstack
                                        navController.navigate(Screen.Welcome.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
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


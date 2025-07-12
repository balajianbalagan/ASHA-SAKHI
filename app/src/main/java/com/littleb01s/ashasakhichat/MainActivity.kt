package com.littleb01s.ashasakhichat

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import com.littleb01s.ashasakhichat.presentation.navigation.Screen
import com.littleb01s.ashasakhichat.presentation.screens.*
import com.littleb01s.ashasakhichat.presentation.HomeContent
import com.littleb01s.ashasakhichat.presentation.LoginScreen
import com.littleb01s.ashasakhichat.presentation.MainScaffold
import com.littleb01s.ashasakhichat.presentation.MainViewModel
import com.littleb01s.ashasakhichat.presentation.SpeechRecognitionScreen
import com.littleb01s.ashasakhichat.presentation.WelcomeScreen
import com.littleb01s.ashasakhichat.presentation.screens.riskanalysis.PregnancyRiskAssessmentScreen
import com.littleb01s.ashasakhichat.ui.theme.AshaTheme
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.AndroidEntryPoint
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.littleb01s.ashasakhichat.presentation.PatientsViewModel
import android.util.Log
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initialize PDFBox resources
        PDFBoxResourceLoader.init(applicationContext)

        setContent {
            val configuration = LocalConfiguration.current
            val locale = configuration.locales[0]
            val navController = rememberNavController()
            val mainViewModel: MainViewModel = hiltViewModel()
            val patientsViewModel: PatientsViewModel = hiltViewModel()
            val context = LocalContext.current

            // Observe toastMessage and show Toast
            val toastMessage by mainViewModel.toastMessage.collectAsState()
            LaunchedEffect(toastMessage) {
                toastMessage?.let {
                    Log.d("MainActivity", "Showing toast: $it")
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    // Add a small delay before clearing to ensure toast is visible
                    delay(100)
                    mainViewModel.clearToast()
                }
            }

            // Debug: Add a test button to manually trigger sync
            LaunchedEffect(Unit) {
                Log.d("MainActivity", "MainActivity initialized")
                Log.d("MainActivity", "User logged in: ${mainViewModel.isUserLoggedIn()}")
            }

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
                        Box(modifier = Modifier.fillMaxSize()) {
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
                                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                                        mainViewModel = mainViewModel
                                    ) {
                                        HomeContent(
                                            navController = navController,
                                            onNavigateToTraining = { navController.navigate(Screen.Training.route) },
                                            onNavigateToRiskAnalysis = { navController.navigate(Screen.RiskAnalysis.route) },
                                            onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                                            onNavigateToMap = { navController.navigate(Screen.RegionalMap.route) },
                                            onNavigateToPatients = { navController.navigate(Screen.Patients.route) },
                                            onNavigateToSpeecher = { navController.navigate(Screen.SpeechRecognitionScreen.route)}
                                        )
                                    }
                                }
                                composable(Screen.Patients.route) {
                                    MainScaffold(
                                        currentRoute = Screen.Patients.route,
                                        onNavigate = { route -> navController.navigate(route) },
                                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                                        mainViewModel = mainViewModel
                                    ) {
                                        PatientsScreen(
                                            onPatientClick = { patient ->
                                                navController.navigate(Screen.PatientDetails.createRoute(patient.patientId))
                                            },
                                            onAddNewPatient = {
                                                navController.navigate(Screen.AddPatient.route)
                                            },
                                            appointmentViewModel = hiltViewModel()
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
                                    val patientId = it.arguments?.getInt("patientId") ?: return@composable
                                    PatientDetailsScreen(
                                        patientId = patientId,
                                        navController = navController,
                                        onNavigateBack = { navController.navigateUp() }
                                    )
                                }
                                composable(Screen.Calendar.route) {
                                    MainScaffold(
                                        currentRoute = Screen.Calendar.route,
                                        onNavigate = { route -> navController.navigate(route) },
                                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                                        mainViewModel = mainViewModel
                                    ) {
                                        CalendarScreen()
                                    }
                                }
                                composable(Screen.Settings.route) {
                                    MainScaffold(
                                        currentRoute = Screen.Settings.route,
                                        onNavigate = { route -> navController.navigate(route) },
                                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                                        mainViewModel = mainViewModel
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
                                        onNavigateToPregnancyRisk = { pId ->
                                            navController.navigate("${Screen.PregnancyRiskAssessment.route}/$pId")
                                        },
                                        patientId = null
                                    )
                                }
                                composable(
                                    route = "${Screen.PregnancyRiskAssessment.route}/{patientId}",
                                    arguments = listOf(
                                        navArgument("patientId") { type = NavType.IntType }
                                    )
                                ) { backStackEntry ->
                                    val patientId = backStackEntry.arguments?.getInt("patientId")
                                    PregnancyRiskAssessmentScreen(
                                        patientId = patientId,
                                        onNavigateBack = { navController.navigateUp() },
                                        onNavigateToAddCheckup = { id: Int ->
                                            navController.navigate("${Screen.AddCheckup.route}/$id")
                                        }
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
                                composable(Screen.SpeechRecognitionScreen.route) {
                                    SpeechRecognitionScreen(
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

                                // Add Medical History and Diet Suggestions routes
                                composable(
                                    route = "${Screen.MedicalHistory.route}/{patientId}",
                                    arguments = listOf(
                                        navArgument("patientId") { type = NavType.IntType }
                                    )
                                ) { backStackEntry ->
                                    val patientId = backStackEntry.arguments?.getInt("patientId") ?: return@composable
                                    MedicalHistoryScreen(
                                        patientId = patientId,
                                        onNavigateBack = { navController.navigateUp() },
                                        onAddCheckup = { id -> 
                                            navController.navigate("${Screen.AddCheckup.route}/$id")
                                        },
                                        navController= navController
                                    )
                                }

                                composable(
                                    route = "${Screen.DietSuggestions.route}/{patientId}/{phoneNumber}",
                                    arguments = listOf(
                                        navArgument("patientId") { type = NavType.IntType },
                                        navArgument("phoneNumber") { type = NavType.StringType }
                                    )
                                ) { backStackEntry ->
                                    val patientId = backStackEntry.arguments?.getInt("patientId") ?: return@composable
                                    val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: return@composable
                                    DietSuggestionsScreen(
                                        patientId = patientId,
                                        phoneNumber = phoneNumber,
                                        onNavigateBack = { navController.navigateUp() }
                                    )
                                }

                                // Add the AddCheckup screen route
                                composable(
                                    route = "${Screen.AddCheckup.route}/{patientId}",
                                    arguments = listOf(
                                        navArgument("patientId") { type = NavType.IntType }
                                    )
                                ) { backStackEntry ->
                                    val patientId = backStackEntry.arguments?.getInt("patientId") ?: return@composable
                                    AddPatientRecordScreen(
                                        patientId = patientId,
                                        onNavigateBack = { navController.navigateUp() }
                                    )
                                }

                                // Add Patient Record View screen route
                                composable(
                                    route = Screen.PatientRecordView.route,
                                    arguments = listOf(
                                        navArgument("checkupId") { type = NavType.IntType }
                                    )
                                ) { backStackEntry ->
                                    val checkupId = backStackEntry.arguments?.getInt("checkupId") ?: return@composable
                                    PatientRecordViewScreen(
                                        checkupId = checkupId,
                                        onNavigateBack = { navController.navigateUp() }
                                    )
                                }

                                // Add Appointments screen route
                                composable(
                                    route = Screen.Appointments.route,
                                    arguments = listOf(
                                        navArgument("patientId") { type = NavType.IntType }
                                    )
                                ) { backStackEntry ->
                                    val patientId = backStackEntry.arguments?.getInt("patientId") ?: return@composable
                                    AppointmentsScreen(
                                        patientId = patientId,
                                        onNavigateBack = { navController.navigateUp() },
                                        onNavigateToAddAppointment = { 
                                            navController.navigate(Screen.AddAppointment.createRoute(patientId))
                                        }
                                    )
                                }
                                composable(
                                    route = Screen.AddAppointment.route,
                                    arguments = listOf(
                                        navArgument("patientId") { type = NavType.IntType }
                                    )
                                ) { backStackEntry ->
                                    val patientId = backStackEntry.arguments?.getInt("patientId") ?: return@composable
                                    AddAppointmentScreen(
                                        patientId = patientId,
                                        onNavigateBack = { navController.navigateUp() }
                                    )
                                }
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

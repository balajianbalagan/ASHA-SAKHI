package com.littleb01s.ashasakhichat.presentation.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Home : Screen("home")
    object Patients : Screen("patients")
    object Training : Screen("training")
    object RiskAnalysis : Screen("risk_analysis")
    object PregnancyRiskAssessment : Screen("pregnancy_risk_assessment")
    object Chat : Screen("chat")
    object RegionalMap : Screen("regional_map")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Notifications : Screen("notifications")
} 
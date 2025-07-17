package com.littleb01s.ashasakhichat.presentation.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Home : Screen("home")
    object Patients : Screen("patients")
    object Training : Screen("training")
    object RiskAnalysis : Screen("risk_analysis")
    object MedicalHistory : Screen("medical_history")
    object DietSuggestions : Screen("diet_suggestions")
    object PregnancyRiskAssessment : Screen("pregnancy_risk_assessment")
    object Chat : Screen("chat")
    object RegionalMap : Screen("regional_map")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Calendar : Screen("calendar")
    object PatientDetails : Screen("patient/{patientId}") {
        fun createRoute(patientId: Int) = "patient/$patientId"
    }
    object AddPatient : Screen("add_patient")
    object AddCheckup : Screen("add_checkup")
    object SpeechRecognitionScreen : Screen("speech_recognition")
    object PatientRecordView : Screen("patient_record_view/{checkupId}") {
        fun createRoute(checkupId: Int) = "patient_record_view/$checkupId"
    }
    object Appointments : Screen("appointments/{patientId}") {
        fun createRoute(patientId: Int) = "appointments/$patientId"
    }
    object AddAppointment : Screen("add_appointment/{patientId}") {
        fun createRoute(patientId: Int) = "add_appointment/$patientId"
    }
    object AppointmentDetails : Screen("appointment_details/{appointmentId}") {
        fun createRoute(appointmentId: Int) = "appointment_details/$appointmentId"
    }
    object CourseDetail : Screen("course_detail/{courseId}") {
        fun createRoute(courseId: String) = "course_detail/$courseId"
    }
    object PatientSchemes : Screen("patient_schemes/{patientId}") {
        fun createRoute(patientId: Int) = "patient_schemes/$patientId"
    }
} 
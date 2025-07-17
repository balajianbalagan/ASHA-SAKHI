package com.littleb01s.ashasakhichat.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.Date

interface PatientService {
    @GET("api/patient/patient-list")
    suspend fun getAllPatients(
        @retrofit2.http.Query("workerId") workerId: Int? = null,
        @retrofit2.http.Query("updatedAt") updatedAt: String? = null
    ): Response<PatientListResponse>

    @POST("api/patient/save-patient")
    suspend fun savePatient(@Body request: SavePatientRequest): Response<SavePatientResponse>

    @GET("api/patient/fetch-schemes")
    suspend fun fetchSchemes(@retrofit2.http.Query("patientId") patientId: Int): Response<SchemeListResponse>
}

data class PatientListResponse(
    val data: List<PatientResponse>
)

data class PatientResponse(
    val patientId: Int,
    val state: String?,
    val city: String?,
    val languagePreference: String?,
    val firstName: String,
    val lastName: String?,
    val dateOfBirth: String,
    val deliveryDate: String?,
    val mobileNumber: String,
    val employmentStatus: String?,
    val religion: String?,
    val education: String?,
    val caste: String?,
    val bloodGroup: String?,
    val previousIllness: String?,
    val createdAt: String,
    val updatedAt: String,
    val checkupData: List<CheckupResponse>,
    val appointmentData: List<PatientAppointmentResponse>,
    val schemeData: List<SchemeResponse>? = null,
    val lmp: String?,
    val latitude: String?,
    val longtitude: String?,
    val profilePhoto: String?,
    val pregnancyStage: String?
)

data class CheckupResponse(
    val checkupId: Int,
    val workerId: Int?,
    val patientId: Int,
    val bloodPressure: String?,
    val oxygen: Float?,
    val weight: Float?,
    val temperature: Float?,
    val sugarLevel: Float?,
    val bmi: Float?,
    val haemoglobin: String?,
    val checkupData: String?,
    val pregnancyStage: String?,
    val checkupStatus: Int?
)

data class PatientAppointmentResponse(
    val appointmentId: Int,
    val workerId: Int,
    val patientId: Int,
    val appointmentDate: String,
    val appointmentStatus: String,
    val appointmentType: String?,
    val appointmentName: String?,
    val appointmentDescription: String?,
    val appointmentPriority: Int?
)

data class SavePatientRequest(
    val patientId: Int? = null,
    val workerId: Int,
    val patientData: PatientData,
    val vitals: VitalsData? = null
)

data class PatientData(
    val firstName: String,
    val lastName: String?,
    val state: String?,
    val city: String?,
    val languagePreference: String?,
    val dateOfBirth: Date,
    val deliveryDate: Date?,
    val mobileNumber: String,
    val employmentStatus: String?,
    val religion: String?,
    val education: String?,
    val caste: String?,
    val bloodGroup: String?,
    val previousIllness: String?,
    val lmp: Date?,
    val latitude: String?,
    val longtitude: String?,
    val profilePhoto: String?,
    val pregnancyStage: String?
)

data class VitalsData(
    val bloodPressure: Float?,
    val oxygen: Float?,
    val weight: Float?,
    val temperature: Float?,
    val sugarLevel: Float?,
    val bmi: Float?,
    val haemoglobin: String?,
    val checkupData: String?,
    val pregnancyStage: String?,
    val checkupStatus: Int?
)

data class AddPatientResponse(
    val message: String,
    val patientData: PatientResponse,
    val schemeData: List<SchemeResponse>? = null
)

data class SavePatientResponse(
    val data: AddPatientResponse
)

data class SchemeListResponse(
    val data: List<SchemeResponse>
)

data class SchemeResponse(
    val schemeId: Int?,
    val patientId: Int,
    val schemeName: String,
    val state: String,
    val description: String,
    val eligibility: String,
    val howToApply: String
)


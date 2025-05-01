package com.littleb01s.ashasakhichat.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.Date

interface PatientService {
    @GET("api/patient/patient-list")
    suspend fun getAllPatients(): Response<PatientListResponse>

    @GET("api/patient/patient-list/{workerId}")
    suspend fun getPatientsByWorkerId(@Path("workerId") workerId: Int): Response<PatientListResponse>

    @POST("api/patient/save-patient")
    suspend fun savePatient(@Body request: SavePatientRequest): Response<SavePatientResponse>
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
    val checkupData: List<CheckupResponse>
)

data class CheckupResponse(
    val checkupId: Int,
    val workerId: Int?,
    val patientId: Int,
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
    val lmp: Date?
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
    val patientData: PatientResponse
)

data class SavePatientResponse(
    val data: AddPatientResponse
)


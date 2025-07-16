package com.littleb01s.ashasakhichat.data.api

import com.littleb01s.ashasakhichat.data.local.entity.Appointment
import com.littleb01s.ashasakhichat.data.repository.CreateAppointmentRequest
import retrofit2.http.*
import retrofit2.Response

interface AppointmentApi {
    @GET("api/appointment/fetch-appointments")
    suspend fun fetchAppointments(
        @Query("workerId") workerId: String,
        @Query("updatedAt") updatedAt: String? = null
    ): AppointmentListResponseWrapper

    @POST("api/appointment/create-appointments")
    suspend fun createAppointment(
        @Body request: CreateAppointmentRequest
    ): AppointmentResponseWrapper
    
    // New appointment action endpoints
    @POST("api/appointment/send-reminder")
    suspend fun sendReminder(
        @Body request: SendReminderRequest
    ): Response<SendReminderResponse>
    
    @GET("api/appointment/fetch-associated-checkups")
    suspend fun fetchCheckupsForAppointment(
        @Query("appointmentId") appointmentId: Int
    ): Response<FetchCheckupsResponse>
    
    @POST("api/appointment/save-appointment")
    suspend fun saveOrUpdateAppointment(
        @Body request: AppointmentStatusUpdateRequest
    ): Response<SaveAppointmentResponse>
}

// Data classes for appointment actions
data class SendReminderRequest(
    val appointmentId: Int
)

data class SendReminderResponse(
    val data: ReminderData
)

data class ReminderData(
    val reminderSent: Boolean
)

data class FetchCheckupsResponse(
    val data: List<AppointmentCheckupResponse>
)

data class AppointmentCheckupResponse(
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
    val checkupType: String?,
    val pregnancyStage: String?,
    val checkupStatus: Int?,
    val checkupTime: String?,
    val createdAt: String,
    val updatedAt: String
)

data class AppointmentStatusUpdateRequest(
    val appointmentData: AppointmentUpdateData,
    val checkupIds: List<Int>?=null
)

data class AppointmentData(
    val appointmentId: Int? = null,
    val workerId: Int,
    val patientId: Int,
    val appointmentDate: String,
    val appointmentStatus: String,
    val appointmentType: String?,
    val appointmentName: String?,
    val appointmentDescription: String?,
    val appointmentPriority: Int?
)

data class SaveAppointmentResponse(
    val data: SaveAppointmentData
)

data class SaveAppointmentData(
    val appointmentId: Int,
    val message: String
)


data class AppointmentUpdateData(
    val appointmentId: Int, // Required - the server ID
    val appointmentStatus: String? = null, // Optional - only send if changing
    val appointmentType: String? = null, // Optional - only send if changing
    val appointmentName: String? = null, // Optional - only send if changing
    val appointmentDescription: String? = null, // Optional - only send if changing
    val appointmentPriority: Int? = null, // Optional - only send if changing
    val appointmentDate: String? = null // Optional - only send if changing
) 
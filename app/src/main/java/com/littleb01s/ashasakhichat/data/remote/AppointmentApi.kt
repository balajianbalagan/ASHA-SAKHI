package com.littleb01s.ashasakhichat.data.remote

import com.littleb01s.ashasakhichat.data.local.entity.Appointment
import com.littleb01s.ashasakhichat.data.api.AppointmentListResponse
import com.littleb01s.ashasakhichat.data.api.AppointmentResponse
import retrofit2.http.*

interface AppointmentApi {
    @GET("fetch-appointments")
    suspend fun fetchAppointments(
        @Query("workerId") workerId: String
    ): AppointmentListResponse

    @POST("appointments/create-appointments")
    suspend fun createAppointment(
        @Body appointment: Appointment
    ): AppointmentResponse
} 
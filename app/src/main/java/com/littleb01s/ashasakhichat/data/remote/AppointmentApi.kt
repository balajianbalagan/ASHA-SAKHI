package com.littleb01s.ashasakhichat.data.remote

import com.littleb01s.ashasakhichat.data.model.Appointment
import com.littleb01s.ashasakhichat.data.model.AppointmentListResponse
import com.littleb01s.ashasakhichat.data.model.AppointmentResponse
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
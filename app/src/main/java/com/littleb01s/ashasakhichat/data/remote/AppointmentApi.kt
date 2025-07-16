package com.littleb01s.ashasakhichat.data.remote

import com.littleb01s.ashasakhichat.data.local.entity.Appointment
import com.littleb01s.ashasakhichat.data.api.AppointmentListResponse
import com.littleb01s.ashasakhichat.data.api.AppointmentResponse
import com.littleb01s.ashasakhichat.data.api.AppointmentResponseWrapper
import com.littleb01s.ashasakhichat.data.api.AppointmentListResponseWrapper
import com.littleb01s.ashasakhichat.data.repository.CreateAppointmentRequest
import retrofit2.http.*

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
} 
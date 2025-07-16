package com.littleb01s.ashasakhichat.data.api

import com.littleb01s.ashasakhichat.data.local.entity.Appointment

data class AppointmentResponse(
    val message: String,
    val appointment: Appointment? = null
)

data class AppointmentListResponse(
    val appointments: List<Appointment>
)

// Wrapper for server response that has a "data" field
data class AppointmentResponseWrapper(
    val data: AppointmentResponse
)

// Server response wrapper for appointment list
data class AppointmentListResponseWrapper(
    val data: List<Appointment>
) 
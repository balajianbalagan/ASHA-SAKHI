package com.littleb01s.ashasakhichat.data.model

import java.time.LocalDateTime
import java.util.Date

data class Appointment(
    val appointmentId: Int? = null,
    val workerId: Int,
    val patientId: Int,
    val appointmentDate: Date,
    val appointmentType:String,
    val appointmentStatus: String,
    val needsUpload: Boolean = false,
    val needsDownload: Boolean = false,
    val lastDownloadedAt: Date? = null,
    val serverId: String? = null
)

data class AppointmentResponse(
    val message: String,
    val data: Appointment? = null
)

data class AppointmentListResponse(
    val appointments: List<Appointment>
) 
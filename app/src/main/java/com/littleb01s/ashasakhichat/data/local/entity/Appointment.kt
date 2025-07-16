package com.littleb01s.ashasakhichat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.littleb01s.ashasakhichat.data.local.converters.Converters
import java.util.Date

@Entity(tableName = "TBL_APPOINTMENT")
@TypeConverters(Converters::class)
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val appointmentId: Int = 0,
    val workerId: Int,
    val patientId: Int,
    val appointmentDate: Date,
    val appointmentStatus: String,
    val appointmentType: String?,
    val appointmentDescription: String? = null,
    val appointmentName: String? = null,
    val appointmentPriority: Int? = null,
    // Sync fields
    val needsUpload: Boolean = true,
    val needsDownload: Boolean = false,
    val lastUploadedAt: Date? = null,
    val lastDownloadedAt: Date? = null,
    val serverUpdatedAt: Date? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val localVersion: Int = 1,
    val serverId: Int? = null,
    // Offline change tracking
    val offlineChangeFlags: List<String>? = null
)

// Offline change flags
object AppointmentOfflineFlags {
    const val CANCELLED = "CANCELLED"
    const val IN_PROGRESS = "IN_PROGRESS"
    const val COMPLETED = "COMPLETED"
    const val CHECKUPS_ADDED = "CHECKUPS_ADDED"
    const val REMINDER_SENT = "REMINDER_SENT"
} 
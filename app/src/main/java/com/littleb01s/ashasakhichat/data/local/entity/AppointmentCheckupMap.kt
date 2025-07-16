package com.littleb01s.ashasakhichat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "TBL_APPOINTMENT_CHECKUP_MAP",
    primaryKeys = ["appointmentId", "checkupId"]
)
data class AppointmentCheckupMap(
    val appointmentId: Int,
    val checkupId: Int,
    
    // Sync status fields
    val needsUpload: Boolean = true,  // True if local changes need to be uploaded to server
    val needsDownload: Boolean = false,  // True if server has newer data that needs to be downloaded
    
    // Sync timestamps
    val lastUploadedAt: Date? = null,  // When this record was last uploaded to server
    val lastDownloadedAt: Date? = null,  // When this record was last updated from server
    val serverUpdatedAt: Date? = null,  // Last update timestamp from server

    // Local tracking
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val localVersion: Int = 1,
    
    // Server ID tracking (in case server uses different ID)
    val serverAppointmentId: Int? = null,  // ID of appointment on the server, if any
    val serverCheckupId: Int? = null  // ID of checkup on the server, if any
) 
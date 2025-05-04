package com.littleb01s.ashasakhichat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "TBL_PATIENT_DIET")
data class Diet(
    @PrimaryKey(autoGenerate = true)
    val dietId: Int = 0,
    val patientId: Int,
    val dietData: String,
    // Sync fields
    val needsUpload: Boolean = true,
    val needsDownload: Boolean = false,
    val lastUploadedAt: Date? = null,
    val lastDownloadedAt: Date? = null,
    val serverUpdatedAt: Date? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val localVersion: Int = 1,
    val serverId: Int? = null
) 
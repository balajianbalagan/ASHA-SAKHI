package com.littleb01s.ashasakhichat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "TBL_INFANT")
data class Infant(
    @PrimaryKey(autoGenerate = true)
    val infantId: Int = 0,
    val patientId: Int,
    val workerId: Int,
    val gender: String? = null,
    val dateOfBirth: Date? = null,
    val weightAtBirth: Float,
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
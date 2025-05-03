package com.littleb01s.ashasakhichat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "TBL_CHECKUP")
data class Checkup(
    @PrimaryKey(autoGenerate = true)
    val checkupId: Int = 0,
    val workerId: Int? = null,
    val patientId: Int,
    val bloodPressure: String? = null,
    val oxygen: Float? = null,
    val weight: Float? = null,
    val temperature: Float? = null,
    val sugarLevel: Float? = null,
    val bmi: Float? = null,
    val haemoglobin: String? = null,
    val checkupData: String? = null,
    val checkupType: String? = null,
    val pregnancyStage: String? = null,
    val checkupStatus: Int? = null,
    val checkupTime: Date? = null,
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
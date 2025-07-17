package com.littleb01s.ashasakhichat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import java.util.Date

@Entity(
    tableName = "TBL_RISK_ASSESSMENT",
    indices = [Index(value = ["patientId"], unique = true)]
)
data class RiskAnalysisResult(
    @PrimaryKey(autoGenerate = true)
    val riskId: Int = 0,
    
    val patientId: Int,
    val checkupId: Int,
    val comments: String, // JSON string containing all the detailed data
    val riskValue: String, // Could be "LOW", "MEDIUM", "HIGH", etc.
    
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
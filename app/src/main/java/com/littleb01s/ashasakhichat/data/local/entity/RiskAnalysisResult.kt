package com.littleb01s.ashasakhichat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "TBL_RISK_ANALYSIS")
data class RiskAnalysisResult(
    @PrimaryKey(autoGenerate = true)
    val analysisId: Int = 0,
    
    val patientId: Int,
    val riskLevel: String, // Could be "LOW", "MEDIUM", "HIGH", etc.
    val analysisData: String, // JSON string containing all the detailed data
    
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
package com.littleb01s.ashasakhichat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.littleb01s.ashasakhichat.data.local.converters.Converters
import java.util.Date

@Entity(tableName = "TBL_PATIENT_SCHEME")
@TypeConverters(Converters::class)
data class Scheme(
    @PrimaryKey(autoGenerate = true)
    val schemeId: Int = 0,
    
    val patientId: Int,
    val schemeName: String,
    val state: String,
    val description: String,
    val eligibility: String,
    val howToApply: String,
    
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

// Offline change flags for Scheme
object SchemeOfflineFlags {
    const val CREATED = "CREATED"
    const val UPDATED = "UPDATED"
    const val DELETED = "DELETED"
} 
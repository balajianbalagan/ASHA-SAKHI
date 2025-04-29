package com.littleb01s.ashasakhichat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "TBL_PROFILE_PATIENT")
data class Patient(
    @PrimaryKey(autoGenerate = true)
    val patientId: Int = 0,
    
    val state: String? = null,
    val city: String? = null,
    val languagePreference: String? = null,
    
    val firstName: String,
    val lastName: String? = null,
    
    val dateOfBirth: Date,
    val deliveryDate: Date? = null,
    
    val mobileNumber: String,
    
    val employmentStatus: String? = null,
    val religion: String? = null,
    val education: String? = null,
    val caste: String? = null,
    val bloodGroup: String? = null,
    val previousIllness: String? = null,
    
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
    val serverId: Int? = null  // ID of this record on the server, if any
) 
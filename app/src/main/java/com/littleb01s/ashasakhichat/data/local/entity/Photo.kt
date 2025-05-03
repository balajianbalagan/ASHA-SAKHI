package com.littleb01s.ashasakhichat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "TBL_CHECKUP_PHOTOS")
data class Photo(
    @PrimaryKey(autoGenerate = true)
    val photoId: Int = 0,
    val checkupId: Int,
    val photoData: String, // Base64 encoded photo data or path
    val localPath: String? = null, // local device path if stored as file
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
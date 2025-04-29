package com.littleb01s.ashasakhichat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "TBL_DOCUMENTS")
data class Document(
    @PrimaryKey(autoGenerate = true)
    val documentId: Int = 0,
    val checkupId: Int,
    val documentPath: String, // server path
    val documentName: String? = null,
    val localPath: String? = null, // local device path
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
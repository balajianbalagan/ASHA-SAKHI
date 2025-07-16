package com.littleb01s.ashasakhichat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "TBL_SYNC_TIMESTAMPS")
data class SyncTimestamp(
    @PrimaryKey
    val entityType: String, // e.g., "appointments", "patients", "checkups"
    val lastSyncTime: Date,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) 
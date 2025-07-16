package com.littleb01s.ashasakhichat.data.local.dao

import androidx.room.*
import com.littleb01s.ashasakhichat.data.local.entity.SyncTimestamp
import java.util.Date

@Dao
interface SyncTimestampDao {
    @Query("SELECT * FROM TBL_SYNC_TIMESTAMPS WHERE entityType = :entityType")
    suspend fun getLastSyncTime(entityType: String): SyncTimestamp?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncTimestamp(syncTimestamp: SyncTimestamp)

    @Update
    suspend fun updateSyncTimestamp(syncTimestamp: SyncTimestamp)

    @Query("UPDATE TBL_SYNC_TIMESTAMPS SET lastSyncTime = :timestamp WHERE entityType = :entityType")
    suspend fun updateLastSyncTime(entityType: String, timestamp: Date)

    @Query("SELECT * FROM TBL_SYNC_TIMESTAMPS")
    suspend fun getAllSyncTimestamps(): List<SyncTimestamp>

    @Query("DELETE FROM TBL_SYNC_TIMESTAMPS WHERE entityType = :entityType")
    suspend fun deleteSyncTimestamp(entityType: String)

    @Query("DELETE FROM TBL_SYNC_TIMESTAMPS")
    suspend fun clearAllSyncTimestamps()
} 
package com.littleb01s.ashasakhichat.data.local.dao

import androidx.room.*
import com.littleb01s.ashasakhichat.data.local.entity.Scheme
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface SchemeDao {
    @Query("SELECT * FROM TBL_PATIENT_SCHEME")
    fun getAllSchemes(): Flow<List<Scheme>>

    @Query("SELECT * FROM TBL_PATIENT_SCHEME WHERE schemeId = :schemeId")
    fun getSchemeById(schemeId: Int): Flow<Scheme?>

    @Query("SELECT * FROM TBL_PATIENT_SCHEME WHERE patientId = :patientId")
    fun getSchemesByPatientId(patientId: Int): Flow<List<Scheme>>

    @Query("SELECT * FROM TBL_PATIENT_SCHEME WHERE state = :state")
    fun getSchemesByState(state: String): Flow<List<Scheme>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheme(scheme: Scheme): Long

    @Update
    suspend fun updateScheme(scheme: Scheme)

    @Delete
    suspend fun deleteScheme(scheme: Scheme)

    @Query("SELECT * FROM TBL_PATIENT_SCHEME WHERE schemeName LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchSchemes(query: String): Flow<List<Scheme>>

    // Upload sync related queries
    @Query("SELECT * FROM TBL_PATIENT_SCHEME WHERE needsUpload = 1")
    fun getSchemesToUpload(): Flow<List<Scheme>>

    @Query("SELECT * FROM TBL_PATIENT_SCHEME WHERE needsUpload = 1")
    suspend fun getSchemesToUploadImmediate(): List<Scheme>

    @Query("UPDATE TBL_PATIENT_SCHEME SET needsUpload = 0, lastUploadedAt = :timestamp, serverId = :serverId WHERE schemeId = :schemeId")
    suspend fun markSchemeAsUploaded(schemeId: Int, serverId: Int, timestamp: Date)

    @Query("UPDATE TBL_PATIENT_SCHEME SET needsUpload = 0, lastUploadedAt = :timestamp WHERE schemeId IN (:schemeIds)")
    suspend fun markSchemesAsUploaded(schemeIds: List<Int>, timestamp: Date)

    // Download sync related queries
    @Query("SELECT * FROM TBL_PATIENT_SCHEME WHERE needsDownload = 1")
    fun getSchemesToDownload(): Flow<List<Scheme>>

    @Query("UPDATE TBL_PATIENT_SCHEME SET needsDownload = 0, lastDownloadedAt = :timestamp WHERE schemeId = :schemeId")
    suspend fun markSchemeAsDownloaded(schemeId: Int, timestamp: Date)

    @Query("UPDATE TBL_PATIENT_SCHEME SET needsDownload = 1 WHERE serverId IN (:serverIds)")
    suspend fun markSchemesForDownload(serverIds: List<Int>)

    // Sync status queries
    @Query("SELECT COUNT(*) FROM TBL_PATIENT_SCHEME WHERE needsUpload = 1")
    fun getPendingUploadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM TBL_PATIENT_SCHEME WHERE needsDownload = 1")
    fun getPendingDownloadCount(): Flow<Int>

    // Server ID related queries
    @Query("SELECT * FROM TBL_PATIENT_SCHEME WHERE serverId = :serverId")
    suspend fun getSchemeByServerId(serverId: Int): Scheme?

    @Query("UPDATE TBL_PATIENT_SCHEME SET serverId = :serverId WHERE schemeId = :localId")
    suspend fun updateServerId(localId: Int, serverId: Int)

    // Batch operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchemes(schemes: List<Scheme>)

    @Query("""
        UPDATE TBL_PATIENT_SCHEME 
        SET updatedAt = :updateTime,
            needsUpload = 1,
            needsDownload = 0
        WHERE schemeId = :schemeId
    """)
    suspend fun updateSchemeTimestamp(schemeId: Int, updateTime: Date)

    // Conflict detection
    @Query("""
        SELECT * FROM TBL_PATIENT_SCHEME 
        WHERE serverUpdatedAt > lastDownloadedAt 
        OR lastDownloadedAt IS NULL
    """)
    fun getSchemesWithPossibleConflicts(): Flow<List<Scheme>>

    @Query("DELETE FROM TBL_PATIENT_SCHEME")
    suspend fun clearAllSchemes()

    @Query("DELETE FROM TBL_PATIENT_SCHEME WHERE patientId = :patientId")
    suspend fun deleteSchemesByPatientId(patientId: Int)
} 
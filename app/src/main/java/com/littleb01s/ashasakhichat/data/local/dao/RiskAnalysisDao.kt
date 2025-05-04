package com.littleb01s.ashasakhichat.data.local.dao

import androidx.room.*
import com.littleb01s.ashasakhichat.data.local.entity.RiskAnalysisResult
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface RiskAnalysisDao {
    @Query("SELECT * FROM TBL_RISK_ANALYSIS")
    fun getAllAnalyses(): Flow<List<RiskAnalysisResult>>

    @Query("SELECT * FROM TBL_RISK_ANALYSIS WHERE analysisId = :id")
    suspend fun getAnalysisById(id: Int): RiskAnalysisResult?

    @Query("SELECT * FROM TBL_RISK_ANALYSIS WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getAnalysesForPatient(patientId: Int): Flow<List<RiskAnalysisResult>>

    @Query("SELECT * FROM TBL_RISK_ANALYSIS WHERE patientId = :patientId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestAnalysisForPatient(patientId: Int): RiskAnalysisResult?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: RiskAnalysisResult): Long

    @Update
    suspend fun updateAnalysis(analysis: RiskAnalysisResult)

    @Delete
    suspend fun deleteAnalysis(analysis: RiskAnalysisResult)

    // Sync-related queries
    @Query("SELECT * FROM TBL_RISK_ANALYSIS WHERE needsUpload = 1")
    fun getAnalysesToUpload(): Flow<List<RiskAnalysisResult>>

    @Query("SELECT * FROM TBL_RISK_ANALYSIS WHERE needsDownload = 1")
    fun getAnalysesToDownload(): Flow<List<RiskAnalysisResult>>

    @Query("UPDATE TBL_RISK_ANALYSIS SET needsUpload = 0, lastUploadedAt = :timestamp, serverId = :serverId WHERE analysisId = :analysisId")
    suspend fun markAnalysisAsUploaded(analysisId: Int, serverId: Int, timestamp: Date)

    @Query("UPDATE TBL_RISK_ANALYSIS SET needsDownload = 0, lastDownloadedAt = :timestamp WHERE analysisId = :analysisId")
    suspend fun markAnalysisAsDownloaded(analysisId: Int, timestamp: Date)

    @Query("UPDATE TBL_RISK_ANALYSIS SET needsDownload = 1 WHERE serverId IN (:serverIds)")
    suspend fun markAnalysesForDownload(serverIds: List<Int>)

    // Server ID related queries
    @Query("SELECT * FROM TBL_RISK_ANALYSIS WHERE serverId = :serverId")
    suspend fun getAnalysisByServerId(serverId: Int): RiskAnalysisResult?

    @Query("UPDATE TBL_RISK_ANALYSIS SET serverId = :serverId WHERE analysisId = :localId")
    suspend fun updateServerId(localId: Int, serverId: Int)

    // Batch operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyses(analyses: List<RiskAnalysisResult>)
} 
package com.littleb01s.ashasakhichat.data.repository

import com.littleb01s.ashasakhichat.data.api.RiskAssessmentService
import com.littleb01s.ashasakhichat.data.api.SaveRiskAssessmentRequest
import com.littleb01s.ashasakhichat.data.api.SaveRiskAssessmentResponse
import com.littleb01s.ashasakhichat.data.api.FetchRiskAssessmentsResponse
import com.littleb01s.ashasakhichat.data.api.RiskAssessmentData
import com.littleb01s.ashasakhichat.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

interface RiskAssessmentRepository {
    suspend fun saveRiskAssessment(
        patientId: Int,
        checkupId: Int,
        riskValue: String,
        comments: String? = null,
        riskId: Int? = null
    ): Flow<Resource<SaveRiskAssessmentResponse>>
    
    suspend fun fetchRiskAssessments(updatedAt: String? = null): Flow<Resource<FetchRiskAssessmentsResponse>>
    
    // Local database operations
    suspend fun saveRiskAnalysisLocally(riskAnalysis: com.littleb01s.ashasakhichat.data.local.entity.RiskAnalysisResult): Long
    suspend fun getLatestRiskAnalysisForPatient(patientId: Int): com.littleb01s.ashasakhichat.data.local.entity.RiskAnalysisResult?
    
    // Server-first save with local fallback
    suspend fun saveRiskAnalysisWithServerFirst(
        patientId: Int,
        checkupId: Int,
        riskValue: String,
        comments: String?,
        riskId: Int? = null
    ): Flow<Resource<Long>>
}

@Singleton
class RiskAssessmentRepositoryImpl @Inject constructor(
    private val api: RiskAssessmentService,
    private val riskAnalysisDao: com.littleb01s.ashasakhichat.data.local.dao.RiskAnalysisDao
) : RiskAssessmentRepository {
    
    override suspend fun saveRiskAssessment(
        patientId: Int,
        checkupId: Int,
        riskValue: String,
        comments: String?,
        riskId: Int?
    ): Flow<Resource<SaveRiskAssessmentResponse>> = flow {
        try {
            emit(Resource.Loading())
            
            val request = SaveRiskAssessmentRequest(
                riskId = riskId,
                patientId = patientId,
                checkupId = checkupId,
                comments = comments,
                riskValue = riskValue
            )
            
            val response = api.saveRiskAssessment(request)
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    emit(Resource.Success(body))
                } ?: emit(Resource.Error("Empty response from server"))
            } else {
                emit(Resource.Error("Failed to save risk assessment: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.message}"))
        }
    }
    
    override suspend fun fetchRiskAssessments(updatedAt: String?): Flow<Resource<FetchRiskAssessmentsResponse>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = api.fetchRiskAssessments(updatedAt)
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    emit(Resource.Success(body))
                } ?: emit(Resource.Error("Empty response from server"))
            } else {
                emit(Resource.Error("Failed to fetch risk assessments: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.message}"))
        }
    }
    
    // Local database operations
    override suspend fun saveRiskAnalysisLocally(riskAnalysis: com.littleb01s.ashasakhichat.data.local.entity.RiskAnalysisResult): Long {
        return riskAnalysisDao.insertAnalysis(riskAnalysis)
    }
    
    override suspend fun getLatestRiskAnalysisForPatient(patientId: Int): com.littleb01s.ashasakhichat.data.local.entity.RiskAnalysisResult? {
        return riskAnalysisDao.getLatestAnalysisForPatient(patientId)
    }
    

    
    override suspend fun saveRiskAnalysisWithServerFirst(
        patientId: Int,
        checkupId: Int,
        riskValue: String,
        comments: String?,
        riskId: Int?
    ): Flow<Resource<Long>> = flow {
        try {
            emit(Resource.Loading())
            
            // First try to save to server
            try {
                val serverResponse = saveRiskAssessment(patientId, checkupId, riskValue, comments, riskId)
                serverResponse.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            // Server save successful, also save locally (will replace existing entry)
                            val localRiskAnalysis = com.littleb01s.ashasakhichat.data.local.entity.RiskAnalysisResult(
                                patientId = patientId,
                                checkupId = checkupId,
                                riskValue = riskValue,
                                comments = comments ?: "",
                                serverId = resource.data?.data?.riskId
                            )
                            val localId = saveRiskAnalysisLocally(localRiskAnalysis)
                            emit(Resource.Success(localId))
                        }
                        is Resource.Error -> {
                            // Server save failed, save locally only (will replace existing entry)
                            val localRiskAnalysis = com.littleb01s.ashasakhichat.data.local.entity.RiskAnalysisResult(
                                patientId = patientId,
                                checkupId = checkupId,
                                riskValue = riskValue,
                                comments = comments ?: "",
                                serverId = null
                            )
                            val localId = saveRiskAnalysisLocally(localRiskAnalysis)
                            emit(Resource.Success(localId))
                        }
                        is Resource.Loading -> {
                            // Continue loading
                        }
                    }
                }
            } catch (e: Exception) {
                // Network error, save locally only (will replace existing entry)
                val localRiskAnalysis = com.littleb01s.ashasakhichat.data.local.entity.RiskAnalysisResult(
                    patientId = patientId,
                    checkupId = checkupId,
                    riskValue = riskValue,
                    comments = comments ?: "",
                    serverId = null
                )
                val localId = saveRiskAnalysisLocally(localRiskAnalysis)
                emit(Resource.Success(localId))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error saving risk analysis: ${e.message}"))
        }
    }
} 
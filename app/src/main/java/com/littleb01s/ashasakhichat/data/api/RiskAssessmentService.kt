package com.littleb01s.ashasakhichat.data.api

import retrofit2.Response
import retrofit2.http.*

data class SaveRiskAssessmentRequest(
    val riskId: Int? = null,
    val patientId: Int,
    val checkupId: Int,
    val comments: String? = null,
    val riskValue: String
)

data class SaveRiskAssessmentResponse(
    val message: String,
    val data: RiskAssessmentData
)

data class RiskAssessmentData(
    val riskId: Int,
    val patientId: Int,
    val checkupId: Int,
    val comments: String?,
    val riskValue: String,
    val createdAt: String?,
    val updatedAt: String?
)

data class FetchRiskAssessmentsResponse(
    val data: List<RiskAssessmentData>
)

interface RiskAssessmentService {
    @POST("api/risk/save")
    suspend fun saveRiskAssessment(@Body request: SaveRiskAssessmentRequest): Response<SaveRiskAssessmentResponse>
    
    @GET("api/risk/fetch")
    suspend fun fetchRiskAssessments(@Query("updatedAt") updatedAt: String? = null): Response<FetchRiskAssessmentsResponse>
} 
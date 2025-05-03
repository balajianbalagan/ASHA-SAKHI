package com.littleb01s.ashasakhichat.data.api

import com.littleb01s.ashasakhichat.data.model.DietPlan
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DietService {
    @GET("api/diet/fetch-diet")
    suspend fun fetchDietPlan(@Query("patientId") patientId: Int): Response<DietResponse>
}

data class DietResponse(
    val data: DietPlan
) 
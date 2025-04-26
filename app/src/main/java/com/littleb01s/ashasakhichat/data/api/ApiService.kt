package com.littleb01s.ashasakhichat.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>
}

data class LoginRequest(
    val phoneNumber: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val message: String,
    val success: Boolean
) 
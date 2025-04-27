package com.littleb01s.ashasakhichat.data.api

import com.littleb01s.ashasakhichat.data.model.LoginRequest
import com.littleb01s.ashasakhichat.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
} 
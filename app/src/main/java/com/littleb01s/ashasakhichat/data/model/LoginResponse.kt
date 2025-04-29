package com.littleb01s.ashasakhichat.data.model

data class LoginResponse(
    val data: LoginData
)

data class LoginData(
    val message: String,
    val token: String,
    val profile: UserProfile?
)

data class UserProfile(
    val firstName: String,
    val lastName: String,
    val profileId: Int,
    val workerId: Int,
    val state: String,
    val city: String,
    val languagePreference: String,
    val specialization: String,
    val createdAt: String,
    val updatedAt: String
) 
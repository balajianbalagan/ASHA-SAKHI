package com.littleb01s.ashasakhichat.data.model

import com.google.gson.annotations.SerializedName

data class LoginErrorResponse(
    @SerializedName("message")
    val message: String
) 
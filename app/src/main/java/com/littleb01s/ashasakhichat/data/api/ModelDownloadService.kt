package com.littleb01s.ashasakhichat.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import javax.inject.Singleton

@Singleton
interface ModelDownloadService {
    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String): Response<ResponseBody>
}

data class ModelDownloadState(
    val isDownloading: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null,
    val isComplete: Boolean = false
) 
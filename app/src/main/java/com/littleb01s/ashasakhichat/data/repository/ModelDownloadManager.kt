package com.littleb01s.ashasakhichat.data.repository

import android.content.Context
import android.util.Log
import com.littleb01s.ashasakhichat.data.api.ModelDownloadService
import com.littleb01s.ashasakhichat.data.api.ModelDownloadState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelDownloadService: ModelDownloadService
) {
    private val _downloadState = MutableStateFlow(ModelDownloadState())
    val downloadState: StateFlow<ModelDownloadState> = _downloadState

    private val modelPath = "/data/local/tmp/llm/gemma-2b-it-cpu-int4.bin"
    private val modelUrl = "https://asha-sakhi-cdn.b-cdn.net/gemma-2b-it-cpu-int4.bin"

    suspend fun ensureModelExists() {
        val modelFile = File(modelPath)
        if (!modelFile.exists()) {
            downloadModel()
        } else {
            _downloadState.value = ModelDownloadState(isComplete = true)
        }
    }

    private suspend fun downloadModel() = withContext(Dispatchers.IO) {
        try {
            _downloadState.value = ModelDownloadState(isDownloading = true)
            
            // Create directory if it doesn't exist
            val modelDir = File("/data/local/tmp/llm")
            if (!modelDir.exists()) {
                modelDir.mkdirs()
            }

            val response = modelDownloadService.downloadModel(modelUrl)
            if (!response.isSuccessful) {
                throw Exception("Failed to download model: ${response.code()}")
            }

            val body = response.body() ?: throw Exception("Empty response body")
            val contentLength = body.contentLength()
            var bytesWritten = 0L

            FileOutputStream(modelPath).use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8192)
                    var bytes = input.read(buffer)
                    while (bytes >= 0) {
                        output.write(buffer, 0, bytes)
                        bytesWritten += bytes
                        val progress = if (contentLength > 0) {
                            bytesWritten.toFloat() / contentLength.toFloat()
                        } else 0f
                        _downloadState.value = _downloadState.value.copy(progress = progress)
                        bytes = input.read(buffer)
                    }
                }
            }

            _downloadState.value = ModelDownloadState(isComplete = true)
            Log.d("ModelDownloadManager", "Model downloaded successfully")
        } catch (e: Exception) {
            Log.e("ModelDownloadManager", "Error downloading model", e)
            _downloadState.value = ModelDownloadState(error = e.message)
        }
    }
} 
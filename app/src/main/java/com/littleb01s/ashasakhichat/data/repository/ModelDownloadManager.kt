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

    private val modelUrl = "https://huggingface.co/google/gemma-1.1-2b-it-tflite/blob/main/gemma-1.1-2b-it-cpu-int4.bin"

    suspend fun ensureModelExists() {
        val llmDir = File(context.getExternalFilesDir(null), "llm")
        if (!llmDir.exists()) {
            llmDir.mkdirs()
        }

        val modelFile = File(llmDir, "gemma-2b-it-cpu-int4.bin")
        if (!modelFile.exists()) {
            downloadModel(modelFile)
        } else {
            _downloadState.value = ModelDownloadState(isComplete = true)
        }
    }

    private suspend fun downloadModel(outputFile: File) = withContext(Dispatchers.IO) {
        try {
            _downloadState.value = ModelDownloadState(isDownloading = true)
            
            val response = modelDownloadService.downloadFile(modelUrl)
            if (!response.isSuccessful) {
                throw Exception("Failed to download model: ${response.code()}")
            }

            val body = response.body() ?: throw Exception("Empty response body")
            val contentLength = body.contentLength()
            var bytesWritten = 0L

            FileOutputStream(outputFile).use { output ->
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
            Log.d("ModelDownloadManager", "Model downloaded successfully to ${outputFile.path}")
        } catch (e: Exception) {
            Log.e("ModelDownloadManager", "Error downloading model", e)
            _downloadState.value = ModelDownloadState(error = e.message)
            throw e
        }
    }
} 
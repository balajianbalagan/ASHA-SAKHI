package com.littleb01s.ashasakhichat.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.littleb01s.ashasakhichat.data.api.ModelDownloadService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val modelDownloadService: ModelDownloadService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    suspend fun downloadModels(
        onProgress: (String, Int) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val llmDir = File(context.getExternalFilesDir(null), "llm1").apply { mkdirs() }

            val filesToDownload = listOf(
                "Gecko_1024_quant.tflite" to "https://asha-sakhi-cdn.b-cdn.net/Gecko_1024_quant.tflite",
                "sentencepiece.model" to "https://asha-sakhi-cdn.b-cdn.net/sentencepiece.model",
                "asha-kb.pdf" to "https://asha-sakhi-cdn.b-cdn.net/asha-kb.pdf",
                "gemma-2b-it-cpu-int4.bin" to "https://asha-sakhi-cdn.b-cdn.net/gemma-2b-it-cpu-int4.bin"
            )

            for ((index, pair) in filesToDownload.withIndex()) {
                val (filename, url) = pair
                val outputFile = File(llmDir, filename)

                if (!outputFile.exists()) {
                    withContext(Dispatchers.IO) {
                        val response = modelDownloadService.downloadFile(url)
                        if (response.isSuccessful) {
                            val body = response.body() ?: throw Exception("Null response body for $filename")
                            val contentLength = body.contentLength()

                            val inputStream = body.byteStream()
                            val outputStream = outputFile.outputStream().buffered()

                            var totalBytesRead = 0L
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead: Int

                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead

                                val fileProgress = if (contentLength > 0)
                                    ((totalBytesRead * 100) / contentLength).toInt()
                                else -1

                                onProgress(filename, fileProgress)
                            }

                            outputStream.flush()
                            outputStream.close()
                            inputStream.close()

                            Log.d("SettingsViewModel", "Downloaded $filename (${totalBytesRead / (1024 * 1024)} MB)")
                        } else {
                            throw Exception("Failed to download $filename: HTTP ${response.code()}")
                        }
                    }
                } else {
                    onProgress(filename, 100)
                }
            }

            onComplete()
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Error downloading files", e)
            onError(e.message ?: "Unknown error occurred")
        }
    }
}

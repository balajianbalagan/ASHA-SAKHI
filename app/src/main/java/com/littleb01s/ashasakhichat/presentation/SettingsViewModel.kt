package com.littleb01s.ashasakhichat.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.littleb01s.ashasakhichat.data.api.ModelDownloadService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val modelDownloadService: ModelDownloadService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val downloadJobs = ConcurrentHashMap<String, Job>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val filesToDownload = listOf(
        "Gecko_1024_quant.tflite" to "https://asha-sakhi-cdn.b-cdn.net/Gecko_1024_quant.tflite",
        "sentencepiece.model" to "https://asha-sakhi-cdn.b-cdn.net/sentencepiece.model",
        "asha-kb.pdf" to "https://asha-sakhi-cdn.b-cdn.net/asha-kb.pdf",
        "gemma-2b-it-cpu-int4.bin" to "https://huggingface.co/google/gemma-1.1-2b-it-tflite/blob/main/gemma-1.1-2b-it-cpu-int4.bin"
    )

    private fun getFileDir(): File {
        return File(context.getExternalFilesDir(null), "llm1").apply { mkdirs() }
    }

    fun cancelDownload(filename: String) {
        downloadJobs[filename]?.cancel()
        downloadJobs.remove(filename)
    }

    fun downloadModels(
        onProgress: (String, Int) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit,
        onFileStart: (String) -> Unit
    ) {
        val dir = getFileDir()

        filesToDownload.forEach { (filename, url) ->
            val outputFile = File(dir, filename)

            if (outputFile.exists()) {
                onProgress(filename, 100)
                return@forEach
            }

            val job = scope.launch {
                try {
                    onFileStart(filename)
                    val response = modelDownloadService.downloadFile(url)

                    if (response.isSuccessful) {
                        val body = response.body() ?: throw Exception("Null response body for $filename")
                        val contentLength = body.contentLength()

                        val inputStream = body.byteStream()
                        val outputStream = outputFile.outputStream().buffered()

                        var totalBytesRead = 0L
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead = inputStream.read(buffer)
                        while (isActive && bytesRead != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead

                            if (contentLength > 0) {
                                val fileProgress = ((totalBytesRead * 100) / contentLength).toInt()
                                onProgress(filename, fileProgress)
                            }

                            bytesRead = inputStream.read(buffer)
                        }

                        outputStream.flush()
                        outputStream.close()
                        inputStream.close()

                        if (!isActive) {
                            outputFile.delete() // Delete incomplete
                            onError("Download of $filename was cancelled")
                        } else {
                            onProgress(filename, 100)
                        }

                    } else {
                        throw Exception("Failed to download $filename: HTTP ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("SettingsViewModel", "Download error for $filename", e)
                    onError("Failed to download $filename: ${e.message}")
                } finally {
                    downloadJobs.remove(filename)
                    if (downloadJobs.isEmpty()) onComplete()
                }
            }

            downloadJobs[filename] = job
        }
    }

    fun retryDownload(
        filename: String,
        onProgress: (String, Int) -> Unit,
        onError: (String) -> Unit,
        onFileStart: (String) -> Unit
    ) {
        val filePair = filesToDownload.find { it.first == filename } ?: return
        val dir = getFileDir()
        val outputFile = File(dir, filename)

        if (outputFile.exists()) outputFile.delete()

        val job = scope.launch {
            try {
                onFileStart(filename)
                val response = modelDownloadService.downloadFile(filePair.second)

                if (response.isSuccessful) {
                    val body = response.body() ?: throw Exception("Null body for $filename")
                    val contentLength = body.contentLength()

                    val inputStream = body.byteStream()
                    val outputStream = outputFile.outputStream().buffered()

                    var totalBytesRead = 0L
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead = inputStream.read(buffer)
                    while (isActive && bytesRead != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        if (contentLength > 0) {
                            val fileProgress = ((totalBytesRead * 100) / contentLength).toInt()
                            onProgress(filename, fileProgress)
                        }

                        bytesRead = inputStream.read(buffer)
                    }

                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()

                    if (!isActive) {
                        outputFile.delete()
                        onError("Download of $filename cancelled.")
                    } else {
                        onProgress(filename, 100)
                    }
                } else {
                    throw Exception("HTTP error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Retry failed for $filename", e)
                onError("Retry failed: ${e.message}")
            } finally {
                downloadJobs.remove(filename)
            }
        }

        downloadJobs[filename] = job
    }
}

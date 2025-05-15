package com.littleb01s.ashasakhichat.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.littleb01s.ashasakhichat.data.api.ModelDownloadService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.HttpURLConnection
import java.net.URL

enum class DownloadState {
    Idle,        // File doesn't exist and not downloading
    Downloading, // File is currently being downloaded
    Completed    // File exists and is complete
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val modelDownloadService: ModelDownloadService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val downloadJobs = ConcurrentHashMap<String, Job>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val TAG = "SettingsViewModel"

    private val filesToDownload = listOf(
        "Gecko_1024_quant.tflite" to "https://asha-sakhi-cdn.b-cdn.net/Gecko_1024_quant.tflite",
        "sentencepiece.model" to "https://asha-sakhi-cdn.b-cdn.net/sentencepiece.model",
        "asha-kb.pdf" to "https://asha-sakhi-cdn.b-cdn.net/asha-kb.pdf",
        "gemma-2b-it-cpu-int4.bin" to "https://asha-sakhi-cdn.b-cdn.net/gemma-2b-it-cpu-int4.bin"
    )

    private fun getFileDir(): File {
        return File(context.getExternalFilesDir(null), "llm1").apply { mkdirs() }
    }

    fun checkFileExists(filename: String): Boolean {
        val file = File(getFileDir(), filename)
        return file.exists() && file.length() > 0
    }

    fun getFileStatus(filename: String): DownloadState {
        return when {
            checkFileExists(filename) -> DownloadState.Completed
            downloadJobs.containsKey(filename) -> DownloadState.Downloading
            else -> DownloadState.Idle
        }
    }

    fun cancelDownload(filename: String) {
        downloadJobs[filename]?.cancel()
        downloadJobs.remove(filename)
    }

    private suspend fun downloadWithChunks(
        url: String,
        outputFile: File,
        onProgress: (Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val urlObj = URL(url)
            connection = urlObj.openConnection() as HttpURLConnection
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept-Encoding", "identity")
            connection.setRequestProperty("Connection", "close")
            
            val fileSize = connection.contentLength.toLong()
            var downloadedSize = 0L
            
            connection.inputStream.use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(4096) // Smaller buffer size
                    var bytesRead = input.read(buffer)
                    
                    while (isActive && bytesRead != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedSize += bytesRead
                        
                        if (fileSize > 0) {
                            val progress = ((downloadedSize * 100) / fileSize).toInt()
                            onProgress(progress)
                        }
                        
                        // Force garbage collection periodically
                        if (downloadedSize % (1024 * 1024) == 0L) { // Every 1MB
                            System.gc()
                        }
                        
                        bytesRead = input.read(buffer)
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}", e)
            if (outputFile.exists()) {
                outputFile.delete()
            }
            false
        } finally {
            connection?.disconnect()
        }
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

            if (outputFile.exists() && outputFile.length() > 0) {
                onProgress(filename, 100)
                return@forEach
            }

            val job = scope.launch {
                try {
                    onFileStart(filename)
                    val success = downloadWithChunks(
                        url = url,
                        outputFile = outputFile,
                        onProgress = { progress -> onProgress(filename, progress) }
                    )

                    if (!success) {
                        throw IOException("Failed to download $filename")
                    }

                    if (!isActive) {
                        outputFile.delete()
                        onError("Download of $filename was cancelled")
                    } else {
                        onProgress(filename, 100)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Download error for $filename", e)
                    if (outputFile.exists()) {
                        outputFile.delete()
                    }
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
                val success = downloadWithChunks(
                    url = filePair.second,
                    outputFile = outputFile,
                    onProgress = { progress -> onProgress(filename, progress) }
                )

                if (!success) {
                    throw IOException("Failed to download $filename")
                }

                if (!isActive) {
                    outputFile.delete()
                    onError("Download of $filename was cancelled")
                } else {
                    onProgress(filename, 100)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Retry failed for $filename", e)
                if (outputFile.exists()) {
                    outputFile.delete()
                }
                onError("Retry failed: ${e.message}")
            } finally {
                downloadJobs.remove(filename)
            }
        }

        downloadJobs[filename] = job
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}

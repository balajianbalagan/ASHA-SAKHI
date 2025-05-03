package com.littleb01s.ashasakhichat.domain

import android.content.Context
import android.util.Log
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "VoskSpeechService"
private const val MODEL_PATH_EN = "model-small-en-in"
private const val MODEL_PATH_HI = "model-small-hi-in"

@Singleton
class VoskSpeechService @Inject constructor(
    private val context: Context
) {
    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var isInitialized = false
    private var currentModelPath: String = MODEL_PATH_EN

    private fun getModelPath(language: String): String {
        return when (language.lowercase()) {
            "hi" -> MODEL_PATH_HI
            else -> MODEL_PATH_EN
        }
    }

    fun initModel(
        language: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val newModelPath = getModelPath(language)
        
        // If the model is already initialized with the same language, just return
        if (isInitialized && currentModelPath == newModelPath) {
            onSuccess()
            return
        }

        // If we're switching languages, shutdown the existing model
        if (isInitialized) {
            shutdown()
        }

        currentModelPath = newModelPath

        try {
            // Check if model is already unpacked
            val modelDir = File(context.filesDir, currentModelPath)
            if (!modelDir.exists()) {
                // Unpack model from assets
                StorageService.unpack(context, currentModelPath, "model",
                    { model ->
                        this.model = model
                        isInitialized = true
                        onSuccess()
                    },
                    { exception ->
                        val errorMsg = "Failed to unpack the model: ${exception.message}"
                        Log.e(TAG, errorMsg, exception)
                        onError(errorMsg)
                    }
                )
            } else {
                // Model already unpacked, just load it
                try {
                    model = Model(modelDir.absolutePath)
                    isInitialized = true
                    onSuccess()
                } catch (e: IOException) {
                    val errorMsg = "Failed to load existing model: ${e.message}"
                    Log.e(TAG, errorMsg, e)
                    onError(errorMsg)
                    // Try to recover by deleting and unpacking again
                    modelDir.deleteRecursively()
                    initModel(language, onSuccess, onError)
                }
            }
        } catch (e: Exception) {
            val errorMsg = "Unexpected error initializing model: ${e.message}"
            Log.e(TAG, errorMsg, e)
            onError(errorMsg)
        }
    }

    fun startListening(listener: RecognitionListener) {
        if (!isInitialized) {
            Log.e(TAG, "Model not initialized")
            listener.onError(IOException("Speech recognition model not initialized"))
            return
        }

        if (speechService != null) {
            Log.w(TAG, "Speech service already running")
            return
        }

        try {
            val recognizer = Recognizer(model, 16000.0f)
            speechService = SpeechService(recognizer, 16000.0f)
            speechService?.startListening(listener)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to start speech service", e)
            listener.onError(e)
        }
    }

    fun stopListening() {
        speechService?.stop()
        speechService = null
    }

    fun setPause(paused: Boolean) {
        speechService?.setPause(paused)
    }

    fun shutdown() {
        stopListening()
        model?.close()
        model = null
        isInitialized = false
    }
} 
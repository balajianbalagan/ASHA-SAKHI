package com.littleb01s.ashasakhichat.data

import android.util.Log
import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.ClassificationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.google.mediapipe.tasks.genai.llminference.ProgressListener
import com.google.common.util.concurrent.ListenableFuture
import okhttp3.internal.notify
import javax.inject.Singleton
import java.io.File

@Singleton
class MediapipeLLMDataSource @Inject constructor(
    private var llmInference: LlmInference,
    private val imageClassifier: ImageClassifier,
    private val context: Context
) {
    private val systemPrompt = """
        You are a healthcare assistant for pregnant women. Provide only factual medical information and nutrition plans. Never build conversations or hallucinate. If unsure, say so. Always recommend consulting doctors for serious concerns. Be specific and limit to three paragraphs
    """.trimIndent()

    fun resetLLMInference() {
        try {
            // Close the current instance
            llmInference.close()
            
            val modelPath = "/data/local/tmp/llm/gemma-2b-it-cpu-int4.bin"
            val modelFile = File(modelPath)
            
            Log.d("MediapipeLLMDataSource", "Model file details:")
            Log.d("MediapipeLLMDataSource", "Exists: ${modelFile.exists()}")
            Log.d("MediapipeLLMDataSource", "Size: ${modelFile.length()} bytes")
            Log.d("MediapipeLLMDataSource", "Can read: ${modelFile.canRead()}")
            Log.d("MediapipeLLMDataSource", "Absolute path: ${modelFile.absolutePath}")
            Log.d("MediapipeLLMDataSource", "Parent exists: ${modelFile.parentFile?.exists()}")
            Log.d("MediapipeLLMDataSource", "Parent can read: ${modelFile.parentFile?.canRead()}")
            
            Log.d("MediapipeLLMDataSource", "Creating LLM options...")
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(256)
                .build()
            
            Log.d("MediapipeLLMDataSource", "Created LLM options successfully")
            Log.d("MediapipeLLMDataSource", "Creating LLM instance...")
            llmInference = LlmInference.createFromOptions(context, options)
            Log.d("MediapipeLLMDataSource", "LLM instance reset successfully")
        } catch (e: Exception) {
            Log.e("MediapipeLLMDataSource", "Error resetting LLM Inference: ${e.message}")
            throw e
        }
    }

    suspend fun start(): String {
        return withContext(Dispatchers.IO) {
            Log.i(
                MediapipeLLMDataSource::class.java.simpleName,
                "Initializing ASHA Sakhi chat"
            )
            llmInference.generateResponse("$systemPrompt\n\nI am ready to assist with your healthcare queries. Please ask your specific question. Limit to 300 words")
        }
    }

    suspend fun sendMessage(message: String): String {
        return withContext(Dispatchers.IO) {
            Log.i(
                MediapipeLLMDataSource::class.java.simpleName,
                "Processing user query: $message"
            )
            llmInference.generateResponse("""
                $systemPrompt
                User's query: $message
            """.trimIndent())
        }
    }

    fun classifyImage(image: MPImage): ClassificationResult =
        imageClassifier.classify(image).classificationResult()

    fun generateResponseAsync(prompt: String, progressListener: ProgressListener<String>): ListenableFuture<String> {
        return llmInference.generateResponseAsync(prompt, progressListener)
    }
}

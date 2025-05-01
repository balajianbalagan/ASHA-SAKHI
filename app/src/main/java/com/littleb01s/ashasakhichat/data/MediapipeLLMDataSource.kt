package com.littleb01s.ashasakhichat.data

import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.ClassificationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.google.mediapipe.tasks.genai.llminference.ProgressListener
import com.google.common.util.concurrent.ListenableFuture
import javax.inject.Singleton

@Singleton
class MediapipeLLMDataSource @Inject constructor(
    private val llmInference: LlmInference,
    private val imageClassifier: ImageClassifier
) {
    private val systemPrompt = """
        You are a healthcare assistant for pregnant women. Provide only factual medical information and nutrition plans. Never build conversations or hallucinate. If unsure, say so. Always recommend consulting doctors for serious concerns.
    """.trimIndent()

    suspend fun start(): String {
        return withContext(Dispatchers.IO) {
            Log.i(
                MediapipeLLMDataSource::class.java.simpleName,
                "Initializing ASHA Sakhi chat"
            )
            llmInference.generateResponse("$systemPrompt\n\nI am ready to assist with your healthcare queries. Please ask your specific question.")
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
        return llmInference.generateResponseAsync(prompt,progressListener)
    }
}

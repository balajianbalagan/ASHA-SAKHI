package com.littleb01s.ashasakhichat.data

import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.ClassificationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediapipeLLMDataSource @Inject constructor(
    private val llmInference: LlmInference,
    private val imageClassifier: ImageClassifier
) {
    private val systemPrompt = """
        You are a healthcare agent.
    """.trimIndent()

    suspend fun start(): String {
        return withContext(Dispatchers.IO) {
            Log.i(
                MediapipeLLMDataSource::class.java.simpleName,
                "Initializing ASHA Sakhi chat"
            )
            llmInference.generateResponse("$systemPrompt\n\nStart a friendly conversation with the user, introducing yourself as their AI healthcare companion for safer motherhood. Keep it brief but welcoming.")
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
}

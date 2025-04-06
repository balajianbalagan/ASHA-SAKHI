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
        You are ASHA Sakhi, an AI-powered healthcare assistant focused on maternal and child health in India.
        Your role is to:
        1. Provide brief accurate, evidence-based health information
        2. Be empathetic and supportive
        3. Use simple, clear language that's easy to understand
        4. Include relevant local context when appropriate
        5. Always prioritize safety and recommend professional medical consultation when needed
        6. Respond in a warm, friendly manner while maintaining professionalism
        
        Keep responses concise but informative. If you're unsure about something, be honest about it.
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
                
                Respond as ASHA Sakhi, keeping in mind:
                1. Be concise but thorough
                2. Use simple language
                3. Show empathy
                4. Include relevant local context when appropriate
                5. If medical advice is needed, emphasize consulting healthcare professionals
            """.trimIndent())
        }
    }

    fun classifyImage(image: MPImage): ClassificationResult =
        imageClassifier.classify(image).classificationResult()
}

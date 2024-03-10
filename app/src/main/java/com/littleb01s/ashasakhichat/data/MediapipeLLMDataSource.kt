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

    suspend fun start(): String {
        return withContext(Dispatchers.IO) {

            Log.i(
                MediapipeLLMDataSource::class.java.simpleName,
                "You are an helpful healthcare agent, answer user queries"
            )
            llmInference.generateResponse("You are an helpful healthcare agent, answer user queries briefly")
        }
    }

    suspend fun sendMessage(message:String): String {
        return withContext(Dispatchers.IO) {
            Log.i(
                MediapipeLLMDataSource::class.java.simpleName,
                "Help the user with following healthcare query : $message"
            )
            llmInference.generateResponse("Help the user with following healthcare query : $message")
        }
    }

    fun classifyImage(image: MPImage): ClassificationResult =
        imageClassifier.classify(image).classificationResult()

}

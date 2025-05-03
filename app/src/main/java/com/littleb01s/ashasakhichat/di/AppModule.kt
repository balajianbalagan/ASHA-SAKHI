package com.littleb01s.ashasakhichat.di

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier.ImageClassifierOptions
import com.littleb01s.ashasakhichat.domain.TranslationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File

private const val TAG = "AppModule"

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    fun provideLlmInference(@ApplicationContext context: Context): LlmInference {
        Log.d(TAG, "Starting LLM initialization...")
        val modelPath = "/data/local/tmp/llm/gemma-2b-it-cpu-int4.bin"
        val modelFile = File(modelPath)
        
        Log.d(TAG, "Model file details:")
        Log.d(TAG, "Exists: ${modelFile.exists()}")
        Log.d(TAG, "Size: ${modelFile.length()} bytes")
        Log.d(TAG, "Can read: ${modelFile.canRead()}")
        Log.d(TAG, "Absolute path: ${modelFile.absolutePath}")
        Log.d(TAG, "Parent exists: ${modelFile.parentFile?.exists()}")
        Log.d(TAG, "Parent can read: ${modelFile.parentFile?.canRead()}")
        
        try {
            Log.d(TAG, "Creating LLM options...")
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath).setMaxTokens(256)
                .build()
            
            Log.d(TAG, "Created LLM options successfully")
            Log.d(TAG, "Creating LLM instance...")
            val llm = LlmInference.createFromOptions(context, options)
            Log.d(TAG, "LLM instance created successfully")
            return llm
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing LLM", e)
            throw e
        }
    }

    @Provides
    fun provideTranslationService(@ApplicationContext context: Context): TranslationService {
        Log.d(TAG, "Creating TranslationService...")
        return TranslationService(context)
    }
}

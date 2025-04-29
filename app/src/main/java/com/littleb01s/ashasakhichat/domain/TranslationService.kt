package com.littleb01s.ashasakhichat.domain

import android.content.Context
import android.content.SharedPreferences
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class TranslationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
    
    private var englishHindiTranslator: Translator? = null
    private var englishTamilTranslator: Translator? = null
    private var englishBengaliTranslator: Translator? = null
    
    fun getCurrentLanguage(): String {
        return sharedPreferences.getString("selected_language", "en") ?: "en"
    }
    
    suspend fun initialize() {
        val currentLanguage = getCurrentLanguage()
        when (currentLanguage) {
            "hi" -> getEnglishHindiTranslator()
            "ta" -> getEnglishTamilTranslator()
            "bn" -> getEnglishBengaliTranslator()
        }
    }
    
    suspend fun translateToHindi(text: String): String {
        if (getCurrentLanguage() != "hi") {
            return text
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val translator = getEnglishHindiTranslator()
                translateText(translator, text)
            } catch (e: Exception) {
                // If translation fails, return the original text
                text
            }
        }
    }
    
    suspend fun translateToTamil(text: String): String {
        if (getCurrentLanguage() != "ta") {
            return text
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val translator = getEnglishTamilTranslator()
                translateText(translator, text)
            } catch (e: Exception) {
                // If translation fails, return the original text
                text
            }
        }
    }
    
    suspend fun translateToBengali(text: String): String {
        if (getCurrentLanguage() != "bn") {
            return text
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val translator = getEnglishBengaliTranslator()
                translateText(translator, text)
            } catch (e: Exception) {
                // If translation fails, return the original text
                text
            }
        }
    }
    
    suspend fun translate(text: String): String {
        return when (getCurrentLanguage()) {
            "hi" -> translateToHindi(text)
            "ta" -> translateToTamil(text)
            "bn" -> translateToBengali(text)
            else -> text
        }
    }
    
    private suspend fun translateText(translator: Translator, text: String): String {
        return suspendCancellableCoroutine { continuation ->
            translator.translate(text)
                .addOnSuccessListener { translatedText ->
                    continuation.resume(translatedText)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }
    
    private suspend fun getEnglishHindiTranslator(): Translator {
        return englishHindiTranslator ?: createEnglishHindiTranslator().also {
            englishHindiTranslator = it
        }
    }
    
    private suspend fun getEnglishTamilTranslator(): Translator {
        return englishTamilTranslator ?: createEnglishTamilTranslator().also {
            englishTamilTranslator = it
        }
    }
    
    private suspend fun getEnglishBengaliTranslator(): Translator {
        return englishBengaliTranslator ?: createEnglishBengaliTranslator().also {
            englishBengaliTranslator = it
        }
    }
    
    private suspend fun createEnglishHindiTranslator(): Translator {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(com.google.mlkit.nl.translate.TranslateLanguage.ENGLISH)
            .setTargetLanguage(com.google.mlkit.nl.translate.TranslateLanguage.HINDI)
            .build()
        
        val translator = Translation.getClient(options)
        
        // Download the model if needed
        suspendCancellableCoroutine<Unit> { continuation ->
            translator.downloadModelIfNeeded()
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
        
        return translator
    }
    
    private suspend fun createEnglishTamilTranslator(): Translator {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(com.google.mlkit.nl.translate.TranslateLanguage.ENGLISH)
            .setTargetLanguage(com.google.mlkit.nl.translate.TranslateLanguage.TAMIL)
            .build()
        
        val translator = Translation.getClient(options)
        
        // Download the model if needed
        suspendCancellableCoroutine<Unit> { continuation ->
            translator.downloadModelIfNeeded()
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
        
        return translator
    }
    
    private suspend fun createEnglishBengaliTranslator(): Translator {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(com.google.mlkit.nl.translate.TranslateLanguage.ENGLISH)
            .setTargetLanguage(com.google.mlkit.nl.translate.TranslateLanguage.BENGALI)
            .build()
        
        val translator = Translation.getClient(options)
        
        // Download the model if needed
        suspendCancellableCoroutine<Unit> { continuation ->
            translator.downloadModelIfNeeded()
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
        
        return translator
    }
    
    fun close() {
        englishHindiTranslator?.close()
        englishHindiTranslator = null
        
        englishTamilTranslator?.close()
        englishTamilTranslator = null
        
        englishBengaliTranslator?.close()
        englishBengaliTranslator = null
    }
} 
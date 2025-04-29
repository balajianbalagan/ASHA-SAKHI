package com.littleb01s.ashasakhichat.domain

import android.content.Context
import com.littleb01s.R
import com.littleb01s.ashasakhichat.data.MediapipeRepository
import com.littleb01s.ashasakhichat.presentation.Message
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StartAshaSakhiChat @Inject constructor(
    private val mediapipeRepository: MediapipeRepository,
    private val translationService: TranslationService,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(): Message {
        // Use static text from strings.xml instead of LLM for faster response
        val staticWelcomeMessage = context.getString(R.string.chat_welcome_message)
        
        // Translate the static message based on the selected language
        val translatedMessage = translationService.translate(staticWelcomeMessage)
        
        return Message(
            text = translatedMessage,
            isFromMe = false
        )
    }
}

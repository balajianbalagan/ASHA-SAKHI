package com.littleb01s.ashasakhichat.domain

import com.littleb01s.ashasakhichat.data.MediapipeRepository
import com.littleb01s.ashasakhichat.presentation.Message
import javax.inject.Inject

class SendMessageToSimon @Inject constructor(private val mediapipeRepository: MediapipeRepository) {
    suspend operator fun invoke(message: Message): Message {
        val messageFromLLM = mediapipeRepository.sendMessage(message)
        return Message(
            text = messageFromLLM,
            isFromMe = false
        )
    }
}

package com.littleb01s.ashasakhichat.data

import com.littleb01s.ashasakhichat.presentation.Message
import com.google.mediapipe.framework.image.MPImage
import javax.inject.Inject


class MediapipeRepository @Inject constructor(private val mediapipeLLMDataSource: MediapipeLLMDataSource) {
    suspend fun startChat(): Message {
        val message = mediapipeLLMDataSource.start()
        return Message(
            text = message,
            isFromMe = false
        )
    }

    suspend fun sendMessage(message: Message): String {
        return mediapipeLLMDataSource.sendMessage(message.text)
    }

}

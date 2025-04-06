package com.littleb01s.ashasakhichat.domain

import com.littleb01s.ashasakhichat.data.MediapipeRepository
import com.littleb01s.ashasakhichat.presentation.Message
import javax.inject.Inject

class StartAshaSakhiChat @Inject constructor(private val mediapipeRepository: MediapipeRepository) {
    suspend operator fun invoke(): Message {
        return mediapipeRepository.startChat()
    }
}

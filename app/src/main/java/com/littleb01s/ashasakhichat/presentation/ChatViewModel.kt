package com.littleb01s.ashasakhichat.presentation

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littleb01s.ashasakhichat.domain.SendMessageToAshaSakhiChat
import com.littleb01s.ashasakhichat.domain.StartAshaSakhiChat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private val awaitingMessageFromAsha = Message(
    text = "ASHA Sakhi is typing...",
    isFromMe = false,
    isLoading = true
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val startAshaSakhiChat: StartAshaSakhiChat,
    private val sendMessageToAsha: SendMessageToAshaSakhiChat,
) : ViewModel() {
    val messages: StateFlow<List<Message>>
        get() = _messages

    val isProcessing: StateFlow<Boolean>
        get() = _isProcessing

    private val _messages: MutableStateFlow<List<Message>> = MutableStateFlow(
        emptyList()
    )

    private val _isProcessing: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun startChat() {
        viewModelScope.launch {
            _isProcessing.value = true
            _messages.update { messages ->
                val mutableList = messages.toMutableList()
                mutableList += awaitingMessageFromAsha
                mutableList
            }

            try {
                val message = startAshaSakhiChat()
                _messages.update { messages ->
                    val mutableList = messages.toMutableList()
                    mutableList.removeLast()
                    mutableList += message.copy(timestamp = LocalDateTime.now())
                    mutableList
                }
            } catch (e: Exception) {
                _messages.update { messages ->
                    val mutableList = messages.toMutableList()
                    mutableList.removeLast()
                    mutableList += Message(
                        text = "Sorry, I'm having trouble connecting right now. Please try again.",
                        isFromMe = false,
                        isError = true,
                        timestamp = LocalDateTime.now()
                    )
                    mutableList
                }
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun sendMessage(text: String) {
        if (_isProcessing.value) return
        
        viewModelScope.launch {
            _isProcessing.value = true
            val newMessage = Message(
                text = text,
                isFromMe = true,
                timestamp = LocalDateTime.now()
            )

            _messages.update { messages ->
                val mutableList = messages.toMutableList()
                mutableList += listOf(newMessage, awaitingMessageFromAsha)
                mutableList
            }

            try {
                val message = sendMessageToAsha(message = newMessage)
                _messages.update { messages ->
                    val mutableList = messages.toMutableList()
                    mutableList.removeLast()
                    mutableList += message.copy(timestamp = LocalDateTime.now())
                    mutableList
                }
            } catch (e: Exception) {
                _messages.update { messages ->
                    val mutableList = messages.toMutableList()
                    mutableList.removeLast()
                    mutableList += Message(
                        text = "I apologize, but I'm having trouble processing your message. Please try again.",
                        isFromMe = false,
                        isError = true,
                        timestamp = LocalDateTime.now()
                    )
                    mutableList
                }
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun retryLastMessage() {
        val lastUserMessage = _messages.value.lastOrNull { it.isFromMe } ?: return
        sendMessage(lastUserMessage.text)
    }

    fun sendPhoto(imageBitmap: ImageBitmap) {
        if (_isProcessing.value) return
        
        _isProcessing.value = true
        val newMessage = Message(
            image = imageBitmap,
            isFromMe = true
        )

        val list = _messages.value.toMutableList()
        list += newMessage

        _messages.update {
            list
        }

        val newMessageFromAsha = Message(
            text = "Processing your image...",
            isFromMe = false,
            isLoading = true
        )

        list += newMessageFromAsha

        _messages.update {
            list
        }
        
        _isProcessing.value = false
    }
}

data class Message(
    val text: String = "", 
    val image: ImageBitmap? = null, 
    val isFromMe: Boolean,
    val isError: Boolean = false,
    val isLoading: Boolean = false,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    val formattedDate: String
        get() = timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    
    val formattedTime: String
        get() = timestamp.format(DateTimeFormatter.ofPattern("hh:mm a"))
}

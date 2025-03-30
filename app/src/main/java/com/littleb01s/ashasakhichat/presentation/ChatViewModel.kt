package com.littleb01s.ashasakhichat.presentation

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littleb01s.ashasakhichat.domain.SendMessageToSimon
import com.littleb01s.ashasakhichat.domain.StartSimonSays
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private val awaitingMessageFromSimon = Message(
    text = "...",
    isFromMe = false
)

@HiltViewModel
class PlayViewModel @Inject constructor(
    private val startSimonSays: StartSimonSays,
    private val sendMessageToSimon: SendMessageToSimon,
) : ViewModel() {
    val messages: StateFlow<List<Message>>
        get() = _messages

    private val _messages: MutableStateFlow<List<Message>> = MutableStateFlow(
        emptyList()
    )

    fun startGame() {
        viewModelScope.launch {
            _messages.update { messages ->
                val mutableList = messages.toMutableList()
                mutableList += awaitingMessageFromSimon
                mutableList
            }

            val message = startSimonSays()

            _messages.update { messages ->
                val mutableList = messages.toMutableList()
                mutableList.removeLast()
                mutableList += message
                mutableList
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val newMessage = Message(
                text = text,
                isFromMe = true
            )

            _messages.update { messages ->
                val mutableList = messages.toMutableList()
                mutableList += listOf(newMessage, awaitingMessageFromSimon)
                mutableList
            }

            val message = sendMessageToSimon(message = newMessage)

            _messages.update { messages ->
                val mutableList = messages.toMutableList()
                mutableList.removeLast()
                mutableList += message
                mutableList
            }
        }
    }

    fun sendPhoto(imageBitmap: ImageBitmap) {
        val newMessage = Message(
            image = imageBitmap,
            isFromMe = true
        )

        val list = _messages.value.toMutableList()
        list += newMessage

        _messages.update {
            list
        }

        val newMessageFromSimon = Message(
            text = "Processing image",
            isFromMe = true
        )

        list += newMessageFromSimon

        _messages.update {
            list
        }
    }
}

data class Message(val text: String = "", val image: ImageBitmap? = null, val isFromMe: Boolean)

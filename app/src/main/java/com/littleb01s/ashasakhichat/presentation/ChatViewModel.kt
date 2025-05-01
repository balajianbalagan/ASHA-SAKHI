package com.littleb01s.ashasakhichat.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littleb01s.ashasakhichat.domain.SendMessageToAshaSakhiChat
import com.littleb01s.ashasakhichat.domain.StartAshaSakhiChat
import com.littleb01s.ashasakhichat.domain.TranslationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import com.google.common.util.concurrent.ListenableFuture
import com.google.mediapipe.tasks.genai.llminference.ProgressListener
import com.littleb01s.ashasakhichat.data.MediapipeLLMDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.Job
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private val awaitingMessageFromAsha = Message(
    text = "ASHA Sakhi is typing...",
    isFromMe = false,
    isLoading = true
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val startAshaSakhiChat: StartAshaSakhiChat,
    private val sendMessageToAsha: SendMessageToAshaSakhiChat,
    private val translationService: TranslationService,
    private val llmDataSource: MediapipeLLMDataSource
) : ViewModel() {
    val messages: StateFlow<List<Message>>
        get() = _messages

    val isProcessing: StateFlow<Boolean>
        get() = _isProcessing

    private val _messages: MutableStateFlow<List<Message>> = MutableStateFlow(
        emptyList()
    )

    private val _isProcessing: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val lastMessageTime = AtomicLong(0)
    private val TIMEOUT_MS = 5000L // 5 seconds timeout
    private val currentAsyncInference = AtomicReference<ListenableFuture<String>?>(null)
    private var timeoutJob: Job? = null
    private var isWaitingForCompletion = false

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
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
                val translatedText = translationService.translate(message.text)
                val formattedText = formatResponse(translatedText)
                _messages.update { messages ->
                    val mutableList = messages.toMutableList()
                    mutableList.removeLast()
                    mutableList += message.copy(
                        text = formattedText,
                        timestamp = LocalDateTime.now()
                    )
                    mutableList
                }
            } catch (e: Exception) {
                val errorMessage = "Sorry, I'm having trouble connecting right now. Please try again."
                val translatedErrorMessage = translationService.translate(errorMessage)
                _messages.update { messages ->
                    val mutableList = messages.toMutableList()
                    mutableList.removeLast()
                    mutableList += Message(
                        text = translatedErrorMessage,
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

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            // Wait for any previous inference to complete
            if (isWaitingForCompletion) {
                return@launch
            }

            isWaitingForCompletion = true
            try {
                // Cancel any existing inference and timeout
                currentAsyncInference.get()?.cancel(true)
                timeoutJob?.cancel()
                _isProcessing.value = true
                lastMessageTime.set(System.currentTimeMillis())
                
                // Add user message
                val updatedMessages = _messages.value.toMutableList()
                updatedMessages.add(Message(
                    text = message,
                    isFromMe = true
                ))
                _messages.value = updatedMessages

                // Add loading message
                val loadingMessage = Message(
                    text = "",
                    isFromMe = false,
                    isLoading = true
                )
                updatedMessages.add(loadingMessage)
                _messages.value = updatedMessages

                // Start timeout check
                timeoutJob = launch {
                    while (true) {
                        delay(1000) // Check every second
                        if (System.currentTimeMillis() - lastMessageTime.get() > TIMEOUT_MS) {
                            // Cancel the async inference if it exists
                            currentAsyncInference.get()?.cancel(true)
                            _isProcessing.value = false
                            
                            // Add timeout error message
                            val errorMessages = _messages.value.toMutableList()
                            if (errorMessages.isNotEmpty() && errorMessages.last().isLoading) {
                                errorMessages.removeAt(errorMessages.lastIndex)
                            }
                            errorMessages.add(Message(
                                text = "Response timeout. Please try again.",
                                isFromMe = false,
                                isError = true
                            ))
                            _messages.value = errorMessages
                            isWaitingForCompletion = false
                            break
                        }
                    }
                }

                val asyncInference = llmDataSource.generateResponseAsync(message, object : ProgressListener<String> {
                    override fun run(partialResult: String?, done: Boolean) {
                        viewModelScope.launch {
                            lastMessageTime.set(System.currentTimeMillis())
                            val currentMessages = _messages.value.toMutableList()
                            if (currentMessages.isNotEmpty() && currentMessages.last().isLoading) {
                                // Remove the loading message
                                currentMessages.removeAt(currentMessages.lastIndex)
                                // Add the initial response message
                                currentMessages.add(Message(
                                    text = partialResult.toString(),
                                    isFromMe = false
                                ))
                            } else {
                                // Update the last message with the new partial result
                                val lastMessage = currentMessages.last()
                                currentMessages[currentMessages.lastIndex] = lastMessage.copy(
                                    text = lastMessage.text + partialResult
                                )
                            }
                            _messages.value = currentMessages

                            if (done) {
                                _isProcessing.value = false
                                timeoutJob?.cancel()
                                isWaitingForCompletion = false
                            }
                        }
                    }
                })

                // Store the current async inference
                currentAsyncInference.set(asyncInference)

                // Handle completion
                asyncInference.addListener({
                    viewModelScope.launch {
                        _isProcessing.value = false
                        timeoutJob?.cancel()
                        currentAsyncInference.set(null)
                        isWaitingForCompletion = false
                    }
                }, Executors.newSingleThreadExecutor())
            } catch (e: Exception) {
                viewModelScope.launch {
                    val errorMessages = _messages.value.toMutableList()
                    if (errorMessages.isNotEmpty() && errorMessages.last().isLoading) {
                        errorMessages.removeAt(errorMessages.lastIndex)
                    }
                    errorMessages.add(Message(
                        text = e.message ?: "An error occurred",
                        isFromMe = false,
                        isError = true
                    ))
                    _messages.value = errorMessages
                    _isProcessing.value = false
                    timeoutJob?.cancel()
                    currentAsyncInference.set(null)
                    isWaitingForCompletion = false
                }
            }
        }
    }

    fun retryLastMessage() {
        val messages = _messages.value
        if (messages.isNotEmpty()) {
            val lastUserMessage = messages.lastOrNull { it.isFromMe }
            if (lastUserMessage != null) {
                sendMessage(lastUserMessage.text)
            }
        }
    }

    suspend fun sendPhoto(imageBitmap: ImageBitmap) {
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

        val processingMessage = "Processing your image..."
        val translatedProcessingMessage = translationService.translate(processingMessage)
        val newMessageFromAsha = Message(
            text = translatedProcessingMessage,
            isFromMe = false,
            isLoading = true
        )

        list += newMessageFromAsha

        _messages.update {
            list
        }
        
        _isProcessing.value = false
    }
    
    override fun onCleared() {
        super.onCleared()
        translationService.close()
    }
    
    /**
     * Formats the response to make it more concise and readable
     * - Adds markdown formatting for better readability
     * - Truncates long responses with a "Show more" option
     * - Formats lists and important information
     */
    private fun formatResponse(text: String): String {
        // If the text is already in markdown format, return it as is
        if (text.contains("**") || text.contains("*") || text.contains("#") || 
            text.contains("- ") || text.contains("1. ") || text.contains("```")) {
            return text
        }
        
        // For very short responses, return as is
        if (text.length < 100) {
            return text
        }
        
        // For longer responses, format them with markdown
        val lines = text.split("\n")
        val formattedLines = mutableListOf<String>()
        
        // Check if the text contains a list-like structure
        val hasListStructure = lines.any { it.trim().startsWith("-") || it.trim().matches(Regex("^\\d+\\..*")) }
        
        if (hasListStructure) {
            // Format as a list
            lines.forEach { line ->
                val trimmedLine = line.trim()
                if (trimmedLine.startsWith("-") || trimmedLine.matches(Regex("^\\d+\\..*"))) {
                    formattedLines.add(trimmedLine)
                } else if (trimmedLine.isNotEmpty()) {
                    formattedLines.add(trimmedLine)
                }
            }
        } else {
            // Format as paragraphs with headers
            var isFirstParagraph = true
            lines.forEach { line ->
                val trimmedLine = line.trim()
                if (trimmedLine.isNotEmpty()) {
                    if (isFirstParagraph) {
                        formattedLines.add("**$trimmedLine**")
                        isFirstParagraph = false
                    } else {
                        formattedLines.add(trimmedLine)
                    }
                }
            }
        }
        
        // If the formatted text is too long, truncate it
        val formattedText = formattedLines.joinToString("\n")
        if (formattedText.length > 500) {
            val truncatedText = formattedText.substring(0, 500) + "..."
            return "$truncatedText\n\n*[Show more]()*"
        }
        
        return formattedText
    }

    private fun getCurrentDate(): String {
        return java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            .format(java.util.Date())
    }

    private fun getCurrentTime(): String {
        return java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            .format(java.util.Date())
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

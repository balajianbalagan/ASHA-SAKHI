package com.littleb01s.ashasakhichat.presentation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.util.concurrent.ListenableFuture
import com.google.mediapipe.tasks.genai.llminference.ProgressListener
import com.littleb01s.ashasakhichat.data.MediapipeLLMDataSource
import com.littleb01s.ashasakhichat.domain.SendMessageToAshaSakhiChat
import com.littleb01s.ashasakhichat.domain.StartAshaSakhiChat
import com.littleb01s.ashasakhichat.domain.TranslationService
import com.littleb01s.ashasakhichat.domain.VoskSpeechService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.vosk.android.RecognitionListener
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
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
    private val translationService: TranslationService,
    private val llmDataSource: MediapipeLLMDataSource,
    private val voskSpeechService: VoskSpeechService
) : ViewModel(), RecognitionListener {
    val messages: StateFlow<List<Message>>
        get() = _messages

    val isProcessing: StateFlow<Boolean>
        get() = _isProcessing

    val isSpeechRecognitionActive: StateFlow<Boolean>
        get() = _isSpeechRecognitionActive

    private val _messages: MutableStateFlow<List<Message>> = MutableStateFlow(
        emptyList()
    )

    private val _isProcessing: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val _isSpeechRecognitionActive = MutableStateFlow(false)

    private val lastMessageTime = AtomicLong(0)
    private val TIMEOUT_MS = 5000L // 5 seconds timeout
    private val currentAsyncInference = AtomicReference<ListenableFuture<String>?>(null)
    private var timeoutJob: Job? = null
    private var isWaitingForCompletion = false

    private var speechRecognitionListener: SpeechRecognitionListener? = null

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
                
                // Reset the LLM inference if there was a previous one
                if (currentAsyncInference.get() != null) {
                    llmDataSource.resetLLMInference()
                }
                
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
                            llmDataSource.resetLLMInference()
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
                    llmDataSource.resetLLMInference()
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
        voskSpeechService.shutdown()
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
        return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            .format(Date())
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault())
            .format(Date())
    }

    fun initSpeechRecognition(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        voskSpeechService.initModel(
            language = translationService.getCurrentLanguage(),
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun toggleSpeechRecognition() {
        if (_isSpeechRecognitionActive.value) {
            stopSpeechRecognition()
        } else {
            startSpeechRecognition()
        }
    }

    private fun startSpeechRecognition() {
        voskSpeechService.startListening(this)
        _isSpeechRecognitionActive.value = true
    }

    private fun stopSpeechRecognition() {
        voskSpeechService.stopListening()
        _isSpeechRecognitionActive.value = false
    }

    fun setSpeechRecognitionListener(listener: SpeechRecognitionListener) {
        speechRecognitionListener = listener
    }

    fun removeSpeechRecognitionListener() {
        speechRecognitionListener = null
    }

    // RecognitionListener implementation
    override fun onResult(hypothesis: String) {
        Log.d("ChatViewModel", "Speech recognition result: $hypothesis")
        speechRecognitionListener?.onResult(hypothesis)
    }

    override fun onFinalResult(hypothesis: String) {
        Log.d("ChatViewModel", "Speech recognition final result: $hypothesis")
        speechRecognitionListener?.onResult(hypothesis)
        stopSpeechRecognition()
    }

    override fun onPartialResult(hypothesis: String) {
        Log.d("ChatViewModel", "Speech recognition partial result: $hypothesis")
        speechRecognitionListener?.onResult(hypothesis)
    }

    override fun onError(exception: Exception) {
        Log.e("ChatViewModel", "Speech recognition error", exception)
        stopSpeechRecognition()
    }

    override fun onTimeout() {
        Log.d("ChatViewModel", "Speech recognition timeout")
        stopSpeechRecognition()
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

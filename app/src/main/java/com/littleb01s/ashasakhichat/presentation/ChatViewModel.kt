package com.littleb01s.ashasakhichat.presentation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.common.util.concurrent.ListenableFuture
import com.google.mediapipe.tasks.genai.llminference.ProgressListener
import com.littleb01s.ashasakhichat.data.MediapipeLLMDataSource
import com.littleb01s.ashasakhichat.data.repository.ModelDownloadManager
import com.littleb01s.ashasakhichat.data.api.ModelDownloadState
import com.littleb01s.ashasakhichat.domain.SendMessageToAshaSakhiChat
import com.littleb01s.ashasakhichat.domain.StartAshaSakhiChat
import com.littleb01s.ashasakhichat.domain.TranslationService
import com.littleb01s.ashasakhichat.domain.VoskSpeechService
import com.littleb01s.ashasakhichat.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.littleb01s.ashasakhichat.data.api.ModelDownloadService
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext

private val awaitingMessageFromAsha = Message(
    text = "ASHA Sakhi is typing...",
    isFromMe = false,
    isLoading = true
)
private const val TAG = "ChatViewModel"

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val startAshaSakhiChat: StartAshaSakhiChat,
    private val sendMessageToAsha: SendMessageToAshaSakhiChat,
    private val translationService: TranslationService,
    private val llmDataSource: MediapipeLLMDataSource,
    private val voskSpeechService: VoskSpeechService,
    @ApplicationContext private val context: Context,
    private val modelDownloadManager: ModelDownloadManager,
    private val modelDownloadService: ModelDownloadService
) : ViewModel(), RecognitionListener {

    // Model initialization states
    private val _isInitializing = MutableStateFlow(false)
    val isInitializing: StateFlow<Boolean> = _isInitializing

    private val _showDownloadDialog = MutableStateFlow(false)
    val showDownloadDialog: StateFlow<Boolean> = _showDownloadDialog

    private val _isLLMInitialized = MutableStateFlow(false)
    val isLLMInitialized: StateFlow<Boolean> = _isLLMInitialized

    private val _shouldNavigateToChat = MutableStateFlow(false)
    val shouldNavigateToChat: StateFlow<Boolean> = _shouldNavigateToChat

    private val _shouldNavigateToSettings = MutableStateFlow(false)
    val shouldNavigateToSettings: StateFlow<Boolean> = _shouldNavigateToSettings

    private val _hasShownWelcome = MutableStateFlow(false)
    val hasShownWelcome: StateFlow<Boolean> = _hasShownWelcome

    private val _initializationProgress = MutableStateFlow(0)
    val initializationProgress: StateFlow<Int> = _initializationProgress

    private val _initializationMessage = MutableStateFlow("")
    val initializationMessage: StateFlow<String> = _initializationMessage

    val modelDownloadState = modelDownloadManager.downloadState

    // Chat states
    val messages: StateFlow<List<Message>>
        get() = _messages

    val isProcessing: StateFlow<Boolean>
        get() = _isProcessing

    val isSpeechRecognitionActive: StateFlow<Boolean>
        get() = _isSpeechRecognitionActive

    private val _messages: MutableStateFlow<List<Message>> = MutableStateFlow(emptyList())
    private val _isProcessing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _isSpeechRecognitionActive = MutableStateFlow(false)

    private val lastMessageTime = AtomicLong(0)
    private val TIMEOUT_MS = 5000L // 5 seconds timeout
    private val currentAsyncInference = AtomicReference<ListenableFuture<String>?>(null)
    private var timeoutJob: Job? = null
    private var isWaitingForCompletion = false
    private var speechRecognitionListener: SpeechRecognitionListener? = null
    private var lastPartialLength = 0

    private var navController: NavController? = null

    // TTS instance and ready flag
    private var tts: android.speech.tts.TextToSpeech? = null
    private var ttsReady: Boolean = false

    init {
        // Initialize TTS with English locale
        tts = android.speech.tts.TextToSpeech(context) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(java.util.Locale.US)
                ttsReady = result != android.speech.tts.TextToSpeech.LANG_MISSING_DATA && result != android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED
            } else {
                ttsReady = false
            }
        }
    }

    /**
     * Speak the given text aloud using TTS in English (US).
     */
    fun speak(text: String) {
        if (ttsReady && text.isNotBlank()) {
            var currentLanguage = translationService.getCurrentLanguage();
            if(currentLanguage.equals("hi")){
                tts?.setLanguage(Locale.forLanguageTag("hin"));
            }
            tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "chat_message_tts")
        }
    }

    fun setNavController(navController: NavController) {
        this.navController = navController
    }

    fun initializeChat() {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Starting chat initialization")
                _isInitializing.value = true
                _initializationMessage.value = "Preparing ASHA Sakhi..."
                
                // Add a 3-second delay
                delay(3000)
                
                // Check if all required files exist
                val llmDir = File(context.getExternalFilesDir(null), "llm")
                if (!llmDir.exists()) {
                    llmDir.mkdirs()
                }

                val requiredFiles = listOf(
                    "Gecko_1024_quant.tflite",
                    "sentencepiece.model",
                    "asha-kb.pdf",
                    "gemma-2b-it-cpu-int4.bin"
                )

                val missingFiles = requiredFiles.filter { filename ->
                    val file = File(llmDir, filename)
                    !file.exists() || file.length() == 0L
                }

                Log.d("ChatViewModel", "Missing files: $missingFiles")

                if (missingFiles.isNotEmpty()) {
                    Log.d("ChatViewModel", "Files missing, showing download dialog")
                    _showDownloadDialog.value = true
                    _isLLMInitialized.value = false
                } else {
                    Log.d("ChatViewModel", "All files present, initializing LLM")
                    try {
                        // Initialize LLM
                        _initializationMessage.value = "Initializing AI model..."
                        llmDataSource.setLLMInference(context)
                        
                        // Initialize models and memorize content
                        _initializationMessage.value = "Loading ASHA guidelines..."
                        llmDataSource.initializeModels()
                        llmDataSource.memorizeContent(File(llmDir, "asha-kb.pdf").path)
                        
                        _isLLMInitialized.value = true
                        Log.d("ChatViewModel", "LLM initialization successful, navigating to chat")
                        navController?.navigate(Screen.Chat.route)
                    } catch (e: Exception) {
                        Log.e("ChatViewModel", "Error initializing LLM", e)
                        _isLLMInitialized.value = false
                        _showDownloadDialog.value = true
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error checking files", e)
                _isLLMInitialized.value = false
                _showDownloadDialog.value = true
            } finally {
                _isInitializing.value = false
                _initializationMessage.value = ""
            }
        }
    }

    private suspend fun initializeLLM() {
        try {
            // Reset and initialize LLM
            llmDataSource.setLLMInference(context)
            _isLLMInitialized.value = true
        } catch (e: Exception) {
            _isLLMInitialized.value = false
            throw e
        }
    }

    private fun initializeGuidelineContent() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pdfFile = File(context.getExternalFilesDir("llm"), "asha-kb.pdf")
                llmDataSource.memorizeContent(pdfFile.path)
                Log.d("ChatViewModel", "Successfully initialized ASHA guidelines content")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error initializing guidelines content: ${e.message}")
                throw e
            }
        }
    }

    fun retryModelDownload() {
        _showDownloadDialog.value = true
    }

    fun dismissDownloadDialog() {
        _showDownloadDialog.value = false
    }

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
            try {
                Log.d("ChatViewModel", "Starting message processing for: $message")
                _isProcessing.value = true
                lastMessageTime.set(System.currentTimeMillis())
                
                // Add user message - show original message in UI
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

                // Translate message to English if needed
                val currentLanguage = translationService.getCurrentLanguage()
                Log.d("ChatViewModel", "Current language: $currentLanguage")
                
                val messageForLLM = if (currentLanguage != "en") {
                    Log.d("ChatViewModel", "Translating message to English")
                    translationService.translateToEnglish(message)
                } else {
                    message
                }
                Log.d("ChatViewModel", "Message for LLM: $messageForLLM")

                // Generate response synchronously
                Log.d("ChatViewModel", "Generating response from LLM")
                val response = llmDataSource.generateResponse(messageForLLM)
                Log.d("ChatViewModel", "Received response from LLM")
                
                // Translate response if needed
                val translatedResponse = if (currentLanguage != "en") {
                    Log.d("ChatViewModel", "Translating response to $currentLanguage")
                    translationService.translate(response)
                } else {
                    response
                }
                Log.d("ChatViewModel", "Final translated response: ${translatedResponse.take(100)}...")

                // Update messages with the response
                val finalMessages = _messages.value.toMutableList()
                if (finalMessages.isNotEmpty() && finalMessages.last().isLoading) {
                    finalMessages.removeAt(finalMessages.lastIndex)
                }
                finalMessages.add(Message(
                    text = translatedResponse,
                    isFromMe = false
                ))
                _messages.value = finalMessages
                Log.d("ChatViewModel", "Message processing completed successfully")

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error in sendMessage: ${e.message}")
                e.printStackTrace()
                
                val errorMessages = _messages.value.toMutableList()
                if (errorMessages.isNotEmpty() && errorMessages.last().isLoading) {
                    errorMessages.removeAt(errorMessages.lastIndex)
                }
                val errorText = e.message ?: "An error occurred"
                val translatedError = translationService.translate(errorText)
                errorMessages.add(Message(
                    text = translatedError,
                    isFromMe = false,
                    isError = true
                ))
                _messages.value = errorMessages
            } finally {
                _isProcessing.value = false
                lastMessageTime.set(0)
                Log.d("ChatViewModel", "Message processing finished")
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
        tts?.shutdown()
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

    override fun onResult(hypothesis: String) {
        try {
            val jsonResult = JSONObject(hypothesis)
            val text = jsonResult.optString("text", "")
            if (text.isNotEmpty()) {
                speechRecognitionListener?.onResult(text)
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error parsing result", e)
        }
    }

    override fun onFinalResult(hypothesis: String) {
        try {
            val jsonResult = JSONObject(hypothesis)
            val text = jsonResult.optString("text", "")
            if (text.isNotEmpty()) {
                speechRecognitionListener?.onResult(text)
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error parsing final result", e)
        }
        stopSpeechRecognition()
    }

    override fun onPartialResult(hypothesis: String) {
        try {
            val jsonResult = JSONObject(hypothesis)
            val partial = jsonResult.optString("partial", "")
            // Only update if the new partial is longer than the previous one
            if (partial.length > lastPartialLength) {
                lastPartialLength = partial.length
                if (partial.isNotEmpty()) {
                    speechRecognitionListener?.onResult(partial)
                }
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error parsing partial result", e)
        }
    }

    override fun onError(exception: Exception) {
        Log.e("ChatViewModel", "Speech recognition error", exception)
        lastPartialLength = 0
        stopSpeechRecognition()
    }

    override fun onTimeout() {
        Log.d("ChatViewModel", "Speech recognition timeout")
        lastPartialLength = 0
        stopSpeechRecognition()
    }

    fun startSpeechRecognition() {
        lastPartialLength = 0
        voskSpeechService.startListening(this)
        _isSpeechRecognitionActive.value = true
    }

    fun stopSpeechRecognition() {
        voskSpeechService.stopListening()
        _isSpeechRecognitionActive.value = false
        lastPartialLength = 0
    }

    fun setSpeechRecognitionListener(listener: SpeechRecognitionListener) {
        speechRecognitionListener = listener
    }

    fun removeSpeechRecognitionListener() {
        speechRecognitionListener = null
    }

    fun resetNavigation() {
        Log.d("ChatViewModel", "Resetting navigation states")
        _shouldNavigateToChat.value = false
        _shouldNavigateToSettings.value = false
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


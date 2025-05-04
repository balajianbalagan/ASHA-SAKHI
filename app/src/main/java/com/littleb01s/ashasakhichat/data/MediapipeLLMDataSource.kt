package com.littleb01s.ashasakhichat.data

import android.content.Context
import android.util.Log
import androidx.compose.ui.res.stringResource
import com.google.ai.edge.localagents.rag.memory.DefaultSemanticTextMemory
import com.google.ai.edge.localagents.rag.memory.SqliteVectorStore
import com.google.ai.edge.localagents.rag.models.Embedder
import com.google.ai.edge.localagents.rag.models.GeckoEmbeddingModel
import com.google.ai.edge.localagents.rag.retrieval.RetrievalConfig
import com.google.ai.edge.localagents.rag.retrieval.RetrievalRequest
import com.google.common.collect.ImmutableList
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.littleb01s.ashasakhichat.data.api.ModelDownloadService
import com.littleb01s.ashasakhichat.utils.PDFReader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

private var llmInference : LlmInference? = null
private const val TAG =  "MediapipeLLMDataSource"
@Singleton
class MediapipeLLMDataSource @Inject constructor(
    private val context: Context,
    private val modelDownloadService: ModelDownloadService
) {
    private val systemPrompt = """
        You are a healthcare assistant for ASHA workers, trained on official ASHA guidelines. Provide accurate information based on the guidelines. If unsure, say so. Always recommend consulting proper medical authorities for serious concerns. Be specific and limit responses to three paragraphs.
    """.trimIndent()

    private lateinit var embedder: Embedder<String>
    private lateinit var semanticMemory: DefaultSemanticTextMemory
    private var isInitialized = false
    private var isContentMemorized = false


    init {
        // Only download files if they don't exist
        CoroutineScope(Dispatchers.IO).launch {
            downloadRequiredFiles()
        }
    }

    private suspend fun downloadRequiredFiles() {
        try {
            // Create llm directory if it doesn't exist
            val llmDir = File(context.getExternalFilesDir(null), "llm")
            if (!llmDir.exists()) {
                llmDir.mkdirs()
            }

            // Download Gecko model if needed
            val geckoModelFile = File(llmDir, "Gecko_1024_quant.tflite")
            if (!geckoModelFile.exists()) {
                Log.d("MediapipeLLMDataSource", "Gecko model not found, downloading...")
                // downloadFile(
                //     url = "https://asha-sakhi-cdn.b-cdn.net/Gecko_1024_quant.tflite",
                //     outputFile = geckoModelFile
                // )
            }

            // Download sentencepiece model if needed
            val sentencepieceFile = File(llmDir, "sentencepiece.model")
            if (!sentencepieceFile.exists()) {
                Log.d("MediapipeLLMDataSource", "Sentencepiece model not found, downloading...")
                // downloadFile(
                //     url = "https://asha-sakhi-cdn.b-cdn.net/sentencepiece.model",
                //     outputFile = sentencepieceFile
                // )
            }

            // Initialize models after downloading files
            initializeModels()
        } catch (e: Exception) {
            Log.e("MediapipeLLMDataSource", "Error downloading files: ${e.message}")
            throw e
        }
    }

    suspend fun initializeModels() {
        if (isInitialized) return

        try {
            val llmDir = File(context.getExternalFilesDir(null), "llm")
            val geckoModelFile = File(llmDir, "Gecko_1024_quant.tflite")
            val sentencepieceFile = File(llmDir, "sentencepiece.model")

            // Verify files exist
            if (!geckoModelFile.exists() || !sentencepieceFile.exists()) {
                return;
                //throw IllegalStateException("Required model files not found")
            }

            // Initialize embedder with the verified paths
            withContext(Dispatchers.Main) {
                embedder = GeckoEmbeddingModel(
                    geckoModelFile.path,
                    Optional.of(sentencepieceFile.path),
                    true
                )

                // Initialize semantic memory with a persistent vector store
                val vectorStore = SqliteVectorStore(768) // Gecko embedding model dimension
                semanticMemory = DefaultSemanticTextMemory(vectorStore, embedder)
            }

            isInitialized = true
            Log.d("MediapipeLLMDataSource", "Successfully initialized models")
        } catch (e: Exception) {
            Log.e("MediapipeLLMDataSource", "Error initializing models: ${e.message}")
            throw e
        }
    }

    private suspend fun downloadFile(url: String, outputFile: File) = withContext(Dispatchers.IO) {
        try {
            val response = modelDownloadService.downloadFile(url)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    // Create parent directories if they don't exist
                    outputFile.parentFile?.mkdirs()
                    
                    // Download the file
                    val inputStream = body.byteStream()
                    val outputStream = FileOutputStream(outputFile)
                    
                    inputStream.copyTo(outputStream)
                    outputStream.close()
                    inputStream.close()
                    
                    Log.d("MediapipeLLMDataSource", "Successfully downloaded ${outputFile.name} to ${outputFile.path}")
                } ?: throw Exception("Response body is null")
            } else {
                throw Exception("Failed to download ${outputFile.name}: HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("MediapipeLLMDataSource", "Error downloading ${outputFile.name}: ${e.message}")
            throw e
        }
    }

    suspend fun memorizeContent(pdfPath: String) {
        if (!isInitialized) {
            Log.e("MediapipeLLMDataSource", "Models not initialized. Call initializeModels() first.")
            return
        }

        try {
            // Check if PDF exists
            val pdfFile = File(pdfPath)
            if (!pdfFile.exists()) {
                throw IllegalStateException("PDF file not found at $pdfPath")
            }

            val chunks = PDFReader.readPDFInChunks(pdfPath)
            if (chunks.isEmpty()) {
                Log.e("MediapipeLLMDataSource", "No content chunks extracted from PDF")
                return
            }

            // Log the paths for verification
            Log.d("MediapipeLLMDataSource", "Gecko model path: ${File(context.getExternalFilesDir("llm"), "Gecko_1024_quant.tflite").path}")
            Log.d("MediapipeLLMDataSource", "Tokenizer model path: ${File(context.getExternalFilesDir("llm"), "sentencepiece.model").path}")
            Log.d("MediapipeLLMDataSource", "PDF path: $pdfPath")
            Log.d("MediapipeLLMDataSource", "Number of chunks: ${chunks.size}")

            // Log each chunk's content
            chunks.forEachIndexed { index, chunk ->
                Log.d("MediapipeLLMDataSource", "Chunk $index: ${chunk.take(200)}...") // Log first 200 chars of each chunk
            }

            // Memorize the chunks
            semanticMemory.recordBatchedMemoryItems(ImmutableList.copyOf(chunks))
            isContentMemorized = true
            Log.d("MediapipeLLMDataSource", "Successfully memorized ${chunks.size} chunks from PDF")
        } catch (e: Exception) {
            Log.e("MediapipeLLMDataSource", "Error memorizing PDF content: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun generateResponse(query: String): String {
        if (!isInitialized || !isContentMemorized) {
            Log.e("MediapipeLLMDataSource", "Models not initialized or content not memorized")
            return "I apologize, but I'm not ready to answer questions yet. Please try again in a moment."
        }

        try {
            Log.d("MediapipeLLMDataSource", "Starting response generation for query: $query")
            
            val retrievalRequest = RetrievalRequest.create(
                query,
                RetrievalConfig.create(
                    1, // topK - increased to get more context
                    0.7f, // minSimilarityScore - slightly lowered to get more matches
                    RetrievalConfig.TaskType.RETRIEVAL_QUERY
                )
            )
            
            Log.d("MediapipeLLMDataSource", "Retrieving relevant context...")
            val retrievalResponse =
                withContext(Dispatchers.IO) {
                    semanticMemory.retrieveResults(retrievalRequest).get()
                }
            val relevantContext = retrievalResponse.entities.map { it.data }
            
            // Log the retrieved context
            Log.d("MediapipeLLMDataSource", "Retrieved ${relevantContext.size} relevant chunks for query: $query")
            relevantContext.forEachIndexed { index, context ->
                Log.d("MediapipeLLMDataSource", "Relevant chunk $index: ${context.take(200)}...")
            }
            
            val contextText = relevantContext.joinToString("\n\n")
            
            val prompt = """
                $systemPrompt
                
                Relevant context from ASHA guidelines:
                $contextText
                
                User's query: $query
                
                Please provide a detailed response based on the ASHA guidelines context provided above. If the context doesn't contain relevant information, say so.
            """.trimIndent()
            
            Log.d("MediapipeLLMDataSource", "Sending prompt to LLM: ${prompt.take(500)}...")
            
            val response = withContext(Dispatchers.IO) {
                try {
                    Log.d("MediapipeLLMDataSource", "Generating LLM response...")
                    var result = llmInference?.generateResponse(prompt)
                    if(result==null) {
                        result = "Error in model"
                    }
                    Log.d("MediapipeLLMDataSource", "LLM response generated successfully")
                    result
                } catch (e: Exception) {
                    Log.e("MediapipeLLMDataSource", "Error in LLM response generation: ${e.message}")
                    e.printStackTrace()
                    throw e
                }
            }
            
            Log.d("MediapipeLLMDataSource", "Response generation completed successfully")
            return response
        } catch (e: Exception) {
            Log.e("MediapipeLLMDataSource", "Error in generateResponse: ${e.message}")
            e.printStackTrace()
            return "I apologize, but I encountered an error while processing your query. Please try again."
        }
    }

    fun setLLMInference(@ApplicationContext context: Context)  {
            Log.d(TAG, "Starting LLM initialization...")
            val llmDir = File(context.getExternalFilesDir(null), "llm")
            if (!llmDir.exists()) {
                llmDir.mkdirs()
            }

            val modelFile = File(llmDir, "gemma-2b-it-cpu-int4.bin")

            Log.d(TAG, "Model file details:")
            Log.d(TAG, "Exists: ${modelFile.exists()}")
            Log.d(TAG, "Size: ${modelFile.length()} bytes")
            Log.d(TAG, "Can read: ${modelFile.canRead()}")
            Log.d(TAG, "Absolute path: ${modelFile.absolutePath}")
            Log.d(TAG, "Parent exists: ${modelFile.parentFile?.exists()}")
            Log.d(TAG, "Parent can read: ${modelFile.parentFile?.canRead()}")

            try {
                Log.d(TAG, "Creating LLM options...")
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.path)
                    .build()

                Log.d(TAG, "Created LLM options successfully")
                Log.d(TAG, "Creating LLM instance...")
                val llm = LlmInference.createFromOptions(context, options)
                Log.d(TAG, "LLM instance created successfully")
                llmInference = llm
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing LLM", e)
                throw e
            }
        }
        // Reset any necessary state
    }

    suspend fun start(): String {
        if (llmInference != null) {
            return withContext(Dispatchers.IO) {
                Log.i(
                    MediapipeLLMDataSource::class.java.simpleName,
                    "Initializing ASHA Sakhi chat"
                )

                llmInference!!.generateResponse("\n\nI am ready to assist with your healthcare queries. Please ask your specific question. Limit to 300 words")
            }
        }
        return "I am ASHA Sakhi! Ready to answer your queries!"
    }



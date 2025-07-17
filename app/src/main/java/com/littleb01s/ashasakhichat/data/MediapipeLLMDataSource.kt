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
import com.littleb01s.ashasakhichat.utils.ASHAJsonReader
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
import com.google.common.util.concurrent.ListenableFuture
import com.google.mediapipe.tasks.genai.llminference.ProgressListener

private var llmInference : LlmInference? = null
private const val TAG =  "MediapipeLLMDataSource"
@Singleton
class MediapipeLLMDataSource @Inject constructor(
    private val context: Context,
    private val modelDownloadService: ModelDownloadService
) {
    private val systemPrompt = """
        You are a healthcare assistant for ASHA workers, trained on official ASHA guidelines. Provide accurate information based on the guidelines. If unsure, say so. Always recommend consulting proper medical authorities for serious concerns. Be specific and provide complete, well-structured responses. Always end your response with a complete sentence and do not cut off mid-sentence.
    """.trimIndent()

    private lateinit var embedder: Embedder<String>
    private lateinit var semanticMemory: DefaultSemanticTextMemory
    private var isInitialized = false
    private var isContentMemorized = false

    init {
        // Remove automatic initialization
    }

    suspend fun initializeModels() {
        if (isInitialized) return

        try {
            Log.d(TAG, "Initializing embedder and semantic memory...")
            val llmDir = File(context.getExternalFilesDir(null), "llm")
            val geckoModelFile = File(llmDir, "Gecko_1024_quant.tflite")
            val sentencepieceFile = File(llmDir, "sentencepiece.model")

            // Verify files exist
            if (!geckoModelFile.exists() || !sentencepieceFile.exists()) {
                Log.e(TAG, "Required model files not found: ${geckoModelFile.path}, ${sentencepieceFile.path}")
                throw IllegalStateException("Required model files not found")
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
            Log.d(TAG, "Successfully initialized embedder and semantic memory")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing models: ${e.message}")
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

    /**
     * Memorizes the ASHA guidelines content from the default app storage location.
     * This should be called after initializeModels() and LLM inference are ready.
     */
    suspend fun memorizeContent() {
        if (!isInitialized) {
            Log.e(TAG, "Models not initialized. Call initializeModels() first.")
            throw IllegalStateException("Models not initialized. Call initializeModels() first.")
        }
        try {
            val llmDir = File(context.getExternalFilesDir(null), "llm")
            val jsonFile = File(llmDir, "asha_guidelines.json")
            val pdfFile = File(llmDir, "asha-kb.pdf")
            Log.d(TAG, "Reading ASHA guidelines for memorization from ${jsonFile.path} and ${pdfFile.path} ...")
            val chunks = ASHAJsonReader.readASHAGuidelines(context, jsonFile.path, pdfFile.path)
            Log.d(TAG, "Number of chunks to memorize: ${chunks.size}")
            if (chunks.isEmpty()) {
                Log.e(TAG, "No content chunks extracted from JSON or PDF")
                throw IllegalStateException("No content chunks extracted from JSON or PDF")
            }
            Log.d(TAG, "Sample chunk for memorization: ${chunks[0]}")
            // Ensure all chunks are at most 1000 characters
            val limitedChunks = mutableListOf<String>()
            for (chunk in chunks) {
                if (chunk.length <= 1000) {
                    limitedChunks.add(chunk)
                } else {
                    var start = 0
                    var partIdx = 1
                    while (start < chunk.length) {
                        val end = minOf(start + 1000, chunk.length)
                        val part = chunk.substring(start, end)
                        // Optionally, add a suffix to indicate part number
                        // But since these are JSON strings, chunk_id is inside the JSON
                        limitedChunks.add(part)
                        start = end
                        partIdx++
                    }
                }
            }
            limitedChunks.forEach { chunk ->
                Log.d(TAG, "Chunk size': ${chunk.length}")
            }
            Log.d(TAG, "Starting embedding and storing chunks in vector store...");
            semanticMemory.recordBatchedMemoryItems(ImmutableList.copyOf(limitedChunks))
            isContentMemorized = true
            Log.d(TAG, "Successfully memorized ${chunks.size} structured chunks from JSON/PDF")
        } catch (e: Exception) {
            Log.e(TAG, "Error memorizing content: ${e.message}")
            throw e
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
                    5, // topK - increased to get more context
                    0.3f, // minSimilarityScore - slightly lowered to get more matches
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
                Log.d("MediapipeLLMDataSource", "Relevant chunk $index: ${context.take(400)}...")
            }
            
            // Parse JSON chunks and extract relevant information
            val structuredContext = relevantContext.mapNotNull { context ->
                try {
                    val jsonObject = org.json.JSONObject(context)
                    val title = jsonObject.optString("title", "")
                    val content = jsonObject.optString("content", "")
                    val keyPoints = jsonObject.optJSONArray("key_points")?.let { array ->
                        (0 until array.length()).map { array.getString(it) }
                    } ?: emptyList()
                    val guidelines = jsonObject.optJSONArray("guidelines")?.let { array ->
                        (0 until array.length()).map { array.getString(it) }
                    } ?: emptyList()
                    val sectionType = jsonObject.optString("section_type", "")
                    val chunkId = jsonObject.optString("chunk_id", "")
                    
                    // Extract additional fields based on section type
                    val additionalInfo = when (sectionType) {
                        "pregnancy_care" -> {
                            val visitType = jsonObject.optString("visit_type", "")
                            val complicationType = jsonObject.optString("complication_type", "")
                            when {
                                visitType.isNotEmpty() -> "Visit Type: $visitType"
                                complicationType.isNotEmpty() -> "Complication: $complicationType"
                                else -> ""
                            }
                        }
                        "delivery_care" -> {
                            val procedureType = jsonObject.optString("procedure_type", "")
                            if (procedureType.isNotEmpty()) "Procedure Type: $procedureType" else ""
                        }
                        "postpartum_care" -> {
                            val careType = jsonObject.optString("care_type", "")
                            if (careType.isNotEmpty()) "Care Type: $careType" else ""
                        }
                        "newborn_care" -> {
                            val careType = jsonObject.optString("care_type", "")
                            if (careType.isNotEmpty()) "Care Type: $careType" else ""
                        }
                        "immunization" -> {
                            val immunizationType = jsonObject.optString("immunization_type", "")
                            if (immunizationType.isNotEmpty()) "Immunization Type: $immunizationType" else ""
                        }
                        "nutrition" -> {
                            val nutritionType = jsonObject.optString("nutrition_type", "")
                            if (nutritionType.isNotEmpty()) "Nutrition Type: $nutritionType" else ""
                        }
                        "emergency_care" -> {
                            val emergencyType = jsonObject.optString("emergency_type", "")
                            if (emergencyType.isNotEmpty()) "Emergency Type: $emergencyType" else ""
                        }
                        "general_guidelines" -> {
                            val category = jsonObject.optString("category", "")
                            if (category.isNotEmpty()) "Category: $category" else ""
                        }
                        "pregnancy_scheme" -> {
                            val schemeType = jsonObject.optString("scheme_type", "")
                            if (schemeType.isNotEmpty()) "Scheme Type: $schemeType" else ""
                        }
                        "clinical_protocol" -> {
                            val protocolType = jsonObject.optString("protocol_type", "")
                            if (protocolType.isNotEmpty()) "Protocol Type: $protocolType" else ""
                        }
                        "healthcare_infrastructure" -> "Healthcare Infrastructure Information"
                        else -> ""
                    }
                    
                    mapOf(
                        "title" to title,
                        "content" to content,
                        "key_points" to keyPoints,
                        "guidelines" to guidelines,
                        "section_type" to sectionType,
                        "chunk_id" to chunkId,
                        "additional_info" to additionalInfo
                    )
                } catch (e: Exception) {
                    Log.w("MediapipeLLMDataSource", "Failed to parse JSON chunk: ${e.message}")
                    null
                }
            }
            
            // Build structured context text
            val contextText = structuredContext.joinToString("\n\n") { chunk ->
                buildString {
                    appendLine("**${chunk["title"]}**")
                    appendLine("Section Type: ${chunk["section_type"]}")
                    
                    val additionalInfo = chunk["additional_info"] as? String
                    if (!additionalInfo.isNullOrEmpty()) {
                        appendLine("Additional Info: $additionalInfo")
                    }
                    
                    val keyPoints = chunk["key_points"] as? List<String>
                    if (!keyPoints.isNullOrEmpty()) {
                        appendLine("Key Points:")
                        for (point in keyPoints) {
                            appendLine("• $point")
                        }
                    }
                    
                    val guidelines = chunk["guidelines"] as? List<String>
                    if (!guidelines.isNullOrEmpty()) {
                        appendLine("Guidelines:")
                        for (guideline in guidelines) {
                            appendLine("• $guideline")
                        }
                    }
                    
                    appendLine("Content: ${chunk["content"]}")
                }
            }

            val trimmedContext = contextText.take(200);
            
            val prompt = """
                $systemPrompt
                
                Relevant context from ASHA guidelines (structured):
                $trimmedContext
                
                User's query: $query
                
                Please provide a detailed response based on the ASHA guidelines context provided above. Focus on the key points and guidelines that are most relevant to the user's query. Structure your response clearly with:
                - Key information from the guidelines
                - Specific procedures or steps if mentioned
                - Important warnings or precautions
                - Additional context from the section type and additional info
                
                If the context doesn't contain relevant information, say so. Provide a complete response with proper conclusion. Do not cut off mid-sentence.
            """.trimIndent()
            
            Log.d("MediapipeLLMDataSource", "Sending prompt to LLM: ${prompt.take(200)}...")
            
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
            
            // Ensure response is complete and add AI note
            val completeResponse = ensureCompleteResponse(response)
            val responseWithNote = if (completeResponse.isNotBlank()) {
                "$completeResponse\n\n**💡 AI Generated Response**"
            } else {
                "I apologize, but I couldn't generate a proper response. Please try again.\n\n**💡 AI Generated Response**"
            }
            
            return responseWithNote
        } catch (e: Exception) {
            Log.e("MediapipeLLMDataSource", "Error in generateResponse: ${e.message}")
            e.printStackTrace()
            return "I apologize, but I encountered an error while processing your query. Please try again."
        }
    }

    suspend fun generateQuickResponse(query: String): String {
        if (!isInitialized) {
            Log.e("MediapipeLLMDataSource", "Models not initialized")
            return "I apologize, but I'm not ready to answer questions yet. Please try again in a moment."
        }

        try {
            Log.d("MediapipeLLMDataSource", "Generating quick response for query: $query")
            
            val quickPrompt = """
                $systemPrompt
                
                User's query: $query
                
                Please provide a brief, helpful statement as an ASHA healthcare assistant. Give general information about ASHA guidelines, pregnancy care, or related topics. Keep it concise (2-3 sentences) but informative. State that you are currently searching through ASHA guidelines to provide a detailed response with specific information.
            """.trimIndent()
            
            val response = withContext(Dispatchers.IO) {
                try {
                    Log.d("MediapipeLLMDataSource", "Generating quick LLM response...")
                    var result = llmInference?.generateResponse(quickPrompt)
                    if(result==null) {
                        result = "Error in model"
                    }
                    Log.d("MediapipeLLMDataSource", "Quick LLM response generated successfully")
                    result
                } catch (e: Exception) {
                    Log.e("MediapipeLLMDataSource", "Error in quick LLM response generation: ${e.message}")
                    e.printStackTrace()
                    throw e
                }
            }
            
            Log.d("MediapipeLLMDataSource", "Quick response generation completed successfully")
            
            // Ensure response is complete and add AI note
            val completeResponse = ensureCompleteResponse(response)
            val responseWithNote = if (completeResponse.isNotBlank()) {
                "$completeResponse\n\n**💡 AI Generated Response**"
            } else {
                "I apologize, but I couldn't generate a proper response. Please try again.\n\n**💡 AI Generated Response**"
            }
            
            return responseWithNote
        } catch (e: Exception) {
            Log.e("MediapipeLLMDataSource", "Error in generateQuickResponse: ${e.message}")
            e.printStackTrace()
            return "I apologize, but I encountered an error while processing your query. Please try again."
        }
    }

    suspend fun generateDetailedResponse(query: String): String {
        if (!isInitialized || !isContentMemorized) {
            Log.e("MediapipeLLMDataSource", "Models not initialized or content not memorized")
            return "I apologize, but I'm not ready to provide detailed responses yet. Please try again in a moment."
        }

        try {
            Log.d("MediapipeLLMDataSource", "Starting detailed response generation for query: $query")
            
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
            
            // Parse JSON chunks and extract relevant information
            val structuredContext = relevantContext.mapNotNull { context ->
                try {
                    val jsonObject = org.json.JSONObject(context)
                    val title = jsonObject.optString("title", "")
                    val content = jsonObject.optString("content", "")
                    val keyPoints = jsonObject.optJSONArray("key_points")?.let { array ->
                        (0 until array.length()).map { array.getString(it) }
                    } ?: emptyList()
                    val guidelines = jsonObject.optJSONArray("guidelines")?.let { array ->
                        (0 until array.length()).map { array.getString(it) }
                    } ?: emptyList()
                    val sectionType = jsonObject.optString("section_type", "")
                    val chunkId = jsonObject.optString("chunk_id", "")
                    
                    // Extract additional fields based on section type
                    val additionalInfo = when (sectionType) {
                        "pregnancy_care" -> {
                            val visitType = jsonObject.optString("visit_type", "")
                            val complicationType = jsonObject.optString("complication_type", "")
                            when {
                                visitType.isNotEmpty() -> "Visit Type: $visitType"
                                complicationType.isNotEmpty() -> "Complication: $complicationType"
                                else -> ""
                            }
                        }
                        "delivery_care" -> {
                            val procedureType = jsonObject.optString("procedure_type", "")
                            if (procedureType.isNotEmpty()) "Procedure Type: $procedureType" else ""
                        }
                        "postpartum_care" -> {
                            val careType = jsonObject.optString("care_type", "")
                            if (careType.isNotEmpty()) "Care Type: $careType" else ""
                        }
                        "newborn_care" -> {
                            val careType = jsonObject.optString("care_type", "")
                            if (careType.isNotEmpty()) "Care Type: $careType" else ""
                        }
                        "immunization" -> {
                            val immunizationType = jsonObject.optString("immunization_type", "")
                            if (immunizationType.isNotEmpty()) "Immunization Type: $immunizationType" else ""
                        }
                        "nutrition" -> {
                            val nutritionType = jsonObject.optString("nutrition_type", "")
                            if (nutritionType.isNotEmpty()) "Nutrition Type: $nutritionType" else ""
                        }
                        "emergency_care" -> {
                            val emergencyType = jsonObject.optString("emergency_type", "")
                            if (emergencyType.isNotEmpty()) "Emergency Type: $emergencyType" else ""
                        }
                        "general_guidelines" -> {
                            val category = jsonObject.optString("category", "")
                            if (category.isNotEmpty()) "Category: $category" else ""
                        }
                        "pregnancy_scheme" -> {
                            val schemeType = jsonObject.optString("scheme_type", "")
                            if (schemeType.isNotEmpty()) "Scheme Type: $schemeType" else ""
                        }
                        "clinical_protocol" -> {
                            val protocolType = jsonObject.optString("protocol_type", "")
                            if (protocolType.isNotEmpty()) "Protocol Type: $protocolType" else ""
                        }
                        "healthcare_infrastructure" -> "Healthcare Infrastructure Information"
                        else -> ""
                    }
                    
                    mapOf(
                        "title" to title,
                        "content" to content,
                        "key_points" to keyPoints,
                        "guidelines" to guidelines,
                        "section_type" to sectionType,
                        "chunk_id" to chunkId,
                        "additional_info" to additionalInfo
                    )
                } catch (e: Exception) {
                    Log.w("MediapipeLLMDataSource", "Failed to parse JSON chunk: ${e.message}")
                    null
                }
            }
            
            // Build structured context text
            val contextText = structuredContext.joinToString("\n\n") { chunk ->
                buildString {
                    appendLine("**${chunk["title"]}**")
                    appendLine("Section Type: ${chunk["section_type"]}")
                    
                    val additionalInfo = chunk["additional_info"] as? String
                    if (!additionalInfo.isNullOrEmpty()) {
                        appendLine("Additional Info: $additionalInfo")
                    }
                    
                    val keyPoints = chunk["key_points"] as? List<String>
                    if (!keyPoints.isNullOrEmpty()) {
                        appendLine("Key Points:")
                        for (point in keyPoints) {
                            appendLine("• $point")
                        }
                    }
                    
                    val guidelines = chunk["guidelines"] as? List<String>
                    if (!guidelines.isNullOrEmpty()) {
                        appendLine("Guidelines:")
                        for (guideline in guidelines) {
                            appendLine("• $guideline")
                        }
                    }
                    
                    appendLine("Content: ${chunk["content"]}")
                }
            }
            
            val prompt = """
                $systemPrompt
                
                Relevant context from ASHA guidelines (structured):
                $contextText
                
                User's query: $query
                
                Please provide a detailed response based on the ASHA guidelines context provided above. Focus on the key points and guidelines that are most relevant to the user's query. Structure your response clearly with:
                - Key information from the guidelines
                - Specific procedures or steps if mentioned
                - Important warnings or precautions
                - Additional context from the section type and additional info
                
                If the context doesn't contain relevant information, say so. Provide a complete response with proper conclusion. Do not cut off mid-sentence.
            """.trimIndent()
            
            Log.d("MediapipeLLMDataSource", "Sending prompt to LLM: ${prompt.take(200)}...")
            
            val response = withContext(Dispatchers.IO) {
                try {
                    Log.d("MediapipeLLMDataSource", "Generating detailed LLM response...")
                    var result = llmInference?.generateResponse(prompt)
                    if(result==null) {
                        result = "Error in model"
                    }
                    Log.d("MediapipeLLMDataSource", "Detailed LLM response generated successfully")
                    result
                } catch (e: Exception) {
                    Log.e("MediapipeLLMDataSource", "Error in detailed LLM response generation: ${e.message}")
                    e.printStackTrace()
                    throw e
                }
            }
            
            Log.d("MediapipeLLMDataSource", "Detailed response generation completed successfully")
            
            // Ensure response is complete and add AI note
            val completeResponse = ensureCompleteResponse(response)
            val responseWithNote = if (completeResponse.isNotBlank()) {
                "$completeResponse\n\n**💡 AI Generated Response**"
            } else {
                "I apologize, but I couldn't generate a proper response. Please try again.\n\n**💡 AI Generated Response**"
            }
            
            return responseWithNote
        } catch (e: Exception) {
            Log.e("MediapipeLLMDataSource", "Error in generateDetailedResponse: ${e.message}")
            e.printStackTrace()
            return "I apologize, but I encountered an error while processing your query. Please try again."
        }
    }

    suspend fun generateInstantResponse(query: String): String {
        if (!isInitialized) {
            Log.e("MediapipeLLMDataSource", "Models not initialized")
            return "I apologize, but I'm not ready to answer questions yet. Please try again in a moment."
        }

        try {
            Log.d("MediapipeLLMDataSource", "Generating instant response for query: $query")
            
            val instantPrompt = """
                You are an ASHA healthcare assistant. The user asked: "$query"
                
                Provide a brief, helpful response in 1-2 sentences about ASHA guidelines, pregnancy care, or maternal health. Be encouraging and mention that you're searching for specific details.
            """.trimIndent()
            
            val response = withContext(Dispatchers.IO) {
                try {
                    Log.d("MediapipeLLMDataSource", "Generating instant LLM response...")
                    var result = llmInference?.generateResponse(instantPrompt)
                    if(result==null) {
                        result = "I'm here to help with ASHA guidelines. Let me search for specific details for you."
                    }
                    Log.d("MediapipeLLMDataSource", "Instant LLM response generated successfully")
                    result
                } catch (e: Exception) {
                    Log.e("MediapipeLLMDataSource", "Error in instant LLM response generation: ${e.message}")
                    e.printStackTrace()
                    throw e
                }
            }
            
            Log.d("MediapipeLLMDataSource", "Instant response generation completed successfully")
            
            // Ensure response is complete and add AI note
            val completeResponse = ensureCompleteResponse(response)
            val responseWithNote = if (completeResponse.isNotBlank()) {
                "$completeResponse\n\n**💡 AI Generated Response**"
            } else {
                "I'm here to help with ASHA guidelines. Let me search for specific details for you.\n\n**💡 AI Generated Response**"
            }
            
            return responseWithNote
        } catch (e: Exception) {
            Log.e("MediapipeLLMDataSource", "Error in generateInstantResponse: ${e.message}")
            e.printStackTrace()
            return "I'm here to help with ASHA guidelines. Let me search for specific details for you.\n\n**💡 AI Generated Response**"
        }
    }

    fun setLLMInference(@ApplicationContext context: Context)  {
        
            Log.d(TAG, "Starting LLM initialization...")
            val llmDir = File(context.getExternalFilesDir(null), "llm")
            if (!llmDir.exists()) {
                llmDir.mkdirs()
            }

            val modelFile = File(llmDir, "gemma3-1b-it-int4.task")

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
                    .setModelPath(modelFile.path).setMaxTopK(1024)
                    .setMaxTokens(1024)
                    .build()

                Log.d(TAG, "Created LLM options successfully")
                Log.d(TAG, "Creating LLM instance...")
                val llm = LlmInference.createFromOptions(context, options)
                Log.d(TAG, "LLM instance created successfully")
                llmInference = llm
                // Do NOT call memorizeContent() here; it must be called after initializeModels() externally
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing LLM", e)
                throw e
            }
        }

    suspend fun generateResponseAsync(query: String, progressListener: ProgressListener<String>): ListenableFuture<String> {
        if (!isInitialized || !isContentMemorized) {
            Log.e("MediapipeLLMDataSource", "Models not initialized or content not memorized")
            throw IllegalStateException("Models not initialized or content not memorized")
        }

        return withContext(Dispatchers.IO) {
            try {
                Log.d("MediapipeLLMDataSource", "Starting async response generation for query: $query")
                
                val retrievalRequest = RetrievalRequest.create(
                    query,
                    RetrievalConfig.create(
                        1, // topK - increased to get more context
                        0.7f, // minSimilarityScore - slightly lowered to get more matches
                        RetrievalConfig.TaskType.RETRIEVAL_QUERY
                    )
                )
                
                Log.d("MediapipeLLMDataSource", "Retrieving relevant context...")
                val retrievalResponse = semanticMemory.retrieveResults(retrievalRequest).get()
                val relevantContext = retrievalResponse.entities.map { it.data }
                
                // Log the retrieved context
                Log.d("MediapipeLLMDataSource", "Retrieved ${relevantContext.size} relevant chunks for query: $query")
                relevantContext.forEachIndexed { index, context ->
                    Log.d("MediapipeLLMDataSource", "Relevant chunk $index: ${context.take(200)}...")
                }
                
                // Parse JSON chunks and extract relevant information
                val structuredContext = relevantContext.mapNotNull { context ->
                    try {
                        val jsonObject = org.json.JSONObject(context)
                        val title = jsonObject.optString("title", "")
                        val content = jsonObject.optString("content", "")
                        val keyPoints = jsonObject.optJSONArray("key_points")?.let { array ->
                            (0 until array.length()).map { array.getString(it) }
                        } ?: emptyList()
                        val guidelines = jsonObject.optJSONArray("guidelines")?.let { array ->
                            (0 until array.length()).map { array.getString(it) }
                        } ?: emptyList()
                        val sectionType = jsonObject.optString("section_type", "")
                        val chunkId = jsonObject.optString("chunk_id", "")
                        
                        // Extract additional fields based on section type
                        val additionalInfo = when (sectionType) {
                            "pregnancy_care" -> {
                                val visitType = jsonObject.optString("visit_type", "")
                                val complicationType = jsonObject.optString("complication_type", "")
                                when {
                                    visitType.isNotEmpty() -> "Visit Type: $visitType"
                                    complicationType.isNotEmpty() -> "Complication: $complicationType"
                                    else -> ""
                                }
                            }
                            "delivery_care" -> {
                                val procedureType = jsonObject.optString("procedure_type", "")
                                if (procedureType.isNotEmpty()) "Procedure Type: $procedureType" else ""
                            }
                            "postpartum_care" -> {
                                val careType = jsonObject.optString("care_type", "")
                                if (careType.isNotEmpty()) "Care Type: $careType" else ""
                            }
                            "newborn_care" -> {
                                val careType = jsonObject.optString("care_type", "")
                                if (careType.isNotEmpty()) "Care Type: $careType" else ""
                            }
                            "immunization" -> {
                                val immunizationType = jsonObject.optString("immunization_type", "")
                                if (immunizationType.isNotEmpty()) "Immunization Type: $immunizationType" else ""
                            }
                            "nutrition" -> {
                                val nutritionType = jsonObject.optString("nutrition_type", "")
                                if (nutritionType.isNotEmpty()) "Nutrition Type: $nutritionType" else ""
                            }
                            "emergency_care" -> {
                                val emergencyType = jsonObject.optString("emergency_type", "")
                                if (emergencyType.isNotEmpty()) "Emergency Type: $emergencyType" else ""
                            }
                            "general_guidelines" -> {
                                val category = jsonObject.optString("category", "")
                                if (category.isNotEmpty()) "Category: $category" else ""
                            }
                            "pregnancy_scheme" -> {
                                val schemeType = jsonObject.optString("scheme_type", "")
                                if (schemeType.isNotEmpty()) "Scheme Type: $schemeType" else ""
                            }
                            "clinical_protocol" -> {
                                val protocolType = jsonObject.optString("protocol_type", "")
                                if (protocolType.isNotEmpty()) "Protocol Type: $protocolType" else ""
                            }
                            "healthcare_infrastructure" -> "Healthcare Infrastructure Information"
                            else -> ""
                        }
                        
                        mapOf(
                            "title" to title,
                            "content" to content,
                            "key_points" to keyPoints,
                            "guidelines" to guidelines,
                            "section_type" to sectionType,
                            "chunk_id" to chunkId,
                            "additional_info" to additionalInfo
                        )
                    } catch (e: Exception) {
                        Log.w("MediapipeLLMDataSource", "Failed to parse JSON chunk: ${e.message}")
                        null
                    }
                }
                
                // Build structured context text
                val contextText = structuredContext.joinToString("\n\n") { chunk ->
                    buildString {
                        appendLine("**${chunk["title"]}**")
                        appendLine("Section Type: ${chunk["section_type"]}")
                        
                        val additionalInfo = chunk["additional_info"] as? String
                        if (!additionalInfo.isNullOrEmpty()) {
                            appendLine("Additional Info: $additionalInfo")
                        }
                        
                        val keyPoints = chunk["key_points"] as? List<String>
                        if (!keyPoints.isNullOrEmpty()) {
                            appendLine("Key Points:")
                            for (point in keyPoints) {
                                appendLine("• $point")
                            }
                        }
                        
                        val guidelines = chunk["guidelines"] as? List<String>
                        if (!guidelines.isNullOrEmpty()) {
                            appendLine("Guidelines:")
                            for (guideline in guidelines) {
                                appendLine("• $guideline")
                            }
                        }
                        
                        appendLine("Content: ${chunk["content"]}")
                    }
                }
                
                val prompt = """
                    $systemPrompt
                    
                    Relevant context from ASHA guidelines (structured):
                    $contextText
                    
                    User's query: $query
                    
                    Please provide a detailed response based on the ASHA guidelines context provided above. Focus on the key points and guidelines that are most relevant to the user's query. Structure your response clearly with:
                    - Key information from the guidelines
                    - Specific procedures or steps if mentioned
                    - Important warnings or precautions
                    - Additional context from the section type and additional info
                    
                    If the context doesn't contain relevant information, say so. Provide a complete response with proper conclusion. Do not cut off mid-sentence.
                """.trimIndent()
                
                Log.d("MediapipeLLMDataSource", "Sending prompt to LLM: ${prompt.take(200)}...")
                
                llmInference?.generateResponseAsync(prompt, progressListener)
                    ?: throw IllegalStateException("LLM not initialized")
            } catch (e: Exception) {
                Log.e("MediapipeLLMDataSource", "Error in generateResponseAsync: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
    }

    suspend fun generateDetailedResponseAsync(query: String, progressListener: ProgressListener<String>): ListenableFuture<String> {
        if (!isInitialized || !isContentMemorized) {
            Log.e("MediapipeLLMDataSource", "Models not initialized or content not memorized")
            throw IllegalStateException("Models not initialized or content not memorized")
        }

        return withContext(Dispatchers.IO) {
            try {
                Log.d("MediapipeLLMDataSource", "Starting async detailed response generation for query: $query")
                
                val retrievalRequest = RetrievalRequest.create(
                    query,
                    RetrievalConfig.create(
                        1, // topK - increased to get more context
                        0.7f, // minSimilarityScore - slightly lowered to get more matches
                        RetrievalConfig.TaskType.RETRIEVAL_QUERY
                    )
                )
                
                Log.d("MediapipeLLMDataSource", "Retrieving relevant context...")
                val retrievalResponse = semanticMemory.retrieveResults(retrievalRequest).get()
                val relevantContext = retrievalResponse.entities.map { it.data }
                
                // Log the retrieved context
                Log.d("MediapipeLLMDataSource", "Retrieved ${relevantContext.size} relevant chunks for query: $query")
                relevantContext.forEachIndexed { index, context ->
                    Log.d("MediapipeLLMDataSource", "Relevant chunk $index: ${context.take(200)}...")
                }
                
                // Parse JSON chunks and extract relevant information
                val structuredContext = relevantContext.mapNotNull { context ->
                    try {
                        val jsonObject = org.json.JSONObject(context)
                        val title = jsonObject.optString("title", "")
                        val content = jsonObject.optString("content", "")
                        val keyPoints = jsonObject.optJSONArray("key_points")?.let { array ->
                            (0 until array.length()).map { array.getString(it) }
                        } ?: emptyList()
                        val guidelines = jsonObject.optJSONArray("guidelines")?.let { array ->
                            (0 until array.length()).map { array.getString(it) }
                        } ?: emptyList()
                        val sectionType = jsonObject.optString("section_type", "")
                        val chunkId = jsonObject.optString("chunk_id", "")
                        
                        // Extract additional fields based on section type
                        val additionalInfo = when (sectionType) {
                            "pregnancy_care" -> {
                                val visitType = jsonObject.optString("visit_type", "")
                                val complicationType = jsonObject.optString("complication_type", "")
                                when {
                                    visitType.isNotEmpty() -> "Visit Type: $visitType"
                                    complicationType.isNotEmpty() -> "Complication: $complicationType"
                                    else -> ""
                                }
                            }
                            "delivery_care" -> {
                                val procedureType = jsonObject.optString("procedure_type", "")
                                if (procedureType.isNotEmpty()) "Procedure Type: $procedureType" else ""
                            }
                            "postpartum_care" -> {
                                val careType = jsonObject.optString("care_type", "")
                                if (careType.isNotEmpty()) "Care Type: $careType" else ""
                            }
                            "newborn_care" -> {
                                val careType = jsonObject.optString("care_type", "")
                                if (careType.isNotEmpty()) "Care Type: $careType" else ""
                            }
                            "immunization" -> {
                                val immunizationType = jsonObject.optString("immunization_type", "")
                                if (immunizationType.isNotEmpty()) "Immunization Type: $immunizationType" else ""
                            }
                            "nutrition" -> {
                                val nutritionType = jsonObject.optString("nutrition_type", "")
                                if (nutritionType.isNotEmpty()) "Nutrition Type: $nutritionType" else ""
                            }
                            "emergency_care" -> {
                                val emergencyType = jsonObject.optString("emergency_type", "")
                                if (emergencyType.isNotEmpty()) "Emergency Type: $emergencyType" else ""
                            }
                            "general_guidelines" -> {
                                val category = jsonObject.optString("category", "")
                                if (category.isNotEmpty()) "Category: $category" else ""
                            }
                            "pregnancy_scheme" -> {
                                val schemeType = jsonObject.optString("scheme_type", "")
                                if (schemeType.isNotEmpty()) "Scheme Type: $schemeType" else ""
                            }
                            "clinical_protocol" -> {
                                val protocolType = jsonObject.optString("protocol_type", "")
                                if (protocolType.isNotEmpty()) "Protocol Type: $protocolType" else ""
                            }
                            "healthcare_infrastructure" -> "Healthcare Infrastructure Information"
                            else -> ""
                        }
                        
                        mapOf(
                            "title" to title,
                            "content" to content,
                            "key_points" to keyPoints,
                            "guidelines" to guidelines,
                            "section_type" to sectionType,
                            "chunk_id" to chunkId,
                            "additional_info" to additionalInfo
                        )
                    } catch (e: Exception) {
                        Log.w("MediapipeLLMDataSource", "Failed to parse JSON chunk: ${e.message}")
                        null
                    }
                }
                
                // Build structured context text
                val contextText = structuredContext.joinToString("\n\n") { chunk ->
                    buildString {
                        appendLine("**${chunk["title"]}**")
                        appendLine("Section Type: ${chunk["section_type"]}")
                        
                        val additionalInfo = chunk["additional_info"] as? String
                        if (!additionalInfo.isNullOrEmpty()) {
                            appendLine("Additional Info: $additionalInfo")
                        }
                        
                        val keyPoints = chunk["key_points"] as? List<String>
                        if (!keyPoints.isNullOrEmpty()) {
                            appendLine("Key Points:")
                            for (point in keyPoints) {
                                appendLine("• $point")
                            }
                        }
                        
                        val guidelines = chunk["guidelines"] as? List<String>
                        if (!guidelines.isNullOrEmpty()) {
                            appendLine("Guidelines:")
                            for (guideline in guidelines) {
                                appendLine("• $guideline")
                            }
                        }
                        
                        appendLine("Content: ${chunk["content"]}")
                    }
                }
                
                val prompt = """
                    $systemPrompt
                    
                    Relevant context from ASHA guidelines (structured):
                    $contextText
                    
                    User's query: $query
                    
                    Please provide a detailed response based on the ASHA guidelines context provided above. Focus on the key points and guidelines that are most relevant to the user's query. Structure your response clearly with:
                    - Key information from the guidelines
                    - Specific procedures or steps if mentioned
                    - Important warnings or precautions
                    - Additional context from the section type and additional info
                    
                    If the context doesn't contain relevant information, say so. Provide a complete response with proper conclusion. Do not cut off mid-sentence.
                """.trimIndent()
                
                Log.d("MediapipeLLMDataSource", "Sending prompt to LLM: ${prompt.take(200)}...")
                
                llmInference?.generateResponseAsync(prompt, progressListener)
                    ?: throw IllegalStateException("LLM not initialized")
            } catch (e: Exception) {
                Log.e("MediapipeLLMDataSource", "Error in generateDetailedResponseAsync: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
    }

    /**
     * Ensures the response is complete by checking for incomplete sentences
     */
    private fun ensureCompleteResponse(response: String): String {
        if (response.isBlank()) return response
        
        val trimmedResponse = response.trim()
        
        // Check if response ends with incomplete sentence patterns
        val incompletePatterns = listOf(
            Regex(".*\\b(and|but|or|however|therefore|thus|hence|so|because|since|while|when|if|although|though)\\s*$"),
            Regex(".*\\b(is|are|was|were|will|can|could|should|would|may|might)\\s*$"),
            Regex(".*\\b(to|for|with|in|on|at|by|from|of|about|against|between|among)\\s*$"),
            Regex(".*\\b(a|an|the|this|that|these|those|some|any|each|every|all|both|either|neither)\\s*$")
        )
        
        // If response ends with incomplete pattern, add a completion
        for (pattern in incompletePatterns) {
            if (pattern.matches(trimmedResponse)) {
                return "$trimmedResponse. Please provide more specific information about your query."
            }
        }
        
        // If response doesn't end with proper punctuation, add it
        if (!trimmedResponse.endsWith(".") && !trimmedResponse.endsWith("!") && !trimmedResponse.endsWith("?")) {
            return "$trimmedResponse."
        }
        
        return trimmedResponse
    }

    suspend fun start(): String {
        if (llmInference != null) {
            return withContext(Dispatchers.IO) {
                Log.i(
                    MediapipeLLMDataSource::class.java.simpleName,
                    "Initializing ASHA Sakhi chat"
                )
                val response = llmInference!!.generateResponse("\n\nI am ready to assist with your healthcare queries. Please ask your specific question.")
                val completeResponse = ensureCompleteResponse(response)
                "$completeResponse\n\n**💡 AI Generated Response**"
            }
        }
        return "I am ASHA Sakhi! Ready to answer your queries!\n\n**💡 AI Generated Response**";
    }

    fun isMemoryReady(): Boolean {
        return isInitialized && isContentMemorized
    }
}



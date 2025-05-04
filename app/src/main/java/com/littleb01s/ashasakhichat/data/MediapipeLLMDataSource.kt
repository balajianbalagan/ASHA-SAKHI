package com.littleb01s.ashasakhichat.data

import android.content.Context
import android.util.Log
import com.google.ai.edge.localagents.rag.memory.DefaultSemanticTextMemory
import com.google.ai.edge.localagents.rag.memory.SqliteVectorStore
import com.google.ai.edge.localagents.rag.models.Embedder
import com.google.ai.edge.localagents.rag.models.GeckoEmbeddingModel
import com.google.ai.edge.localagents.rag.retrieval.RetrievalConfig
import com.google.ai.edge.localagents.rag.retrieval.RetrievalRequest
import com.google.common.collect.ImmutableList
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.littleb01s.ashasakhichat.utils.PDFReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediapipeLLMDataSource @Inject constructor(
    private var llmInference: LlmInference,
    private val context: Context
) {
    private val systemPrompt = """
        You are a healthcare assistant for ASHA workers, trained on official ASHA guidelines. Provide accurate information based on the guidelines. If unsure, say so. Always recommend consulting proper medical authorities for serious concerns. Be specific and limit responses to three paragraphs.
    """.trimIndent()

    private val embedder: Embedder<String> = GeckoEmbeddingModel(
        "/data/local/tmp/llm/Gecko_1024_quant.tflite",
        Optional.of("/data/local/tmp/llm/sentencepiece.model"),
        true
    )

    private val semanticMemory = DefaultSemanticTextMemory(
        SqliteVectorStore(768), // Gecko embedding model dimension
        embedder
    )

    fun memorizeContent(pdfPath: String) {
        try {
            val chunks = PDFReader.readPDFInChunks(pdfPath)
            if (chunks.isEmpty()) {
                Log.e("MediapipeLLMDataSource", "No content chunks extracted from PDF")
                return
            }

            // Log the paths for verification
            Log.d("MediapipeLLMDataSource", "Gecko model path: /data/local/tmp/llm/Gecko_1024_quant.tflite")
            Log.d("MediapipeLLMDataSource", "Tokenizer model path: /data/local/tmp/llm/sentencepiece.model")
            Log.d("MediapipeLLMDataSource", "PDF path: $pdfPath")
            Log.d("MediapipeLLMDataSource", "Number of chunks: ${chunks.size}")

            // Log each chunk's content
            chunks.forEachIndexed { index, chunk ->
                Log.d("MediapipeLLMDataSource", "Chunk $index: ${chunk.take(200)}...") // Log first 200 chars of each chunk
            }

            semanticMemory.recordBatchedMemoryItems(ImmutableList.copyOf(chunks))
            Log.d("MediapipeLLMDataSource", "Successfully memorized ${chunks.size} chunks from PDF")
        } catch (e: Exception) {
            Log.e("MediapipeLLMDataSource", "Error memorizing PDF content: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun generateResponse(query: String): String {
        try {
            Log.d("MediapipeLLMDataSource", "Starting response generation for query: $query")
            
            val retrievalRequest = RetrievalRequest.create(
                query,
                RetrievalConfig.create(
                    1, // topK
                    0.8f, // minSimilarityScore
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
                    val result = llmInference.generateResponse(prompt)
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

    fun resetLLMInference() {
        // Reset any necessary state
    }

    suspend fun start(): String {
        return withContext(Dispatchers.IO) {
            Log.i(
                MediapipeLLMDataSource::class.java.simpleName,
                "Initializing ASHA Sakhi chat"
            )
            llmInference.generateResponse("$systemPrompt\n\nI am ready to assist with your healthcare queries. Please ask your specific question. Limit to 300 words")
        }
    }

    suspend fun sendMessage(message: String): String {
        return withContext(Dispatchers.IO) {
            Log.i(
                MediapipeLLMDataSource::class.java.simpleName,
                "Processing user query: $message"
            )
            llmInference.generateResponse("""
                $systemPrompt
                User's query: $message
            """.trimIndent())
        }
    }
}

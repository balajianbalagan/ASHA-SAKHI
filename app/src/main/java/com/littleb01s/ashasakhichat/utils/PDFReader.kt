package com.littleb01s.ashasakhichat.utils

import android.content.Context
import android.util.Log
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import java.io.IOException
import org.json.JSONObject
import org.json.JSONArray

object PDFReader {
    private const val TAG = "PDFReader"
    private const val CHUNK_SIZE = 1000 // Characters per chunk
    private const val CHUNK_OVERLAP = 200 // Overlap between chunks to maintain context

    fun readPDFInChunks(filePath: String): List<String> {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Log.e(TAG, "PDF file does not exist at path: $filePath")
                return emptyList()
            }

            PDDocument.load(file).use { document ->
                val stripper = PDFTextStripper()
                val text = stripper.getText(document)
                return chunkText(text)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading PDF: ${e.message}")
            return emptyList()
        }
    }

    fun readPDFAsStructuredChunks(filePath: String): List<String> {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Log.e(TAG, "PDF file does not exist at path: $filePath")
                return emptyList()
            }

            PDDocument.load(file).use { document ->
                val stripper = PDFTextStripper()
                val text = stripper.getText(document)
                return extractStructuredChunks(text)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading PDF: ${e.message}")
            return emptyList()
        }
    }

    private fun chunkText(text: String): List<String> {
        val chunks = mutableListOf<String>()
        var startIndex = 0

        while (startIndex < text.length) {
            val endIndex = minOf(startIndex + CHUNK_SIZE, text.length)
            var chunk = text.substring(startIndex, endIndex)

            // Clean up the chunk
            chunk = chunk.replace("\\s+".toRegex(), " ").trim()
            
            if (chunk.isNotEmpty()) {
                chunks.add(chunk)
            }

            startIndex += (CHUNK_SIZE - CHUNK_OVERLAP)
        }

        return chunks
    }

    private fun extractStructuredChunks(text: String): List<String> {
        val structuredChunks = mutableListOf<String>()
        
        // Split text into sections based on common ASHA guideline patterns
        val sections = text.split(Regex("(?=\\d+\\.\\s*[A-Z])|(?=Chapter\\s*\\d+)|(?=Section\\s*\\d+)"))
        
        sections.forEachIndexed { index, section ->
            if (section.trim().isNotEmpty()) {
                val structuredChunk = createStructuredChunk(section.trim(), index)
                if (structuredChunk.isNotEmpty()) {
                    structuredChunks.add(structuredChunk)
                }
            }
        }
        
        // If no structured sections found, fall back to regular chunking
        if (structuredChunks.isEmpty()) {
            return chunkText(text)
        }
        
        return structuredChunks
    }

    private fun createStructuredChunk(section: String, index: Int): String {
        try {
            val jsonObject = JSONObject()
            
            // Extract title/heading
            val titleMatch = Regex("^(\\d+\\.\\s*[A-Z][^\\n]*|Chapter\\s*\\d+[^\\n]*|Section\\s*\\d+[^\\n]*)").find(section)
            val title = titleMatch?.value?.trim() ?: "Section ${index + 1}"
            
            // Extract key points and guidelines
            val keyPoints = extractKeyPoints(section)
            val guidelines = extractGuidelines(section)
            val procedures = extractProcedures(section)
            val warnings = extractWarnings(section)
            
            jsonObject.put("title", title)
            jsonObject.put("content", section.trim())
            jsonObject.put("key_points", JSONArray(keyPoints))
            jsonObject.put("guidelines", JSONArray(guidelines))
            jsonObject.put("procedures", JSONArray(procedures))
            jsonObject.put("warnings", JSONArray(warnings))
            jsonObject.put("section_type", determineSectionType(title))
            jsonObject.put("chunk_id", index)
            
            return jsonObject.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating structured chunk: ${e.message}")
            // Fallback to simple JSON with just content
            return JSONObject().apply {
                put("title", "Section ${index + 1}")
                put("content", section.trim())
                put("chunk_id", index)
            }.toString()
        }
    }

    private fun extractKeyPoints(section: String): List<String> {
        val keyPoints = mutableListOf<String>()
        val lines = section.split("\n")
        
        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.matches(Regex("^[•\\-*]\\s*.+")) || 
                trimmed.matches(Regex("^\\d+\\.\\s*.+")) ||
                trimmed.contains("important", ignoreCase = true) ||
                trimmed.contains("key", ignoreCase = true)) {
                keyPoints.add(trimmed)
            }
        }
        
        return keyPoints
    }

    private fun extractGuidelines(section: String): List<String> {
        val guidelines = mutableListOf<String>()
        val lines = section.split("\n")
        
        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.contains("guideline", ignoreCase = true) ||
                trimmed.contains("should", ignoreCase = true) ||
                trimmed.contains("must", ignoreCase = true) ||
                trimmed.contains("recommended", ignoreCase = true)) {
                guidelines.add(trimmed)
            }
        }
        
        return guidelines
    }

    private fun extractProcedures(section: String): List<String> {
        val procedures = mutableListOf<String>()
        val lines = section.split("\n")
        
        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.contains("procedure", ignoreCase = true) ||
                trimmed.contains("step", ignoreCase = true) ||
                trimmed.contains("process", ignoreCase = true) ||
                trimmed.matches(Regex("^\\d+\\.\\s*[A-Z].+"))) {
                procedures.add(trimmed)
            }
        }
        
        return procedures
    }

    private fun extractWarnings(section: String): List<String> {
        val warnings = mutableListOf<String>()
        val lines = section.split("\n")
        
        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.contains("warning", ignoreCase = true) ||
                trimmed.contains("caution", ignoreCase = true) ||
                trimmed.contains("danger", ignoreCase = true) ||
                trimmed.contains("emergency", ignoreCase = true)) {
                warnings.add(trimmed)
            }
        }
        
        return warnings
    }

    private fun determineSectionType(title: String): String {
        return when {
            title.contains("pregnancy", ignoreCase = true) -> "pregnancy_care"
            title.contains("delivery", ignoreCase = true) -> "delivery_care"
            title.contains("postpartum", ignoreCase = true) -> "postpartum_care"
            title.contains("newborn", ignoreCase = true) -> "newborn_care"
            title.contains("immunization", ignoreCase = true) -> "immunization"
            title.contains("nutrition", ignoreCase = true) -> "nutrition"
            title.contains("hygiene", ignoreCase = true) -> "hygiene"
            title.contains("emergency", ignoreCase = true) -> "emergency_care"
            else -> "general_guidelines"
        }
    }
} 
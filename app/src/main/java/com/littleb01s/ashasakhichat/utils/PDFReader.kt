package com.littleb01s.ashasakhichat.utils

import android.content.Context
import android.util.Log
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import java.io.IOException

object PDFReader {
    private const val TAG = "PDFReader"
    private const val CHUNK_SIZE = 500 // Characters per chunk
    private const val CHUNK_OVERLAP = 100 // Overlap between chunks to maintain context

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
} 
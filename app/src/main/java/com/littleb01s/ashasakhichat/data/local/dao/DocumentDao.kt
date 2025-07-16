package com.littleb01s.ashasakhichat.data.local.dao

import androidx.room.*
import com.littleb01s.ashasakhichat.data.local.entity.Document
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM TBL_DOCUMENTS")
    fun getAllDocuments(): Flow<List<Document>>

    @Query("SELECT * FROM TBL_DOCUMENTS WHERE documentId = :id")
    suspend fun getDocumentById(id: Int): Document?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document): Long

    @Update
    suspend fun updateDocument(document: Document)

    @Delete
    suspend fun deleteDocument(document: Document)

    @Query("SELECT * FROM TBL_DOCUMENTS WHERE checkupId = :checkupId")
    fun getDocumentsForCheckup(checkupId: Int): Flow<List<Document>>

    // Sync-related queries
    @Query("SELECT * FROM TBL_DOCUMENTS WHERE needsUpload = 1")
    fun getDocumentsToUpload(): Flow<List<Document>>

    @Query("SELECT * FROM TBL_DOCUMENTS WHERE needsDownload = 1")
    fun getDocumentsToDownload(): Flow<List<Document>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<Document>)

    @Query("DELETE FROM TBL_DOCUMENTS")
    suspend fun clearAllDocuments()
} 
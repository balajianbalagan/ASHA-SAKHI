package com.littleb01s.ashasakhichat.data.repository

import com.littleb01s.ashasakhichat.data.local.dao.SchemeDao
import com.littleb01s.ashasakhichat.data.local.entity.Scheme
import com.littleb01s.ashasakhichat.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SchemeRepository @Inject constructor(
    private val schemeDao: SchemeDao
) {
    // Local database operations
    fun getAllSchemes(): Flow<List<Scheme>> = schemeDao.getAllSchemes()

    fun getSchemeById(schemeId: Int): Flow<Scheme?> = schemeDao.getSchemeById(schemeId)

    fun getSchemesByPatientId(patientId: Int): Flow<List<Scheme>> = schemeDao.getSchemesByPatientId(patientId)

    fun getSchemesByState(state: String): Flow<List<Scheme>> = schemeDao.getSchemesByState(state)

    fun searchSchemes(query: String): Flow<List<Scheme>> = schemeDao.searchSchemes(query)

    // CRUD operations
    suspend fun insertScheme(scheme: Scheme): Long {
        return try {
            schemeDao.insertScheme(scheme)
        } catch (e: Exception) {
            throw Exception("Failed to insert scheme: ${e.message}")
        }
    }

    suspend fun updateScheme(scheme: Scheme) {
        try {
            schemeDao.updateScheme(scheme)
        } catch (e: Exception) {
            throw Exception("Failed to update scheme: ${e.message}")
        }
    }

    suspend fun deleteScheme(scheme: Scheme) {
        try {
            schemeDao.deleteScheme(scheme)
        } catch (e: Exception) {
            throw Exception("Failed to delete scheme: ${e.message}")
        }
    }

    // Batch operations
    suspend fun insertSchemes(schemes: List<Scheme>) {
        try {
            schemeDao.insertSchemes(schemes)
        } catch (e: Exception) {
            throw Exception("Failed to insert schemes: ${e.message}")
        }
    }

    suspend fun deleteSchemesByPatientId(patientId: Int) {
        try {
            schemeDao.deleteSchemesByPatientId(patientId)
        } catch (e: Exception) {
            throw Exception("Failed to delete schemes for patient: ${e.message}")
        }
    }

    // Sync operations
    fun getSchemesToUpload(): Flow<List<Scheme>> = schemeDao.getSchemesToUpload()

    fun getSchemesToDownload(): Flow<List<Scheme>> = schemeDao.getSchemesToDownload()

    suspend fun markSchemeAsUploaded(schemeId: Int, serverId: Int, timestamp: Date) {
        try {
            schemeDao.markSchemeAsUploaded(schemeId, serverId, timestamp)
        } catch (e: Exception) {
            throw Exception("Failed to mark scheme as uploaded: ${e.message}")
        }
    }

    suspend fun markSchemesAsUploaded(schemeIds: List<Int>, timestamp: Date) {
        try {
            schemeDao.markSchemesAsUploaded(schemeIds, timestamp)
        } catch (e: Exception) {
            throw Exception("Failed to mark schemes as uploaded: ${e.message}")
        }
    }

    suspend fun markSchemeAsDownloaded(schemeId: Int, timestamp: Date) {
        try {
            schemeDao.markSchemeAsDownloaded(schemeId, timestamp)
        } catch (e: Exception) {
            throw Exception("Failed to mark scheme as downloaded: ${e.message}")
        }
    }

    suspend fun markSchemesForDownload(serverIds: List<Int>) {
        try {
            schemeDao.markSchemesForDownload(serverIds)
        } catch (e: Exception) {
            throw Exception("Failed to mark schemes for download: ${e.message}")
        }
    }

    // Sync status queries
    fun getPendingUploadCount(): Flow<Int> = schemeDao.getPendingUploadCount()

    fun getPendingDownloadCount(): Flow<Int> = schemeDao.getPendingDownloadCount()

    // Server ID operations
    suspend fun getSchemeByServerId(serverId: Int): Scheme? {
        return try {
            schemeDao.getSchemeByServerId(serverId)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateServerId(localId: Int, serverId: Int) {
        try {
            schemeDao.updateServerId(localId, serverId)
        } catch (e: Exception) {
            throw Exception("Failed to update server ID: ${e.message}")
        }
    }

    // Conflict detection
    fun getSchemesWithPossibleConflicts(): Flow<List<Scheme>> = schemeDao.getSchemesWithPossibleConflicts()

    // Utility operations
    suspend fun clearAllSchemes() {
        try {
            schemeDao.clearAllSchemes()
        } catch (e: Exception) {
            throw Exception("Failed to clear all schemes: ${e.message}")
        }
    }

    // Create a new scheme with proper sync flags
    suspend fun createScheme(
        patientId: Int,
        schemeName: String,
        state: String,
        description: String,
        eligibility: String,
        howToApply: String
    ): Long {
        val scheme = Scheme(
            patientId = patientId,
            schemeName = schemeName,
            state = state,
            description = description,
            eligibility = eligibility,
            howToApply = howToApply,
            needsUpload = true,
            needsDownload = false,
            createdAt = Date(),
            updatedAt = Date(),
            localVersion = 1
        )
        return insertScheme(scheme)
    }

    // Update scheme with sync tracking
    suspend fun updateSchemeWithSync(
        schemeId: Int,
        schemeName: String? = null,
        state: String? = null,
        description: String? = null,
        eligibility: String? = null,
        howToApply: String? = null
    ) {
        val currentScheme = schemeDao.getSchemeById(schemeId).first()
        currentScheme?.let { scheme ->
            val updatedScheme = scheme.copy(
                schemeName = schemeName ?: scheme.schemeName,
                state = state ?: scheme.state,
                description = description ?: scheme.description,
                eligibility = eligibility ?: scheme.eligibility,
                howToApply = howToApply ?: scheme.howToApply,
                needsUpload = true,
                updatedAt = Date(),
                localVersion = scheme.localVersion + 1
            )
            updateScheme(updatedScheme)
        }
    }
} 
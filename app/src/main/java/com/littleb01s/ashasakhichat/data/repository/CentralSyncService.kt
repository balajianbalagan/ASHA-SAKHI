package com.littleb01s.ashasakhichat.data.repository

import android.util.Log
import com.littleb01s.ashasakhichat.data.local.PreferencesManager
import com.littleb01s.ashasakhichat.data.local.dao.AppointmentDao
import com.littleb01s.ashasakhichat.data.local.dao.PatientDao
import com.littleb01s.ashasakhichat.data.local.dao.SyncTimestampDao
import com.littleb01s.ashasakhichat.data.local.entity.Appointment
import com.littleb01s.ashasakhichat.data.local.entity.SyncTimestamp
import com.littleb01s.ashasakhichat.data.api.AppointmentApi
import com.littleb01s.ashasakhichat.data.api.AppointmentResponse
import com.littleb01s.ashasakhichat.data.repository.PatientRepository
import com.littleb01s.ashasakhichat.data.repository.CreateAppointmentRequest
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CentralSyncService @Inject constructor(
    private val appointmentApi: AppointmentApi,
    private val appointmentDao: AppointmentDao,
    private val patientDao: PatientDao,
    private val syncTimestampDao: SyncTimestampDao,
    private val preferencesManager: PreferencesManager,
    private val patientRepository: PatientRepository
) {
    
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("Asia/Kolkata")
    }

    companion object {
        private const val TAG = "CentralSyncService"
        private const val ENTITY_APPOINTMENTS = "appointments"
        private const val ENTITY_PATIENTS = "patients"
    }

    /**
     * Central sync function that syncs all entities
     */
    suspend fun performFullSync() {
        Log.d(TAG, "Starting full sync")
        try {
            // Step 1: Sync patients with incremental support
            syncPatients()
            
            // Step 2: Sync appointments with incremental support
            syncAppointments()
            
            // Step 3: Sync checkups (when backend is ready)
            // syncCheckups()
            
            Log.d(TAG, "Full sync completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Full sync failed", e)
            throw e
        }
    }

    /**
     * Sync patients with incremental updates
     */
    suspend fun syncPatients() {
        Log.d(TAG, "Starting patient sync")
        
        try {
            // Get last sync time for patients
            val lastSyncTime = syncTimestampDao.getLastSyncTime(ENTITY_PATIENTS)
            val updatedAtParam = lastSyncTime?.lastSyncTime?.let { formatDateForApi(it) }
            Log.d(TAG, "Updated at param: $updatedAtParam")
            // Use PatientRepository's syncPatients with incremental support
            patientRepository.syncPatients(updatedAtParam)
            
            // Update sync timestamp
            updateSyncTimestamp(ENTITY_PATIENTS)
            
            Log.d(TAG, "Patient sync completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Patient sync failed", e)
            throw e
        }
    }

    /**
     * Sync appointments with incremental updates
     */
    suspend fun syncAppointments() {
        Log.d(TAG, "Starting appointment sync")
        
        val workerId = preferencesManager.getWorkerId()?.toString() 
            ?: throw IllegalStateException("Worker ID not found")
        
        try {
            // 1. Upload local appointments that need sync
            uploadLocalAppointments(workerId)
            
            // 2. Download appointments from server (incremental)
            downloadAppointmentsFromServer(workerId)
            
            // 3. Update sync timestamp
            updateSyncTimestamp(ENTITY_APPOINTMENTS)
            
            Log.d(TAG, "Appointment sync completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Appointment sync failed", e)
            throw e
        }
    }

    /**
     * Upload local appointments that need sync
     */
    private suspend fun uploadLocalAppointments(workerId: String) {
        Log.d(TAG, "Uploading local appointments")
        
        val appointmentsToUpload = appointmentDao.getAppointmentsToUpload().first()
        Log.d(TAG, "Appointments to upload: ${appointmentsToUpload.size}")

        appointmentsToUpload.forEach { localAppointment ->
            try {
                Log.d(TAG, "Uploading appointment: ${localAppointment.appointmentId}")
                
                val request = CreateAppointmentRequest(
                    workerId = localAppointment.workerId,
                    patientId = localAppointment.patientId,
                    appointmentDate = formatDateForApi(localAppointment.appointmentDate),
                    appointmentStatus = localAppointment.appointmentStatus,
                    appointmentType = localAppointment.appointmentType,
                    appointmentName = localAppointment.appointmentName,
                    appointmentDescription = localAppointment.appointmentDescription,
                    appointmentPriority = localAppointment.appointmentPriority
                )
                
                val response = appointmentApi.createAppointment(request)
                
                if (response.data?.appointment != null) {
                    // Update local appointment with server data
                    val updatedAppointment = localAppointment.copy(
                        needsUpload = false,
                        serverId = response.data.appointment.appointmentId,
                        lastUploadedAt = Date()
                    )
                    appointmentDao.updateAppointment(updatedAppointment)
                    Log.d(TAG, "Successfully uploaded appointment: ${localAppointment.appointmentId}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload appointment ${localAppointment.appointmentId}", e)
                // Don't throw here, continue with other appointments
            }
        }
    }

    /**
     * Download appointments from server with incremental updates
     */
    private suspend fun downloadAppointmentsFromServer(workerId: String) {
        Log.d(TAG, "Downloading appointments from server")
        
        // Get last sync time for appointments
        val lastSyncTime = syncTimestampDao.getLastSyncTime(ENTITY_APPOINTMENTS)
        val updatedAtParam = lastSyncTime?.lastSyncTime?.let { formatDateForApi(it) }
        
        try {
            val response = appointmentApi.fetchAppointments(workerId, updatedAtParam)
            
            // Add null safety check
            if (response.data == null) {
                Log.w(TAG, "Server returned null data for appointments")
                return
            }
            
            response.data.forEach { serverAppointment ->
                // Check if we already have this appointment locally
                val existingAppointment = appointmentDao.getAppointmentByServerId(serverAppointment.appointmentId)
                
                if (existingAppointment != null) {
                    // Merge server data with existing local data, preserving local fields if server doesn't have them
                    val mergedAppointment = existingAppointment.copy(
                        appointmentDate = serverAppointment.appointmentDate,
                        appointmentStatus = serverAppointment.appointmentStatus,
                        appointmentType = serverAppointment.appointmentType ?: existingAppointment.appointmentType,
                        appointmentName = serverAppointment.appointmentName ?: existingAppointment.appointmentName,
                        appointmentDescription = serverAppointment.appointmentDescription ?: existingAppointment.appointmentDescription,
                        appointmentPriority = serverAppointment.appointmentPriority ?: existingAppointment.appointmentPriority,
                        needsUpload = false, // Successfully synced
                        lastDownloadedAt = Date(),
                        serverId = serverAppointment.appointmentId
                    )
                    appointmentDao.updateAppointment(mergedAppointment)
                    Log.d(TAG, "Updated existing appointment: ${serverAppointment.appointmentId}")
                } else {
                    // New appointment from server
                    val localAppointment = Appointment(
                        appointmentId = 0, // Will be auto-generated
                        workerId = workerId.toInt(),
                        patientId = serverAppointment.patientId,
                        appointmentDate = serverAppointment.appointmentDate,
                        appointmentStatus = serverAppointment.appointmentStatus,
                        needsUpload = false,
                        needsDownload = false,
                        lastDownloadedAt = Date(),
                        appointmentType = serverAppointment.appointmentType ?: "Regular",
                        appointmentName = serverAppointment.appointmentName,
                        appointmentDescription = serverAppointment.appointmentDescription,
                        appointmentPriority = serverAppointment.appointmentPriority,
                        serverId = serverAppointment.appointmentId
                    )
                    appointmentDao.insertAppointment(localAppointment)
                    Log.d(TAG, "Inserted new appointment from server: ${serverAppointment.appointmentId}")
                }
            }
            
            Log.d(TAG, "Successfully downloaded ${response.data.size} appointments from server")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download appointments from server", e)
            throw e
        }
    }

    /**
     * Update sync timestamp for an entity
     */
    private suspend fun updateSyncTimestamp(entityType: String) {
        val now = Date()
        val syncTimestamp = SyncTimestamp(
            entityType = entityType,
            lastSyncTime = now
        )
        syncTimestampDao.insertSyncTimestamp(syncTimestamp)
        Log.d(TAG, "Updated sync timestamp for $entityType: $now")
    }

    /**
     * Get last sync time for an entity
     */
    suspend fun getLastSyncTime(entityType: String): Date? {
        return syncTimestampDao.getLastSyncTime(entityType)?.lastSyncTime
    }

    /**
     * Get sync status for all entities
     */
    suspend fun getSyncStatus(): Map<String, Date?> {
        val timestamps = syncTimestampDao.getAllSyncTimestamps()
        return timestamps.associate { it.entityType to it.lastSyncTime }
    }

    /**
     * Check if an entity needs sync (no timestamp or old timestamp)
     */
    suspend fun needsSync(entityType: String, maxAgeMinutes: Long = 30): Boolean {
        val lastSync = getLastSyncTime(entityType)
        return if (lastSync == null) {
            true // Never synced
        } else {
            val ageInMinutes = (Date().time - lastSync.time) / (1000 * 60)
            ageInMinutes > maxAgeMinutes
        }
    }

    /**
     * Format date for API calls
     */
    private fun formatDateForApi(date: Date): String {
        return apiDateFormat.format(date)
    }

    /**
     * Parse ISO date string
     */
    private fun parseIsoDate(dateString: String?): Date? {
        return try {
            dateString?.let { isoDateFormat.parse(it) }
        } catch (e: Exception) {
            null
        }
    }
}

package com.littleb01s.ashasakhichat.data.repository

import android.util.Log
import com.littleb01s.ashasakhichat.data.local.PreferencesManager
import com.littleb01s.ashasakhichat.data.local.dao.AppointmentDao
import com.littleb01s.ashasakhichat.data.local.entity.Appointment
import com.littleb01s.ashasakhichat.data.api.AppointmentListResponse
import com.littleb01s.ashasakhichat.data.api.AppointmentResponse
import com.littleb01s.ashasakhichat.data.api.AppointmentListResponseWrapper
import com.littleb01s.ashasakhichat.data.api.AppointmentApi
import com.littleb01s.ashasakhichat.data.api.*
import com.littleb01s.ashasakhichat.data.local.entity.AppointmentOfflineFlags
import com.littleb01s.ashasakhichat.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

// API request object for creating appointments
data class CreateAppointmentRequest(
    val workerId: Int,
    val patientId: Int,
    val appointmentDate: String, // Formatted date string
    val appointmentStatus: String,
    val appointmentType: String?,
    val appointmentName: String?,
    val appointmentDescription: String?,
    val appointmentPriority: Int?
)

interface AppointmentRepository {
    suspend fun createAppointment(appointment: Appointment): Flow<Resource<AppointmentResponse>>
    suspend fun fetchAppointments(): Flow<Resource<AppointmentListResponse>>
    suspend fun getAppointmentById(appointmentId: Int): Appointment?
    
    // New appointment action functions
    suspend fun sendReminder(appointmentId: Int): Flow<Resource<Boolean>>
    suspend fun fetchCheckupsForAppointment(appointmentId: Int): Flow<Resource<List<com.littleb01s.ashasakhichat.data.local.entity.Checkup>>>
    suspend fun saveOrUpdateAppointment(appointment: Appointment, checkupIds: List<Int>): Flow<Resource<Int>>
}

@Singleton
class AppointmentRepositoryImpl @Inject constructor(
    private val api: AppointmentApi,
    private val preferencesManager: PreferencesManager,
    private val appointmentDao: AppointmentDao
) : AppointmentRepository {
    
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("Asia/Kolkata")
    }

    private fun parseIsoDate(dateString: String?): Date? {
        return try {
            dateString?.let { isoDateFormat.parse(it) }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun formatDateForApi(date: Date): String {
        return apiDateFormat.format(date)
    }

    override suspend fun createAppointment(appointment: Appointment): Flow<Resource<AppointmentResponse>> = flow {
        try {
            emit(Resource.Loading())
            val workerId = preferencesManager.getWorkerId()?.toString() 
                ?: throw IllegalStateException("Worker ID not found")
            
            // First try to save to server
            try {
                Log.d("AppointmentRepository", "Starting server save attempt")
                // Create API request with formatted date
                val request = CreateAppointmentRequest(
                    workerId = appointment.workerId,
                    patientId = appointment.patientId,
                    appointmentDate = formatDateForApi(appointment.appointmentDate),
                    appointmentStatus = appointment.appointmentStatus,
                    appointmentType = appointment.appointmentType,
                    appointmentName = appointment.appointmentName,
                    appointmentDescription = appointment.appointmentDescription,
                    appointmentPriority = appointment.appointmentPriority
                )
                Log.d("AppointmentRepository", "Making API call with request: $request")
                val responseWrapper = api.createAppointment(request)
                Log.d("AppointmentRepository", "API call successful, response wrapper: $responseWrapper")
                
                val response = responseWrapper.data
                Log.d("AppointmentRepository", "Extracted response: $response")
                
                // If successful, save to local database
                response.appointment?.let { appointmentData ->
                    val localAppointment = Appointment(
                        appointmentId = appointment.appointmentId, // Will be auto-generated
                        workerId = workerId.toInt(),
                        patientId = appointmentData.patientId,
                        appointmentDate = appointmentData.appointmentDate,
                        appointmentStatus = appointmentData.appointmentStatus,
                        needsUpload = false, // Successfully synced to server
                        needsDownload = false,
                        lastDownloadedAt = Date(),
                        lastUploadedAt = Date(), // Mark as uploaded
                        appointmentType = appointmentData.appointmentType ?: appointment.appointmentType ?: "Regular",
                        appointmentName = appointment.appointmentName,
                        appointmentDescription = appointment.appointmentDescription,
                        appointmentPriority = appointment.appointmentPriority,
                        serverId = appointmentData.appointmentId
                    )
                    Log.d("AppointmentRepository", "Appointment saved to server: $localAppointment")
                    val insertedId = appointmentDao.insertAppointment(localAppointment)
                    Log.d("AppointmentRepository", "Appointment saved to local database with ID: $insertedId")
                } ?: run {
                    // If no appointment data in response, still save locally with sync pending
                    val localAppointment = Appointment(
                        appointmentId = appointment.appointmentId,
                        workerId = workerId.toInt(),
                        patientId = appointment.patientId,
                        appointmentDate = appointment.appointmentDate,
                        appointmentStatus = appointment.appointmentStatus,
                        needsUpload = true, // Still needs upload since response was empty
                        needsDownload = false,
                        lastDownloadedAt = null,
                        appointmentType = appointment.appointmentType ?: "Regular",
                        appointmentName = appointment.appointmentName,
                        appointmentDescription = appointment.appointmentDescription,
                        appointmentPriority = appointment.appointmentPriority,
                        serverId = null
                    )
                    val insertedId = appointmentDao.insertAppointment(localAppointment)
                    Log.d("AppointmentRepository", "Appointment saved locally (no server data): $insertedId")
                }
                emit(Resource.Success(response))
            } catch (e: Exception) {
                Log.d("AppointmentRepository", "Server save failed, falling back to local: ${e.message}")
                // If server save fails, save locally and mark for sync
                val localAppointment = Appointment(
                    appointmentId = 0, // Will be auto-generated
                    workerId = workerId.toInt(),
                    patientId = appointment.patientId,
                    appointmentDate = appointment.appointmentDate,
                    appointmentStatus = appointment.appointmentStatus,
                    needsUpload = true,
                    needsDownload = false,
                    lastDownloadedAt = null,
                    appointmentType = appointment.appointmentType ?: "Regular",
                    appointmentName = appointment.appointmentName,
                    appointmentDescription = appointment.appointmentDescription,
                    appointmentPriority = appointment.appointmentPriority,
                    serverId = null
                )
                val insertedId = appointmentDao.insertAppointment(localAppointment)
                Log.d("AppointmentRepository", "Appointment saved locally only with ID: $insertedId")
                emit(Resource.Error("Appointment saved locally. Will sync when online."))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unexpected error occurred"))
        }
    }

    override suspend fun fetchAppointments(): Flow<Resource<AppointmentListResponse>> = flow {
        try {
            emit(Resource.Loading())
            val workerId = preferencesManager.getWorkerId()?.toString() 
                ?: throw IllegalStateException("Worker ID not found")
            
            // Only fetch from local database - server sync is handled by CentralSyncService
            val localAppointments = appointmentDao.getAllAppointments().first()
            val filteredAppointments = localAppointments.filter { it.workerId == workerId.toInt() }
            val response = AppointmentListResponse(
                appointments = filteredAppointments.map { appointment ->
                    Appointment(
                        appointmentId = appointment.serverId ?: 0,
                        workerId = appointment.workerId,
                        patientId = appointment.patientId,
                        appointmentDate = appointment.appointmentDate,
                        appointmentType = appointment.appointmentType,
                        appointmentStatus = appointment.appointmentStatus,
                        appointmentName = appointment.appointmentName,
                        appointmentDescription = appointment.appointmentDescription,
                        appointmentPriority = appointment.appointmentPriority,
                        needsUpload = appointment.needsUpload,
                        needsDownload = appointment.needsDownload,
                        lastDownloadedAt = appointment.lastDownloadedAt,
                        serverId = appointment.serverId
                    )
                }
            )
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unexpected error occurred"))
        }
    }

    // Local database operations
    fun getAppointmentsForPatient(patientId: Int): Flow<List<Appointment>> {
        return appointmentDao.getAppointmentsForPatient(patientId).map { localAppointments ->
            localAppointments.map { appointment ->
                Appointment(
                    appointmentId = appointment.serverId ?:appointment.appointmentId,
                    workerId = appointment.workerId,
                    patientId = appointment.patientId,
                    appointmentDate = appointment.appointmentDate,
                    appointmentType = appointment.appointmentType,
                    appointmentStatus = appointment.appointmentStatus,
                    appointmentName = appointment.appointmentName,
                    appointmentDescription = appointment.appointmentDescription,
                    appointmentPriority = appointment.appointmentPriority,
                    needsUpload = appointment.needsUpload,
                    needsDownload = appointment.needsDownload,
                    lastDownloadedAt = appointment.lastDownloadedAt,
                    serverId = appointment.serverId
                )
            }
        }
    }

    // Get single appointment by ID
    override suspend fun getAppointmentById(appointmentId: Int): Appointment? {
        return try {
            val localAppointment = appointmentDao.getAppointmentById(appointmentId)
            localAppointment?.let { appointment ->
                Appointment(
                    appointmentId = appointment.serverId ?: appointment.appointmentId,
                    workerId = appointment.workerId,
                    patientId = appointment.patientId,
                    appointmentDate = appointment.appointmentDate,
                    appointmentType = appointment.appointmentType,
                    appointmentStatus = appointment.appointmentStatus,
                    appointmentName = appointment.appointmentName,
                    appointmentDescription = appointment.appointmentDescription,
                    appointmentPriority = appointment.appointmentPriority,
                    needsUpload = appointment.needsUpload,
                    needsDownload = appointment.needsDownload,
                    lastDownloadedAt = appointment.lastDownloadedAt,
                    serverId = appointment.serverId
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    // New appointment action functions - we'll implement these one by one
    override suspend fun sendReminder(appointmentId: Int): Flow<Resource<Boolean>> = flow {
        // TODO: Implement send reminder functionality
        emit(Resource.Error("Not implemented yet"))
    }

    override suspend fun fetchCheckupsForAppointment(appointmentId: Int): Flow<Resource<List<com.littleb01s.ashasakhichat.data.local.entity.Checkup>>> = flow {
        // TODO: Implement fetch checkups functionality
        emit(Resource.Error("Not implemented yet"))
    }

    override suspend fun saveOrUpdateAppointment(appointment: Appointment, checkupIds: List<Int>): Flow<Resource<Int>> = flow {
        // TODO: Implement save/update appointment functionality
        emit(Resource.Error("Not implemented yet"))
    }
    
    // Cancel appointment with offline-first approach using save-appointment endpoint
    suspend fun cancelAppointment(appointmentId: Int): Resource<SaveAppointmentResponse> {
        return try {
            // Get the appointment from local database
            val appointment = appointmentDao.getAppointmentById(appointmentId)
            if (appointment == null) {
                return Resource.Error("Appointment not found")
            }
            
            // Create status update request with only the changing field
            val statusUpdateRequest = AppointmentStatusUpdateRequest(
                appointmentData = AppointmentUpdateData(
                    appointmentId = appointment.serverId ?: 0, // Required - server ID
                    appointmentStatus = "Cancelled" // Only the changing value
                    )
            )
            
            // Try to cancel via API using save-appointment endpoint
            val response = api.saveOrUpdateAppointment(statusUpdateRequest)
            
            if (response.isSuccessful) {
                // API call successful, update local database
                response.body()?.let { saveResponse ->
                    appointmentDao.updateAppointment(appointment.copy(
                        appointmentStatus = "Cancelled",
                        needsUpload = false,
                        offlineChangeFlags = null, // Clear flags since sync was successful
                        updatedAt = Date()
                    ))
                }
                Resource.Success(response.body()!!)
            } else {
                // API call failed, save locally with offline flag
                saveCancellationLocally(appointmentId)
                Resource.Error("Failed to cancel appointment: ${response.message()}")
            }
        } catch (e: Exception) {
            // Network error, save locally with offline flag
            saveCancellationLocally(appointmentId)
            Resource.Error("Network error: ${e.message}")
        }
    }
    
    private suspend fun saveCancellationLocally(appointmentId: Int) {
        val appointment = appointmentDao.getAppointmentById(appointmentId)
        appointment?.let {
            val currentFlags = it.offlineChangeFlags?.toMutableList() ?: mutableListOf()
            if (!currentFlags.contains(AppointmentOfflineFlags.CANCELLED)) {
                currentFlags.add(AppointmentOfflineFlags.CANCELLED)
            }
            
            appointmentDao.updateAppointment(it.copy(
                appointmentStatus = "Cancelled",
                needsUpload = true,
                offlineChangeFlags = currentFlags,
                updatedAt = Date()
            ))
        }
    }
} 
package com.littleb01s.ashasakhichat.data.repository

import com.littleb01s.ashasakhichat.data.local.PreferencesManager
import com.littleb01s.ashasakhichat.data.local.dao.AppointmentDao
import com.littleb01s.ashasakhichat.data.model.Appointment
import com.littleb01s.ashasakhichat.data.model.AppointmentListResponse
import com.littleb01s.ashasakhichat.data.model.AppointmentResponse
import com.littleb01s.ashasakhichat.data.remote.AppointmentApi
import com.littleb01s.ashasakhichat.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

interface AppointmentRepository {
    suspend fun createAppointment(appointment: Appointment): Flow<Resource<AppointmentResponse>>
    suspend fun fetchAppointments(): Flow<Resource<AppointmentListResponse>>
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

    private fun parseIsoDate(dateString: String?): Date? {
        return try {
            dateString?.let { isoDateFormat.parse(it) }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createAppointment(appointment: Appointment): Flow<Resource<AppointmentResponse>> = flow {
        try {
            emit(Resource.Loading())
            val workerId = preferencesManager.getWorkerId()?.toString() 
                ?: throw IllegalStateException("Worker ID not found")
            
            // First try to save to server
            try {
                val response = api.createAppointment(appointment)
                // If successful, save to local database
                response.data?.let { appointmentData ->
                    val localAppointment = com.littleb01s.ashasakhichat.data.local.entity.Appointment(
                        appointmentId = 0, // Will be auto-generated
                        workerId = workerId.toInt(),
                        patientId = appointmentData.patientId,
                        appointmentDate = appointmentData.appointmentDate,
                        appointmentStatus = appointmentData.appointmentStatus,
                        needsUpload = false,
                        needsDownload = false,
                        lastDownloadedAt = Date(),
                        appointmentType = appointmentData.appointmentType,
                        serverId = appointmentData.appointmentId
                    )
                    appointmentDao.insertAppointment(localAppointment)
                }
                emit(Resource.Success(response))
            } catch (e: Exception) {
                // If server save fails, save locally and mark for sync
                val localAppointment = com.littleb01s.ashasakhichat.data.local.entity.Appointment(
                    appointmentId = 0, // Will be auto-generated
                    workerId = workerId.toInt(),
                    patientId = appointment.patientId,
                    appointmentDate = appointment.appointmentDate,
                    appointmentStatus = appointment.appointmentStatus,
                    needsUpload = true,
                    needsDownload = false,
                    lastDownloadedAt = null,
                    appointmentType = appointment.appointmentType,
                    serverId = null
                )
                appointmentDao.insertAppointment(localAppointment)
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
            
            // First try to fetch from server
            try {
                val response = api.fetchAppointments(workerId)
                // Cache the appointments in local database
                response.appointments.forEach { appointment ->
                    val localAppointment = com.littleb01s.ashasakhichat.data.local.entity.Appointment(
                        appointmentId = 0, // Will be auto-generated
                        workerId = workerId.toInt(),
                        patientId = appointment.patientId,
                        appointmentDate = appointment.appointmentDate,
                        appointmentStatus = appointment.appointmentStatus,
                        needsUpload = false,
                        needsDownload = false,
                        lastDownloadedAt = Date(),
                        appointmentType = appointment.appointmentType,
                        serverId = appointment.appointmentId

                    )
                    appointmentDao.insertAppointment(localAppointment)
                }
                emit(Resource.Success(response))
            } catch (e: Exception) {
                // If server fetch fails, return local data
                val localAppointments = appointmentDao.getAllAppointments()
                    .collect { appointments ->
                        val filteredAppointments = appointments.filter { it.workerId == workerId.toInt() }
                        val response = AppointmentListResponse(
                            appointments = filteredAppointments.map { appointment ->
                                Appointment(
                                    appointmentId = appointment.serverId ?: 0,
                                    workerId = appointment.workerId,
                                    patientId = appointment.patientId,
                                    appointmentDate = appointment.appointmentDate,
                                    appointmentType = appointment.appointmentType,
                                    appointmentStatus = appointment.appointmentStatus,
                                    needsUpload = appointment.needsUpload,
                                    needsDownload = appointment.needsDownload,
                                    lastDownloadedAt = appointment.lastDownloadedAt,
                                    serverId = appointment.serverId?.toString()
                                )
                            }
                        )
                        emit(Resource.Success(response))
                    }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unexpected error occurred"))
        }
    }

    // Local database operations
    fun getAppointmentsForPatient(patientId: Int): Flow<List<Appointment>> {
        return appointmentDao.getAppointmentsForPatient(patientId).map { localAppointments ->
            localAppointments.map { appointment ->
                Appointment(
                    appointmentId = appointment.serverId,
                    workerId = appointment.workerId,
                    patientId = appointment.patientId,
                    appointmentDate = appointment.appointmentDate,
                    appointmentType = appointment.appointmentType,
                    appointmentStatus = appointment.appointmentStatus,
                    needsUpload = appointment.needsUpload,
                    needsDownload = appointment.needsDownload,
                    lastDownloadedAt = appointment.lastDownloadedAt
                )
            }
        }
    }


} 
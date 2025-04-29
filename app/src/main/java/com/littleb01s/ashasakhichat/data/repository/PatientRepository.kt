package com.littleb01s.ashasakhichat.data.repository

import com.littleb01s.ashasakhichat.data.api.PatientService
import com.littleb01s.ashasakhichat.data.api.SavePatientRequest
import com.littleb01s.ashasakhichat.data.api.PatientData
import com.littleb01s.ashasakhichat.data.api.VitalsData
import com.littleb01s.ashasakhichat.data.local.dao.AppointmentDao
import com.littleb01s.ashasakhichat.data.local.dao.CheckupDao
import com.littleb01s.ashasakhichat.data.local.dao.DocumentDao
import com.littleb01s.ashasakhichat.data.local.dao.PatientDao
import com.littleb01s.ashasakhichat.data.local.entity.Patient
import com.littleb01s.ashasakhichat.data.local.entity.Checkup
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepository @Inject constructor(
    private val patientService: PatientService,
    private val patientDao: PatientDao,
    private val checkupDao: CheckupDao,
    private val appointmentDao: AppointmentDao,
    private val documentDao: DocumentDao
) {
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

    // Local database operations
    fun getAllPatients(): Flow<List<Patient>> = patientDao.getAllPatients()

    fun getPatientWithDetails(patientId: Int): Flow<PatientWithDetails> {
        // TODO: Implement this to return patient with checkups, appointments, and documents
        TODO()
    }

    // API operations with local caching
    suspend fun fetchAndCachePatients(workerId: Int?) {
        try {
            val response = if (workerId != null) {
                patientService.getPatientsByWorkerId(workerId)
            } else {
                patientService.getAllPatients()
            }

            if (response.isSuccessful) {
                response.body()?.data?.forEach { patientResponse ->
                    // Convert and save patient
                    val patient = Patient(
                        patientId = patientResponse.patientId,
                        state = patientResponse.state,
                        city = patientResponse.city,
                        languagePreference = patientResponse.languagePreference,
                        firstName = patientResponse.firstName,
                        lastName = patientResponse.lastName,
                        dateOfBirth = parseIsoDate(patientResponse.dateOfBirth) ?: Date(),
                        deliveryDate = parseIsoDate(patientResponse.deliveryDate),
                        mobileNumber = patientResponse.mobileNumber,
                        employmentStatus = patientResponse.employmentStatus,
                        religion = patientResponse.religion,
                        education = patientResponse.education,
                        caste = patientResponse.caste,
                        bloodGroup = patientResponse.bloodGroup,
                        previousIllness = patientResponse.previousIllness,
                        needsUpload = false,
                        needsDownload = false,
                        lastDownloadedAt = Date(),
                        serverId = patientResponse.patientId
                    )
                    patientDao.insertPatient(patient)

                    // Convert and save checkups
                    patientResponse.checkupData.forEach { checkupResponse ->
                        val checkup = Checkup(
                            checkupId = checkupResponse.checkupId,
                            workerId = checkupResponse.workerId,
                            patientId = checkupResponse.patientId,
                            bloodPressure = checkupResponse.bloodPressure,
                            oxygen = checkupResponse.oxygen,
                            weight = checkupResponse.weight,
                            temperature = checkupResponse.temperature,
                            sugarLevel = checkupResponse.sugarLevel,
                            bmi = checkupResponse.bmi,
                            haemoglobin = checkupResponse.haemoglobin,
                            checkupData = checkupResponse.checkupData,
                            pregnancyStage = checkupResponse.pregnancyStage,
                            checkupStatus = checkupResponse.checkupStatus,
                            needsUpload = false,
                            needsDownload = false,
                            lastDownloadedAt = Date(),
                            serverId = checkupResponse.checkupId
                        )
                        checkupDao.insertCheckup(checkup)
                    }
                }
            } else {
                throw Exception("Failed to fetch patients: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to sync: ${e.message}")
        }
    }

    // Complex operations
    suspend fun savePatientWithVitals(
        patientData: PatientData,
        vitalsData: VitalsData?,
        workerId: Int,
        patientId: Int? = null
    ) {
        val request = SavePatientRequest(
            patientId = patientId,
            workerId = workerId,
            patientData = patientData,
            vitals = vitalsData
        )

        try {
            val response = patientService.savePatient(request)
            if (response.isSuccessful) {
                // Update local database with response data
                response.body()?.data?.let { patientResponse ->
                    // Similar conversion and saving as in fetchAndCachePatients
                    // but mark as synced
                }
            }
        } catch (e: Exception) {
            // If API fails, save locally and mark for future sync
            // TODO: Implement local-only saving with sync flags
        }
    }

    // Sync operations
    suspend fun syncPatients() {
        // First, upload any local changes
        val patientsToUpload = patientDao.getPatientsToUploadImmediate()
        patientsToUpload.forEach { patient ->
            try {
                val request = SavePatientRequest(
                    patientId = patient.serverId,
                    workerId = 0, // TODO: Get actual worker ID
                    patientData = PatientData(
                        firstName = patient.firstName,
                        lastName = patient.lastName,
                        mobileNumber = patient.mobileNumber,
                        state = patient.state,
                        city = patient.city,
                        dateOfBirth = patient.dateOfBirth,
                        deliveryDate = patient.deliveryDate,
                        bloodGroup = patient.bloodGroup,
                        education = patient.education,
                        employmentStatus = patient.employmentStatus,
                        religion = patient.religion,
                        caste = patient.caste,
                        previousIllness = patient.previousIllness,
                        languagePreference = patient.languagePreference,
                        lmp = patient.lmp,
                    )
                )
                val response = patientService.savePatient(request)
                if (response.isSuccessful) {
                    // Update local patient with server ID and mark as synced
                    patientDao.updatePatient(patient.copy(
                        needsUpload = false,
                        serverId = response.body()?.data?.patientId ?: patient.serverId
                    ))
                } else {
                    throw Exception("Failed to sync patient: ${response.message()}")
                }
            } catch (e: Exception) {
                throw Exception("Failed to sync patient: ${e.message}")
            }
        }

        // Then fetch and cache all patients from server
        fetchAndCachePatients(null)
    }
}

// Data class for combined patient details
data class PatientWithDetails(
    val patient: Patient,
    val checkups: List<Checkup>,
    // Add appointments and documents when needed
) 
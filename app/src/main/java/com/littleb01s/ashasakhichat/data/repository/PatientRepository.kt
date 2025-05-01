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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
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

    fun getPatientWithDetails(patientId: Int): Flow<PatientWithDetails?> {
        val patientFlow = patientDao.getPatientById(patientId)
        val checkupsFlow = checkupDao.getCheckupsForPatient(patientId)
        
        return patientFlow.combine(checkupsFlow) { patient, checkups ->
            patient?.let {
                PatientWithDetails(
                    patient = it,
                    checkups = checkups
                )
            }
        }
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
                response.body()?.let { patientResponse ->
                    // Similar conversion and saving as in fetchAndCachePatients
                    // but mark as synced
                }
            }
        } catch (e: Exception) {
            // If API fails, save locally and mark for future sync
            // TODO: Implement local-only saving with sync flags
        }
    }

    // New function to add a new patient
    suspend fun addNewPatient(
        patientData: PatientData,
        workerId: Int
    ): Boolean {  // Return true if synced with server, false if local only
        val request = SavePatientRequest(
            patientId = null, // For new patients, we don't have an ID yet
            workerId = workerId,
            patientData = patientData,
            vitals = null
        )

        try {
            val response = patientService.savePatient(request)
            if (response.isSuccessful) {
                // Update local database with response data
                response.body()?.data?.let { patientResponse ->
                    val patient = Patient(
                        patientId = patientResponse.patientData.patientId,
                        state = patientResponse.patientData.state,
                        city = patientResponse.patientData.city,
                        languagePreference = patientResponse.patientData.languagePreference,
                        firstName = patientResponse.patientData.firstName,
                        lastName = patientResponse.patientData.lastName,
                        dateOfBirth = parseIsoDate(patientResponse.patientData.dateOfBirth) ?: Date(),
                        deliveryDate = parseIsoDate(patientResponse.patientData.deliveryDate),
                        mobileNumber = patientResponse.patientData.mobileNumber,
                        employmentStatus = patientResponse.patientData.employmentStatus,
                        religion = patientResponse.patientData.religion,
                        education = patientResponse.patientData.education,
                        caste = patientResponse.patientData.caste,
                        bloodGroup = patientResponse.patientData.bloodGroup,
                        previousIllness = patientResponse.patientData.previousIllness,
                        needsUpload = false,
                        needsDownload = false,
                        lastDownloadedAt = Date(),
                        serverId = patientResponse.patientData.patientId
                    )
                    patientDao.insertPatient(patient)
                }
                return true // Successfully synced with server
            } else {
                // Server request failed, save locally
                saveLocally(patientData)
                return false // Local save only
            }
        } catch (e: Exception) {
            // Network/server error, save locally
            saveLocally(patientData)
            return false // Local save only
        }
    }

    private suspend fun saveLocally(patientData: PatientData) {
        val patient = Patient(
            patientId = 0, // Will be auto-generated
            state = patientData.state,
            city = patientData.city,
            languagePreference = patientData.languagePreference,
            firstName = patientData.firstName,
            lastName = patientData.lastName,
            dateOfBirth = patientData.dateOfBirth,
            deliveryDate = patientData.deliveryDate,
            mobileNumber = patientData.mobileNumber,
            employmentStatus = patientData.employmentStatus,
            religion = patientData.religion,
            education = patientData.education,
            caste = patientData.caste,
            bloodGroup = patientData.bloodGroup,
            previousIllness = patientData.previousIllness,
            needsUpload = true, // Mark for future sync
            needsDownload = false,
            lastDownloadedAt = null,
            serverId = null, // Will be updated after sync
            lmp = patientData.lmp
        )
        patientDao.insertPatient(patient)
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
                        serverId = response.body()?.data?.patientData?.patientId ?: patient.serverId
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
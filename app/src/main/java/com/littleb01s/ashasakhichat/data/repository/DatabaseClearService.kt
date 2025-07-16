package com.littleb01s.ashasakhichat.data.repository

import android.util.Log
import com.littleb01s.ashasakhichat.data.local.dao.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseClearService @Inject constructor(
    private val patientDao: PatientDao,
    private val appointmentDao: AppointmentDao,
    private val checkupDao: CheckupDao,
    private val documentDao: DocumentDao,
    private val doctorDao: DoctorDao,
    private val doctorVerificationDao: DoctorVerificationDao,
    private val dietDao: DietDao,
    private val photoDao: PhotoDao,
    private val infantDao: InfantDao,
    private val riskAnalysisDao: RiskAnalysisDao,
    private val syncTimestampDao: SyncTimestampDao
) {
    
    companion object {
        private const val TAG = "DatabaseClearService"
    }

    /**
     * Clear all local database data
     */
    suspend fun clearAllData() {
        try {
            Log.d(TAG, "Starting database clear operation")
            
            // Clear all tables
            patientDao.clearAllPatients()
            appointmentDao.clearAllAppointments()
            checkupDao.clearAllCheckups()
            documentDao.clearAllDocuments()
            doctorDao.clearAllDoctors()
            doctorVerificationDao.clearAllVerifications()
            dietDao.clearAllDiets()
            photoDao.clearAllPhotos()
            infantDao.clearAllInfants()
            riskAnalysisDao.clearAllRiskAnalyses()
            syncTimestampDao.clearAllSyncTimestamps()
            
            Log.d(TAG, "Database clear operation completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Database clear operation failed", e)
            throw e
        }
    }
} 
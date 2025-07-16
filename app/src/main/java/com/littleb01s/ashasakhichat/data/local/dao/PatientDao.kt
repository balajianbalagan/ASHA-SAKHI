package com.littleb01s.ashasakhichat.data.local.dao

import androidx.room.*
import com.littleb01s.ashasakhichat.data.local.entity.Patient
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface PatientDao {
    @Query("SELECT * FROM TBL_PROFILE_PATIENT")
    fun getAllPatients(): Flow<List<Patient>>

    @Query("SELECT * FROM TBL_PROFILE_PATIENT WHERE patientId = :patientId")
    fun getPatientById(patientId: Int): Flow<Patient?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient): Long

    @Update
    suspend fun updatePatient(patient: Patient)

    @Delete
    suspend fun deletePatient(patient: Patient)

    @Query("SELECT * FROM TBL_PROFILE_PATIENT WHERE firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%'")
    fun searchPatients(query: String): Flow<List<Patient>>

    // Upload sync related queries
    @Query("SELECT * FROM TBL_PROFILE_PATIENT WHERE needsUpload = 1")
    fun getPatientsToUpload(): Flow<List<Patient>>

    @Query("SELECT * FROM TBL_PROFILE_PATIENT WHERE needsUpload = 1")
    suspend fun getPatientsToUploadImmediate(): List<Patient>

    @Query("UPDATE TBL_PROFILE_PATIENT SET needsUpload = 0, lastUploadedAt = :timestamp, serverId = :serverId WHERE patientId = :patientId")
    suspend fun markPatientAsUploaded(patientId: Int, serverId: Int, timestamp: Date)

    @Query("UPDATE TBL_PROFILE_PATIENT SET needsUpload = 0, lastUploadedAt = :timestamp WHERE patientId IN (:patientIds)")
    suspend fun markPatientsAsUploaded(patientIds: List<Int>, timestamp: Date)

    // Download sync related queries
    @Query("SELECT * FROM TBL_PROFILE_PATIENT WHERE needsDownload = 1")
    fun getPatientsToDownload(): Flow<List<Patient>>

    @Query("UPDATE TBL_PROFILE_PATIENT SET needsDownload = 0, lastDownloadedAt = :timestamp WHERE patientId = :patientId")
    suspend fun markPatientAsDownloaded(patientId: Int, timestamp: Date)

    @Query("UPDATE TBL_PROFILE_PATIENT SET needsDownload = 1 WHERE serverId IN (:serverIds)")
    suspend fun markPatientsForDownload(serverIds: List<Int>)

    // Sync status queries
    @Query("SELECT COUNT(*) FROM TBL_PROFILE_PATIENT WHERE needsUpload = 1")
    fun getPendingUploadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM TBL_PROFILE_PATIENT WHERE needsDownload = 1")
    fun getPendingDownloadCount(): Flow<Int>

    // Server ID related queries
    @Query("SELECT * FROM TBL_PROFILE_PATIENT WHERE serverId = :serverId")
    suspend fun getPatientByServerId(serverId: Int): Patient?

    @Query("UPDATE TBL_PROFILE_PATIENT SET serverId = :serverId WHERE patientId = :localId")
    suspend fun updateServerId(localId: Int, serverId: Int)

    // Batch operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatients(patients: List<Patient>)

    @Query("""
        UPDATE TBL_PROFILE_PATIENT 
        SET updatedAt = :updateTime,
            needsUpload = 1,
            needsDownload = 0
        WHERE patientId = :patientId
    """)
    suspend fun updatePatientTimestamp(patientId: Int, updateTime: Date)

    // Conflict detection
    @Query("""
        SELECT * FROM TBL_PROFILE_PATIENT 
        WHERE serverUpdatedAt > lastDownloadedAt 
        OR lastDownloadedAt IS NULL
    """)
    fun getPatientsWithPossibleConflicts(): Flow<List<Patient>>

    @Query("SELECT * FROM TBL_PROFILE_PATIENT WHERE firstName = :firstName AND (lastName = :lastName OR (:lastName IS NULL AND lastName IS NULL)) ORDER BY patientId DESC LIMIT 1")
    suspend fun getPatientByName(firstName: String, lastName: String?): Patient?

    @Query("DELETE FROM TBL_PROFILE_PATIENT")
    suspend fun clearAllPatients()
} 
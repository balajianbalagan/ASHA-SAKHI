package com.littleb01s.ashasakhichat.data.local.dao

import androidx.room.*
import com.littleb01s.ashasakhichat.data.local.entity.DoctorVerification
import kotlinx.coroutines.flow.Flow

@Dao
interface DoctorVerificationDao {
    @Query("SELECT * FROM TBL_DOCTOR_VERIFICATION")
    fun getAllVerifications(): Flow<List<DoctorVerification>>

    @Query("SELECT * FROM TBL_DOCTOR_VERIFICATION WHERE verificationId = :id")
    suspend fun getVerificationById(id: Int): DoctorVerification?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerification(verification: DoctorVerification): Long

    @Update
    suspend fun updateVerification(verification: DoctorVerification)

    @Delete
    suspend fun deleteVerification(verification: DoctorVerification)

    @Query("SELECT * FROM TBL_DOCTOR_VERIFICATION WHERE checkupId = :checkupId")
    fun getVerificationsForCheckup(checkupId: Int): Flow<List<DoctorVerification>>

    // Sync-related queries
    @Query("SELECT * FROM TBL_DOCTOR_VERIFICATION WHERE needsUpload = 1")
    fun getVerificationsToUpload(): Flow<List<DoctorVerification>>

    @Query("SELECT * FROM TBL_DOCTOR_VERIFICATION WHERE needsDownload = 1")
    fun getVerificationsToDownload(): Flow<List<DoctorVerification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerifications(verifications: List<DoctorVerification>)
} 
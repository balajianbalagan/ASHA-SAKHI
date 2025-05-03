package com.littleb01s.ashasakhichat.data.local.dao

import androidx.room.*
import com.littleb01s.ashasakhichat.data.local.entity.Doctor
import java.util.Date

@Dao
interface DoctorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(doctor: Doctor): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(doctors: List<Doctor>)

    @Update
    suspend fun update(doctor: Doctor)

    @Delete
    suspend fun delete(doctor: Doctor)

    @Query("SELECT * FROM TBL_PROFILE_DOCTOR WHERE doctorId = :doctorId")
    suspend fun getDoctorById(doctorId: Int): Doctor?

    @Query("SELECT * FROM TBL_PROFILE_DOCTOR WHERE profileId = :profileId")
    suspend fun getDoctorByProfileId(profileId: Int): Doctor?

    @Query("SELECT * FROM TBL_PROFILE_DOCTOR WHERE needsUpload = 1")
    suspend fun getDoctorsNeedingUpload(): List<Doctor>

    @Query("SELECT * FROM TBL_PROFILE_DOCTOR WHERE needsDownload = 1")
    suspend fun getDoctorsNeedingDownload(): List<Doctor>

    @Query("UPDATE TBL_PROFILE_DOCTOR SET needsUpload = 0, lastUploadedAt = :timestamp WHERE doctorId = :doctorId")
    suspend fun markAsUploaded(doctorId: Int, timestamp: Date = Date())

    @Query("UPDATE TBL_PROFILE_DOCTOR SET needsDownload = 0, lastDownloadedAt = :timestamp WHERE doctorId = :doctorId")
    suspend fun markAsDownloaded(doctorId: Int, timestamp: Date = Date())
} 
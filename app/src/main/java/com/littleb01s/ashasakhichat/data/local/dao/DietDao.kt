package com.littleb01s.ashasakhichat.data.local.dao

import androidx.room.*
import com.littleb01s.ashasakhichat.data.local.entity.Diet
import java.util.Date

@Dao
interface DietDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diet: Diet): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(diets: List<Diet>)

    @Update
    suspend fun update(diet: Diet)

    @Delete
    suspend fun delete(diet: Diet)

    @Query("SELECT * FROM TBL_PATIENT_DIET WHERE dietId = :dietId")
    suspend fun getDietById(dietId: Int): Diet?

    @Query("SELECT * FROM TBL_PATIENT_DIET WHERE patientId = :patientId")
    suspend fun getDietsByPatientId(patientId: Int): List<Diet>

    @Query("SELECT * FROM TBL_PATIENT_DIET WHERE needsUpload = 1")
    suspend fun getDietsNeedingUpload(): List<Diet>

    @Query("SELECT * FROM TBL_PATIENT_DIET WHERE needsDownload = 1")
    suspend fun getDietsNeedingDownload(): List<Diet>

    @Query("UPDATE TBL_PATIENT_DIET SET needsUpload = 0, lastUploadedAt = :timestamp WHERE dietId = :dietId")
    suspend fun markAsUploaded(dietId: Int, timestamp: Date = Date())

    @Query("UPDATE TBL_PATIENT_DIET SET needsDownload = 0, lastDownloadedAt = :timestamp WHERE dietId = :dietId")
    suspend fun markAsDownloaded(dietId: Int, timestamp: Date = Date())

    @Query("DELETE FROM TBL_PATIENT_DIET")
    suspend fun clearAllDiets()
} 
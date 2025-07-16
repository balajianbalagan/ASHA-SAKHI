package com.littleb01s.ashasakhichat.data.local.dao

import androidx.room.*
import com.littleb01s.ashasakhichat.data.local.entity.Infant
import java.util.Date

@Dao
interface InfantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(infant: Infant): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(infants: List<Infant>)

    @Update
    suspend fun update(infant: Infant)

    @Delete
    suspend fun delete(infant: Infant)

    @Query("SELECT * FROM TBL_INFANT WHERE infantId = :infantId")
    suspend fun getInfantById(infantId: Int): Infant?

    @Query("SELECT * FROM TBL_INFANT WHERE patientId = :patientId")
    suspend fun getInfantsByPatientId(patientId: Int): List<Infant>

    @Query("SELECT * FROM TBL_INFANT WHERE workerId = :workerId")
    suspend fun getInfantsByWorkerId(workerId: Int): List<Infant>

    @Query("SELECT * FROM TBL_INFANT WHERE needsUpload = 1")
    suspend fun getInfantsNeedingUpload(): List<Infant>

    @Query("SELECT * FROM TBL_INFANT WHERE needsDownload = 1")
    suspend fun getInfantsNeedingDownload(): List<Infant>

    @Query("UPDATE TBL_INFANT SET needsUpload = 0, lastUploadedAt = :timestamp WHERE infantId = :infantId")
    suspend fun markAsUploaded(infantId: Int, timestamp: Date = Date())

    @Query("UPDATE TBL_INFANT SET needsDownload = 0, lastDownloadedAt = :timestamp WHERE infantId = :infantId")
    suspend fun markAsDownloaded(infantId: Int, timestamp: Date = Date())

    @Query("DELETE FROM TBL_INFANT")
    suspend fun clearAllInfants()
} 
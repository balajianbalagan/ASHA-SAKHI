package com.littleb01s.ashasakhichat.data.local.dao

import androidx.room.*
import com.littleb01s.ashasakhichat.data.local.entity.Checkup
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface CheckupDao {
    @Query("SELECT * FROM TBL_CHECKUP")
    fun getAllCheckups(): Flow<List<Checkup>>

    @Query("SELECT * FROM TBL_CHECKUP WHERE checkupId = :checkupId")
    suspend fun getCheckupById(checkupId: Int): Checkup?

    @Query("SELECT * FROM TBL_CHECKUP WHERE patientId = :patientId ORDER BY checkupId DESC")
    fun getCheckupsForPatient(patientId: Int): Flow<List<Checkup>>

    @Query("SELECT * FROM TBL_CHECKUP WHERE patientId = :patientId AND checkupType = :checkupType ORDER BY checkupId DESC")
    fun getCheckupsForPatientByType(patientId: Int, checkupType: String): Flow<List<Checkup>>

    @Query("SELECT * FROM TBL_CHECKUP WHERE patientId = :patientId AND checkupType = :checkupType ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestCheckupByType(patientId: Int, checkupType: String): Checkup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckup(checkup: Checkup): Long

    @Update
    suspend fun updateCheckup(checkup: Checkup)

    @Delete
    suspend fun deleteCheckup(checkup: Checkup)

    // Sync-related queries
    @Query("SELECT * FROM TBL_CHECKUP WHERE needsUpload = 1")
    fun getCheckupsToUpload(): Flow<List<Checkup>>

    @Query("SELECT * FROM TBL_CHECKUP WHERE needsDownload = 1")
    fun getCheckupsToDownload(): Flow<List<Checkup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckups(checkups: List<Checkup>)

    @Query("DELETE FROM TBL_CHECKUP")
    suspend fun clearAllCheckups()
} 
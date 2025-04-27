package com.littleb01s.ashasakhichat.data.local.dao

import androidx.room.*
import com.littleb01s.ashasakhichat.data.local.entity.Patient
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM TBL_PROFILE_PATIENT")
    fun getAllPatients(): Flow<List<Patient>>

    @Query("SELECT * FROM TBL_PROFILE_PATIENT WHERE patientId = :id")
    suspend fun getPatientById(id: Int): Patient?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient): Long

    @Update
    suspend fun updatePatient(patient: Patient)

    @Delete
    suspend fun deletePatient(patient: Patient)

    @Query("SELECT * FROM TBL_PROFILE_PATIENT WHERE firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%'")
    fun searchPatients(query: String): Flow<List<Patient>>
} 
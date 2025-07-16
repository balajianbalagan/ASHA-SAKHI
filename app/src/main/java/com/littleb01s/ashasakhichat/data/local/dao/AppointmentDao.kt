package com.littleb01s.ashasakhichat.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.littleb01s.ashasakhichat.data.local.entity.Appointment
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM TBL_APPOINTMENT")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM TBL_APPOINTMENT WHERE appointmentId = :id")
    suspend fun getAppointmentById(id: Int): Appointment?

    @Query("SELECT * FROM TBL_APPOINTMENT WHERE serverId = :serverId")
    suspend fun getAppointmentByServerId(serverId: Int): Appointment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)

    @Query("SELECT * FROM TBL_APPOINTMENT WHERE patientId = :patientId")
    fun getAppointmentsForPatient(patientId: Int): Flow<List<Appointment>>

    // Sync-related queries
    @Query("SELECT * FROM TBL_APPOINTMENT WHERE needsUpload = 1")
    fun getAppointmentsToUpload(): Flow<List<Appointment>>

    @Query("SELECT * FROM TBL_APPOINTMENT WHERE needsDownload = 1")
    fun getAppointmentsToDownload(): Flow<List<Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<Appointment>)

    @Query("DELETE FROM TBL_APPOINTMENT")
    suspend fun clearAllAppointments()
    
    // Get appointments with offline changes that need to be synced
    @Query("SELECT * FROM TBL_APPOINTMENT WHERE offlineChangeFlags IS NOT NULL AND needsUpload = 1")
    fun getAppointmentsWithOfflineChanges(): Flow<List<Appointment>>
    
    // Get appointments with specific offline change flag
    @Query("SELECT * FROM TBL_APPOINTMENT WHERE offlineChangeFlags LIKE '%' || :flag || '%' AND needsUpload = 1")
    fun getAppointmentsWithOfflineFlag(flag: String): Flow<List<Appointment>>
} 
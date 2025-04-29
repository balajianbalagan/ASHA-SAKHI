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
} 
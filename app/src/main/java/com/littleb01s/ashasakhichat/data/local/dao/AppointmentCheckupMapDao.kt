package com.littleb01s.ashasakhichat.data.local.dao

import androidx.room.*
import com.littleb01s.ashasakhichat.data.local.entity.AppointmentCheckupMap
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentCheckupMapDao {
    
    @Query("SELECT * FROM TBL_APPOINTMENT_CHECKUP_MAP WHERE appointmentId = :appointmentId")
    suspend fun getCheckupsForAppointment(appointmentId: Int): List<AppointmentCheckupMap>
    
    @Query("SELECT * FROM TBL_APPOINTMENT_CHECKUP_MAP WHERE checkupId = :checkupId")
    suspend fun getAppointmentsForCheckup(checkupId: Int): List<AppointmentCheckupMap>
    
    @Query("SELECT * FROM TBL_APPOINTMENT_CHECKUP_MAP WHERE needsUpload = 1")
    suspend fun getPendingUploads(): List<AppointmentCheckupMap>
    
    @Query("SELECT * FROM TBL_APPOINTMENT_CHECKUP_MAP WHERE needsDownload = 1")
    suspend fun getPendingDownloads(): List<AppointmentCheckupMap>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mapping: AppointmentCheckupMap)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mappings: List<AppointmentCheckupMap>)
    
    @Update
    suspend fun update(mapping: AppointmentCheckupMap)
    
    @Delete
    suspend fun delete(mapping: AppointmentCheckupMap)
    
    @Query("DELETE FROM TBL_APPOINTMENT_CHECKUP_MAP WHERE appointmentId = :appointmentId")
    suspend fun deleteByAppointmentId(appointmentId: Int)
    
    @Query("DELETE FROM TBL_APPOINTMENT_CHECKUP_MAP WHERE checkupId = :checkupId")
    suspend fun deleteByCheckupId(checkupId: Int)
    
    @Query("SELECT COUNT(*) FROM TBL_APPOINTMENT_CHECKUP_MAP WHERE appointmentId = :appointmentId")
    suspend fun getCheckupCountForAppointment(appointmentId: Int): Int
} 
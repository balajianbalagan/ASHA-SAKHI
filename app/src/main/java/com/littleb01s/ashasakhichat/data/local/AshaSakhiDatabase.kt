package com.littleb01s.ashasakhichat.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.littleb01s.ashasakhichat.data.local.converters.Converters
import com.littleb01s.ashasakhichat.data.local.dao.*
import com.littleb01s.ashasakhichat.data.local.entity.*

@Database(
    entities = [
        Patient::class,
        Appointment::class,
        Checkup::class,
        Document::class,
        DoctorVerification::class,
        Diet::class,
        Photo::class,
        Doctor::class,
        Infant::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AshaSakhiDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun checkupDao(): CheckupDao
    abstract fun documentDao(): DocumentDao
    abstract fun doctorVerificationDao(): DoctorVerificationDao
    abstract fun dietDao(): DietDao
    abstract fun photoDao(): PhotoDao
    abstract fun doctorDao(): DoctorDao
    abstract fun infantDao(): InfantDao

    companion object {
        private const val DATABASE_NAME = "asha_sakhi_db"

        @Volatile
        private var INSTANCE: AshaSakhiDatabase? = null

        fun getInstance(context: Context): AshaSakhiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AshaSakhiDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration() // For development only, remove in production
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 
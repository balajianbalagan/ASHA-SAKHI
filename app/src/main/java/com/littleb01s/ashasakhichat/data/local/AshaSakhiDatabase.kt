package com.littleb01s.ashasakhichat.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.littleb01s.ashasakhichat.data.local.converters.Converters
import com.littleb01s.ashasakhichat.data.local.dao.PatientDao
import com.littleb01s.ashasakhichat.data.local.entity.Patient

@Database(
    entities = [Patient::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AshaSakhiDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao

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
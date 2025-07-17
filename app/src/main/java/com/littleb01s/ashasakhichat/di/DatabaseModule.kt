package com.littleb01s.ashasakhichat.di

import android.content.Context
import com.littleb01s.ashasakhichat.data.local.AshaSakhiDatabase
import com.littleb01s.ashasakhichat.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AshaSakhiDatabase {
        return AshaSakhiDatabase.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun providePatientDao(database: AshaSakhiDatabase): PatientDao {
        return database.patientDao()
    }
    
    @Provides
    @Singleton
    fun provideCheckupDao(database: AshaSakhiDatabase): CheckupDao {
        return database.checkupDao()
    }

    @Provides
    @Singleton
    fun provideAppointmentDao(database: AshaSakhiDatabase): AppointmentDao {
        return database.appointmentDao()
    }

    @Provides
    @Singleton
    fun provideDocumentDao(database: AshaSakhiDatabase): DocumentDao {
        return database.documentDao()
    }

    @Provides
    @Singleton
    fun provideRiskAnalysisDao(database: AshaSakhiDatabase): RiskAnalysisDao {
        return database.riskAnalysisDao()
    }
    
    @Provides
    @Singleton
    fun provideSyncTimestampDao(database: AshaSakhiDatabase): SyncTimestampDao {
        return database.syncTimestampDao()
    }
    
    @Provides
    @Singleton
    fun provideDoctorDao(database: AshaSakhiDatabase): DoctorDao {
        return database.doctorDao()
    }
    
    @Provides
    @Singleton
    fun provideDoctorVerificationDao(database: AshaSakhiDatabase): DoctorVerificationDao {
        return database.doctorVerificationDao()
    }
    
    @Provides
    @Singleton
    fun provideDietDao(database: AshaSakhiDatabase): DietDao {
        return database.dietDao()
    }
    
    @Provides
    @Singleton
    fun providePhotoDao(database: AshaSakhiDatabase): PhotoDao {
        return database.photoDao()
    }
    
    @Provides
    @Singleton
    fun provideInfantDao(database: AshaSakhiDatabase): InfantDao {
        return database.infantDao()
    }
    
    @Provides
    @Singleton
    fun provideSchemeDao(database: AshaSakhiDatabase): SchemeDao {
        return database.schemeDao()
    }
    
    // We'll add more providers here as needed (DAOs, Repositories, etc.)
} 
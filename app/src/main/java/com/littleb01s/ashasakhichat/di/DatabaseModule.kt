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
    
    // We'll add more providers here as needed (DAOs, Repositories, etc.)
} 
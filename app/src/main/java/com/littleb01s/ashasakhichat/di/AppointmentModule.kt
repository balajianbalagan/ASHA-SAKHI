package com.littleb01s.ashasakhichat.di

import com.littleb01s.ashasakhichat.data.repository.AppointmentRepository
import com.littleb01s.ashasakhichat.data.repository.AppointmentRepositoryImpl
import com.littleb01s.ashasakhichat.data.repository.CentralSyncService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppointmentModule {
    
    @Binds
    @Singleton
    abstract fun bindAppointmentRepository(
        appointmentRepositoryImpl: AppointmentRepositoryImpl
    ): AppointmentRepository
} 
package com.littleb01s.ashasakhichat.di

import com.littleb01s.ashasakhichat.data.repository.RiskAssessmentRepository
import com.littleb01s.ashasakhichat.data.repository.RiskAssessmentRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RiskAssessmentModule {
    
    @Binds
    @Singleton
    abstract fun bindRiskAssessmentRepository(
        riskAssessmentRepositoryImpl: RiskAssessmentRepositoryImpl
    ): RiskAssessmentRepository
} 
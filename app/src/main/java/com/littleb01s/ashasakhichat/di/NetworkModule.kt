package com.littleb01s.ashasakhichat.di
import com.littleb01s.ashasakhichat.data.api.ApiConstants
import com.littleb01s.ashasakhichat.data.api.AuthService
import com.littleb01s.ashasakhichat.data.api.PatientService
import com.littleb01s.ashasakhichat.data.api.DietService
import com.littleb01s.ashasakhichat.data.api.ModelDownloadService
import com.littleb01s.ashasakhichat.data.remote.AppointmentApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .connectTimeout(5, TimeUnit.MINUTES)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideModelDownloadService(retrofit: Retrofit): ModelDownloadService {
        return retrofit.create(ModelDownloadService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    @Provides
    @Singleton
    fun providePatientService(retrofit: Retrofit): PatientService {
        return retrofit.create(PatientService::class.java)
    }

    @Provides
    @Singleton
    fun provideDietService(retrofit: Retrofit): DietService {
        return retrofit.create(DietService::class.java)
    }

    @Provides
    @Singleton
    fun provideAppointmentApi(retrofit: Retrofit): AppointmentApi {
        return retrofit.create(AppointmentApi::class.java)
    }
} 
package com.littleb01s.ashasakhichat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "TBL_PROFILE_PATIENT")
data class Patient(
    @PrimaryKey(autoGenerate = true)
    val patientId: Int = 0,
    
    val state: String? = null,
    val city: String? = null,
    val languagePreference: String? = null,
    val firstName: String,
    val lastName: String? = null,
    val dateOfBirth: Date,
    val deliveryDate: Date? = null,
    val mobileNumber: String,
    val employmentStatus: String? = null,
    val religion: String? = null,
    val education: String? = null,
    val caste: String? = null,
    val bloodGroup: String? = null,
    val previousIllness: String? = null
) 
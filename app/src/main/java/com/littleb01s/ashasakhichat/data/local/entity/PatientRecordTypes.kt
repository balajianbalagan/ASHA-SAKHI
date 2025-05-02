package com.littleb01s.ashasakhichat.data.local.entity

import java.util.Date

sealed interface PatientRecordData

// Symptoms record
data class SymptomsRecord(
    val symptoms: List<String>,
    val onsetDate: Date? = null,
    val severity: String? = null, // e.g., mild, moderate, severe
    val notes: String? = null
) : PatientRecordData

// Notes record
data class NotesRecord(
    val note: String,
    val author: String? = null,
    val timestamp: Date = Date()
) : PatientRecordData

// Test results record
data class TestResultsRecord(
    val testName: String,
    val result: String,
    val unit: String? = null,
    val referenceRange: String? = null,
    val testDate: Date? = null,
    val notes: String? = null
) : PatientRecordData

// ANC (Antenatal Care) visit record
data class ANCVisitRecord(
    val visitNumber: Int? = null,
    val visitDate: Date = Date(),
    val pregnancyStage: String? = null, // e.g., Trimester
    val findings: String? = null,
    val interventions: String? = null,
    val nextVisitDate: Date? = null,
    val notes: String? = null
) : PatientRecordData

// Vaccination record
data class VaccinationRecord(
    val vaccineName: String,
    val doseNumber: Int? = null,
    val administrationDate: Date = Date(),
    val batchNumber: String? = null,
    val administeredBy: String? = null,
    val notes: String? = null
) : PatientRecordData

// Medical report record
data class MedicalReportRecord(
    val reportType: String, // e.g., Ultrasound, Blood Test
    val reportDate: Date = Date(),
    val summary: String? = null,
    val fileUrl: String? = null, // If report is stored as a file
    val notes: String? = null
) : PatientRecordData

// Add more record types as needed, following the same pattern. 
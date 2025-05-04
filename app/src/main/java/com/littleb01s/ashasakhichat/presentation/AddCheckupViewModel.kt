package com.littleb01s.ashasakhichat.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littleb01s.ashasakhichat.data.local.dao.CheckupDao
import com.littleb01s.ashasakhichat.data.local.entity.Checkup
import com.littleb01s.ashasakhichat.data.local.entity.*
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class CheckupFormState(
    val checkupType: String = "",
    val bloodPressure: String = "",
    val oxygen: String = "",
    val weight: String = "",
    val temperature: String = "",
    val sugarLevel: String = "",
    val bmi: String = "",
    val haemoglobin: String = "",
    val checkupData: String = "",
    val pregnancyStage: String = "",
    // Symptoms form fields
    val symptoms: List<String> = listOf(),
    val severity: String = "",
    // Notes form fields
    val note: String ="",
    val author: String = "",
    // Test Results form fields
    val testName: String = "",
    val testResult: String = "",
    val testUnit: String = "",
    val testReferenceRange: String = "",
    // ANC Visit form fields
    val visitNumber: String = "",
    val findings: String = "",
    val interventions: String = "",
    val nextVisitDate: String = "",
    // Vaccination form fields
    val vaccineName: String = "",
    val doseNumber: String = "",
    val batchNumber: String = "",
    val administeredBy: String = "",
    val nextDoseDate: String = "",
    // Medical Report form fields
    val reportType: String = "",
    val summary: String = "",
    val fileUrl: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddCheckupViewModel @Inject constructor(
    private val checkupDao: CheckupDao
) : ViewModel() {

    private val _formState = MutableStateFlow(CheckupFormState())
    val formState: StateFlow<CheckupFormState> = _formState.asStateFlow()

    fun updateCheckupType(type: String) {
        _formState.value = CheckupFormState(checkupType = type)
    }

    fun updateBloodPressure(value: String) {
        _formState.value = _formState.value.copy(bloodPressure = value)
    }

    fun updateOxygen(value: String) {
        _formState.value = _formState.value.copy(oxygen = value)
    }

    fun updateWeight(value: String) {
        _formState.value = _formState.value.copy(weight = value)
    }

    fun updateTemperature(value: String) {
        _formState.value = _formState.value.copy(temperature = value)
    }

    fun updateSugarLevel(value: String) {
        _formState.value = _formState.value.copy(sugarLevel = value)
    }

    fun updateBMI(value: String) {
        _formState.value = _formState.value.copy(bmi = value)
    }

    fun updateHaemoglobin(value: String) {
        _formState.value = _formState.value.copy(haemoglobin = value)
    }

    fun updateCheckupData(value: String) {
        _formState.value = _formState.value.copy(checkupData = value)
    }

    fun updatePregnancyStage(value: String) {
        _formState.value = _formState.value.copy(pregnancyStage = value)
    }

    // Symptoms form update methods
    fun  updateSymptoms(value:List<String>){
        _formState.value = _formState.value.copy(symptoms = value)
    }
    fun updateSeverity(value: String) {
        _formState.value = _formState.value.copy(severity = value)
    }

    // Notes form update methods
    fun updateNote(value: String) {
        _formState.value = _formState.value.copy(note = value)
    }

    fun updateAuthor(value: String) {
        _formState.value = _formState.value.copy(author = value)
    }

    // Test Results form update methods
    fun updateTestName(value: String) {
        _formState.value = _formState.value.copy(testName = value)
    }

    fun updateTestResult(value: String) {
        _formState.value = _formState.value.copy(testResult = value)
    }

    fun updateTestUnit(value: String) {
        _formState.value = _formState.value.copy(testUnit = value)
    }

    fun updateTestReferenceRange(value: String) {
        _formState.value = _formState.value.copy(testReferenceRange = value)
    }

    // ANC Visit form update methods
    fun updateVisitNumber(value: String) {
        _formState.value = _formState.value.copy(visitNumber = value)
    }

    fun updateFindings(value: String) {
        _formState.value = _formState.value.copy(findings = value)
    }

    fun updateInterventions(value: String) {
        _formState.value = _formState.value.copy(interventions = value)
    }

    fun updateNextVisitDate(value: String) {
        _formState.value = _formState.value.copy(nextVisitDate = value)
    }

    // Vaccination form update methods
    fun updateVaccineName(value: String) {
        _formState.value = _formState.value.copy(vaccineName = value)
    }

    fun updateDoseNumber(value: String) {
        _formState.value = _formState.value.copy(doseNumber = value)
    }

    fun updateBatchNumber(value: String) {
        _formState.value = _formState.value.copy(batchNumber = value)
    }

    fun updateAdministeredBy(value: String) {
        _formState.value = _formState.value.copy(administeredBy = value)
    }

    fun updateNextDoseDate(value: String) {
        _formState.value = _formState.value.copy(nextDoseDate = value)
    }

    // Medical Report form update methods
    fun updateReportType(value: String) {
        _formState.value = _formState.value.copy(reportType = value)
    }

    fun updateSummary(value: String) {
        _formState.value = _formState.value.copy(summary = value)
    }

    fun updateFileUrl(value: String) {
        _formState.value = _formState.value.copy(fileUrl = value)
    }

    fun updateNotes(value: String) {
        _formState.value = _formState.value.copy(notes = value)
    }

    fun saveCheckup(patientId: Int) {
        viewModelScope.launch {
            try {
                _formState.value = _formState.value.copy(isLoading = true, error = null)
                val gson = Gson()
                val checkupType = _formState.value.checkupType
                Log.d("AddCheckupViewModel", "Checkup type: $checkupType")
                val checkupDataJson = when (checkupType) {
                    "SYMPTOMS" -> gson.toJson(
                        SymptomsRecord(
                            symptoms = _formState.value.checkupData.split(",").map { it.trim() },
                            severity = _formState.value.severity
                        )
                    )
                    "NOTES" -> gson.toJson(
                        NotesRecord(
                            note = _formState.value.checkupData,
                            author = _formState.value.author
                        )
                    )
                    "TEST_RESULTS" -> gson.toJson(
                        TestResultsRecord(
                            testName = _formState.value.testName,
                            result = _formState.value.testResult,
                            unit = _formState.value.testUnit,
                            referenceRange = _formState.value.testReferenceRange
                        )
                    )
                    "ANC_VISIT" -> gson.toJson(
                        ANCVisitRecord(
                            visitNumber = _formState.value.visitNumber.toIntOrNull(),
                            pregnancyStage = _formState.value.pregnancyStage,
                            findings = _formState.value.findings,
                            interventions = _formState.value.interventions,
                            nextVisitDate = _formState.value.nextVisitDate.takeIf { it.isNotBlank() }?.let { parseDate(it) }
                        )
                    )
                    "VACCINATION" -> gson.toJson(
                        VaccinationRecord(
                            vaccineName = _formState.value.vaccineName,
                            doseNumber = _formState.value.doseNumber.toIntOrNull(),
                            batchNumber = _formState.value.batchNumber,
                            administeredBy = _formState.value.administeredBy
                        )
                    )
                    "MEDICAL_REPORT" -> gson.toJson(
                        MedicalReportRecord(
                            reportType = _formState.value.reportType,
                            summary = _formState.value.summary,
                            fileUrl = _formState.value.fileUrl,
                            notes = _formState.value.notes
                        )
                    )
                    "VITALS" -> gson.toJson(null)
                    else -> _formState.value.checkupData
                }

                val checkup = Checkup(
                    patientId = patientId,
                    checkupType = checkupType,
                    bloodPressure = _formState.value.bloodPressure.takeIf { it.isNotBlank() },
                    oxygen = _formState.value.oxygen.toFloatOrNull(),
                    weight = _formState.value.weight.toFloatOrNull(),
                    temperature = _formState.value.temperature.toFloatOrNull(),
                    sugarLevel = _formState.value.sugarLevel.toFloatOrNull(),
                    bmi = _formState.value.bmi.toFloatOrNull(),
                    haemoglobin = _formState.value.haemoglobin.takeIf { it.isNotBlank() },
                    checkupData = checkupDataJson,
                    pregnancyStage = _formState.value.pregnancyStage.takeIf { it.isNotBlank() },
                    createdAt = Date(),
                    updatedAt = Date()
                )
                checkupDao.insertCheckup(checkup)
                _formState.value = CheckupFormState() // Reset form
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(error = e.message)
            } finally {
                _formState.value = _formState.value.copy(isLoading = false)
            }
        }
    }

    private fun parseDate(dateString: String): Date? {
        return try {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        val CHECKUP_TYPES = listOf(
            "SYMPTOMS",
            "VITALS",
            "NOTES",
            "TEST_RESULTS",
            "ANC_VISIT",
            "VACCINATION",
            "MEDICAL_REPORT"
        )
    }
} 
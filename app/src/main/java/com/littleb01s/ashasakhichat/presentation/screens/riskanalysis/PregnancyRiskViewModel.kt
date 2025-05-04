package com.littleb01s.ashasakhichat.presentation.screens.riskanalysis

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.littleb01s.ashasakhichat.data.local.dao.CheckupDao
import com.littleb01s.ashasakhichat.data.local.entity.CheckupType
import com.littleb01s.ashasakhichat.data.onnx.RiskAssessmentResult
import com.littleb01s.ashasakhichat.data.onnx.RiskPredictor
import com.littleb01s.ashasakhichat.data.local.dao.PatientDao
import com.littleb01s.ashasakhichat.data.local.entity.Patient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class PregnancyRiskViewModel @Inject constructor(
    application: Application,
    private val checkupDao: CheckupDao,
    private val patientDao: PatientDao
) : AndroidViewModel(application) {
    private val riskPredictor = RiskPredictor(application)
    
    private val _riskLevel = MutableStateFlow<String?>(null)
    val riskLevel: StateFlow<String?> = _riskLevel.asStateFlow()
    
    private val _observations = MutableStateFlow<Map<String, String>>(emptyMap())
    val observations: StateFlow<Map<String, String>> = _observations.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _hasRecentVitals = MutableStateFlow(false)
    val hasRecentVitals: StateFlow<Boolean> = _hasRecentVitals.asStateFlow()

    private val _patientData = MutableStateFlow<Patient?>(null)
    val patientData: StateFlow<Patient?> = _patientData.asStateFlow()

    private val _formData = MutableStateFlow<Map<String, String>>(emptyMap())
    val formData: StateFlow<Map<String, String>> = _formData.asStateFlow()

    fun loadPatientData(patientId: Int) {
        viewModelScope.launch {
            try {
                patientDao.getPatientById(patientId).collect { patient ->
                    _patientData.value = patient
                    if (patient != null) {
                        // Calculate age from date of birth
                        val age = calculateAge(patient.dateOfBirth)
                        _formData.value = _formData.value + ("age" to age.toString())
                    }
                }
            } catch (e: Exception) {
                Log.e("PregnancyRiskViewModel", "Error loading patient data: ${e.message}", e)
                _error.value = "Error loading patient data: ${e.message}"
            }
        }
    }

    private fun celsiusToFahrenheit(celsius: Float): Float = celsius * 9 / 5 + 32

    fun loadVitalsData(patientId: Int) {
        viewModelScope.launch {
            try {
                val latestVitals = checkupDao.getLatestCheckupByType(
                    patientId = patientId,
                    checkupType = CheckupType.VITALS.name
                )
                
                if (latestVitals != null) {
                    val formDataMap = mutableMapOf<String, String>()
                    
                    // Split blood pressure
                    latestVitals.bloodPressure?.let { bp ->
                        val parts = bp.split("/")
                        if (parts.size == 2) {
                            formDataMap["systolicBP"] = parts[0]
                            formDataMap["diastolicBP"] = parts[1]
                        }
                    }
                    
                    // Add other vitals
                    latestVitals.temperature?.let { 
                        formDataMap["bodyTemp"] = celsiusToFahrenheit(it.toFloat()).toString() 
                    }
                    latestVitals.sugarLevel?.let { formDataMap["bloodSugar"] = it.toString() }
                    latestVitals.oxygen?.let { formDataMap["heartRate"] = it.toString() }
                    
                    _formData.value = formDataMap
                }
            } catch (e: Exception) {
                Log.e("PregnancyRiskViewModel", "Error loading vitals data: ${e.message}", e)
                _error.value = "Error loading vitals data: ${e.message}"
            }
        }
    }

    private fun calculateAge(dateOfBirth: Date): Int {
        val today = Calendar.getInstance()
        val birthDate = Calendar.getInstance().apply { time = dateOfBirth }
        var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    fun checkRecentVitals(patientId: Int) {
        viewModelScope.launch {
            try {
                Log.d("PregnancyRiskViewModel", "Checking for vitals for patient $patientId")

                val latestVitals = checkupDao.getLatestCheckupByType(
                    patientId = patientId,
                    checkupType = CheckupType.VITALS.name
                )
                Log.d("PregnancyRiskViewModel", "CheckupType is ${CheckupType.VITALS.name}")
                Log.d("PregnancyRiskViewModel", "Latest vitals checkup: $latestVitals")
                
                // Check if we have a valid vitals checkup
                val hasValidVitals = latestVitals != null && 
                    latestVitals.checkupType == CheckupType.VITALS.name

                Log.d("PregnancyRiskViewModel", "Has valid vitals: $hasValidVitals")
                _hasRecentVitals.value = hasValidVitals

                if (hasValidVitals) {
                    loadVitalsData(patientId)
                }
            } catch (e: Exception) {
                Log.e("PregnancyRiskViewModel", "Error checking for recent vitals: ${e.message}", e)
                _error.value = "Error checking for recent vitals: ${e.message}"
                _hasRecentVitals.value = false
            }
        }
    }

    fun assessRisk(
        age: Float,
        systolicBP: Float,
        diastolicBP: Float,
        bloodSugar: Float,
        bodyTemp: Float,
        heartRate: Float
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = withContext(Dispatchers.IO) {
                    riskPredictor.assessRisk(
                        age = age,
                        systolicBP = systolicBP,
                        diastolicBP = diastolicBP,
                        bloodSugar = bloodSugar,
                        bodyTemp = bodyTemp,
                        heartRate = heartRate
                    )
                }
                
                _riskLevel.value = result.riskLevel
                _observations.value = result.observations
            } catch (e: Exception) {
                _error.value = "Error assessing risk: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 
package com.littleb01s.ashasakhichat.presentation.screens.riskanalysis

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.littleb01s.ashasakhichat.data.local.dao.CheckupDao
import com.littleb01s.ashasakhichat.data.local.dao.PatientDao
import com.littleb01s.ashasakhichat.data.local.dao.RiskAnalysisDao
import com.littleb01s.ashasakhichat.data.repository.RiskAssessmentRepository
import com.littleb01s.ashasakhichat.data.local.entity.CheckupType
import com.littleb01s.ashasakhichat.data.local.entity.Patient
import com.littleb01s.ashasakhichat.data.local.entity.RiskAnalysisResult
import com.littleb01s.ashasakhichat.data.onnx.RiskAssessmentResult
import com.littleb01s.ashasakhichat.data.onnx.RiskPredictor
import com.littleb01s.ashasakhichat.util.Resource
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
    private val patientDao: PatientDao,
    private val riskAnalysisDao: RiskAnalysisDao,
    private val riskAssessmentRepository: RiskAssessmentRepository
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

    private val _latestRiskAnalysis = MutableStateFlow<RiskAnalysisResult?>(null)
    val latestRiskAnalysis: StateFlow<RiskAnalysisResult?> = _latestRiskAnalysis.asStateFlow()

    private val _success = MutableStateFlow<String?>(null)
    val success: StateFlow<String?> = _success.asStateFlow()

    data class RiskAnalysisDetails(
        val age: Float,
        val systolicBP: Float,
        val diastolicBP: Float,
        val bloodSugar: Float,
        val bodyTemp: Float,
        val heartRate: Float,
        val observations: Map<String, String>
    )

    fun loadPatientData(patientId: Int) {
        viewModelScope.launch {
            try {
                patientDao.getPatientById(patientId).collect { patient ->
                    _patientData.value = patient
                    if (patient != null) {
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
                
                val currentFormData = _formData.value.toMutableMap()
                // Only update the vitals fields, not age
                if (latestVitals != null) {
                    latestVitals.bloodPressure?.let { bp ->
                        val parts = bp.split("/")
                        if (parts.size == 2) {
                            currentFormData["systolicBP"] = parts[0]
                            currentFormData["diastolicBP"] = parts[1]
                        }
                    }
                    latestVitals.temperature?.let { 
                        currentFormData["bodyTemp"] = celsiusToFahrenheit(it.toFloat()).toString() 
                    }
                    latestVitals.sugarLevel?.let { currentFormData["bloodSugar"] = it.toString() }
                    latestVitals.oxygen?.let { currentFormData["heartRate"] = it.toString() }
                } else {
                    // If no vitals, clear the fields except age
                    currentFormData.remove("systolicBP")
                    currentFormData.remove("diastolicBP")
                    currentFormData.remove("bodyTemp")
                    currentFormData.remove("bloodSugar")
                    currentFormData.remove("heartRate")
                }
                _formData.value = currentFormData
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
        patientId: Int,
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
            _success.value = null
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
                
                // Get the latest checkup ID for this patient
                val latestCheckup = checkupDao.getLatestCheckupByType(
                    patientId = patientId,
                    checkupType = CheckupType.VITALS.name
                )
                val checkupId = latestCheckup?.checkupId ?: 0
                
                // Save the risk analysis result with all input values and observations
                val details = RiskAnalysisDetails(
                    age = age,
                    systolicBP = systolicBP,
                    diastolicBP = diastolicBP,
                    bloodSugar = bloodSugar,
                    bodyTemp = bodyTemp,
                    heartRate = heartRate,
                    observations = result.observations
                )
                
                // Use repository to save with server-first approach
                riskAssessmentRepository.saveRiskAnalysisWithServerFirst(
                    patientId = patientId,
                    checkupId = checkupId,
                    riskValue = result.riskLevel,
                    comments = Gson().toJson(details),
                    riskId = null
                ).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            Log.d("PregnancyRiskViewModel", "Risk analysis saved successfully with ID: ${resource.data}")
                            _riskLevel.value = result.riskLevel
                            _observations.value = result.observations
                            _success.value = "Risk analysis completed and saved successfully."
                        }
                        is Resource.Error -> {
                            Log.e("PregnancyRiskViewModel", "Error saving risk analysis: ${resource.message}")
                            _error.value = "Error saving risk analysis: ${resource.message}"
                        }
                        is Resource.Loading -> {
                            // Loading state is already handled by _isLoading
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error assessing risk: ${e.message}"
                _success.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadLatestRiskAnalysis(patientId: Int) {
        viewModelScope.launch {
            try {
                val analysis = riskAssessmentRepository.getLatestRiskAnalysisForPatient(patientId)
                _latestRiskAnalysis.value = analysis
                if (analysis != null) {
                    _riskLevel.value = analysis.riskValue
                    // Parse analysis.comments back to RiskAnalysisDetails
                    val details = Gson().fromJson(analysis.comments, RiskAnalysisDetails::class.java)
                    // Set all fields in formData as String
                    val formMap = mutableMapOf<String, String>()
                    formMap["age"] = details.age.toString()
                    formMap["systolicBP"] = details.systolicBP.toString()
                    formMap["diastolicBP"] = details.diastolicBP.toString()
                    formMap["bloodSugar"] = details.bloodSugar.toString()
                    formMap["bodyTemp"] = details.bodyTemp.toString()
                    formMap["heartRate"] = details.heartRate.toString()
                    _formData.value = formMap
                    // Set observations
                    _observations.value = details.observations.mapValues { it.value.toString() }
                }
            } catch (e: Exception) {
                Log.e("PregnancyRiskViewModel", "Error loading risk analysis: ${e.message}", e)
                _error.value = "Error loading risk analysis: ${e.message}"
            }
        }
    }
} 
package com.littleb01s.ashasakhichat.presentation.screens.riskanalysis

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.littleb01s.ashasakhichat.data.onnx.RiskPredictor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PregnancyRiskViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private val riskPredictor = RiskPredictor(application)
    
    private val _riskLevel = MutableStateFlow<String?>(null)
    val riskLevel = _riskLevel.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun predictRisk(
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
                    riskPredictor.predictRiskLevel(
                        age = age,
                        systolicBP = systolicBP,
                        diastolicBP = diastolicBP,
                        bloodSugar = bloodSugar,
                        bodyTemp = bodyTemp,
                        heartRate = heartRate
                    )
                }
                
                _riskLevel.value = result
            } catch (e: Exception) {
                _error.value = "Error predicting risk: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 
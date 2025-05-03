package com.littleb01s.ashasakhichat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littleb01s.ashasakhichat.data.local.dao.CheckupDao
import com.littleb01s.ashasakhichat.data.local.entity.Checkup
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
        _formState.value = _formState.value.copy(checkupType = type)
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

    fun saveCheckup(patientId: Int) {
        viewModelScope.launch {
            try {
                _formState.value = _formState.value.copy(isLoading = true, error = null)
                
                val checkup = Checkup(
                    patientId = patientId,
                    checkupType = _formState.value.checkupType,
                    bloodPressure = _formState.value.bloodPressure.toFloatOrNull(),
                    oxygen = _formState.value.oxygen.toFloatOrNull(),
                    weight = _formState.value.weight.toFloatOrNull(),
                    temperature = _formState.value.temperature.toFloatOrNull(),
                    sugarLevel = _formState.value.sugarLevel.toFloatOrNull(),
                    bmi = _formState.value.bmi.toFloatOrNull(),
                    haemoglobin = _formState.value.haemoglobin.takeIf { it.isNotBlank() },
                    checkupData = _formState.value.checkupData.takeIf { it.isNotBlank() },
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

    companion object {
        val CHECKUP_TYPES = listOf(
            "SYMPTOMS",
            "VITALS",
            "NOTES",
            "TEST_RESULTS",
            "ANC_VISIT"
        )
    }
} 
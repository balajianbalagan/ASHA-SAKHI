package com.littleb01s.ashasakhichat.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littleb01s.ashasakhichat.data.local.dao.CheckupDao
import com.littleb01s.ashasakhichat.data.local.entity.Checkup
import com.littleb01s.ashasakhichat.data.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.serialization.json.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class PatientInfo(
    val name: String,
    val age: Int,
    val trimester: String,
    val edd: String
)

@HiltViewModel
class MedicalHistoryViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val checkupDao: CheckupDao
) : ViewModel() {
    private val _checkups = MutableStateFlow<List<Checkup>>(emptyList())
    val checkups: StateFlow<List<Checkup>> = _checkups.asStateFlow()

    private val _patientInfo = MutableStateFlow<PatientInfo?>(null)
    val patientInfo: StateFlow<PatientInfo?> = _patientInfo.asStateFlow()

    fun loadCheckups(patientId: Int) {
        viewModelScope.launch {
            checkupDao.getCheckupsForPatient(patientId).collect { checkupList ->
                _checkups.value = checkupList
            }
        }
    }

    fun loadPatientInfo(patientId: Int) {
        viewModelScope.launch {
            patientRepository.getPatientWithDetails(patientId).collect { patientWithDetails ->
                patientWithDetails?.let { details ->
                    val patient = details.patient
                    
                    // Debug log for LMP
                    Log.d("MedicalHistoryVM", "LMP Date: ${patient.lmp}")
                    Log.d("MedicalHistoryVM", "Delivery Date: ${patient.deliveryDate}")
                    
                    // Calculate trimester based on either LMP or delivery date
                    val trimester = if (patient.lmp != null) {
                        calculateTrimesterFromLMP(patient.lmp)
                    } else if (patient.deliveryDate != null) {
                        calculateTrimesterFromEDD(patient.deliveryDate)
                    } else {
                        "Unknown"
                    }
                    
                    Log.d("MedicalHistoryVM", "Calculated Trimester: $trimester")
                    
                    val edd = when {
                        patient.deliveryDate != null -> formatDate(patient.deliveryDate)
                        patient.lmp != null -> calculateEDD(patient.lmp)
                        else -> "Unknown"
                    }
                    
                    _patientInfo.value = PatientInfo(
                        name = "${patient.firstName} ${patient.lastName ?: ""}".trim(),
                        age = calculateAge(patient.dateOfBirth),
                        trimester = trimester,
                        edd = edd
                    )
                }
            }
        }
    }

    private fun calculateTrimesterFromLMP(lmp: Date): String {
        val today = Calendar.getInstance().timeInMillis
        val lmpTime = lmp.time
        val weeksDiff = TimeUnit.MILLISECONDS.toDays(today - lmpTime) / 7
        
        Log.d("MedicalHistoryVM", "Weeks since LMP: $weeksDiff")

        return when {
            weeksDiff < 0 -> "Unknown"
            weeksDiff <= 13 -> "1st"
            weeksDiff <= 26 -> "2nd"
            weeksDiff <= 40 -> "3rd"
            else -> "Post-term"
        }
    }

    private fun calculateTrimesterFromEDD(edd: Date): String {
        val today = Calendar.getInstance().timeInMillis
        val eddTime = edd.time
        val weeksUntilEDD = TimeUnit.MILLISECONDS.toDays(eddTime - today) / 7
        
        Log.d("MedicalHistoryVM", "Weeks until EDD: $weeksUntilEDD")

        return when {
            weeksUntilEDD >= 27 -> "1st"
            weeksUntilEDD >= 14 -> "2nd"
            weeksUntilEDD >= 0 -> "3rd"
            else -> "Post-term"
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

    private fun calculateEDD(lmp: Date): String {
        return formatDate(calculateEDDDate(lmp))
    }

    private fun calculateEDDDate(lmp: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = lmp
        calendar.add(Calendar.WEEK_OF_YEAR, 40)
        return calendar.time
    }

    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(date)
    }
} 
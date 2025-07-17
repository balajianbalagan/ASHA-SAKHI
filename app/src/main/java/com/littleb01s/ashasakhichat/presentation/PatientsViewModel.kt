package com.littleb01s.ashasakhichat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littleb01s.ashasakhichat.data.api.PatientData
import com.littleb01s.ashasakhichat.data.api.VitalsData
import com.littleb01s.ashasakhichat.data.local.entity.Patient
import com.littleb01s.ashasakhichat.data.local.entity.Scheme
import com.littleb01s.ashasakhichat.data.local.PreferencesManager
import com.littleb01s.ashasakhichat.data.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientsViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    fun filteredPatients(searchQuery: String): Flow<List<Patient>> {
        return patientRepository.getAllPatients().map { patients ->
            if (searchQuery.isBlank()) {
                patients
            } else {
                patients.filter { patient ->
                    val fullName = "${patient.firstName} ${patient.lastName ?: ""}".lowercase()
                    fullName.contains(searchQuery.lowercase())
                }
            }
        }
    }

    fun savePatient(patientData: PatientData, vitalsData: VitalsData) {
        viewModelScope.launch {
            try {
                val workerId = preferencesManager.getWorkerId() ?: throw Exception("Worker ID not found. Please log in again.")
                patientRepository.savePatientWithVitals(patientData, vitalsData, workerId)
            } catch (e: Exception) {
                // Handle error
                throw e
            }
        }
    }

    fun addNewPatient(
        patientData: PatientData,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val workerId = preferencesManager.getWorkerId() ?: throw Exception("app issue: Worker ID not found. Please log in again.")
                val isSynced = patientRepository.addNewPatient(patientData, workerId)
                onSuccess(isSynced)
            } catch (e: Exception) {
                onError(e.message ?: "app issue: Unknown error occurred")
            }
        }
    }

    // Function to trigger sync
    suspend fun syncPatients() {
        patientRepository.syncPatients()
    }

    fun getPatientDetails(patientId: Int): Flow<Patient?> {
        return patientRepository.getPatientWithDetails(patientId).map { patientWithDetails ->
            patientWithDetails?.patient
        }
    }

    fun getSchemesForPatient(patientId: Int): Flow<List<Scheme>> {
        return patientRepository.getSchemesByPatientId(patientId)
    }
} 
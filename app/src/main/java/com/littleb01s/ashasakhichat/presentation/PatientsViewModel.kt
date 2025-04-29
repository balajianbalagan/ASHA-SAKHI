package com.littleb01s.ashasakhichat.presentation

import androidx.lifecycle.ViewModel
import com.littleb01s.ashasakhichat.data.local.entity.Patient
import com.littleb01s.ashasakhichat.data.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class PatientsViewModel @Inject constructor(
    private val patientRepository: PatientRepository
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

    // Function to trigger sync (to be implemented)
    suspend fun syncPatients() {
        patientRepository.syncPatients()
    }
} 
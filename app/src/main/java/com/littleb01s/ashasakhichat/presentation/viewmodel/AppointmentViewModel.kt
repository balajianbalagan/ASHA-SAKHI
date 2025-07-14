package com.littleb01s.ashasakhichat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littleb01s.ashasakhichat.data.local.PreferencesManager
import com.littleb01s.ashasakhichat.data.local.entity.Appointment
import com.littleb01s.ashasakhichat.data.api.AppointmentListResponse
import com.littleb01s.ashasakhichat.data.api.AppointmentResponse
import com.littleb01s.ashasakhichat.data.repository.AppointmentRepository
import com.littleb01s.ashasakhichat.data.repository.PatientRepository
import com.littleb01s.ashasakhichat.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val repository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _appointments = MutableStateFlow<Resource<AppointmentListResponse>>(Resource.Loading())
    val appointments: StateFlow<Resource<AppointmentListResponse>> = _appointments

    private val _createAppointmentState = MutableStateFlow<Resource<AppointmentResponse>>(Resource.Loading())
    val createAppointmentState: StateFlow<Resource<AppointmentResponse>> = _createAppointmentState

    fun fetchAppointments() {
        viewModelScope.launch {
            repository.fetchAppointments()
                .onEach { result ->
                    _appointments.value = result
                }
                .launchIn(viewModelScope)
        }
    }
    
    // Fetch appointments for a specific patient from local database
    fun fetchAppointmentsForPatient(patientId: Int) {
        viewModelScope.launch {
            patientRepository.getPatientAppointments(patientId)
                .onEach { result ->
                    _appointments.value = result
                }
                .launchIn(viewModelScope)
        }
    }

    fun createAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.createAppointment(appointment)
                .onEach { result ->
                    _createAppointmentState.value = result
                    // If successful, refetch appointments to show the new one
                    if (result is Resource.Success) {
                        // Refetch appointments for the same patient
                        fetchAppointmentsForPatient(appointment.patientId)
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun resetCreateAppointmentState() {
        _createAppointmentState.value = Resource.Loading()
    }

    fun getWorkerId(): Int? {
        return preferencesManager.getWorkerId()
    }
} 
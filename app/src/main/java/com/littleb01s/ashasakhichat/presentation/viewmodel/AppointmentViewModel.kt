package com.littleb01s.ashasakhichat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littleb01s.ashasakhichat.data.local.PreferencesManager
import com.littleb01s.ashasakhichat.data.model.Appointment
import com.littleb01s.ashasakhichat.data.model.AppointmentListResponse
import com.littleb01s.ashasakhichat.data.model.AppointmentResponse
import com.littleb01s.ashasakhichat.data.repository.AppointmentRepository
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

    fun createAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.createAppointment(appointment)
                .onEach { result ->
                    _createAppointmentState.value = result
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
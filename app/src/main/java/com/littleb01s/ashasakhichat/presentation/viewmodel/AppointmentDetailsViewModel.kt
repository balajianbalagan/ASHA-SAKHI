package com.littleb01s.ashasakhichat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littleb01s.ashasakhichat.data.local.entity.Appointment
import com.littleb01s.ashasakhichat.data.repository.AppointmentRepository
import com.littleb01s.ashasakhichat.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentDetailsViewModel @Inject constructor(
    private val repository: AppointmentRepository
) : ViewModel() {

    private val _appointment = MutableStateFlow<Resource<Appointment>>(Resource.Loading())
    val appointment: StateFlow<Resource<Appointment>> = _appointment

    fun fetchAppointmentById(appointmentId: Int) {
        viewModelScope.launch {
            try {
                _appointment.value = Resource.Loading()
                val appointment = repository.getAppointmentById(appointmentId)
                if (appointment != null) {
                    _appointment.value = Resource.Success(appointment)
                } else {
                    _appointment.value = Resource.Error("Appointment not found")
                }
            } catch (e: Exception) {
                _appointment.value = Resource.Error(e.message ?: "Failed to fetch appointment")
            }
        }
    }
} 
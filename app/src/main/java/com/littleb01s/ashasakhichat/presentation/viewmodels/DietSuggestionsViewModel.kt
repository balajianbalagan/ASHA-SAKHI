package com.littleb01s.ashasakhichat.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littleb01s.ashasakhichat.data.api.DietService
import com.littleb01s.ashasakhichat.data.model.DayMeals
import com.littleb01s.ashasakhichat.data.model.DietPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DietSuggestionsViewModel @Inject constructor(
    private val dietService: DietService
) : ViewModel() {
    private val _dietPlan = MutableStateFlow<DietPlan?>(null)
    val dietPlan: StateFlow<DietPlan?> = _dietPlan

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadDietPlan(patientId: Int, isOnlineMode: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (isOnlineMode) {
                    // Online mode: Fetch from API
                    val response = dietService.fetchDietPlan(patientId)
                    if (response.isSuccessful && response.body() != null) {
                        _dietPlan.value = response.body()?.data
                    } else {
                        _error.value = "Failed to fetch diet plan"
                        Log.e("DietSuggestionsViewModel", "Error: ${response.errorBody()?.string()}")
                    }
                } else {
                    // Offline mode: Use mock data
                    _dietPlan.value = createMockDietPlan()
                }
            } catch (e: Exception) {
                Log.e("DietSuggestionsViewModel", "Error loading diet plan", e)
                _error.value = e.message ?: "Unknown error occurred"
                _dietPlan.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendToPatient(patientId: String,patientMobileno:String) {
        viewModelScope.launch {
            try {
                // TODO: Implement actual SMS sending logic
                Log.d("DietSuggestionsViewModel", "Sending diet plan to patient $patientId")
            } catch (e: Exception) {
                Log.e("DietSuggestionsViewModel", "Error sending diet plan", e)
                _error.value = e.message ?: "Failed to send diet plan"
            }
        }
    }

    private fun createMockDietPlan(): DietPlan {
        val dayMeals = DayMeals(
            breakfast = "2 Chapati with vegetable curry",
            morning_snack = "Mixed nuts and fruits",
            lunch = "Rice, dal, and mixed vegetables",
            evening_snack = "Sprouts salad",
            dinner = "2 Chapati with dal and vegetables"
        )

        val dietPlan = mutableMapOf<String, DayMeals>()
        for (i in 1..7) {
            dietPlan["day$i"] = dayMeals
        }

        return DietPlan(
            diet_plan = dietPlan,
            region = "Maharashtra"
        )
    }
} 
package com.littleb01s.ashasakhichat.presentation

import androidx.lifecycle.ViewModel
import com.littleb01s.ashasakhichat.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _userName = MutableStateFlow<String>("User")
    val userName: StateFlow<String> = _userName

    init {
        // Initialize user name from preferences
        preferencesManager.getUserProfile()?.let { profile ->
            _userName.value = profile.firstName
        }
    }
    
    fun isUserLoggedIn(): Boolean {
        return preferencesManager.getAuthToken() != null
    }
} 
package com.littleb01s.ashasakhichat.presentation

import androidx.lifecycle.ViewModel
import com.littleb01s.ashasakhichat.data.local.PreferencesManager
import com.littleb01s.ashasakhichat.data.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _userProfile = MutableStateFlow(
        UserProfile(
            firstName = "",
            lastName = "",
            profileId = 0,
            workerId = 0,
            state = "",
            city = "",
            languagePreference = "",
            specialization = "",
            createdAt = "",
            updatedAt = ""
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile

    private val _showSignOutDialog = MutableStateFlow(false)
    val showSignOutDialog: StateFlow<Boolean> = _showSignOutDialog

    init {
        // Load user profile from preferences
        preferencesManager.getUserProfile()?.let { profile ->
            _userProfile.value = profile
        }
    }

    fun signOut() {
        // Clear all preferences
        preferencesManager.clearAll()
        _showSignOutDialog.value = true
    }

    fun hideSignOutDialog() {
        _showSignOutDialog.value = false
    }
} 
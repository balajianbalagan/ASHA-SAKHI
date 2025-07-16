package com.littleb01s.ashasakhichat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littleb01s.ashasakhichat.data.local.PreferencesManager
import com.littleb01s.ashasakhichat.data.model.UserProfile
import com.littleb01s.ashasakhichat.data.repository.DatabaseClearService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val databaseClearService: DatabaseClearService
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

    private val _shouldResetSync = MutableStateFlow(false)
    val shouldResetSync: StateFlow<Boolean> = _shouldResetSync

    init {
        // Load user profile from preferences
        preferencesManager.getUserProfile()?.let { profile ->
            _userProfile.value = profile
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                // Clear all local database data
                databaseClearService.clearAllData()
                
                // Clear all preferences
                preferencesManager.clearAll()
                
                // Signal that sync should be reset
                _shouldResetSync.value = true
                
                _showSignOutDialog.value = true
            } catch (e: Exception) {
                // Even if database clear fails, still clear preferences and show dialog
                preferencesManager.clearAll()
                _shouldResetSync.value = true
                _showSignOutDialog.value = true
            }
        }
    }

    fun hideSignOutDialog() {
        _showSignOutDialog.value = false
    }

    fun resetSyncSignal() {
        _shouldResetSync.value = false
    }
} 
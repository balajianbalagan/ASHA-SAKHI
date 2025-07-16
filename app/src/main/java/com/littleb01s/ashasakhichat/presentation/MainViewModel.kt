package com.littleb01s.ashasakhichat.presentation

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littleb01s.ashasakhichat.data.local.PreferencesManager
import com.littleb01s.ashasakhichat.data.repository.PatientRepository
import com.littleb01s.ashasakhichat.data.repository.CentralSyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SyncStatus {
    IDLE, SYNCING, SUCCESS, FAILED, NO_INTERNET
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val patientRepository: PatientRepository,
    private val centralSyncService: CentralSyncService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private var hasInitialSyncRun = false
        private var hasPostLoginSyncRun = false
    }

    private val _userName = MutableStateFlow<String>("User")
    val userName: StateFlow<String> = _userName

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    init {
        // Initialize user name from preferences
        preferencesManager.getUserProfile()?.let { profile ->
            _userName.value = profile.firstName
        }
        
        Log.d("MainViewModel", "Initializing MainViewModel")
        Log.d("MainViewModel", "User logged in: ${isUserLoggedIn()}")
        Log.d("MainViewModel", "Internet available: ${isInternetAvailable()}")
        Log.d("MainViewModel", "Has initial sync run: $hasInitialSyncRun")
        
        // Auto-sync if user is logged in and internet is available and sync hasn't run yet
        if (isUserLoggedIn() && isInternetAvailable() && !hasInitialSyncRun) {
            Log.d("MainViewModel", "Starting auto-sync")
            hasInitialSyncRun = true
            performSync()
        } else {
            Log.d("MainViewModel", "Skipping sync - conditions not met or already run")
            if (!isInternetAvailable()) {
                _syncStatus.value = SyncStatus.NO_INTERNET
            }
        }
    }

    private fun performSync() {
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "Starting central sync")
                _syncStatus.value = SyncStatus.SYNCING
                
                // Perform full sync using CentralSyncService (includes patients, appointments, checkups)
                centralSyncService.performFullSync()
                
                Log.d("MainViewModel", "Sync successful, setting toast")
                _syncStatus.value = SyncStatus.SUCCESS
                _toastMessage.value = "Data synced successfully!"
            } catch (e: Exception) {
                Log.e("MainViewModel", "Sync failed", e)
                _syncStatus.value = SyncStatus.FAILED
                _toastMessage.value = "Sync failed: ${e.message}"
            }
        }
    }
    
    fun isUserLoggedIn(): Boolean {
        return preferencesManager.getAuthToken() != null
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun resetSyncFlag() {
        hasInitialSyncRun = false
    }

    fun resetSyncStatus() {
        _syncStatus.value = SyncStatus.IDLE
        hasInitialSyncRun = false
        hasPostLoginSyncRun = false
        _toastMessage.value = null
    }

    fun triggerSyncAfterLogin() {
        if (!hasPostLoginSyncRun) {
            hasPostLoginSyncRun = true
            performSync()
        }
    }

    fun manualSync() {
        if (isInternetAvailable()) {
            performSync()
        } else {
            _syncStatus.value = SyncStatus.NO_INTERNET
            _toastMessage.value = "No internet connection"
        }
    }
} 
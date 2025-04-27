package com.littleb01s.ashasakhichat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.littleb01s.ashasakhichat.data.api.AuthService
import com.littleb01s.ashasakhichat.data.local.PreferencesManager
import com.littleb01s.ashasakhichat.data.model.LoginRequest
import com.littleb01s.ashasakhichat.data.model.LoginErrorResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authService: AuthService,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(phoneNumber: String, password: String) {
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading
                
                val response = authService.login(LoginRequest(phoneNumber, password))
                
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    // Save to SharedPreferences
                    preferencesManager.saveAuthToken(loginResponse.data.token)
                    loginResponse.data.profile?.let { preferencesManager.saveUserProfile(it) }
                    
                    _loginState.value = LoginState.Success(loginResponse.data.message)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val errorResponse = Gson().fromJson(errorBody, LoginErrorResponse::class.java)
                        errorResponse.message
                    } catch (e: Exception) {
                        "An unknown error occurred"
                    }
                    _loginState.value = LoginState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val message: String) : LoginState()
    data class Error(val message: String) : LoginState()
} 
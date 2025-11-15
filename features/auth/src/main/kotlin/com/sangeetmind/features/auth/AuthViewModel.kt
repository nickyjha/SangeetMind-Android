package com.sangeetmind.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.libs.models.AuthResponse
import com.sangeetmind.libs.models.LoginRequest
import com.sangeetmind.libs.models.SignupRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    // TODO: Inject AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun login() {
        val state = _uiState.value
        
        if (!validateEmail(state.email)) {
            _uiState.update { it.copy(error = "Invalid email address") }
            return
        }
        
        if (state.password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // TODO: Replace with actual API call
            delay(1500)
            
            // Mock success
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    error = null
                )
            }
        }
    }

    fun signup() {
        val state = _uiState.value
        
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }
        
        if (!validateEmail(state.email)) {
            _uiState.update { it.copy(error = "Invalid email address") }
            return
        }
        
        if (state.password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // TODO: Replace with actual API call
            delay(1500)
            
            // Mock success
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    error = null
                )
            }
        }
    }

    fun loginWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // TODO: Implement Google Sign-In
            delay(1500)
            
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isAuthenticated = true
                )
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}


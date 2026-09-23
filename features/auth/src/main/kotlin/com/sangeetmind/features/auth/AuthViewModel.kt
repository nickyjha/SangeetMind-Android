package com.sangeetmind.features.auth

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthMode { LOGIN, SIGNUP, FORGOT_PASSWORD }

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val resetEmailSent: Boolean = false,
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val languageManager: LanguageManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(isAuthenticated = authRepository.currentUser != null)
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private fun str(@StringRes id: Int): String =
        appContext.withAppLanguage(languageManager.current).getString(id)

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun setMode(mode: AuthMode) {
        _uiState.update { it.copy(mode = mode, error = null, resetEmailSent = false) }
    }

    fun login() {
        val state = _uiState.value

        if (!validateEmail(state.email)) {
            _uiState.update { it.copy(error = str(R.string.auth_error_invalid_email)) }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(error = str(R.string.auth_error_password_short)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signIn(state.email, state.password)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, isAuthenticated = true, error = null)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message ?: str(R.string.auth_error_sign_in_failed))
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun signup() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = str(R.string.auth_error_name_required)) }
            return
        }
        if (!validateEmail(state.email)) {
            _uiState.update { it.copy(error = str(R.string.auth_error_invalid_email)) }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(error = str(R.string.auth_error_password_short)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signUp(state.email, state.password)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, isAuthenticated = true, error = null)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message ?: str(R.string.auth_error_sign_up_failed))
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun sendPasswordReset() {
        val state = _uiState.value
        if (!validateEmail(state.email)) {
            _uiState.update { it.copy(error = str(R.string.auth_error_invalid_email)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.sendPasswordReset(state.email)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, resetEmailSent = true, error = null)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message ?: str(R.string.auth_error_reset_failed))
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

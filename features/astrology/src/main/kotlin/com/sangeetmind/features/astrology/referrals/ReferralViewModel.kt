package com.sangeetmind.features.astrology.referrals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sangeetmind.core.common.Result
import com.sangeetmind.libs.models.ReferralStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReferralUiState(
    val myCode: String = "",
    val codeInput: String = "",
    val isLoading: Boolean = false,
    val stats: ReferralStats? = null,
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class ReferralViewModel @Inject constructor(
    private val repository: ReferralRepository,
    firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReferralUiState(myCode = firebaseAuth.currentUser?.uid.orEmpty())
    )
    val uiState: StateFlow<ReferralUiState> = _uiState.asStateFlow()

    init {
        refreshStats()
    }

    fun onCodeInputChange(value: String) {
        _uiState.update { it.copy(codeInput = value, error = null, message = null) }
    }

    fun refreshStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getStats()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, stats = result.data) }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message ?: "Failed to load stats")
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun applyCode() {
        val code = _uiState.value.codeInput.trim()
        if (code.isBlank()) {
            _uiState.update { it.copy(error = "Enter a referral code") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            when (val result = repository.applyReferral(code)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            codeInput = "",
                            message = if (result.data.created) "Referral applied!" else "Already applied"
                        )
                    }
                    refreshStats()
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message ?: "Failed to apply code")
                }
                is Result.Loading -> Unit
            }
        }
    }
}

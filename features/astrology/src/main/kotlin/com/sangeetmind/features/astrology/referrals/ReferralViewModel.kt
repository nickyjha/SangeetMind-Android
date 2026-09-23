package com.sangeetmind.features.astrology.referrals

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.ReferralStats
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
    private val languageManager: LanguageManager,
    firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReferralUiState(myCode = firebaseAuth.currentUser?.uid.orEmpty())
    )
    val uiState: StateFlow<ReferralUiState> = _uiState.asStateFlow()

    private fun str(@StringRes id: Int): String =
        context.withAppLanguage(languageManager.current).getString(id)

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
                    it.copy(
                        isLoading = false,
                        error = result.message ?: str(R.string.referrals_error_load_stats)
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun applyCode() {
        val code = _uiState.value.codeInput.trim()
        if (code.isBlank()) {
            _uiState.update { it.copy(error = str(R.string.referrals_error_enter_code)) }
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
                            message = str(
                                if (result.data.created) R.string.referrals_msg_applied
                                else R.string.referrals_msg_already_applied
                            )
                        )
                    }
                    refreshStats()
                }
                is Result.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.message ?: str(R.string.referrals_error_apply_code)
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }
}

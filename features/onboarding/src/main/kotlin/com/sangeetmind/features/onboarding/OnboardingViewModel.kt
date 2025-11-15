package com.sangeetmind.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentPage: Int = 0,
    val hasAcceptedTerms: Boolean = false,
    val hasAcceptedPrivacy: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    // TODO: Inject PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun nextPage() {
        _uiState.update { it.copy(currentPage = it.currentPage + 1) }
    }

    fun previousPage() {
        _uiState.update { it.copy(currentPage = maxOf(0, it.currentPage - 1)) }
    }

    fun setAcceptedTerms(accepted: Boolean) {
        _uiState.update { it.copy(hasAcceptedTerms = accepted) }
    }

    fun setAcceptedPrivacy(accepted: Boolean) {
        _uiState.update { it.copy(hasAcceptedPrivacy = accepted) }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            // TODO: Save onboarding completion to DataStore
        }
    }
}


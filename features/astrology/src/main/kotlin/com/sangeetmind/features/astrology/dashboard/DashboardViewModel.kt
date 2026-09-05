package com.sangeetmind.features.astrology.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.features.astrology.profile.AstroProfileRepository
import com.sangeetmind.libs.models.AstroProfileSummary
import com.sangeetmind.libs.models.Kundli
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val primaryKundli: Kundli? = null,
    val hasNoKundlis: Boolean = false,
    val profile: AstroProfileSummary? = null,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val kundliRepository: KundliRepository,
    private val astroProfileRepository: AstroProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val kundliResult = kundliRepository.listKundlis()) {
                is Result.Success -> {
                    val primary = kundliResult.data.firstOrNull { it.isPrimary }
                        ?: kundliResult.data.firstOrNull()
                    if (primary == null) {
                        _uiState.update {
                            it.copy(isLoading = false, hasNoKundlis = true, primaryKundli = null, profile = null)
                        }
                        return@launch
                    }
                    _uiState.update { it.copy(primaryKundli = primary, hasNoKundlis = false) }
                    when (val profileResult = astroProfileRepository.getProfile(primary)) {
                        is Result.Success -> _uiState.update {
                            it.copy(isLoading = false, profile = profileResult.data)
                        }
                        is Result.Error -> _uiState.update {
                            it.copy(isLoading = false, error = profileResult.message)
                        }
                        is Result.Loading -> Unit
                    }
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = kundliResult.message ?: "Failed to load dashboard")
                }
                is Result.Loading -> Unit
            }
        }
    }
}

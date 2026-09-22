package com.sangeetmind.features.astrology.varshaphal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.VarshaphalResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Year
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VarshaphalUiState(
    val isLoading: Boolean = true,
    val hasNoKundli: Boolean = false,
    val kundli: Kundli? = null,
    val year: Int = Year.now().value,
    val varshaphal: VarshaphalResponse? = null,
    val error: String? = null
)

@HiltViewModel
class VarshaphalViewModel @Inject constructor(
    private val varshaphalRepository: VarshaphalRepository,
    private val kundliRepository: KundliRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VarshaphalUiState())
    val uiState: StateFlow<VarshaphalUiState> = _uiState.asStateFlow()

    private var loadedKundli: Kundli? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, hasNoKundli = false) }
            val kundli = loadedKundli ?: when (val listResult = kundliRepository.listKundlis()) {
                is Result.Success -> {
                    val primary = listResult.data.firstOrNull { it.isPrimary }
                        ?: listResult.data.firstOrNull()
                    if (primary == null) {
                        _uiState.update { it.copy(isLoading = false, hasNoKundli = true, kundli = null) }
                        return@launch
                    }
                    loadedKundli = primary
                    primary
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = listResult.message) }
                    return@launch
                }
                is Result.Loading -> return@launch
            }
            _uiState.update { it.copy(kundli = kundli) }
            when (val result = varshaphalRepository.getVarshaphal(kundli, _uiState.value.year)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, varshaphal = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun selectYear(year: Int) {
        _uiState.update { it.copy(year = year) }
        refresh()
    }
}

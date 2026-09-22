package com.sangeetmind.features.astrology.holistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.libs.models.HolisticCombinedResponse
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.toTitleCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HolisticUiState(
    val isLoading: Boolean = true,
    val hasNoKundli: Boolean = false,
    /** Numerology needs a birth name (backend requires >= 2 chars); the kundli has none. */
    val needsName: Boolean = false,
    val kundli: Kundli? = null,
    val language: String = "en",
    val analysis: HolisticCombinedResponse? = null,
    val error: String? = null
)

@HiltViewModel
class HolisticViewModel @Inject constructor(
    private val holisticRepository: HolisticRepository,
    private val kundliRepository: KundliRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HolisticUiState())
    val uiState: StateFlow<HolisticUiState> = _uiState.asStateFlow()

    private var loadedKundli: Kundli? = null

    init {
        refresh()
    }

    /** Full re-fetch. Also what "Regenerate" does: the narrative is not cached server-side,
     * so every POST asks Gemini afresh. */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, hasNoKundli = false, needsName = false) }
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
            // Title-cased because the Gemini/template narrative addresses the person by this
            // string verbatim ("Dear nicky jha" otherwise); letter values are case-insensitive.
            val name = kundli.fullName?.trim()?.toTitleCase().orEmpty()
            if (name.length < 2) {
                _uiState.update { it.copy(isLoading = false, needsName = true) }
                return@launch
            }
            when (val result = holisticRepository.getCombined(kundli, name, _uiState.value.language)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, analysis = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun selectLanguage(language: String) {
        if (language == _uiState.value.language) return
        _uiState.update { it.copy(language = language) }
        refresh()
    }
}

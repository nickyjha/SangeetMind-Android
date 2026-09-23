package com.sangeetmind.features.astrology.interpretation

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.kundli.KundliRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InterpretationUiState(
    val isLoading: Boolean = false,
    val data: InterpretationData? = null,
    val hasNoKundli: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class InterpretationViewModel @Inject constructor(
    private val kundliRepository: KundliRepository,
    private val interpretationRepository: InterpretationRepository,
    private val languageManager: LanguageManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(InterpretationUiState())
    val uiState: StateFlow<InterpretationUiState> = _uiState.asStateFlow()

    private fun str(@StringRes id: Int): String =
        context.withAppLanguage(languageManager.current).getString(id)

    init {
        refresh()
        viewModelScope.launch {
            // The analysis text is fetched in a language; reload it when the app language
            // changes (skip the initial emission — refresh() above already covers it).
            languageManager.language.drop(1).collect { refresh() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val kundliResult = kundliRepository.listKundlis()) {
                is Result.Success -> {
                    val primary = kundliResult.data.firstOrNull { it.isPrimary }
                        ?: kundliResult.data.firstOrNull()
                    if (primary == null) {
                        _uiState.update { it.copy(isLoading = false, hasNoKundli = true) }
                        return@launch
                    }
                    when (val result = interpretationRepository.getInterpretation(primary)) {
                        is Result.Success -> _uiState.update {
                            it.copy(isLoading = false, data = result.data, hasNoKundli = false)
                        }
                        is Result.Error -> _uiState.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                        is Result.Loading -> Unit
                    }
                }
                is Result.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = kundliResult.message ?: str(R.string.interpretation_err_load)
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }
}

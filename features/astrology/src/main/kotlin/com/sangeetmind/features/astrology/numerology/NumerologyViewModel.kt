package com.sangeetmind.features.astrology.numerology

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.libs.models.ChaldeanResponse
import com.sangeetmind.libs.models.NumerologyResponse
import com.sangeetmind.libs.models.VedicResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NumerologySystemUi(@StringRes val labelRes: Int) {
    PYTHAGOREAN(R.string.numerology_system_pythagorean),
    CHALDEAN(R.string.numerology_system_chaldean),
    VEDIC(R.string.numerology_system_vedic)
}

data class NumerologyUiState(
    val fullName: String = "",
    val dateOfBirth: String = "",
    val currentName: String = "",
    val gender: String = "male",
    val system: NumerologySystemUi = NumerologySystemUi.PYTHAGOREAN,
    val isLoading: Boolean = false,
    val pythagoreanResult: NumerologyResponse? = null,
    val chaldeanResult: ChaldeanResponse? = null,
    val vedicResult: VedicResponse? = null,
    val error: String? = null
)

@HiltViewModel
class NumerologyViewModel @Inject constructor(
    private val numerologyRepository: NumerologyRepository,
    private val kundliRepository: KundliRepository,
    private val languageManager: LanguageManager,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(NumerologyUiState())
    val uiState: StateFlow<NumerologyUiState> = _uiState.asStateFlow()

    private fun str(@StringRes id: Int): String =
        appContext.withAppLanguage(languageManager.current).getString(id)

    init {
        prefillFromPrimaryKundli()
    }

    private fun prefillFromPrimaryKundli() {
        viewModelScope.launch {
            val result = kundliRepository.listKundlis()
            if (result is Result.Success) {
                val primary = result.data.firstOrNull { it.isPrimary } ?: result.data.firstOrNull()
                if (primary != null) {
                    _uiState.update {
                        it.copy(
                            fullName = primary.fullName ?: it.fullName,
                            dateOfBirth = primary.birthDate
                        )
                    }
                }
            }
        }
    }

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, error = null) }
    }

    fun onDateOfBirthChange(value: String) {
        _uiState.update { it.copy(dateOfBirth = value, error = null) }
    }

    fun onCurrentNameChange(value: String) {
        _uiState.update { it.copy(currentName = value, error = null) }
    }

    fun onGenderChange(value: String) {
        _uiState.update { it.copy(gender = value) }
    }

    fun onSystemChange(system: NumerologySystemUi) {
        _uiState.update { it.copy(system = system, error = null) }
    }

    fun analyze() {
        val state = _uiState.value
        if (state.fullName.isBlank()) {
            _uiState.update { it.copy(error = str(R.string.numerology_error_enter_name)) }
            return
        }
        if (state.dateOfBirth.isBlank()) {
            _uiState.update { it.copy(error = str(R.string.numerology_error_enter_dob)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (state.system) {
                NumerologySystemUi.PYTHAGOREAN -> {
                    when (val result = numerologyRepository.analyzePythagorean(state.fullName, state.dateOfBirth)) {
                        is Result.Success -> _uiState.update {
                            it.copy(isLoading = false, pythagoreanResult = result.data)
                        }
                        is Result.Error -> _uiState.update {
                            it.copy(isLoading = false, error = result.message ?: str(R.string.numerology_error_calc_failed))
                        }
                        is Result.Loading -> Unit
                    }
                }
                NumerologySystemUi.CHALDEAN -> {
                    when (
                        val result = numerologyRepository.analyzeChaldean(
                            state.fullName,
                            state.dateOfBirth,
                            state.currentName
                        )
                    ) {
                        is Result.Success -> _uiState.update {
                            it.copy(isLoading = false, chaldeanResult = result.data)
                        }
                        is Result.Error -> _uiState.update {
                            it.copy(isLoading = false, error = result.message ?: str(R.string.numerology_error_calc_failed))
                        }
                        is Result.Loading -> Unit
                    }
                }
                NumerologySystemUi.VEDIC -> {
                    when (
                        val result = numerologyRepository.analyzeVedic(
                            state.fullName,
                            state.dateOfBirth,
                            state.gender
                        )
                    ) {
                        is Result.Success -> _uiState.update {
                            it.copy(isLoading = false, vedicResult = result.data)
                        }
                        is Result.Error -> _uiState.update {
                            it.copy(isLoading = false, error = result.message ?: str(R.string.numerology_error_calc_failed))
                        }
                        is Result.Loading -> Unit
                    }
                }
            }
        }
    }
}

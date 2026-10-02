package com.sangeetmind.features.astrology.prashna

import android.content.Context
import com.sangeetmind.core.network.friendlyErrorMessage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.core.network.PanchangApi
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.PrashnaRequest
import com.sangeetmind.libs.models.PrashnaResponse
import com.sangeetmind.libs.models.RulingPlanetsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

val PRASHNA_QUESTIONS = listOf("marriage", "job", "property", "abroad", "children", "exam")

data class PrashnaUiState(
    val place: Kundli? = null,
    val ruling: RulingPlanetsResponse? = null,
    val question: String = "marriage",
    val number: String = "",
    val isAsking: Boolean = false,
    val answer: PrashnaResponse? = null,
    val error: String? = null,
    /** True when [error] came from the server (offer "Try again"), false for input errors. */
    val errorRetryable: Boolean = false
)

/** KP horary. Times and houses use the primary kundli's place (Delhi when there is none). */
@HiltViewModel
class PrashnaViewModel @Inject constructor(
    private val panchangApi: PanchangApi,
    private val kundliRepository: KundliRepository,
    @ApplicationContext private val appContext: Context,
    private val languageManager: LanguageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrashnaUiState())
    val uiState: StateFlow<PrashnaUiState> = _uiState.asStateFlow()

    private fun str(id: Int): String = appContext.withAppLanguage(languageManager.current).getString(id)

    init {
        viewModelScope.launch {
            val place = when (val kundlis = kundliRepository.listKundlis()) {
                is Result.Success -> kundlis.data.firstOrNull { it.isPrimary } ?: kundlis.data.firstOrNull()
                else -> null
            }
            _uiState.update { it.copy(place = place) }
            runCatching {
                panchangApi.getRulingPlanets(
                    lat = place?.latitude ?: 28.6139,
                    lon = place?.longitude ?: 77.209,
                    tz = place?.timezone?.ifBlank { null } ?: "Asia/Kolkata"
                )
            }.onSuccess { rp -> _uiState.update { it.copy(ruling = rp) } }
        }
    }

    fun setQuestion(question: String) = _uiState.update { it.copy(question = question, answer = null) }

    fun setNumber(value: String) = _uiState.update { it.copy(number = value.filter(Char::isDigit).take(3), error = null) }

    fun ask() {
        val state = _uiState.value
        val n = state.number.toIntOrNull()
        if (n == null || n !in 1..249) {
            _uiState.update { it.copy(error = str(R.string.prashna_error_number), errorRetryable = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isAsking = true, error = null) }
            val place = state.place
            runCatching {
                panchangApi.askPrashna(
                    PrashnaRequest(
                        number = n,
                        question = state.question,
                        lat = place?.latitude ?: 28.6139,
                        lon = place?.longitude ?: 77.209,
                        tz = place?.timezone?.ifBlank { null } ?: "Asia/Kolkata"
                    )
                )
            }.onSuccess { r -> _uiState.update { it.copy(isAsking = false, answer = r) } }
                .onFailure { e ->
                    val message = friendlyErrorMessage(
                        e,
                        appContext.withAppLanguage(languageManager.current),
                        str(R.string.prashna_error_failed)
                    )
                    _uiState.update { it.copy(isAsking = false, error = message, errorRetryable = true) }
                }
        }
    }
}

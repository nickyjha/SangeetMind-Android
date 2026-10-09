package com.sangeetmind.features.astrology.accuracy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.network.AccuracySurveyApi
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.SurveyAnswers
import com.sangeetmind.libs.models.SurveyBirth
import com.sangeetmind.libs.models.SurveyRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Must match the backend's CONSENT_VERSION (app/routes/accuracy_survey_routes.py). */
const val SURVEY_CONSENT_VERSION = "2026-10-10"

data class AccuracySurveyUiState(
    val loading: Boolean = true,
    val submitted: Boolean = false,
    val consent: Boolean = false,
    val answers: SurveyAnswers = SurveyAnswers(),
    val saving: Boolean = false,
    /** "saved" | "withdrawn" | "error" | "no_kundli" | null */
    val message: String? = null
)

@HiltViewModel
class AccuracySurveyViewModel @Inject constructor(
    private val api: AccuracySurveyApi,
    private val kundliRepository: KundliRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AccuracySurveyUiState())
    val state: StateFlow<AccuracySurveyUiState> = _state.asStateFlow()
    private var kundli: Kundli? = null

    init {
        viewModelScope.launch {
            kundli = (kundliRepository.listKundlis() as? Result.Success)?.data
                ?.let { list -> list.firstOrNull { it.isPrimary } ?: list.firstOrNull() }
            val status = runCatching { api.get() }.getOrNull()
            _state.update {
                it.copy(
                    loading = false,
                    submitted = status?.submitted == true,
                    consent = status?.submitted == true,
                    answers = status?.answers ?: SurveyAnswers(),
                    message = if (kundli == null) "no_kundli" else null
                )
            }
        }
    }

    fun setConsent(value: Boolean) = _state.update { it.copy(consent = value, message = null) }

    fun update(transform: (SurveyAnswers) -> SurveyAnswers) =
        _state.update { it.copy(answers = transform(it.answers), message = null) }

    fun save() {
        val k = kundli ?: return _state.update { it.copy(message = "no_kundli") }
        if (!_state.value.consent) return
        viewModelScope.launch {
            _state.update { it.copy(saving = true, message = null) }
            val ok = runCatching {
                api.submit(
                    SurveyRequest(
                        consent = true,
                        consentVersion = SURVEY_CONSENT_VERSION,
                        birth = SurveyBirth(
                            date = k.birthDate,
                            time = k.birthTime,
                            timezone = k.timezone,
                            lat = k.latitude,
                            lon = k.longitude
                        ),
                        answers = _state.value.answers
                    )
                ).saved
            }.getOrDefault(false)
            _state.update {
                it.copy(saving = false, submitted = it.submitted || ok, message = if (ok) "saved" else "error")
            }
        }
    }

    fun withdraw() {
        viewModelScope.launch {
            _state.update { it.copy(saving = true, message = null) }
            val ok = runCatching { api.withdraw().deleted }.getOrDefault(false)
            _state.update {
                if (ok) AccuracySurveyUiState(loading = false, message = "withdrawn")
                else it.copy(saving = false, message = "error")
            }
        }
    }
}

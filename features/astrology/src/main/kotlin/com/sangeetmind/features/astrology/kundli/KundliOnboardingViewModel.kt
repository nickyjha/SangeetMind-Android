package com.sangeetmind.features.astrology.kundli

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.network.NominatimApi
import com.sangeetmind.core.network.NominatimPlace
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.toTitleCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BirthPlaceSuggestion(
    val label: String,
    val latitude: Double,
    val longitude: Double
)

data class KundliOnboardingUiState(
    val fullName: String = "",
    val birthDate: String = "", // YYYY-MM-DD
    val birthTime: String = "", // HH:MM
    val birthPlaceQuery: String = "",
    val placeSuggestions: List<BirthPlaceSuggestion> = emptyList(),
    val selectedPlace: BirthPlaceSuggestion? = null,
    val isSearchingPlace: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

/**
 * Birth-place autocomplete via OSM Nominatim (same as the website's geocodePlace.ts).
 * No client-side timezone lookup: the backend infers the IANA timezone from lat/lon
 * server-side when timezone is omitted, so we never send one from here.
 */
@HiltViewModel
class KundliOnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val kundliRepository: KundliRepository,
    private val nominatimApi: NominatimApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(KundliOnboardingUiState())
    val uiState: StateFlow<KundliOnboardingUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, error = null) }
    }

    fun onBirthDateChange(value: String) {
        _uiState.update { it.copy(birthDate = value, error = null) }
    }

    fun onBirthTimeChange(value: String) {
        _uiState.update { it.copy(birthTime = value, error = null) }
    }

    fun onBirthPlaceQueryChange(query: String) {
        _uiState.update { it.copy(birthPlaceQuery = query, selectedPlace = null, error = null) }
        searchJob?.cancel()
        if (query.length < 3) {
            _uiState.update { it.copy(placeSuggestions = emptyList(), isSearchingPlace = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(400) // debounce, be gentle with Nominatim's public endpoint
            _uiState.update { it.copy(isSearchingPlace = true) }
            val results = runCatching { nominatimApi.search(query) }.getOrDefault(emptyList())
            _uiState.update {
                it.copy(
                    isSearchingPlace = false,
                    placeSuggestions = results.map { place -> place.toSuggestion() }
                )
            }
        }
    }

    fun onPlaceSelected(suggestion: BirthPlaceSuggestion) {
        _uiState.update {
            it.copy(
                birthPlaceQuery = suggestion.label,
                selectedPlace = suggestion,
                placeSuggestions = emptyList()
            )
        }
    }

    fun save() {
        val state = _uiState.value
        val place = state.selectedPlace

        if (state.birthDate.isBlank()) {
            _uiState.update { it.copy(error = context.getString(R.string.kundli_error_dob_required)) }
            return
        }
        if (state.birthTime.isBlank()) {
            _uiState.update { it.copy(error = context.getString(R.string.kundli_error_tob_required)) }
            return
        }
        val normalizedTime = normalizeBirthTime(state.birthTime)
        if (normalizedTime == null) {
            _uiState.update { it.copy(error = context.getString(R.string.kundli_error_tob_format)) }
            return
        }
        if (place == null) {
            _uiState.update { it.copy(error = context.getString(R.string.kundli_error_place_required)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = kundliRepository.createKundli(
                fullName = state.fullName.ifBlank { null }?.toTitleCase(),
                birthDate = state.birthDate,
                birthTime = normalizedTime,
                birthPlace = place.label,
                latitude = place.latitude,
                longitude = place.longitude,
                timezone = null
            )
            when (result) {
                is Result.Success -> _uiState.update { it.copy(isSaving = false, saved = true) }
                is Result.Error -> _uiState.update {
                    it.copy(isSaving = false, error = result.message ?: context.getString(R.string.kundli_error_save_failed))
                }
                is Result.Loading -> Unit
            }
        }
    }
}

private val birthTimeRegex = Regex("""^([01]?\d|2[0-3]):([0-5]\d)$""")

/** Backend requires a zero-padded HH:MM (Pydantic time parsing rejects e.g. "6:35"). */
private fun normalizeBirthTime(raw: String): String? {
    val match = birthTimeRegex.matchEntire(raw.trim()) ?: return null
    val (hour, minute) = match.destructured
    return "%02d:%s".format(hour.toInt(), minute)
}

private fun NominatimPlace.toSuggestion() = BirthPlaceSuggestion(
    label = displayName,
    latitude = lat.toDoubleOrNull() ?: 0.0,
    longitude = lon.toDoubleOrNull() ?: 0.0
)

package com.sangeetmind.features.astrology.panchang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.network.NominatimApi
import com.sangeetmind.core.network.NominatimPlace
import com.sangeetmind.features.astrology.kundli.BirthPlaceSuggestion
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.libs.models.PanchangResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class PanchangUiState(
    val date: String = todayIso(),
    val placeQuery: String = "",
    val placeSuggestions: List<BirthPlaceSuggestion> = emptyList(),
    val isSearchingPlace: Boolean = false,
    val selectedPlace: BirthPlaceSuggestion? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: PanchangResponse? = null
)

private fun todayIso(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

/**
 * Defaults location to the user's primary kundli's birth place when available,
 * matching PanchangPage's fallback pattern on the website (it defaults to London;
 * here we prefer the user's own saved kundli, falling back to Delhi like the
 * kundli-onboarding flow does elsewhere in this module).
 */
@HiltViewModel
class PanchangViewModel @Inject constructor(
    private val panchangRepository: PanchangRepository,
    private val kundliRepository: KundliRepository,
    private val nominatimApi: NominatimApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(PanchangUiState())
    val uiState: StateFlow<PanchangUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        prefillFromPrimaryKundli()
    }

    private fun prefillFromPrimaryKundli() {
        viewModelScope.launch {
            when (val result = kundliRepository.listKundlis()) {
                is Result.Success -> {
                    val primary = result.data.firstOrNull { it.isPrimary } ?: result.data.firstOrNull()
                    if (primary != null) {
                        val suggestion = BirthPlaceSuggestion(
                            label = primary.birthPlace,
                            latitude = primary.latitude,
                            longitude = primary.longitude
                        )
                        _uiState.update { it.copy(placeQuery = suggestion.label, selectedPlace = suggestion) }
                        fetchPanchang()
                    }
                }
                else -> Unit
            }
        }
    }

    fun onDateChange(value: String) {
        _uiState.update { it.copy(date = value, error = null) }
    }

    fun onPlaceQueryChange(query: String) {
        _uiState.update { it.copy(placeQuery = query, selectedPlace = null, error = null) }
        searchJob?.cancel()
        if (query.length < 3) {
            _uiState.update { it.copy(placeSuggestions = emptyList(), isSearchingPlace = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(400)
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
            it.copy(placeQuery = suggestion.label, selectedPlace = suggestion, placeSuggestions = emptyList())
        }
    }

    fun fetchPanchang() {
        val state = _uiState.value
        val place = state.selectedPlace
        if (state.date.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a date") }
            return
        }
        if (place == null) {
            _uiState.update { it.copy(error = "Please pick a place from the suggestions") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = panchangRepository.getPanchang(state.date, place.latitude, place.longitude)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, result = result.data) }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message ?: "Failed to load panchang")
                }
                is Result.Loading -> Unit
            }
        }
    }
}

private fun NominatimPlace.toSuggestion() = BirthPlaceSuggestion(
    label = displayName,
    latitude = lat.toDoubleOrNull() ?: 0.0,
    longitude = lon.toDoubleOrNull() ?: 0.0
)

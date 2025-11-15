package com.sangeetmind.features.astrology

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.libs.models.AstrologyProfile
import com.sangeetmind.libs.models.AstrologyRecommendation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AstrologyUiState(
    val name: String = "",
    val dateOfBirth: String = "",
    val timeOfBirth: String = "",
    val placeOfBirth: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timezone: String = "Asia/Kolkata",
    val isGenerating: Boolean = false,
    val recommendation: AstrologyRecommendation? = null,
    val error: String? = null,
    val validationError: String? = null
)

@HiltViewModel
class AstrologyViewModel @Inject constructor(
    private val repository: AstrologyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AstrologyUiState())
    val uiState: StateFlow<AstrologyUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, validationError = null) }
    }

    fun onDateOfBirthChange(date: String) {
        _uiState.update { it.copy(dateOfBirth = date, validationError = null) }
    }

    fun onTimeOfBirthChange(time: String) {
        _uiState.update { it.copy(timeOfBirth = time, validationError = null) }
    }

    fun onPlaceOfBirthChange(place: String) {
        _uiState.update { it.copy(placeOfBirth = place, validationError = null) }
    }

    fun onCoordinatesChange(latitude: Double, longitude: Double) {
        _uiState.update { it.copy(latitude = latitude, longitude = longitude) }
    }

    fun generateRecommendation() {
        val state = _uiState.value
        
        // Validate inputs
        when {
            state.name.isBlank() -> {
                _uiState.update { it.copy(validationError = "Please enter your name") }
                return
            }
            state.dateOfBirth.isBlank() -> {
                _uiState.update { it.copy(validationError = "Please enter your date of birth") }
                return
            }
            state.timeOfBirth.isBlank() -> {
                _uiState.update { it.copy(validationError = "Please enter your time of birth") }
                return
            }
            state.placeOfBirth.isBlank() -> {
                _uiState.update { it.copy(validationError = "Please enter your place of birth") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null, validationError = null) }
            
            val profile = AstrologyProfile(
                name = state.name,
                dateOfBirth = state.dateOfBirth,
                timeOfBirth = state.timeOfBirth,
                placeOfBirth = state.placeOfBirth,
                latitude = state.latitude ?: 28.6139, // Default to Delhi
                longitude = state.longitude ?: 77.2090,
                timezone = state.timezone
            )
            
            when (val result = repository.generateRecommendation(profile)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            recommendation = result.data,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            error = result.message ?: "Failed to generate recommendation"
                        )
                    }
                }
                is Result.Loading -> {
                    // Already handled
                }
            }
        }
    }

    fun clearRecommendation() {
        _uiState.update { it.copy(recommendation = null, error = null) }
    }

    fun clearForm() {
        _uiState.value = AstrologyUiState()
    }
}


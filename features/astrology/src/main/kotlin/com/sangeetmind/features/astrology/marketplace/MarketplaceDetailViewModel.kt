package com.sangeetmind.features.astrology.marketplace

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.Astrologer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketplaceDetailUiState(
    val isLoading: Boolean = false,
    val astrologer: Astrologer? = null,
    val reviewSubmitted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MarketplaceDetailViewModel @Inject constructor(
    private val repository: MarketplaceRepository,
    @ApplicationContext private val context: Context,
    private val languageManager: LanguageManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val astrologerId: String = checkNotNull(savedStateHandle["astrologerId"])

    private val _uiState = MutableStateFlow(MarketplaceDetailUiState())
    val uiState: StateFlow<MarketplaceDetailUiState> = _uiState.asStateFlow()

    private fun str(@StringRes id: Int): String =
        context.withAppLanguage(languageManager.current).getString(id)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getAstrologer(astrologerId)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, astrologer = result.data) }
                is Result.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.message ?: str(R.string.marketplace_error_load_astrologer)
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun submitReview(rating: Int, comment: String) {
        viewModelScope.launch {
            when (val result = repository.submitReview(astrologerId, rating, comment)) {
                is Result.Success -> _uiState.update { it.copy(reviewSubmitted = true) }
                is Result.Error -> _uiState.update {
                    it.copy(error = result.message ?: str(R.string.marketplace_error_submit_review))
                }
                is Result.Loading -> Unit
            }
        }
    }
}

package com.sangeetmind.features.astrology.match

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.KundliMatchResult
import com.sangeetmind.libs.models.RelationMatchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** "marriage" runs the classical Ashtakoot; the others go to /v1/match/relation. */
val MATCH_RELATIONS = listOf("marriage", "parent_child", "siblings", "business", "friends")

data class MatchUiState(
    val relation: String = "marriage",
    val relationResult: RelationMatchResult? = null,
    val isLoadingKundlis: Boolean = false,
    val kundlis: List<Kundli> = emptyList(),
    val personA: Kundli? = null,
    val personB: Kundli? = null,
    val isMatching: Boolean = false,
    val result: KundliMatchResult? = null,
    val error: String? = null
)

@HiltViewModel
class MatchViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val languageManager: LanguageManager,
    private val kundliRepository: KundliRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchUiState())
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    private fun str(@StringRes id: Int): String =
        appContext.withAppLanguage(languageManager.current).getString(id)

    init {
        loadKundlis()
    }

    fun loadKundlis() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingKundlis = true, error = null) }
            when (val result = kundliRepository.listKundlis()) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoadingKundlis = false, kundlis = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoadingKundlis = false, error = result.message ?: str(R.string.match_error_load_kundlis))
                }
                is Result.Loading -> Unit
            }
        }
    }

    /** Retry from the error card: reload kundlis if that failed, otherwise re-run the match. */
    fun retry() {
        val state = _uiState.value
        when {
            state.kundlis.isEmpty() -> loadKundlis()
            state.personA != null && state.personB != null -> checkCompatibility()
            else -> _uiState.update { it.copy(error = null) }
        }
    }

    fun selectPersonA(kundli: Kundli) {
        _uiState.update { it.copy(personA = kundli, result = null, relationResult = null, error = null) }
    }

    fun selectPersonB(kundli: Kundli) {
        _uiState.update { it.copy(personB = kundli, result = null, relationResult = null, error = null) }
    }

    fun setRelation(relation: String) {
        _uiState.update { it.copy(relation = relation, result = null, relationResult = null, error = null) }
    }

    fun checkCompatibility() {
        val state = _uiState.value
        val a = state.personA
        val b = state.personB
        if (a == null || b == null) {
            _uiState.update { it.copy(error = str(R.string.match_error_pick_both)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isMatching = true, error = null) }
            if (state.relation != "marriage") {
                when (val result = matchRepository.matchRelation(a, b, state.relation)) {
                    is Result.Success -> _uiState.update {
                        it.copy(isMatching = false, relationResult = result.data)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(isMatching = false, error = result.message ?: str(R.string.match_error_compute))
                    }
                    is Result.Loading -> Unit
                }
                return@launch
            }
            when (val result = matchRepository.matchKundlis(a, b)) {
                is Result.Success -> _uiState.update {
                    it.copy(isMatching = false, result = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isMatching = false, error = result.message ?: str(R.string.match_error_compute))
                }
                is Result.Loading -> Unit
            }
        }
    }
}

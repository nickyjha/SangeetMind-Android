package com.sangeetmind.features.astrology.muhurat

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
import com.sangeetmind.libs.models.MuhuratSlot
import com.sangeetmind.libs.models.VivahResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

internal const val DEFAULT_WINDOW_DAYS = 30L

/**
 * New start date for the window; if the current end would fall before it, the end moves
 * to start + [DEFAULT_WINDOW_DAYS] so the window is never inverted.
 */
internal fun adjustWindowForStart(start: String, end: String): Pair<String, String> {
    val s = runCatching { LocalDate.parse(start) }.getOrNull() ?: return start to end
    val e = runCatching { LocalDate.parse(end) }.getOrNull()
    return if (e == null || e.isBefore(s)) start to s.plusDays(DEFAULT_WINDOW_DAYS).toString() else start to end
}

val MUHURAT_INTENTS = listOf("general", "marriage", "business", "travel", "griha pravesh")

data class MuhuratUiState(
    val intent: String = "general",
    // ISO yyyy-MM-dd. Defaults to the next 30 days so the search works without picking dates.
    val windowStart: String = LocalDate.now().toString(),
    val windowEnd: String = LocalDate.now().plusDays(DEFAULT_WINDOW_DAYS).toString(),
    val isSearching: Boolean = false,
    val results: List<MuhuratSlot> = emptyList(),
    // Marriage uses the vivah endpoint: windows, blocked periods, optional couple check.
    val vivah: VivahResponse? = null,
    val kundlis: List<Kundli> = emptyList(),
    val coupleIds: Set<String> = emptySet(),
    val error: String? = null
) {
    val isMarriage: Boolean get() = intent == "marriage"
}

@HiltViewModel
class MuhuratViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val languageManager: LanguageManager,
    private val muhuratRepository: MuhuratRepository,
    private val kundliRepository: KundliRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MuhuratUiState())
    val uiState: StateFlow<MuhuratUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val result = kundliRepository.listKundlis()
            if (result is Result.Success) _uiState.update { it.copy(kundlis = result.data) }
        }
    }

    /** Toggle a kundli for the couple check; at most two. */
    fun toggleCouple(id: String) {
        _uiState.update {
            val ids = it.coupleIds
            it.copy(coupleIds = if (id in ids) ids - id else if (ids.size < 2) ids + id else ids)
        }
    }

    private fun str(@StringRes id: Int): String =
        appContext.withAppLanguage(languageManager.current).getString(id)

    fun onIntentChange(intent: String) {
        _uiState.update { it.copy(intent = intent, error = null) }
    }

    fun onWindowStartChange(value: String) {
        _uiState.update {
            val (start, end) = adjustWindowForStart(value, it.windowEnd)
            it.copy(windowStart = start, windowEnd = end, error = null)
        }
    }

    fun onWindowEndChange(value: String) {
        _uiState.update { it.copy(windowEnd = value, error = null) }
    }

    fun findMuhurat() {
        val state = _uiState.value
        if (state.windowStart.isBlank() || state.windowEnd.isBlank()) {
            _uiState.update { it.copy(error = str(R.string.muhurat_error_enter_dates)) }
            return
        }

        if (state.isMarriage) return findVivah(state)
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, error = null) }
            when (val result = muhuratRepository.findMuhurat(state.intent, state.windowStart, state.windowEnd)) {
                is Result.Success -> _uiState.update {
                    it.copy(isSearching = false, results = result.data.results)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isSearching = false, error = result.message ?: str(R.string.muhurat_error_find_failed))
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun findVivah(state: MuhuratUiState) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, error = null) }
            val place = state.kundlis.firstOrNull { it.isPrimary } ?: state.kundlis.firstOrNull()
            val couple = state.kundlis.filter { it.id in state.coupleIds }
            when (val result = muhuratRepository.findVivah(state.windowStart, state.windowEnd, place, couple)) {
                is Result.Success -> _uiState.update { it.copy(isSearching = false, vivah = result.data) }
                is Result.Error -> _uiState.update {
                    it.copy(isSearching = false, error = result.message ?: str(R.string.muhurat_error_find_failed))
                }
                is Result.Loading -> Unit
            }
        }
    }
}

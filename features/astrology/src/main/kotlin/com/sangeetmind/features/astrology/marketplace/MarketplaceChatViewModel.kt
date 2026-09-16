package com.sangeetmind.features.astrology.marketplace

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.libs.models.Astrologer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatMessage(val fromMe: Boolean, val text: String)

data class MarketplaceChatUiState(
    val astrologer: Astrologer? = null,
    val sessionActive: Boolean = false,
    val minutesBilled: Int = 0,
    val totalPaise: Long = 0,
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = "",
    val error: String? = null
)

/**
 * NOTE: there is no real-time messaging backend yet — /v1/marketplace/chat/debit-minute is
 * purely a per-minute billing meter (see marketplace_routes.py). Messages here are local-only
 * (appended to an in-memory list, never sent to the astrologer); only the per-minute wallet
 * debit while a session is "active" is real.
 */
@HiltViewModel
class MarketplaceChatViewModel @Inject constructor(
    private val repository: MarketplaceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val astrologerId: String = checkNotNull(savedStateHandle["astrologerId"])

    private val _uiState = MutableStateFlow(MarketplaceChatUiState())
    val uiState: StateFlow<MarketplaceChatUiState> = _uiState.asStateFlow()

    private var billingJob: Job? = null

    init {
        viewModelScope.launch {
            when (val result = repository.getAstrologer(astrologerId)) {
                is Result.Success -> _uiState.update { it.copy(astrologer = result.data) }
                is Result.Error -> _uiState.update { it.copy(error = result.message) }
                is Result.Loading -> Unit
            }
        }
    }

    fun startSession() {
        if (_uiState.value.sessionActive) return
        _uiState.update {
            it.copy(
                sessionActive = true,
                messages = it.messages + ChatMessage(
                    fromMe = false,
                    text = "Session started — you're now being billed per minute."
                )
            )
        }
        billingJob = viewModelScope.launch {
            while (true) {
                delay(60_000)
                billOneMinute()
            }
        }
    }

    fun endSession() {
        billingJob?.cancel()
        billingJob = null
        _uiState.update {
            it.copy(
                sessionActive = false,
                messages = it.messages + ChatMessage(fromMe = false, text = "Session ended.")
            )
        }
    }

    private suspend fun billOneMinute() {
        when (val result = repository.debitChatMinute(astrologerId, UUID.randomUUID().toString())) {
            is Result.Success -> {
                if (!result.data.applied) {
                    // Insufficient balance / premium check failed server-side — stop the meter.
                    endSession()
                    _uiState.update { it.copy(error = "Session ended: insufficient balance") }
                } else {
                    _uiState.update {
                        it.copy(
                            minutesBilled = it.minutesBilled + 1,
                            totalPaise = it.totalPaise + result.data.amountPaise
                        )
                    }
                }
            }
            is Result.Error -> {
                endSession()
                _uiState.update { it.copy(error = result.message ?: "Billing failed, session ended") }
            }
            is Result.Loading -> Unit
        }
    }

    fun onDraftChange(text: String) {
        _uiState.update { it.copy(draft = text) }
    }

    fun sendDraft() {
        val text = _uiState.value.draft.trim()
        if (text.isEmpty()) return
        _uiState.update {
            it.copy(messages = it.messages + ChatMessage(fromMe = true, text = text), draft = "")
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingJob?.cancel()
    }
}

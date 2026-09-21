package com.sangeetmind.features.astrology.chatmind

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.features.astrology.payments.PaymentsRepository
import com.sangeetmind.libs.models.LlmBirthDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToLong

enum class ChatRole { USER, ASSISTANT }

/** [tier] is the model tier that produced an ASSISTANT reply ("standard"/"advanced"); null for USER messages. */
data class ChatMessage(val role: ChatRole, val text: String, val tier: String? = null)

data class ChatMindUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isSending: Boolean = false,
    val error: String? = null,
    /** Mirrors the website's "Advanced analysis" toggle — off (standard/gemini-2.5-flash-lite)
     * by default, since advanced (gemini-2.5-pro) costs more per reply from the user's wallet. */
    val advancedAnalysis: Boolean = false
)

@HiltViewModel
class ChatMindViewModel @Inject constructor(
    private val repository: ChatMindRepository,
    private val paymentsRepository: PaymentsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatMindUiState())
    val uiState: StateFlow<ChatMindUiState> = _uiState.asStateFlow()

    private var birthDetails: LlmBirthDetails? = null

    fun onInputChange(value: String) {
        _uiState.update { it.copy(input = value, error = null) }
    }

    fun setAdvancedAnalysis(enabled: Boolean) {
        _uiState.update { it.copy(advancedAnalysis = enabled) }
    }

    fun send() {
        val question = _uiState.value.input.trim()
        if (question.isBlank()) return
        val tier = if (_uiState.value.advancedAnalysis) "advanced" else "standard"

        _uiState.update {
            it.copy(
                messages = it.messages + ChatMessage(ChatRole.USER, question),
                input = "",
                isSending = true,
                error = null
            )
        }

        viewModelScope.launch {
            val details = birthDetails ?: when (val result = repository.getPrimaryBirthDetails()) {
                is Result.Success -> result.data.also { birthDetails = it }
                is Result.Error -> {
                    _uiState.update { it.copy(isSending = false, error = result.message) }
                    return@launch
                }
                is Result.Loading -> return@launch
            }

            when (val result = repository.ask(details, question, analysisTier = tier)) {
                is Result.Success -> {
                    val response = result.data
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            messages = it.messages + ChatMessage(
                                ChatRole.ASSISTANT,
                                response.answer ?: response.error ?: "No answer returned",
                                tier = tier
                            )
                        )
                    }
                    val paise = (response.costEstimate.costInr * 100).roundToLong()
                    if (paise > 0) {
                        paymentsRepository.debitWallet(
                            skuId = "llm_chat",
                            idempotencyKey = UUID.randomUUID().toString(),
                            amountPaise = paise
                        )
                    }
                }
                is Result.Error -> _uiState.update {
                    it.copy(isSending = false, error = result.message)
                }
                is Result.Loading -> Unit
            }
        }
    }
}

package com.sangeetmind.features.astrology.chatmind

import androidx.lifecycle.SavedStateHandle
import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.payments.PaymentsRepository
import com.sangeetmind.libs.models.LlmBirthDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
data class ChatMessage(val role: ChatRole, val text: String, val tier: String? = null, val persona: String? = null)

/** ChatMind personas, in display order (backend PERSONAS in llm_chatmind_service.py). */
val CHAT_PERSONAS = listOf("pandit", "counsellor", "analyst", "friend")

data class ChatMindUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isSending: Boolean = false,
    val error: String? = null,
    /** The question whose answer failed — "Try again" re-asks it without duplicating the bubble. */
    val failedQuestion: String? = null,
    /** Mirrors the website's "Advanced analysis" toggle — off (standard model tier) by
     * default, since the advanced tier costs more per reply from the user's wallet. */
    val advancedAnalysis: Boolean = false,
    val persona: String = CHAT_PERSONAS.first()
)

@HiltViewModel
class ChatMindViewModel @Inject constructor(
    private val repository: ChatMindRepository,
    private val paymentsRepository: PaymentsRepository,
    private val languageManager: LanguageManager,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // A question handed over from the dashboard's "Ask" buttons pre-fills the input.
    private val _uiState = MutableStateFlow(ChatMindUiState(input = savedStateHandle.get<String>("q").orEmpty()))
    val uiState: StateFlow<ChatMindUiState> = _uiState.asStateFlow()

    private var birthDetails: LlmBirthDetails? = null

    private fun str(@StringRes id: Int): String =
        context.withAppLanguage(languageManager.current).getString(id)

    fun onInputChange(value: String) {
        _uiState.update { it.copy(input = value, error = null) }
    }

    fun setAdvancedAnalysis(enabled: Boolean) {
        _uiState.update { it.copy(advancedAnalysis = enabled) }
    }

    fun setPersona(persona: String) {
        _uiState.update { it.copy(persona = persona) }
    }

    fun send() {
        val question = _uiState.value.input.trim()
        if (question.isBlank()) return
        ask(question, appendUserMessage = true)
    }

    /** Re-asks the last question that failed (the user bubble is already in the list). */
    fun retry() {
        val question = _uiState.value.failedQuestion
        if (question.isNullOrBlank() || _uiState.value.isSending) {
            _uiState.update { it.copy(error = null) }
        } else {
            ask(question, appendUserMessage = false)
        }
    }

    private fun ask(question: String, appendUserMessage: Boolean) {
        val tier = if (_uiState.value.advancedAnalysis) "advanced" else "standard"
        val persona = _uiState.value.persona

        _uiState.update {
            it.copy(
                messages = if (appendUserMessage) it.messages + ChatMessage(ChatRole.USER, question) else it.messages,
                input = if (appendUserMessage) "" else it.input,
                isSending = true,
                error = null,
                failedQuestion = null
            )
        }

        viewModelScope.launch {
            val details = birthDetails ?: when (val result = repository.getPrimaryBirthDetails()) {
                is Result.Success -> result.data.also { birthDetails = it }
                is Result.Error -> {
                    _uiState.update { it.copy(isSending = false, error = result.message, failedQuestion = question) }
                    return@launch
                }
                is Result.Loading -> return@launch
            }

            when (val result = repository.ask(details, question, analysisTier = tier, persona = persona)) {
                is Result.Success -> {
                    val response = result.data
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            messages = it.messages + ChatMessage(
                                ChatRole.ASSISTANT,
                                response.answer ?: response.error ?: str(R.string.chatmind_no_answer),
                                tier = tier,
                                persona = response.persona
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
                    it.copy(isSending = false, error = result.message, failedQuestion = question)
                }
                is Result.Loading -> Unit
            }
        }
    }
}

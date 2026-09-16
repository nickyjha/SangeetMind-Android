package com.sangeetmind.features.astrology.chatmind

import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.LlmApi
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.libs.models.ChatMindRequest
import com.sangeetmind.libs.models.ChatMindResponse
import com.sangeetmind.libs.models.LlmBirthDetails
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatMindRepository @Inject constructor(
    private val llmApi: LlmApi,
    private val kundliRepository: KundliRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getPrimaryBirthDetails(): Result<LlmBirthDetails> = withContext(ioDispatcher) {
        when (val result = kundliRepository.listKundlis()) {
            is Result.Success -> {
                val primary = result.data.firstOrNull { it.isPrimary } ?: result.data.firstOrNull()
                if (primary == null) {
                    Result.Error(IllegalStateException("No kundli"), "Create a kundli first")
                } else {
                    Result.Success(
                        LlmBirthDetails(
                            date = primary.birthDate,
                            time = primary.birthTime,
                            timezone = primary.timezone,
                            place = primary.birthPlace,
                            lat = primary.latitude,
                            lon = primary.longitude
                        )
                    )
                }
            }
            is Result.Error -> Result.Error(result.exception, result.message)
            is Result.Loading -> Result.Loading
        }
    }

    suspend fun ask(
        birthDetails: LlmBirthDetails,
        question: String,
        analysisTier: String? = null
    ): Result<ChatMindResponse> = withContext(ioDispatcher) {
        try {
            Result.Success(llmApi.chat(ChatMindRequest(birthDetails, question, analysisTier = analysisTier)))
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "ChatMind could not answer that")
        }
    }
}

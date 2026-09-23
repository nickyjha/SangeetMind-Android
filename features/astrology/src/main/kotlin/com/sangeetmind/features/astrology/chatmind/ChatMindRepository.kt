package com.sangeetmind.features.astrology.chatmind

import android.content.Context
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.network.LlmApi
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.libs.models.ChatMindRequest
import com.sangeetmind.libs.models.ChatMindResponse
import com.sangeetmind.libs.models.LlmBirthDetails
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatMindRepository @Inject constructor(
    private val llmApi: LlmApi,
    private val kundliRepository: KundliRepository,
    private val languageManager: LanguageManager,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getPrimaryBirthDetails(): Result<LlmBirthDetails> = withContext(ioDispatcher) {
        when (val result = kundliRepository.listKundlis()) {
            is Result.Success -> {
                val primary = result.data.firstOrNull { it.isPrimary } ?: result.data.firstOrNull()
                if (primary == null) {
                    Result.Error(
                        IllegalStateException("No kundli"),
                        context.getString(CoreR.string.common_add_kundli_first)
                    )
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

    /** Each question is answered in the app's current display language. */
    suspend fun ask(
        birthDetails: LlmBirthDetails,
        question: String,
        analysisTier: String? = null
    ): Result<ChatMindResponse> = withContext(ioDispatcher) {
        try {
            Result.Success(
                llmApi.chat(
                    ChatMindRequest(
                        birthDetails,
                        question,
                        analysisTier = analysisTier,
                        lang = languageManager.current.code
                    )
                )
            )
        } catch (e: Exception) {
            Result.Error(e, e.message ?: context.getString(R.string.chatmind_err_could_not_answer))
        }
    }
}

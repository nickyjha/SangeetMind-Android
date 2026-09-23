package com.sangeetmind.features.astrology.marketplace

import android.content.Context
import androidx.annotation.StringRes
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.core.network.MarketplaceApi
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.Astrologer
import com.sangeetmind.libs.models.DebitChatMinuteRequest
import com.sangeetmind.libs.models.DebitChatMinuteResponse
import com.sangeetmind.libs.models.SubmitReviewRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketplaceRepository @Inject constructor(
    private val marketplaceApi: MarketplaceApi,
    private val languageManager: LanguageManager,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private fun str(@StringRes id: Int): String =
        context.withAppLanguage(languageManager.current).getString(id)

    suspend fun listAstrologers(onlineOnly: Boolean = false): Result<List<Astrologer>> =
        withContext(ioDispatcher) {
            try {
                Result.Success(marketplaceApi.listAstrologers(onlineOnly).astrologers)
            } catch (e: Exception) {
                Result.Error(e, e.message ?: str(R.string.marketplace_error_load_astrologers))
            }
        }

    suspend fun getAstrologer(id: String): Result<Astrologer> = withContext(ioDispatcher) {
        try {
            Result.Success(marketplaceApi.getAstrologer(id))
        } catch (e: Exception) {
            Result.Error(e, e.message ?: str(R.string.marketplace_error_load_astrologer))
        }
    }

    suspend fun submitReview(astrologerId: String, rating: Int, comment: String): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                marketplaceApi.submitReview(astrologerId, SubmitReviewRequest(rating, comment))
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e, e.message ?: str(R.string.marketplace_error_submit_review))
            }
        }

    suspend fun debitChatMinute(astrologerId: String, idempotencyKey: String): Result<DebitChatMinuteResponse> =
        withContext(ioDispatcher) {
            try {
                Result.Success(
                    marketplaceApi.debitChatMinute(DebitChatMinuteRequest(astrologerId, idempotencyKey))
                )
            } catch (e: Exception) {
                Result.Error(e, e.message ?: str(R.string.marketplace_error_bill_minute))
            }
        }
}

package com.sangeetmind.features.astrology.marketplace

import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.MarketplaceApi
import com.sangeetmind.libs.models.Astrologer
import com.sangeetmind.libs.models.DebitChatMinuteRequest
import com.sangeetmind.libs.models.DebitChatMinuteResponse
import com.sangeetmind.libs.models.SubmitReviewRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketplaceRepository @Inject constructor(
    private val marketplaceApi: MarketplaceApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun listAstrologers(onlineOnly: Boolean = false): Result<List<Astrologer>> =
        withContext(ioDispatcher) {
            try {
                Result.Success(marketplaceApi.listAstrologers(onlineOnly).astrologers)
            } catch (e: Exception) {
                Result.Error(e, e.message ?: "Failed to load astrologers")
            }
        }

    suspend fun getAstrologer(id: String): Result<Astrologer> = withContext(ioDispatcher) {
        try {
            Result.Success(marketplaceApi.getAstrologer(id))
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to load astrologer")
        }
    }

    suspend fun submitReview(astrologerId: String, rating: Int, comment: String): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                marketplaceApi.submitReview(astrologerId, SubmitReviewRequest(rating, comment))
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e, e.message ?: "Failed to submit review")
            }
        }

    suspend fun debitChatMinute(astrologerId: String, idempotencyKey: String): Result<DebitChatMinuteResponse> =
        withContext(ioDispatcher) {
            try {
                Result.Success(
                    marketplaceApi.debitChatMinute(DebitChatMinuteRequest(astrologerId, idempotencyKey))
                )
            } catch (e: Exception) {
                Result.Error(e, e.message ?: "Could not bill this minute")
            }
        }
}

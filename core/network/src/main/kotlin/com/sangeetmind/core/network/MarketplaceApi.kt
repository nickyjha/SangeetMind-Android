package com.sangeetmind.core.network

import com.sangeetmind.libs.models.AstrologerListResponse
import com.sangeetmind.libs.models.Astrologer
import com.sangeetmind.libs.models.DebitChatMinuteRequest
import com.sangeetmind.libs.models.DebitChatMinuteResponse
import com.sangeetmind.libs.models.SubmitReviewRequest
import com.sangeetmind.libs.models.SubmitReviewResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MarketplaceApi {
    @GET("v1/marketplace/astrologers")
    suspend fun listAstrologers(@Query("online_only") onlineOnly: Boolean = false): AstrologerListResponse

    @GET("v1/marketplace/astrologers/{id}")
    suspend fun getAstrologer(@Path("id") astrologerId: String): Astrologer

    @POST("v1/marketplace/astrologers/{id}/reviews")
    suspend fun submitReview(
        @Path("id") astrologerId: String,
        @Body body: SubmitReviewRequest
    ): SubmitReviewResponse

    @POST("v1/marketplace/chat/debit-minute")
    suspend fun debitChatMinute(@Body body: DebitChatMinuteRequest): DebitChatMinuteResponse
}

package com.sangeetmind.core.network

import com.sangeetmind.libs.models.PlayVerifyRequest
import com.sangeetmind.libs.models.PlayVerifyResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Google Play Billing verification. Provided to Hilt by
 * `features.astrology.payments.PlayBillingModule` (not ApiClient) so the payments feature
 * owns the whole Play path.
 */
interface PlayBillingApi {
    @POST("v1/play/verify")
    suspend fun verify(@Body body: PlayVerifyRequest): PlayVerifyResponse
}

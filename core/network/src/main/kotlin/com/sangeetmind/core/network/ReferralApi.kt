package com.sangeetmind.core.network

import com.sangeetmind.libs.models.ApplyReferralRequest
import com.sangeetmind.libs.models.ApplyReferralResponse
import com.sangeetmind.libs.models.ReferralStats
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ReferralApi {
    @POST("v1/referrals/apply")
    suspend fun applyReferral(@Body body: ApplyReferralRequest): ApplyReferralResponse

    @GET("v1/referrals/stats")
    suspend fun getStats(): ReferralStats
}

package com.sangeetmind.core.network

import com.sangeetmind.libs.models.KundliMatchEnvelope
import com.sangeetmind.libs.models.KundliMatchRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface MatchApi {
    @POST("v1/match/kundli")
    suspend fun matchKundli(@Body body: KundliMatchRequest): KundliMatchEnvelope
}

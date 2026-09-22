package com.sangeetmind.core.network

import com.sangeetmind.libs.models.ChaldeanResponse
import com.sangeetmind.libs.models.NumerologyRequest
import com.sangeetmind.libs.models.NumerologyResponse
import com.sangeetmind.libs.models.VedicResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface NumerologyApi {
    @POST("numerology/calculate/pythagorean")
    suspend fun calculatePythagorean(@Body body: NumerologyRequest): NumerologyResponse

    @POST("numerology/calculate/chaldean")
    suspend fun calculateChaldean(@Body body: NumerologyRequest): ChaldeanResponse

    @POST("numerology/calculate/vedic")
    suspend fun calculateVedic(@Body body: NumerologyRequest): VedicResponse
}

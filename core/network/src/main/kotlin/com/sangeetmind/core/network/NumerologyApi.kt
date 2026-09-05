package com.sangeetmind.core.network

import com.sangeetmind.libs.models.NumerologyRequest
import com.sangeetmind.libs.models.NumerologyResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface NumerologyApi {
    @POST("numerology/calculate/pythagorean")
    suspend fun calculatePythagorean(@Body body: NumerologyRequest): NumerologyResponse
}

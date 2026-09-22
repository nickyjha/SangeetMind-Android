package com.sangeetmind.core.network

import com.sangeetmind.libs.models.ChaldeanResponse
import com.sangeetmind.libs.models.NumerologyNumberInterpretationResponse
import com.sangeetmind.libs.models.NumerologyNumbersListResponse
import com.sangeetmind.libs.models.NumerologyRequest
import com.sangeetmind.libs.models.NumerologyResponse
import com.sangeetmind.libs.models.NumerologySystemsResponse
import com.sangeetmind.libs.models.VedicResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NumerologyApi {
    @POST("numerology/calculate/pythagorean")
    suspend fun calculatePythagorean(@Body body: NumerologyRequest): NumerologyResponse

    @POST("numerology/calculate/chaldean")
    suspend fun calculateChaldean(@Body body: NumerologyRequest): ChaldeanResponse

    @POST("numerology/calculate/vedic")
    suspend fun calculateVedic(@Body body: NumerologyRequest): VedicResponse

    @GET("numerology/systems")
    suspend fun getSystems(): NumerologySystemsResponse

    @GET("numerology/numbers")
    suspend fun getNumbers(): NumerologyNumbersListResponse

    @GET("numerology/number/{number}")
    suspend fun getNumberInterpretation(@Path("number") number: Int): NumerologyNumberInterpretationResponse
}

package com.sangeetmind.core.network

import com.sangeetmind.libs.models.CareerReadingRequest
import com.sangeetmind.libs.models.CareerReadingResponse
import com.sangeetmind.libs.models.ChatMindRequest
import com.sangeetmind.libs.models.ChatMindResponse
import com.sangeetmind.libs.models.StrengthsReadingRequest
import com.sangeetmind.libs.models.StrengthsReadingResponse
import retrofit2.http.Body
import retrofit2.http.POST

// Methods added by the ChatMind and career/strengths reading features (/llm/*).
interface LlmApi {
    @POST("llm/chat")
    suspend fun chat(@Body body: ChatMindRequest): ChatMindResponse

    @POST("llm/career")
    suspend fun getCareerReading(@Body body: CareerReadingRequest): CareerReadingResponse

    @POST("llm/strengths")
    suspend fun getStrengthsReading(@Body body: StrengthsReadingRequest): StrengthsReadingResponse
}

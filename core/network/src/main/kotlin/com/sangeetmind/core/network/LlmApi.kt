package com.sangeetmind.core.network

import com.sangeetmind.libs.models.CareerReadingRequest
import com.sangeetmind.libs.models.CareerReadingResponse
import com.sangeetmind.libs.models.ChatMindRequest
import com.sangeetmind.libs.models.ChatMindResponse
import com.sangeetmind.libs.models.ChildrenReadingRequest
import com.sangeetmind.libs.models.ChildrenReadingResponse
import com.sangeetmind.libs.models.MarriageReadingRequest
import com.sangeetmind.libs.models.MarriageReadingResponse
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

    @POST("llm/marriage")
    suspend fun getMarriageReading(@Body body: MarriageReadingRequest): MarriageReadingResponse

    @POST("llm/children")
    suspend fun getChildrenReading(@Body body: ChildrenReadingRequest): ChildrenReadingResponse
}

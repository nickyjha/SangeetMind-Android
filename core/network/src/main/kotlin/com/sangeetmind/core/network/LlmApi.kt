package com.sangeetmind.core.network

import com.sangeetmind.libs.models.ReadingPreview
import com.sangeetmind.libs.models.ReadingPreviewRequest
import com.sangeetmind.libs.models.CareerReadingRequest
import com.sangeetmind.libs.models.CareerReadingResponse
import com.sangeetmind.libs.models.ChatMindRequest
import com.sangeetmind.libs.models.ChatMindResponse
import com.sangeetmind.libs.models.ChildrenReadingRequest
import com.sangeetmind.libs.models.ChildrenReadingResponse
import com.sangeetmind.libs.models.ForeignReadingRequest
import com.sangeetmind.libs.models.ForeignReadingResponse
import com.sangeetmind.libs.models.MarriageReadingRequest
import com.sangeetmind.libs.models.MarriageReadingResponse
import com.sangeetmind.libs.models.StrengthsReadingRequest
import com.sangeetmind.libs.models.StrengthsReadingResponse
import com.sangeetmind.libs.models.CareerQuestionRequest
import com.sangeetmind.libs.models.CareerQuestionResponse
import com.sangeetmind.libs.models.DebtReadingRequest
import com.sangeetmind.libs.models.DebtReadingResponse
import com.sangeetmind.libs.models.RelationshipReadingRequest
import com.sangeetmind.libs.models.RelationshipReadingResponse
import com.sangeetmind.libs.models.EducationReadingRequest
import com.sangeetmind.libs.models.HealthReadingRequest
import com.sangeetmind.libs.models.HealthReadingResponse
import com.sangeetmind.libs.models.EducationReadingResponse
import com.sangeetmind.libs.models.PropertyReadingRequest
import com.sangeetmind.libs.models.PropertyReadingResponse
import com.sangeetmind.libs.models.WealthReadingRequest
import com.sangeetmind.libs.models.WealthReadingResponse
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

    @POST("llm/foreign")
    suspend fun getForeignReading(@Body body: ForeignReadingRequest): ForeignReadingResponse

    @POST("llm/wealth")
    suspend fun getWealthReading(@Body body: WealthReadingRequest): WealthReadingResponse

    @POST("llm/debt")
    suspend fun getDebtReading(@Body body: DebtReadingRequest): DebtReadingResponse

    @POST("llm/relationship")
    suspend fun getRelationshipReading(@Body body: RelationshipReadingRequest): RelationshipReadingResponse

    @POST("llm/preview")
    suspend fun getReadingPreview(@Body body: ReadingPreviewRequest): ReadingPreview

    @POST("llm/health")
    suspend fun getHealthReading(@Body body: HealthReadingRequest): HealthReadingResponse

    @POST("llm/education")
    suspend fun getEducationReading(@Body body: EducationReadingRequest): EducationReadingResponse

    @POST("llm/property")
    suspend fun getPropertyReading(@Body body: PropertyReadingRequest): PropertyReadingResponse

    @POST("llm/career-question")
    suspend fun getCareerQuestionReading(@Body body: CareerQuestionRequest): CareerQuestionResponse
}

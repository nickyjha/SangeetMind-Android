package com.sangeetmind.core.network

import com.sangeetmind.libs.models.SurveyDeleted
import com.sangeetmind.libs.models.SurveyRequest
import com.sangeetmind.libs.models.SurveySaved
import com.sangeetmind.libs.models.SurveyStatus
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

/** Opt-in accuracy survey (app/routes/accuracy_survey_routes.py); Firebase-authenticated. */
interface AccuracySurveyApi {
    @GET("v1/accuracy-survey")
    suspend fun get(): SurveyStatus

    @POST("v1/accuracy-survey")
    suspend fun submit(@Body body: SurveyRequest): SurveySaved

    @DELETE("v1/accuracy-survey")
    suspend fun withdraw(): SurveyDeleted
}

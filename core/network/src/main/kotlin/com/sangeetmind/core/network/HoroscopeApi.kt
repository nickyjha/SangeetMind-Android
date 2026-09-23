package com.sangeetmind.core.network

import com.sangeetmind.libs.models.DailyHoroscopeResponse
import com.sangeetmind.libs.models.PeriodHoroscopeResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HoroscopeApi {
    /** `lang` (en|hi) drives the Gemini-written fields only (app/routes/daily_horoscope.py). */
    @GET("v1/daily/sign/{sign}")
    suspend fun getDailyForSign(
        @Path("sign") sign: String,
        @Query("lang") lang: String = "en"
    ): DailyHoroscopeResponse

    /** `lang` only swaps the static house-theme copy (house_themes_hi); the summary body is
     *  the same non-LLM computation Daily uses before its Gemini enhancement step. */
    @GET("v1/weekly/sign/{sign}")
    suspend fun getWeekly(
        @Path("sign") sign: String,
        @Query("lang") lang: String = "en"
    ): PeriodHoroscopeResponse

    @GET("v1/monthly/sign/{sign}")
    suspend fun getMonthly(
        @Path("sign") sign: String,
        @Query("lang") lang: String = "en"
    ): PeriodHoroscopeResponse

    @GET("v1/yearly/sign/{sign}")
    suspend fun getYearly(
        @Path("sign") sign: String,
        @Query("lang") lang: String = "en"
    ): PeriodHoroscopeResponse
}

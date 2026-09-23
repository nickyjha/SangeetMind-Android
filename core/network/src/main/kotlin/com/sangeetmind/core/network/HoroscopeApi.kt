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

    @GET("v1/weekly/sign/{sign}")
    suspend fun getWeekly(@Path("sign") sign: String): PeriodHoroscopeResponse

    @GET("v1/monthly/sign/{sign}")
    suspend fun getMonthly(@Path("sign") sign: String): PeriodHoroscopeResponse

    @GET("v1/yearly/sign/{sign}")
    suspend fun getYearly(@Path("sign") sign: String): PeriodHoroscopeResponse
}

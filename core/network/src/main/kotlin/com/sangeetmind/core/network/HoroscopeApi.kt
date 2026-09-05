package com.sangeetmind.core.network

import com.sangeetmind.libs.models.DailyHoroscopeResponse
import com.sangeetmind.libs.models.PeriodHoroscopeResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface HoroscopeApi {
    @GET("v1/daily/sign/{sign}")
    suspend fun getDailyForSign(@Path("sign") sign: String): DailyHoroscopeResponse

    @GET("v1/weekly/sign/{sign}")
    suspend fun getWeekly(@Path("sign") sign: String): PeriodHoroscopeResponse

    @GET("v1/monthly/sign/{sign}")
    suspend fun getMonthly(@Path("sign") sign: String): PeriodHoroscopeResponse

    @GET("v1/yearly/sign/{sign}")
    suspend fun getYearly(@Path("sign") sign: String): PeriodHoroscopeResponse
}

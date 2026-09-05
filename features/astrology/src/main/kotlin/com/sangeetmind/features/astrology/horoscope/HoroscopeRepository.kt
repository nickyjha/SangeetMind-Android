package com.sangeetmind.features.astrology.horoscope

import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.HoroscopeApi
import com.sangeetmind.libs.models.DailyHoroscope
import com.sangeetmind.libs.models.PeriodHoroscope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HoroscopeRepository @Inject constructor(
    private val horoscopeApi: HoroscopeApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getDaily(sign: String): Result<DailyHoroscope> = withContext(ioDispatcher) {
        try {
            Result.Success(horoscopeApi.getDailyForSign(sign).horoscope)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to load daily horoscope")
        }
    }

    suspend fun getWeekly(sign: String): Result<PeriodHoroscope> = withContext(ioDispatcher) {
        try {
            Result.Success(horoscopeApi.getWeekly(sign).horoscope)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to load weekly horoscope")
        }
    }

    suspend fun getMonthly(sign: String): Result<PeriodHoroscope> = withContext(ioDispatcher) {
        try {
            Result.Success(horoscopeApi.getMonthly(sign).horoscope)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to load monthly horoscope")
        }
    }

    suspend fun getYearly(sign: String): Result<PeriodHoroscope> = withContext(ioDispatcher) {
        try {
            Result.Success(horoscopeApi.getYearly(sign).horoscope)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to load yearly horoscope")
        }
    }
}

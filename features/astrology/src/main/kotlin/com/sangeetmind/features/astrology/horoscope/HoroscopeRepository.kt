package com.sangeetmind.features.astrology.horoscope

import android.content.Context
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.core.network.HoroscopeApi
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.DailyHoroscope
import com.sangeetmind.libs.models.PeriodHoroscope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HoroscopeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val horoscopeApi: HoroscopeApi,
    private val languageManager: LanguageManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private fun str(@androidx.annotation.StringRes id: Int): String =
        context.withAppLanguage(languageManager.current).getString(id)

    /** Daily's Gemini-written fields follow `lang`; weekly/monthly/yearly reuse the same
     * non-LLM computation as Daily, so `lang` there only swaps the static house-theme copy. */
    suspend fun getDaily(sign: String): Result<DailyHoroscope> = withContext(ioDispatcher) {
        try {
            Result.Success(horoscopeApi.getDailyForSign(sign, lang = languageManager.current.code).horoscope)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: str(R.string.horoscope_error_daily))
        }
    }

    suspend fun getWeekly(sign: String): Result<PeriodHoroscope> = withContext(ioDispatcher) {
        try {
            Result.Success(horoscopeApi.getWeekly(sign, lang = languageManager.current.code).horoscope)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: str(R.string.horoscope_error_weekly))
        }
    }

    suspend fun getMonthly(sign: String): Result<PeriodHoroscope> = withContext(ioDispatcher) {
        try {
            Result.Success(horoscopeApi.getMonthly(sign, lang = languageManager.current.code).horoscope)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: str(R.string.horoscope_error_monthly))
        }
    }

    suspend fun getYearly(sign: String): Result<PeriodHoroscope> = withContext(ioDispatcher) {
        try {
            Result.Success(horoscopeApi.getYearly(sign, lang = languageManager.current.code).horoscope)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: str(R.string.horoscope_error_yearly))
        }
    }
}

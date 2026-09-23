package com.sangeetmind.features.astrology.numerology

import android.content.Context
import androidx.annotation.StringRes
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.core.network.NumerologyApi
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.ChaldeanResponse
import com.sangeetmind.libs.models.NumerologyNumberInterpretationResponse
import com.sangeetmind.libs.models.NumerologyNumbersListResponse
import com.sangeetmind.libs.models.NumerologyRequest
import com.sangeetmind.libs.models.NumerologyResponse
import com.sangeetmind.libs.models.NumerologySystemsResponse
import com.sangeetmind.libs.models.VedicResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NumerologyRepository @Inject constructor(
    private val numerologyApi: NumerologyApi,
    private val languageManager: LanguageManager,
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private fun str(@StringRes id: Int, vararg args: Any): String =
        appContext.withAppLanguage(languageManager.current).getString(id, *args)

    suspend fun analyzePythagorean(fullName: String, dateOfBirth: String): Result<NumerologyResponse> =
        withContext(ioDispatcher) {
            try {
                val response = numerologyApi.calculatePythagorean(
                    NumerologyRequest(
                        fullName = fullName,
                        dateOfBirth = dateOfBirth,
                        preferredLanguage = languageManager.current.code
                    )
                )
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e, e.message ?: str(R.string.numerology_error_calc_failed))
            }
        }

    suspend fun analyzeChaldean(
        fullName: String,
        dateOfBirth: String,
        currentName: String?
    ): Result<ChaldeanResponse> =
        withContext(ioDispatcher) {
            try {
                val response = numerologyApi.calculateChaldean(
                    NumerologyRequest(
                        fullName = fullName,
                        dateOfBirth = dateOfBirth,
                        currentName = currentName?.takeIf { it.isNotBlank() },
                        preferredLanguage = languageManager.current.code
                    )
                )
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e, e.message ?: str(R.string.numerology_error_calc_failed))
            }
        }

    suspend fun analyzeVedic(
        fullName: String,
        dateOfBirth: String,
        gender: String
    ): Result<VedicResponse> =
        withContext(ioDispatcher) {
            try {
                val response = numerologyApi.calculateVedic(
                    NumerologyRequest(
                        fullName = fullName,
                        dateOfBirth = dateOfBirth,
                        gender = gender,
                        preferredLanguage = languageManager.current.code
                    )
                )
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e, e.message ?: str(R.string.numerology_error_calc_failed))
            }
        }

    suspend fun getSystems(): Result<NumerologySystemsResponse> =
        withContext(ioDispatcher) {
            try {
                Result.Success(numerologyApi.getSystems())
            } catch (e: Exception) {
                Result.Error(e, e.message ?: str(R.string.numerology_error_load_systems))
            }
        }

    suspend fun getNumbers(): Result<NumerologyNumbersListResponse> =
        withContext(ioDispatcher) {
            try {
                Result.Success(numerologyApi.getNumbers())
            } catch (e: Exception) {
                Result.Error(e, e.message ?: str(R.string.numerology_error_load_numbers))
            }
        }

    suspend fun getNumberInterpretation(number: Int): Result<NumerologyNumberInterpretationResponse> =
        withContext(ioDispatcher) {
            try {
                Result.Success(numerologyApi.getNumberInterpretation(number))
            } catch (e: Exception) {
                Result.Error(e, e.message ?: str(R.string.numerology_error_load_interpretation_fmt, number))
            }
        }
}

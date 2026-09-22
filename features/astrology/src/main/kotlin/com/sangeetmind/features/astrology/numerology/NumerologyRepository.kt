package com.sangeetmind.features.astrology.numerology

import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.NumerologyApi
import com.sangeetmind.libs.models.ChaldeanResponse
import com.sangeetmind.libs.models.NumerologyRequest
import com.sangeetmind.libs.models.NumerologyResponse
import com.sangeetmind.libs.models.VedicResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NumerologyRepository @Inject constructor(
    private val numerologyApi: NumerologyApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun analyzePythagorean(fullName: String, dateOfBirth: String): Result<NumerologyResponse> =
        withContext(ioDispatcher) {
            try {
                val response = numerologyApi.calculatePythagorean(
                    NumerologyRequest(fullName = fullName, dateOfBirth = dateOfBirth)
                )
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e, e.message ?: "Failed to calculate numerology")
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
                        currentName = currentName?.takeIf { it.isNotBlank() }
                    )
                )
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e, e.message ?: "Failed to calculate numerology")
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
                    NumerologyRequest(fullName = fullName, dateOfBirth = dateOfBirth, gender = gender)
                )
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e, e.message ?: "Failed to calculate numerology")
            }
        }
}

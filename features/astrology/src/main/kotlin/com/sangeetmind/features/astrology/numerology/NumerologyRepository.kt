package com.sangeetmind.features.astrology.numerology

import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.NumerologyApi
import com.sangeetmind.libs.models.NumerologyRequest
import com.sangeetmind.libs.models.NumerologyResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NumerologyRepository @Inject constructor(
    private val numerologyApi: NumerologyApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun analyze(fullName: String, dateOfBirth: String): Result<NumerologyResponse> =
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
}

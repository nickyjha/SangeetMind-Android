package com.sangeetmind.features.astrology.muhurat

import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.MuhuratApi
import com.sangeetmind.libs.models.MuhuratRequest
import com.sangeetmind.libs.models.MuhuratResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MuhuratRepository @Inject constructor(
    private val muhuratApi: MuhuratApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun findMuhurat(
        intent: String,
        windowStart: String,
        windowEnd: String,
        lat: Double = 28.6139,
        lon: Double = 77.209
    ): Result<MuhuratResponse> = withContext(ioDispatcher) {
        try {
            val response = muhuratApi.findMuhurat(
                MuhuratRequest(intent = intent, windowStart = windowStart, windowEnd = windowEnd, lat = lat, lon = lon)
            )
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to find muhurat")
        }
    }
}

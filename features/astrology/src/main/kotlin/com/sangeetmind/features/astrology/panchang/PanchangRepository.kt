package com.sangeetmind.features.astrology.panchang

import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.PanchangApi
import com.sangeetmind.libs.models.PanchangResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PanchangRepository @Inject constructor(
    private val panchangApi: PanchangApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getPanchang(date: String, latitude: Double, longitude: Double): Result<PanchangResponse> =
        withContext(ioDispatcher) {
            try {
                Result.Success(panchangApi.getPanchang(date, latitude, longitude))
            } catch (e: Exception) {
                Result.Error(e, e.message ?: "Failed to load panchang")
            }
        }
}

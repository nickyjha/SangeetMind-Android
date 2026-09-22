package com.sangeetmind.features.astrology.holistic

import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.HolisticApi
import com.sangeetmind.libs.models.HolisticCombinedRequest
import com.sangeetmind.libs.models.HolisticCombinedResponse
import com.sangeetmind.libs.models.Kundli
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HolisticRepository @Inject constructor(
    private val holisticApi: HolisticApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getCombined(
        kundli: Kundli,
        fullName: String,
        language: String,
        enhanceWithLlm: Boolean = true
    ): Result<HolisticCombinedResponse> = withContext(ioDispatcher) {
        try {
            val response = holisticApi.getCombined(
                HolisticCombinedRequest(
                    fullName = fullName,
                    dateOfBirth = kundli.birthDate,
                    timeOfBirth = kundli.birthTime,
                    place = kundli.birthPlace,
                    latitude = kundli.latitude,
                    longitude = kundli.longitude,
                    timezone = kundli.timezone,
                    enhanceWithLlm = enhanceWithLlm,
                    language = language
                )
            )
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to load holistic reading")
        }
    }
}

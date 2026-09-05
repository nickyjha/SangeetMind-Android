package com.sangeetmind.features.astrology.match

import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.MatchApi
import com.sangeetmind.libs.models.KundliMatchRequest
import com.sangeetmind.libs.models.KundliMatchResult
import com.sangeetmind.libs.models.MatchBirthDetails
import com.sangeetmind.libs.models.Kundli
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepository @Inject constructor(
    private val matchApi: MatchApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun matchKundlis(personA: Kundli, personB: Kundli): Result<KundliMatchResult> =
        withContext(ioDispatcher) {
            try {
                val response = matchApi.matchKundli(
                    KundliMatchRequest(
                        personA = personA.toMatchBirthDetails(),
                        personB = personB.toMatchBirthDetails()
                    )
                )
                Result.Success(response.match)
            } catch (e: Exception) {
                Result.Error(e, e.message ?: "Failed to compute compatibility")
            }
        }
}

private fun Kundli.toMatchBirthDetails() = MatchBirthDetails(
    date = birthDate,
    time = birthTime,
    place = birthPlace,
    lat = latitude,
    lon = longitude,
    timezone = timezone
)

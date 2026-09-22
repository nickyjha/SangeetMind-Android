package com.sangeetmind.features.astrology.varshaphal

import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.InterpretationApi
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.VarshaphalRequest
import com.sangeetmind.libs.models.VarshaphalResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VarshaphalRepository @Inject constructor(
    private val interpretationApi: InterpretationApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getVarshaphal(kundli: Kundli, year: Int): Result<VarshaphalResponse> =
        withContext(ioDispatcher) {
            try {
                val varshaphal = interpretationApi.getVarshaphal(
                    VarshaphalRequest(
                        date = kundli.birthDate,
                        time = kundli.birthTime,
                        timezone = kundli.timezone,
                        place = kundli.birthPlace,
                        lat = kundli.latitude,
                        lon = kundli.longitude,
                        year = year
                    )
                )
                Result.Success(varshaphal)
            } catch (e: Exception) {
                Result.Error(e, e.message ?: "Failed to calculate Varshaphal")
            }
        }
}

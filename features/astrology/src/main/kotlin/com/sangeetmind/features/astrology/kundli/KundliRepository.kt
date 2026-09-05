package com.sangeetmind.features.astrology.kundli

import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.KundliApi
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.KundliCreateRequest
import com.sangeetmind.libs.models.KundliUpdateRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KundliRepository @Inject constructor(
    private val kundliApi: KundliApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun listKundlis(): Result<List<Kundli>> = withContext(ioDispatcher) {
        try {
            Result.Success(kundliApi.listKundlis())
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to load kundlis")
        }
    }

    suspend fun createKundli(
        fullName: String?,
        birthDate: String,
        birthTime: String,
        birthPlace: String,
        latitude: Double,
        longitude: Double,
        timezone: String? = null
    ): Result<Kundli> = withContext(ioDispatcher) {
        try {
            val created = kundliApi.createKundli(
                KundliCreateRequest(fullName, birthDate, birthTime, birthPlace, latitude, longitude, timezone)
            )
            Result.Success(created)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to save kundli")
        }
    }

    suspend fun updateKundli(
        id: String,
        fullName: String?,
        birthDate: String,
        birthTime: String,
        birthPlace: String,
        latitude: Double,
        longitude: Double,
        timezone: String? = null
    ): Result<Kundli> = withContext(ioDispatcher) {
        try {
            val updated = kundliApi.updateKundli(
                id,
                KundliUpdateRequest(fullName, birthDate, birthTime, birthPlace, latitude, longitude, timezone)
            )
            Result.Success(updated)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to update kundli")
        }
    }

    suspend fun setPrimary(id: String): Result<Kundli> = withContext(ioDispatcher) {
        try {
            Result.Success(kundliApi.setPrimaryKundli(id))
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to set primary kundli")
        }
    }

    suspend fun delete(id: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            kundliApi.deleteKundli(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to delete kundli")
        }
    }
}

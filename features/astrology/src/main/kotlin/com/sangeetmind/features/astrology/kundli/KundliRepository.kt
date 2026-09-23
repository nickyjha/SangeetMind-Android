package com.sangeetmind.features.astrology.kundli

import android.content.Context
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.KundliApi
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.KundliCreateRequest
import com.sangeetmind.libs.models.KundliUpdateRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KundliRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val kundliApi: KundliApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun listKundlis(): Result<List<Kundli>> = withContext(ioDispatcher) {
        try {
            Result.Success(kundliApi.listKundlis())
        } catch (e: Exception) {
            Result.Error(e, e.message ?: context.getString(R.string.kundli_error_load_failed))
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
            Result.Error(e, e.message ?: context.getString(R.string.kundli_error_save_kundli_failed))
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
            Result.Error(e, e.message ?: context.getString(R.string.kundli_error_update_failed))
        }
    }

    suspend fun setPrimary(id: String): Result<Kundli> = withContext(ioDispatcher) {
        try {
            Result.Success(kundliApi.setPrimaryKundli(id))
        } catch (e: Exception) {
            Result.Error(e, e.message ?: context.getString(R.string.kundli_error_set_primary_failed))
        }
    }

    suspend fun delete(id: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            kundliApi.deleteKundli(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: context.getString(R.string.kundli_error_delete_failed))
        }
    }
}

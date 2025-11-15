package com.sangeetmind.features.raaglibrary

import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.database.dao.RaagDao
import com.sangeetmind.core.database.entity.toEntity
import com.sangeetmind.core.database.entity.toRaag
import com.sangeetmind.integration.backendstub.MockApiService
import com.sangeetmind.libs.models.Raag
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RaagRepository @Inject constructor(
    private val raagDao: RaagDao,
    private val mockApiService: MockApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    fun getRaags(): Flow<Result<List<Raag>>> = flow {
        emit(Result.Loading)
        try {
            // Fetch from mock API (in production, use real API)
            val raags = mockApiService.getMockRaags()
            
            // Cache in database
            raagDao.insertRaags(raags.map { it.toEntity() })
            
            // Emit success
            emit(Result.Success(raags))
        } catch (e: Exception) {
            // Fallback to cached data
            raagDao.getAllRaags().map { entities ->
                if (entities.isEmpty()) {
                    Result.Error(e, "Failed to load raags")
                } else {
                    Result.Success(entities.map { it.toRaag() })
                }
            }.collect { emit(it) }
        }
    }.flowOn(ioDispatcher)

    fun searchRaags(query: String): Flow<Result<List<Raag>>> = flow {
        emit(Result.Loading)
        try {
            val allRaags = mockApiService.getMockRaags()
            val filtered = allRaags.filter { raag ->
                raag.name.contains(query, ignoreCase = true) ||
                raag.nameHindi?.contains(query, ignoreCase = true) == true ||
                raag.description.contains(query, ignoreCase = true) ||
                raag.tags.any { it.contains(query, ignoreCase = true) }
            }
            emit(Result.Success(filtered))
        } catch (e: Exception) {
            emit(Result.Error(e, "Search failed"))
        }
    }.flowOn(ioDispatcher)

    suspend fun getRaagById(id: String): Result<Raag> = withContext(ioDispatcher) {
        try {
            val raag = raagDao.getRaagById(id)?.toRaag()
                ?: mockApiService.getMockRaags().find { it.id == id }
            
            if (raag != null) {
                Result.Success(raag)
            } else {
                Result.Error(Exception("Raag not found"), "Raag not found")
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to load raag")
        }
    }

    suspend fun toggleFavorite(raagId: String, isFavorite: Boolean) = withContext(ioDispatcher) {
        raagDao.updateFavoriteStatus(raagId, isFavorite)
    }

    fun getFavoriteRaags(): Flow<List<Raag>> =
        raagDao.getFavoriteRaags().map { entities -> entities.map { it.toRaag() } }
}


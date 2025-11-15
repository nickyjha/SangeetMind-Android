package com.sangeetmind.features.meditation

import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.integration.backendstub.MockApiService
import com.sangeetmind.libs.models.MeditationSession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeditationRepository @Inject constructor(
    private val mockApiService: MockApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    fun getMeditationSessions(): Flow<Result<List<MeditationSession>>> = flow {
        emit(Result.Loading)
        try {
            val sessions = mockApiService.getMockMeditationSessions()
            emit(Result.Success(sessions))
        } catch (e: Exception) {
            emit(Result.Error(e, "Failed to load meditation sessions"))
        }
    }.flowOn(ioDispatcher)

    fun getSessionById(id: String): Flow<Result<MeditationSession>> = flow {
        emit(Result.Loading)
        try {
            val session = mockApiService.getMockMeditationSessions().find { it.id == id }
            if (session != null) {
                emit(Result.Success(session))
            } else {
                emit(Result.Error(Exception("Session not found"), "Session not found"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e, "Failed to load session"))
        }
    }.flowOn(ioDispatcher)
}


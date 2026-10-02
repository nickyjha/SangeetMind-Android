package com.sangeetmind.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.http.DELETE
import javax.inject.Singleton

/** Response of `DELETE /v1/users/me`: per-table row counts that were removed. */
data class AccountDeletionResponse(
    val ok: Boolean = false,
    val deleted: Map<String, Int> = emptyMap(),
    val firebaseUserDeleted: Boolean = false
)

/** Account-level endpoints (the Firebase ID token is attached by the auth interceptor). */
interface UsersApi {
    /**
     * Deletes every server-side row owned by the signed-in user (kundlis, readings,
     * wallet + ledger, subscription, devices, ...) in one transaction. Irreversible.
     */
    @DELETE("v1/users/me")
    suspend fun deleteMyAccount(): AccountDeletionResponse
}

@Module
@InstallIn(SingletonComponent::class)
object UsersApiModule {
    @Provides
    @Singleton
    fun provideUsersApi(retrofit: Retrofit): UsersApi = retrofit.create(UsersApi::class.java)
}

package com.sangeetmind.features.astrology.payments

import com.sangeetmind.core.network.PlayBillingApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/** Retrofit binding for the Play verify endpoint; lives with the payments feature that owns it. */
@Module
@InstallIn(SingletonComponent::class)
object PlayBillingModule {
    @Provides
    @Singleton
    fun providePlayBillingApi(retrofit: Retrofit): PlayBillingApi = retrofit.create(PlayBillingApi::class.java)
}

package com.sangeetmind.features.astrology.panchang

import android.content.Context
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.core.network.friendlyErrorMessage
import com.sangeetmind.features.astrology.R
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val appContext: Context,
    private val languageManager: LanguageManager,
    private val panchangApi: PanchangApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private fun localized(): Context = appContext.withAppLanguage(languageManager.current)

    suspend fun getPanchang(date: String, latitude: Double, longitude: Double): Result<PanchangResponse> =
        withContext(ioDispatcher) {
            try {
                Result.Success(panchangApi.getPanchang(date, latitude, longitude))
            } catch (e: Exception) {
                Result.Error(e, friendlyErrorMessage(e, localized(), localized().getString(R.string.panchang_error_load_failed)))
            }
        }
}

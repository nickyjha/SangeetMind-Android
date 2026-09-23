package com.sangeetmind.features.astrology.referrals

import android.content.Context
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.ReferralApi
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.ApplyReferralRequest
import com.sangeetmind.libs.models.ApplyReferralResponse
import com.sangeetmind.libs.models.ReferralStats
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReferralRepository @Inject constructor(
    private val referralApi: ReferralApi,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun applyReferral(referrerUserId: String): Result<ApplyReferralResponse> =
        withContext(ioDispatcher) {
            try {
                Result.Success(referralApi.applyReferral(ApplyReferralRequest(referrerUserId)))
            } catch (e: Exception) {
                Result.Error(e, e.message ?: context.getString(R.string.referrals_error_apply_code))
            }
        }

    suspend fun getStats(): Result<ReferralStats> = withContext(ioDispatcher) {
        try {
            Result.Success(referralApi.getStats())
        } catch (e: Exception) {
            Result.Error(e, e.message ?: context.getString(R.string.referrals_error_load_stats))
        }
    }
}

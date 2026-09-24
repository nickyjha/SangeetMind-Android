package com.sangeetmind.features.astrology.readings

import android.content.Context
import androidx.annotation.StringRes
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.core.network.LlmApi
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.features.astrology.payments.PaymentsRepository
import com.sangeetmind.libs.models.CareerBirthDetails
import com.sangeetmind.libs.models.CareerReadingRequest
import com.sangeetmind.libs.models.CareerReadingResponse
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.MarriageReadingRequest
import com.sangeetmind.libs.models.MarriageReadingResponse
import com.sangeetmind.libs.models.StrengthsBirthDetails
import com.sangeetmind.libs.models.StrengthsReadingRequest
import com.sangeetmind.libs.models.StrengthsReadingResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val CAREER_SKU = "llm_career"
private const val CAREER_PRICE_PAISE = 9900L
private const val STRENGTHS_SKU = "llm_strengths"
private const val STRENGTHS_PRICE_PAISE = 9900L
private const val MARRIAGE_SKU = "llm_marriage"
private const val MARRIAGE_PRICE_PAISE = 9900L

@Singleton
class ReadingsRepository @Inject constructor(
    private val llmApi: LlmApi,
    private val kundliRepository: KundliRepository,
    private val paymentsRepository: PaymentsRepository,
    private val languageManager: LanguageManager,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private fun str(@StringRes id: Int): String =
        context.withAppLanguage(languageManager.current).getString(id)

    private suspend fun primaryKundli(): Result<Kundli> = when (val result = kundliRepository.listKundlis()) {
        is Result.Success -> {
            val primary = result.data.firstOrNull { it.isPrimary } ?: result.data.firstOrNull()
            if (primary == null) Result.Error(
                IllegalStateException("No kundli"),
                str(CoreR.string.common_add_kundli_first)
            )
            else Result.Success(primary)
        }
        is Result.Error -> Result.Error(result.exception, result.message)
        is Result.Loading -> Result.Loading
    }

    /** Debits [skuId]/[amountPaise] unless the user is already Premium. */
    private suspend fun spendUnlessPremium(skuId: String, amountPaise: Long): Result<Unit> {
        val status = paymentsRepository.getPremiumStatus()
        if (status is Result.Success && status.data.premium) return Result.Success(Unit)
        return when (
            val debit = paymentsRepository.debitWallet(skuId, UUID.randomUUID().toString(), amountPaise)
        ) {
            is Result.Success -> if (debit.data.success) Result.Success(Unit) else
                Result.Error(
                    IllegalStateException("Debit failed"),
                    str(R.string.readings_err_insufficient_balance)
                )
            is Result.Error -> Result.Error(debit.exception, debit.message)
            is Result.Loading -> Result.Loading
        }
    }

    /** True when the user can pay [amountPaise] (Premium, or enough wallet balance). */
    private suspend fun canAfford(amountPaise: Long): Result<Boolean> {
        val status = paymentsRepository.getPremiumStatus()
        if (status is Result.Success && status.data.premium) return Result.Success(true)
        return when (val balance = paymentsRepository.getWalletBalance()) {
            is Result.Success -> Result.Success(balance.data.balancePaise >= amountPaise)
            is Result.Error -> Result.Error(balance.exception, balance.message)
            is Result.Loading -> Result.Loading
        }
    }

    /**
     * Marriage reading. Unlike career/strengths, the wallet is debited only after the
     * reading comes back successfully, so a failed Gemini call never costs the user.
     * The balance is checked first so nobody gets a reading they can't pay for.
     */
    suspend fun getMarriageReading(maritalStatus: String): Result<MarriageReadingResponse> =
        withContext(ioDispatcher) {
            val kundli = when (val r = primaryKundli()) {
                is Result.Success -> r.data
                is Result.Error -> return@withContext Result.Error(r.exception, r.message)
                is Result.Loading -> return@withContext Result.Loading
            }
            when (val afford = canAfford(MARRIAGE_PRICE_PAISE)) {
                is Result.Success -> if (!afford.data) return@withContext Result.Error(
                    IllegalStateException("Insufficient balance"),
                    str(R.string.readings_err_insufficient_balance)
                )
                is Result.Error -> return@withContext Result.Error(afford.exception, afford.message)
                is Result.Loading -> return@withContext Result.Loading
            }
            val response = try {
                llmApi.getMarriageReading(
                    MarriageReadingRequest(
                        CareerBirthDetails(
                            date = kundli.birthDate,
                            time = kundli.birthTime,
                            timezone = kundli.timezone,
                            place = kundli.birthPlace,
                            lat = kundli.latitude,
                            lon = kundli.longitude
                        ),
                        maritalStatus = maritalStatus,
                        lang = languageManager.current.code
                    )
                )
            } catch (e: Exception) {
                return@withContext Result.Error(e, e.message ?: str(R.string.readings_err_marriage))
            }
            if (!response.ok || response.reading == null) {
                return@withContext Result.Error(
                    IllegalStateException(response.error ?: "Reading failed"),
                    str(R.string.readings_err_marriage)
                )
            }
            when (val spend = spendUnlessPremium(MARRIAGE_SKU, MARRIAGE_PRICE_PAISE)) {
                is Result.Error -> Result.Error(spend.exception, spend.message)
                else -> Result.Success(response)
            }
        }

    suspend fun getCareerReading(): Result<CareerReadingResponse> = withContext(ioDispatcher) {
        val kundli = when (val r = primaryKundli()) {
            is Result.Success -> r.data
            is Result.Error -> return@withContext Result.Error(r.exception, r.message)
            is Result.Loading -> return@withContext Result.Loading
        }
        when (val spend = spendUnlessPremium(CAREER_SKU, CAREER_PRICE_PAISE)) {
            is Result.Error -> return@withContext Result.Error(spend.exception, spend.message)
            else -> Unit
        }
        try {
            val request = CareerReadingRequest(
                CareerBirthDetails(
                    date = kundli.birthDate,
                    time = kundli.birthTime,
                    timezone = kundli.timezone,
                    place = kundli.birthPlace,
                    lat = kundli.latitude,
                    lon = kundli.longitude
                ),
                lang = languageManager.current.code
            )
            Result.Success(llmApi.getCareerReading(request))
        } catch (e: Exception) {
            Result.Error(e, e.message ?: str(R.string.readings_err_career))
        }
    }

    suspend fun getStrengthsReading(): Result<StrengthsReadingResponse> = withContext(ioDispatcher) {
        val kundli = when (val r = primaryKundli()) {
            is Result.Success -> r.data
            is Result.Error -> return@withContext Result.Error(r.exception, r.message)
            is Result.Loading -> return@withContext Result.Loading
        }
        when (val spend = spendUnlessPremium(STRENGTHS_SKU, STRENGTHS_PRICE_PAISE)) {
            is Result.Error -> return@withContext Result.Error(spend.exception, spend.message)
            else -> Unit
        }
        try {
            val request = StrengthsReadingRequest(
                StrengthsBirthDetails(
                    date = kundli.birthDate,
                    time = kundli.birthTime,
                    place = kundli.birthPlace,
                    timezone = kundli.timezone
                ),
                lang = languageManager.current.code
            )
            Result.Success(llmApi.getStrengthsReading(request))
        } catch (e: Exception) {
            Result.Error(e, e.message ?: str(R.string.readings_err_strengths))
        }
    }
}

package com.sangeetmind.features.astrology.readings

import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.LlmApi
import com.sangeetmind.features.astrology.kundli.KundliRepository
import com.sangeetmind.features.astrology.payments.PaymentsRepository
import com.sangeetmind.libs.models.CareerBirthDetails
import com.sangeetmind.libs.models.CareerReadingRequest
import com.sangeetmind.libs.models.CareerReadingResponse
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.StrengthsBirthDetails
import com.sangeetmind.libs.models.StrengthsReadingRequest
import com.sangeetmind.libs.models.StrengthsReadingResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val CAREER_SKU = "llm_career"
private const val CAREER_PRICE_PAISE = 9900L
private const val STRENGTHS_SKU = "llm_strengths"
private const val STRENGTHS_PRICE_PAISE = 9900L

@Singleton
class ReadingsRepository @Inject constructor(
    private val llmApi: LlmApi,
    private val kundliRepository: KundliRepository,
    private val paymentsRepository: PaymentsRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private suspend fun primaryKundli(): Result<Kundli> = when (val result = kundliRepository.listKundlis()) {
        is Result.Success -> {
            val primary = result.data.firstOrNull { it.isPrimary } ?: result.data.firstOrNull()
            if (primary == null) Result.Error(IllegalStateException("No kundli"), "Create a kundli first")
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
                Result.Error(IllegalStateException("Debit failed"), "Insufficient wallet balance")
            is Result.Error -> Result.Error(debit.exception, debit.message)
            is Result.Loading -> Result.Loading
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
                )
            )
            Result.Success(llmApi.getCareerReading(request))
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Could not generate career reading")
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
                )
            )
            Result.Success(llmApi.getStrengthsReading(request))
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Could not generate strengths reading")
        }
    }
}

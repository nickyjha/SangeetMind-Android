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
import com.sangeetmind.libs.models.ChildrenReadingRequest
import com.sangeetmind.libs.models.ChildrenReadingResponse
import com.sangeetmind.libs.models.ForeignReadingRequest
import com.sangeetmind.libs.models.ForeignReadingResponse
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
private const val CHILDREN_SKU = "llm_children"
private const val CHILDREN_PRICE_PAISE = 9900L
private const val FOREIGN_SKU = "llm_foreign"
private const val FOREIGN_PRICE_PAISE = 9900L

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
            is Result.Success -> {
                if (balance.data.balancePaise >= amountPaise) return Result.Success(true)
                // Short on balance: a paid recharge may not have been credited yet.
                val reconciled = paymentsRepository.reconcileWallet()
                Result.Success(
                    reconciled is Result.Success && reconciled.data.balancePaise >= amountPaise
                )
            }
            is Result.Error -> Result.Error(balance.exception, balance.message)
            is Result.Loading -> Result.Loading
        }
    }

    private fun birthDetails(kundli: Kundli) = CareerBirthDetails(
        date = kundli.birthDate,
        time = kundli.birthTime,
        timezone = kundli.timezone,
        place = kundli.birthPlace,
        lat = kundli.latitude,
        lon = kundli.longitude
    )

    /**
     * Shared flow for every paid reading: the balance is checked first (so nobody gets a
     * reading they can't pay for), and the wallet is debited only after [succeeded] says
     * the reading came back usable, so a failed Gemini call never costs the user.
     */
    private suspend fun <T> payAfterSuccess(
        skuId: String,
        pricePaise: Long,
        @StringRes errorRes: Int,
        succeeded: (T) -> Boolean,
        call: suspend (Kundli) -> T
    ): Result<T> = withContext(ioDispatcher) {
        val kundli = when (val r = primaryKundli()) {
            is Result.Success -> r.data
            is Result.Error -> return@withContext Result.Error(r.exception, r.message)
            is Result.Loading -> return@withContext Result.Loading
        }
        when (val afford = canAfford(pricePaise)) {
            is Result.Success -> if (!afford.data) return@withContext Result.Error(
                IllegalStateException("Insufficient balance"),
                str(R.string.readings_err_insufficient_balance)
            )
            is Result.Error -> return@withContext Result.Error(afford.exception, afford.message)
            is Result.Loading -> return@withContext Result.Loading
        }
        val response = try {
            call(kundli)
        } catch (e: Exception) {
            return@withContext Result.Error(e, e.message ?: str(errorRes))
        }
        if (!succeeded(response)) {
            return@withContext Result.Error(IllegalStateException("Reading failed"), str(errorRes))
        }
        when (val spend = spendUnlessPremium(skuId, pricePaise)) {
            is Result.Error -> Result.Error(spend.exception, spend.message)
            else -> Result.Success(response)
        }
    }

    suspend fun getMarriageReading(maritalStatus: String): Result<MarriageReadingResponse> =
        payAfterSuccess(
            MARRIAGE_SKU, MARRIAGE_PRICE_PAISE, R.string.readings_err_marriage,
            succeeded = { it.ok && it.reading != null }
        ) { kundli ->
            llmApi.getMarriageReading(
                MarriageReadingRequest(
                    birthDetails(kundli),
                    maritalStatus = maritalStatus,
                    lang = languageManager.current.code
                )
            )
        }

    /** Children (santaan) reading; [status] is "planning" or "parent". */
    suspend fun getChildrenReading(status: String): Result<ChildrenReadingResponse> =
        payAfterSuccess(
            CHILDREN_SKU, CHILDREN_PRICE_PAISE, R.string.readings_err_children,
            succeeded = { it.ok && it.reading != null }
        ) { kundli ->
            llmApi.getChildrenReading(
                ChildrenReadingRequest(
                    birthDetails(kundli),
                    status = status,
                    lang = languageManager.current.code
                )
            )
        }

    /** Foreign travel / settlement reading; [status] is "planning" or "abroad". */
    suspend fun getForeignReading(status: String): Result<ForeignReadingResponse> =
        payAfterSuccess(
            FOREIGN_SKU, FOREIGN_PRICE_PAISE, R.string.readings_err_foreign,
            succeeded = { it.ok && it.reading != null }
        ) { kundli ->
            llmApi.getForeignReading(
                ForeignReadingRequest(
                    birthDetails(kundli),
                    status = status,
                    lang = languageManager.current.code
                )
            )
        }

    suspend fun getCareerReading(): Result<CareerReadingResponse> =
        payAfterSuccess(
            CAREER_SKU, CAREER_PRICE_PAISE, R.string.readings_err_career,
            succeeded = { it.ok && (it.analysis != null || !it.rawModelText.isNullOrBlank()) }
        ) { kundli ->
            llmApi.getCareerReading(
                CareerReadingRequest(birthDetails(kundli), lang = languageManager.current.code)
            )
        }

    suspend fun getStrengthsReading(): Result<StrengthsReadingResponse> =
        payAfterSuccess(
            STRENGTHS_SKU, STRENGTHS_PRICE_PAISE, R.string.readings_err_strengths,
            succeeded = { it.error == null && it.strengths.isNotEmpty() }
        ) { kundli ->
            llmApi.getStrengthsReading(
                StrengthsReadingRequest(
                    StrengthsBirthDetails(
                        date = kundli.birthDate,
                        time = kundli.birthTime,
                        place = kundli.birthPlace,
                        timezone = kundli.timezone
                    ),
                    lang = languageManager.current.code
                )
            )
        }
}

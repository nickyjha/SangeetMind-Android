package com.sangeetmind.features.astrology.readings

import com.sangeetmind.core.network.friendlyErrorMessage
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
import com.sangeetmind.libs.models.CareerQuestionRequest
import com.sangeetmind.libs.models.CareerQuestionResponse
import com.sangeetmind.libs.models.DebtReadingRequest
import com.sangeetmind.libs.models.DebtReadingResponse
import com.sangeetmind.libs.models.RelationshipReadingRequest
import com.sangeetmind.libs.models.RelationshipReadingResponse
import com.sangeetmind.libs.models.EducationReadingRequest
import com.sangeetmind.libs.models.DashaStoryRequest
import com.sangeetmind.libs.models.DashaStoryResponse
import com.sangeetmind.libs.models.HealthReadingRequest
import com.sangeetmind.libs.models.HealthReadingResponse
import com.sangeetmind.libs.models.EducationReadingResponse
import com.sangeetmind.libs.models.PropertyReadingRequest
import com.sangeetmind.libs.models.ReadingPreview
import com.sangeetmind.libs.models.ReadingPreviewRequest
import com.sangeetmind.libs.models.SmallReadingRequest
import com.sangeetmind.libs.models.SmallReadingResponse
import com.sangeetmind.libs.models.PropertyReadingResponse
import com.sangeetmind.libs.models.WealthReadingRequest
import com.sangeetmind.libs.models.WealthReadingResponse
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
private const val WEALTH_SKU = "llm_wealth"
private const val WEALTH_PRICE_PAISE = 9900L
private const val CAREER_QUESTION_SKU = "llm_career_question"
private const val PROPERTY_SKU = "llm_property"
private const val EDUCATION_SKU = "llm_education"
private const val HEALTH_SKU = "llm_health"
private const val DASHA_STORY_SKU = "llm_dasha_story"
private const val DASHA_STORY_PRICE_PAISE = 9900L
private const val HEALTH_PRICE_PAISE = 9900L
private const val RELATIONSHIP_SKU = "llm_relationship"
private const val RELATIONSHIP_PRICE_PAISE = 9900L
private const val DEBT_SKU = "llm_debt"
private const val DEBT_PRICE_PAISE = 9900L
private const val EDUCATION_PRICE_PAISE = 9900L
private const val PROPERTY_PRICE_PAISE = 9900L
private const val CAREER_QUESTION_PRICE_PAISE = 9900L
// Small readings: SKU is "llm_" + topic (llm_love_style, llm_ideal_partner, llm_in_laws).
private const val SMALL_PRICE_PAISE = 4900L

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

    /** True while the backend's free beta makes every reading free; false on any error. */
    suspend fun isFreeBeta(): Boolean {
        val status = paymentsRepository.getPremiumStatus()
        return status is Result.Success && status.data.beta
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

    /** Free chart-based preview of a paid reading ([topic] as in reading_preview_service.py). */
    suspend fun getPreview(topic: String, status: String): Result<ReadingPreview> =
        withContext(ioDispatcher) {
            val kundli = when (val r = primaryKundli()) {
                is Result.Success -> r.data
                is Result.Error -> return@withContext Result.Error(r.exception, r.message)
                is Result.Loading -> return@withContext Result.Loading
            }
            try {
                Result.Success(llmApi.getReadingPreview(ReadingPreviewRequest(birthDetails(kundli), topic, status)))
            } catch (e: Exception) {
                Result.Error(e, friendlyErrorMessage(e, context.withAppLanguage(languageManager.current)))
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
            return@withContext Result.Error(e, friendlyErrorMessage(e, context.withAppLanguage(languageManager.current), str(errorRes)))
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

    /** Wealth (dhana) reading; [status] is "job" or "business". */
    suspend fun getWealthReading(status: String): Result<WealthReadingResponse> =
        payAfterSuccess(
            WEALTH_SKU, WEALTH_PRICE_PAISE, R.string.readings_err_wealth,
            succeeded = { it.ok && it.reading != null }
        ) { kundli ->
            llmApi.getWealthReading(
                WealthReadingRequest(
                    birthDetails(kundli),
                    status = status,
                    lang = languageManager.current.code
                )
            )
        }

    /** Debt or disputes reading; [status] is "debt" or "dispute". */
    suspend fun getDebtReading(status: String): Result<DebtReadingResponse> =
        payAfterSuccess(
            DEBT_SKU, DEBT_PRICE_PAISE, R.string.readings_err_debt,
            succeeded = { it.ok && it.reading != null }
        ) { kundli ->
            llmApi.getDebtReading(
                DebtReadingRequest(
                    birthDetails(kundli),
                    status = status,
                    lang = languageManager.current.code
                )
            )
        }

    /** Relationship reading; [status] is "strain" or "remarriage". */
    suspend fun getRelationshipReading(status: String): Result<RelationshipReadingResponse> =
        payAfterSuccess(
            RELATIONSHIP_SKU, RELATIONSHIP_PRICE_PAISE, R.string.readings_err_relationship,
            succeeded = { it.ok && it.reading != null }
        ) { kundli ->
            llmApi.getRelationshipReading(
                RelationshipReadingRequest(
                    birthDetails(kundli),
                    status = status,
                    lang = languageManager.current.code
                )
            )
        }

    /** A short reading on one question; [topic] is one of SMALL_TOPICS. */
    suspend fun getSmallReading(topic: String): Result<SmallReadingResponse> =
        payAfterSuccess(
            "llm_$topic", SMALL_PRICE_PAISE, R.string.readings_err_small,
            succeeded = { it.ok && it.reading != null }
        ) { kundli ->
            llmApi.getSmallReading(
                SmallReadingRequest(birthDetails(kundli), topic = topic, lang = languageManager.current.code)
            )
        }

    /** Lifetime dasha story: one chapter per mahadasha from birth. */
    suspend fun getDashaStoryReading(): Result<DashaStoryResponse> =
        payAfterSuccess(
            DASHA_STORY_SKU, DASHA_STORY_PRICE_PAISE, R.string.readings_err_dasha,
            succeeded = { it.ok && it.reading != null }
        ) { kundli ->
            llmApi.getDashaStoryReading(
                DashaStoryRequest(birthDetails(kundli), lang = languageManager.current.code)
            )
        }

    /** Health / wellbeing reading; [status] is "body" or "mind". */
    suspend fun getHealthReading(status: String): Result<HealthReadingResponse> =
        payAfterSuccess(
            HEALTH_SKU, HEALTH_PRICE_PAISE, R.string.readings_err_health,
            succeeded = { it.ok && it.reading != null }
        ) { kundli ->
            llmApi.getHealthReading(
                HealthReadingRequest(
                    birthDetails(kundli),
                    status = status,
                    lang = languageManager.current.code
                )
            )
        }

    /** Education reading; [status] is "student" or "higher". */
    suspend fun getEducationReading(status: String): Result<EducationReadingResponse> =
        payAfterSuccess(
            EDUCATION_SKU, EDUCATION_PRICE_PAISE, R.string.readings_err_education,
            succeeded = { it.ok && it.reading != null }
        ) { kundli ->
            llmApi.getEducationReading(
                EducationReadingRequest(
                    birthDetails(kundli),
                    status = status,
                    lang = languageManager.current.code
                )
            )
        }

    /** Property (home, land) or vehicle reading; [status] is "property" or "vehicle". */
    suspend fun getPropertyReading(status: String): Result<PropertyReadingResponse> =
        payAfterSuccess(
            PROPERTY_SKU, PROPERTY_PRICE_PAISE, R.string.readings_err_property,
            succeeded = { it.ok && it.reading != null }
        ) { kundli ->
            llmApi.getPropertyReading(
                PropertyReadingRequest(
                    birthDetails(kundli),
                    status = status,
                    lang = languageManager.current.code
                )
            )
        }

    /** One career question: job_change, promotion, govt_private or job_business. */
    suspend fun getCareerQuestionReading(question: String): Result<CareerQuestionResponse> =
        payAfterSuccess(
            CAREER_QUESTION_SKU, CAREER_QUESTION_PRICE_PAISE, R.string.readings_err_career,
            succeeded = { it.ok && it.reading != null }
        ) { kundli ->
            llmApi.getCareerQuestionReading(
                CareerQuestionRequest(
                    birthDetails(kundli),
                    question = question,
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

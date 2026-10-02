package com.sangeetmind.features.astrology.match

import android.content.Context
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.core.network.friendlyErrorMessage
import com.sangeetmind.features.astrology.R
import dagger.hilt.android.qualifiers.ApplicationContext
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.network.MatchApi
import com.sangeetmind.libs.models.KundliMatchRequest
import com.sangeetmind.libs.models.KundliMatchResult
import com.sangeetmind.libs.models.MatchBirthDetails
import com.sangeetmind.libs.models.RelationMatchRequest
import com.sangeetmind.libs.models.RelationMatchResult
import com.sangeetmind.libs.models.Kundli
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val languageManager: LanguageManager,
    private val matchApi: MatchApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private fun localized(): Context = appContext.withAppLanguage(languageManager.current)

    suspend fun matchKundlis(personA: Kundli, personB: Kundli): Result<KundliMatchResult> =
        withContext(ioDispatcher) {
            try {
                val response = matchApi.matchKundli(
                    KundliMatchRequest(
                        personA = personA.toMatchBirthDetails(),
                        personB = personB.toMatchBirthDetails()
                    )
                )
                Result.Success(response.match)
            } catch (e: Exception) {
                Result.Error(e, friendlyErrorMessage(e, localized(), localized().getString(R.string.match_error_compute)))
            }
        }

    /** Non-marital compatibility; [relation] is parent_child | siblings | business | friends. */
    suspend fun matchRelation(personA: Kundli, personB: Kundli, relation: String): Result<RelationMatchResult> =
        withContext(ioDispatcher) {
            try {
                val response = matchApi.matchRelation(
                    RelationMatchRequest(
                        personA = personA.toMatchBirthDetails(),
                        personB = personB.toMatchBirthDetails(),
                        relation = relation
                    )
                )
                Result.Success(response.match)
            } catch (e: Exception) {
                Result.Error(e, friendlyErrorMessage(e, localized(), localized().getString(R.string.match_error_compute)))
            }
        }
}

private fun Kundli.toMatchBirthDetails() = MatchBirthDetails(
    date = birthDate,
    time = birthTime,
    place = birthPlace,
    lat = latitude,
    lon = longitude,
    timezone = timezone
)

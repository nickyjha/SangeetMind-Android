package com.sangeetmind.features.astrology.chart

import com.sangeetmind.core.network.friendlyErrorMessage
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import androidx.annotation.StringRes
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.core.network.InterpretationApi
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.ChartRequest
import com.sangeetmind.libs.models.ChartSummaryResponse
import com.sangeetmind.libs.models.Kundli
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChartRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val interpretationApi: InterpretationApi,
    private val languageManager: LanguageManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private fun str(@StringRes id: Int): String =
        appContext.withAppLanguage(languageManager.current).getString(id)

    suspend fun getChart(kundli: Kundli): Result<ChartSummaryResponse> =
        withContext(ioDispatcher) {
            try {
                val chart = interpretationApi.getChart(
                    ChartRequest(
                        date = kundli.birthDate,
                        time = kundli.birthTime,
                        timezone = kundli.timezone,
                        place = kundli.birthPlace,
                        lat = kundli.latitude,
                        lon = kundli.longitude
                    )
                )
                Result.Success(chart)
            } catch (e: Exception) {
                Result.Error(e, friendlyErrorMessage(e, appContext.withAppLanguage(languageManager.current), str(R.string.chart_error_calculate)))
            }
        }

    /** Downloads the kundli PDF into files/reports and returns a content:// Uri for sharing. */
    suspend fun downloadKundliPdf(kundli: Kundli): Result<Uri> =
        withContext(ioDispatcher) {
            try {
                val response = interpretationApi.getChartPdf(
                    ChartRequest(
                        date = kundli.birthDate,
                        time = kundli.birthTime,
                        timezone = kundli.timezone,
                        place = kundli.birthPlace,
                        lat = kundli.latitude,
                        lon = kundli.longitude
                    ),
                    kundli.fullName?.takeIf { it.isNotBlank() }
                )
                val body = response.body()
                if (!response.isSuccessful || body == null) {
                    return@withContext Result.Error(
                        IllegalStateException("HTTP ${response.code()}"),
                        str(R.string.chart_pdf_error)
                    )
                }
                val dir = File(appContext.filesDir, "reports").apply { mkdirs() }
                val safe = (kundli.fullName ?: "").replace(Regex("[^A-Za-z0-9 _-]"), "").trim().ifBlank { "kundli" }
                val file = File(dir, "$safe-kundli.pdf")
                file.outputStream().use { out -> body.byteStream().copyTo(out) }
                Result.Success(FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", file))
            } catch (e: Exception) {
                Result.Error(e, friendlyErrorMessage(e, appContext.withAppLanguage(languageManager.current), str(R.string.chart_pdf_error)))
            }
        }
}

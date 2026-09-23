package com.sangeetmind.features.astrology.reports

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.di.IoDispatcher
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.network.ReportsApi
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.BirthDetailsPayload
import com.sangeetmind.libs.models.MyReportItem
import com.sangeetmind.libs.models.PurchaseReportRequest
import com.sangeetmind.libs.models.ReportJob
import com.sangeetmind.libs.models.ReportJobStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportsRepository @Inject constructor(
    private val reportsApi: ReportsApi,
    @ApplicationContext private val appContext: Context,
    private val languageManager: LanguageManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    /** [lang] defaults to the app's current display language so the PDF is generated in it. */
    suspend fun purchase(
        skuId: String,
        birthDetails: BirthDetailsPayload,
        lang: String = languageManager.current.code
    ): Result<ReportJob> = withContext(ioDispatcher) {
        try {
            val request = PurchaseReportRequest(
                skuId = skuId,
                birthDetails = birthDetails,
                lang = lang,
                idempotencyKey = UUID.randomUUID().toString()
            )
            Result.Success(reportsApi.purchaseReport(request))
        } catch (e: Exception) {
            Result.Error(e, e.message ?: appContext.getString(R.string.reports_error_purchase))
        }
    }

    suspend fun getStatus(jobId: String): Result<ReportJobStatus> = withContext(ioDispatcher) {
        try {
            Result.Success(reportsApi.getReportStatus(jobId))
        } catch (e: Exception) {
            Result.Error(e, e.message ?: appContext.getString(R.string.reports_error_check_status))
        }
    }

    suspend fun listMine(): Result<List<MyReportItem>> = withContext(ioDispatcher) {
        try {
            Result.Success(reportsApi.listMyReports().reports)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: appContext.getString(R.string.reports_error_load_mine))
        }
    }

    /** Downloads the PDF and returns a content:// Uri suitable for Intent.ACTION_VIEW. */
    suspend fun downloadAndGetViewUri(jobId: String, skuId: String): Result<Uri> =
        withContext(ioDispatcher) {
            try {
                val response = reportsApi.downloadReport(jobId)
                val body = response.body() ?: return@withContext Result.Error(
                    IllegalStateException("Empty response"),
                    appContext.getString(R.string.reports_error_not_ready)
                )
                val dir = File(appContext.filesDir, "reports").apply { mkdirs() }
                val file = File(dir, "$skuId-$jobId.pdf")
                file.outputStream().use { out -> body.byteStream().copyTo(out) }
                val uri = FileProvider.getUriForFile(
                    appContext,
                    "${appContext.packageName}.fileprovider",
                    file
                )
                Result.Success(uri)
            } catch (e: Exception) {
                Result.Error(e, e.message ?: appContext.getString(R.string.reports_error_download))
            }
        }
}

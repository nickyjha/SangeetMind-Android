package com.sangeetmind.features.astrology.reports.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.reports.ReportsViewModel
import com.sangeetmind.libs.models.MyReportItem
import com.sangeetmind.libs.models.Sku

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.pdfUri) {
        val uri = uiState.pdfUri ?: return@LaunchedEffect
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
        viewModel.clearPdfUri()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reports_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(uiState.error!!, modifier = Modifier.padding(12.dp))
                }
            }

            if (uiState.activeJobId != null) {
                ActiveJobCard(
                    status = uiState.activeStatus,
                    isLoading = uiState.isLoading,
                    onViewPdf = { viewModel.viewPdf() }
                )
            }

            if (uiState.isLoading && uiState.activeJobId == null && uiState.skus.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.myReports.isNotEmpty()) {
                    item {
                        Text(stringResource(R.string.reports_my_reports), style = MaterialTheme.typography.titleMedium)
                    }
                    items(uiState.myReports, key = { it.jobId }) { report ->
                        MyReportCard(
                            report = report,
                            enabled = !uiState.isLoading && report.status == "completed",
                            onViewPdf = { viewModel.viewPdf(report.jobId, report.skuId) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.reports_buy_a_report), style = MaterialTheme.typography.titleMedium)
                    }
                }

                items(uiState.skus, key = { it.skuId }) { sku ->
                    ReportSkuCard(sku, enabled = !uiState.isLoading, onBuy = { viewModel.purchase(sku) })
                }
            }
        }
    }
}

@Composable
private fun ActiveJobCard(status: String?, isLoading: Boolean, onViewPdf: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(
                        R.string.reports_status_fmt,
                        status ?: stringResource(R.string.reports_status_starting)
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (status == "completed") {
                Button(onClick = onViewPdf, enabled = !isLoading) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.reports_view_pdf))
                }
            } else if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun MyReportCard(
    report: MyReportItem,
    enabled: Boolean,
    onViewPdf: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(report.skuId, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = report.status,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                report.createdAt?.takeIf { it.isNotBlank() }?.let { createdAt ->
                    Text(
                        text = createdAt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (report.status == "completed") {
                Button(onClick = onViewPdf, enabled = enabled) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.reports_pdf))
                }
            }
        }
    }
}

@Composable
private fun ReportSkuCard(sku: Sku, enabled: Boolean, onBuy: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(sku.label, style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.reports_price_fmt, "%.2f".format(sku.pricePaise / 100.0)),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(onClick = onBuy, enabled = enabled) { Text(stringResource(R.string.reports_buy)) }
        }
    }
}

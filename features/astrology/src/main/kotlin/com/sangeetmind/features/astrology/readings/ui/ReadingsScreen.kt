package com.sangeetmind.features.astrology.readings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.flattenToReadableText
import com.sangeetmind.features.astrology.readings.ReadingTab
import com.sangeetmind.features.astrology.readings.ReadingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReadingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.readings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = uiState.tab.ordinal) {
                Tab(
                    selected = uiState.tab == ReadingTab.CAREER,
                    onClick = { viewModel.setTab(ReadingTab.CAREER) },
                    text = { Text(stringResource(R.string.readings_tab_career)) }
                )
                Tab(
                    selected = uiState.tab == ReadingTab.STRENGTHS,
                    onClick = { viewModel.setTab(ReadingTab.STRENGTHS) },
                    text = { Text(stringResource(R.string.readings_tab_strengths)) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (uiState.error != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(uiState.error!!, modifier = Modifier.padding(12.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                when (uiState.tab) {
                    ReadingTab.CAREER -> CareerTab(
                        isLoading = uiState.isLoading,
                        text = uiState.career?.let { it.analysis?.flattenToReadableText() ?: it.rawModelText },
                        onGenerate = viewModel::generateCareerReading
                    )
                    ReadingTab.STRENGTHS -> StrengthsTab(
                        isLoading = uiState.isLoading,
                        strengths = uiState.strengths,
                        onGenerate = viewModel::generateStrengthsReading
                    )
                }
            }
        }
    }
}

@Composable
private fun CareerTab(isLoading: Boolean, text: String?, onGenerate: () -> Unit) {
    Text(
        stringResource(R.string.readings_career_desc),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onGenerate, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        else Text(stringResource(R.string.readings_career_generate))
    }
    if (text != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(text, modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
private fun StrengthsTab(
    isLoading: Boolean,
    strengths: com.sangeetmind.libs.models.StrengthsReadingResponse?,
    onGenerate: () -> Unit
) {
    Text(
        stringResource(R.string.readings_strengths_desc),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onGenerate, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        else Text(stringResource(R.string.readings_strengths_generate))
    }
    if (strengths != null) {
        Spacer(modifier = Modifier.height(16.dp))
        if (strengths.strengths.isNotEmpty()) {
            SectionCard(stringResource(R.string.readings_section_strengths), strengths.strengths)
        }
        if (strengths.weaknesses.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            SectionCard(stringResource(R.string.readings_section_growth_areas), strengths.weaknesses)
        }
        strengths.remedies.forEach { remedy ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(remedy.mantraTitle, style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.readings_raag_fmt, remedy.raag), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    remedy.mantraText.forEach { line -> Text(line) }
                    remedy.why?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, items: List<String>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { item ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("• ")
                    Text(item)
                }
            }
        }
    }
}

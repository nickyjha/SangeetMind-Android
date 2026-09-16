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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                title = { Text("Readings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                    text = { Text("Career") }
                )
                Tab(
                    selected = uiState.tab == ReadingTab.STRENGTHS,
                    onClick = { viewModel.setTab(ReadingTab.STRENGTHS) },
                    text = { Text("Strengths") }
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
        "₹99, or free with Premium — a Gemini-generated career outlook from your kundli's D1/D10 charts and current dasha.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onGenerate, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        else Text("Generate career reading")
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
        "₹99, or free with Premium — key strengths, growth areas, and raag-based mantra remedies.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onGenerate, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        else Text("Generate strengths reading")
    }
    if (strengths != null) {
        Spacer(modifier = Modifier.height(16.dp))
        if (strengths.strengths.isNotEmpty()) {
            SectionCard("Strengths", strengths.strengths)
        }
        if (strengths.weaknesses.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            SectionCard("Growth areas", strengths.weaknesses)
        }
        strengths.remedies.forEach { remedy ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(remedy.mantraTitle, style = MaterialTheme.typography.titleMedium)
                    Text("Raag: ${remedy.raag}", style = MaterialTheme.typography.bodySmall)
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

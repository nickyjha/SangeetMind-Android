package com.sangeetmind.features.astrology.interpretation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.features.astrology.interpretation.InterpretationViewModel
import com.sangeetmind.libs.models.ChartSummaryResponse
import com.sangeetmind.libs.models.RuleEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterpretationScreen(
    onNavigateBack: () -> Unit,
    viewModel: InterpretationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Full Reading") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.hasNoKundli -> {
                    Text(
                        text = "Add a kundli first to see your full reading.",
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                uiState.data != null -> {
                    InterpretationContent(
                        chart = uiState.data!!.chart,
                        narrative = uiState.data!!.analysis?.narrative,
                        positives = uiState.data!!.analysis?.positiveEffects.orEmpty(),
                        challenges = uiState.data!!.analysis?.challenges.orEmpty(),
                        remedies = uiState.data!!.analysis?.remedies.orEmpty()
                    )
                }
            }
        }
    }
}

@Composable
private fun InterpretationContent(
    chart: ChartSummaryResponse,
    narrative: String?,
    positives: List<RuleEffect>,
    challenges: List<RuleEffect>,
    remedies: List<RuleEffect>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ChartSummaryCard(chart) }
        item { DashaCard(chart) }

        if (!narrative.isNullOrBlank()) {
            item { NarrativeCard(narrative) }
        }

        if (positives.isNotEmpty()) {
            item { SectionHeader("Strengths") }
            items(positives, key = { it.ruleId }) { EffectCard(it, isPositive = true) }
        }
        if (challenges.isNotEmpty()) {
            item { SectionHeader("Challenges") }
            items(challenges, key = { it.ruleId }) { EffectCard(it, isPositive = false) }
        }
        if (remedies.isNotEmpty()) {
            item { SectionHeader("Remedies") }
            items(remedies, key = { it.ruleId }) { EffectCard(it, isPositive = true) }
        }
    }
}

@Composable
private fun ChartSummaryCard(chart: ChartSummaryResponse) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Lagna: ${chart.lagna.sign}", style = MaterialTheme.typography.titleMedium)
            Text(
                "Moon nakshatra pada ${chart.moonNakshatra.pada} · ruled by ${chart.moonNakshatra.lord}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("Planetary positions", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(4.dp))
            chart.planets.forEach { (name, planet) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        planet.sign + if (planet.retrograde) " (R)" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DashaCard(chart: ChartSummaryResponse) {
    val current = chart.vimshottari.current ?: return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Current dasha", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            current.mahadasha?.let { Text("Mahadasha: ${it.lord}", style = MaterialTheme.typography.bodyMedium) }
            current.antardasha?.let { Text("Antardasha: ${it.lord}", style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun NarrativeCard(narrative: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = narrative,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun EffectCard(effect: RuleEffect, isPositive: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isPositive) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(effect.ruleName, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(effect.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

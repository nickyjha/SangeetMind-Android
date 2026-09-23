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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.features.astrology.R
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
                title = { Text(stringResource(R.string.interpretation_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.shukra.copy(alpha = 0.14f),
                    titleContentColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.shukra,
                    navigationIconContentColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.shukra
                )
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
                        text = stringResource(R.string.interpretation_no_kundli),
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
            item { SectionHeader(stringResource(R.string.interpretation_section_strengths)) }
            items(positives, key = { it.ruleId }) { EffectCard(it, isPositive = true) }
        }
        if (challenges.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.interpretation_section_challenges)) }
            items(challenges, key = { it.ruleId }) { EffectCard(it, isPositive = false) }
        }
        if (remedies.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.interpretation_section_remedies)) }
            items(remedies, key = { it.ruleId }) { EffectCard(it, isPositive = true) }
        }
    }
}

@Composable
private fun ChartSummaryCard(chart: ChartSummaryResponse) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.interpretation_lagna_fmt, astroTerm(chart.lagna.sign)),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                stringResource(
                    R.string.interpretation_moon_nakshatra_fmt,
                    chart.moonNakshatra.pada,
                    astroTerm(chart.moonNakshatra.lord)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(stringResource(R.string.interpretation_planetary_positions), style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(4.dp))
            chart.planets.forEach { (name, planet) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Text(astroTerm(name), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        if (planet.retrograde) stringResource(R.string.interpretation_sign_retrograde_fmt, astroTerm(planet.sign))
                        else astroTerm(planet.sign),
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
            Text(stringResource(R.string.interpretation_current_dasha), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            current.mahadasha?.let {
                Text(stringResource(R.string.interpretation_mahadasha_fmt, astroTerm(it.lord)), style = MaterialTheme.typography.bodyMedium)
            }
            current.antardasha?.let {
                Text(stringResource(R.string.interpretation_antardasha_fmt, astroTerm(it.lord)), style = MaterialTheme.typography.bodyMedium)
            }
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
            Text(astroTerm(effect.ruleName), style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(effect.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

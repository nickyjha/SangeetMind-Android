package com.sangeetmind.features.astrology.numerology.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.numerology.NumerologyGlossaryViewModel
import com.sangeetmind.libs.models.NumerologyNumberInterpretation
import com.sangeetmind.libs.models.NumerologyNumberSummary
import com.sangeetmind.libs.models.NumerologySystemInfo

/** Read-only numerology reference: what each system is (Pythagorean/Chaldean/Vedic) and
 * what each number means — content that was always fully built on the backend
 * (app/numerology/api.py: /systems, /numbers, /number/{n}) but had no Android UI until
 * now. Reachable from [com.sangeetmind.features.astrology.numerology.ui.NumerologyScreen]'s
 * app bar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumerologyGlossaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: NumerologyGlossaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val budha = LocalGrahaColors.current.budha

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.numerology_number_meanings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = budha.copy(alpha = 0.14f),
                    titleContentColor = budha,
                    navigationIconContentColor = budha
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null && uiState.systems.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = viewModel::refresh) { Text(stringResource(CoreR.string.common_retry)) }
                    }
                }
                else -> {
                    GlossaryContent(
                        systems = uiState.systems,
                        numbers = uiState.numbers,
                        selectedNumber = uiState.selectedNumber,
                        isLoadingDetail = uiState.isLoadingDetail,
                        detail = uiState.detail,
                        onSelectNumber = viewModel::selectNumber
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun GlossaryContent(
    systems: List<NumerologySystemInfo>,
    numbers: List<NumerologyNumberSummary>,
    selectedNumber: Int?,
    isLoadingDetail: Boolean,
    detail: NumerologyNumberInterpretation?,
    onSelectNumber: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(stringResource(R.string.numerology_glossary_systems), style = MaterialTheme.typography.titleMedium)
        }
        items(systems, key = { it.id }) { system ->
            SystemCard(system)
        }
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.numerology_glossary_numbers), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.numerology_glossary_tap_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                numbers.forEach { summary ->
                    FilterChip(
                        selected = summary.number == selectedNumber,
                        onClick = { onSelectNumber(summary.number) },
                        label = { Text("${summary.number} · ${summary.name}") }
                    )
                }
            }
        }
        if (selectedNumber != null) {
            item {
                if (isLoadingDetail) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                } else {
                    detail?.let { NumberDetailCard(it) }
                }
            }
        }
    }
}

@Composable
private fun SystemCard(system: NumerologySystemInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(system.name, style = MaterialTheme.typography.titleMedium)
            Text(
                system.origin,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(system.description, style = MaterialTheme.typography.bodyMedium)
            if (system.bestFor.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    stringResource(R.string.numerology_glossary_best_for_fmt, system.bestFor),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NumberDetailCard(detail: NumerologyNumberInterpretation) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${detail.number} · ${detail.name}", style = MaterialTheme.typography.titleMedium)
            Text(
                "${detail.archetype} · ${astroTerm(detail.element)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(detail.spiritualTheme, style = MaterialTheme.typography.bodyMedium)
            DetailSection(stringResource(R.string.numerology_glossary_core_traits), detail.coreTraits)
            DetailSection(stringResource(R.string.numerology_glossary_strengths), detail.strengths)
            DetailSection(stringResource(R.string.numerology_glossary_challenges), detail.challenges)
            DetailSection(stringResource(R.string.numerology_glossary_growth_advice), detail.growthAdvice)
            detail.contextNotes?.takeIf { it.isNotBlank() }?.let { notes ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    notes,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, items: List<String>) {
    if (items.isEmpty()) return
    Spacer(modifier = Modifier.height(10.dp))
    Text(title, style = MaterialTheme.typography.titleSmall)
    items.forEach { line ->
        Text(
            "· $line",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

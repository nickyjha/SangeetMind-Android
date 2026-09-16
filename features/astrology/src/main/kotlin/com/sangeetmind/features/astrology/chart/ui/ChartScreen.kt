package com.sangeetmind.features.astrology.chart.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.features.astrology.chart.ChartViewModel
import com.sangeetmind.libs.models.ChartSummaryResponse
import com.sangeetmind.libs.models.DashaPeriod
import com.sangeetmind.libs.models.MahadashaPeriod
import com.sangeetmind.libs.models.PlanetInfo
import com.sangeetmind.libs.models.displayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Birth Chart") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
                        text = "Add a kundli first to calculate your Vedic birth chart.",
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                uiState.error != null -> {
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
                        TextButton(onClick = viewModel::refresh) { Text("Retry") }
                    }
                }
                uiState.chart != null -> {
                    ChartContent(
                        personName = uiState.kundli?.fullName,
                        chart = uiState.chart!!,
                        showFullTimeline = uiState.showFullTimeline,
                        onToggleTimeline = viewModel::toggleFullTimeline
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartContent(
    personName: String?,
    chart: ChartSummaryResponse,
    showFullTimeline: Boolean,
    onToggleTimeline: () -> Unit
) {
    val moon = chart.planets["Moon"]
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = personName?.takeIf { it.isNotBlank() } ?: "Your Vedic Birth Chart",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            SummaryStrip(
                lagna = chart.lagna.sign,
                lagnaDegree = chart.lagna.absoluteDms ?: "${chart.lagna.degree.toInt()}°",
                moonSign = moon?.sign.orEmpty(),
                moonDegree = moon?.let { degreeDisplay(it) }.orEmpty(),
                nakshatra = chart.moonNakshatra.displayName(),
                nakshatraDetail = "Pada ${chart.moonNakshatra.pada} · ${chart.moonNakshatra.lord}",
                currentDasha = formatCurrentDasha(chart)
            )
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                NorthIndianHouseChart(
                    lagna = chart.lagna,
                    planets = chart.planets,
                    title = "D1 Rasi Chart",
                    subtitle = "North Indian · whole-sign houses",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        item {
            PlanetListCard(chart.planets)
        }
        item {
            CurrentDashaCard(chart)
        }
        item {
            TextButton(onClick = onToggleTimeline) {
                Text(if (showFullTimeline) "Hide full timeline" else "View full Vimshottari timeline")
            }
        }
        if (showFullTimeline) {
            if (chart.vimshottari.mahadashas.isEmpty()) {
                item {
                    Text(
                        text = "Timeline details are not available for this chart response.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(chart.vimshottari.mahadashas, key = { "${it.lord}-${it.start}" }) { md ->
                    MahadashaCard(md)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryStrip(
    lagna: String,
    lagnaDegree: String,
    moonSign: String,
    moonDegree: String,
    nakshatra: String,
    nakshatraDetail: String,
    currentDasha: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        FlowRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryChip("Lagna", "$lagna $lagnaDegree")
            if (moonSign.isNotBlank()) {
                SummaryChip("Moon", "$moonSign $moonDegree".trim())
            }
            SummaryChip("Nakshatra", "$nakshatra · $nakshatraDetail")
            if (currentDasha.isNotBlank()) {
                SummaryChip("Current dasha", currentDasha)
            }
        }
    }
}

@Composable
private fun SummaryChip(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PlanetListCard(planets: Map<String, PlanetInfo>) {
    val order = listOf(
        "Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu"
    )
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Planetary positions", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            order.forEach { name ->
                val planet = planets[name] ?: return@forEach
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(name, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = buildString {
                            append(planet.sign)
                            degreeDisplay(planet)?.let { append(" $it") }
                            append(dignitySuffix(planet))
                            planet.house?.let { append(" · H$it") }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentDashaCard(chart: ChartSummaryResponse) {
    val current = chart.vimshottari.current ?: return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Current Vimshottari", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            DashaRow("Mahadasha", current.mahadasha)
            DashaRow("Antardasha", current.antardasha)
            DashaRow("Pratyantar", current.resolvedPratyantar)
        }
    }
}

@Composable
private fun DashaRow(label: String, period: DashaPeriod?) {
    if (period == null) return
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label · ${period.lord}", style = MaterialTheme.typography.bodyLarge)
        Text(
            text = "${period.start} → ${period.end}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MahadashaCard(md: MahadashaPeriod) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${md.lord} Mahadasha", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${md.start} → ${md.end}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (md.bhuktis.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                md.bhuktis.forEach { bhukti ->
                    Text(
                        text = "  ${bhukti.lord}: ${bhukti.start} → ${bhukti.end}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

private fun formatCurrentDasha(chart: ChartSummaryResponse): String {
    val current = chart.vimshottari.current ?: return ""
    return listOfNotNull(
        current.mahadasha?.lord,
        current.antardasha?.lord,
        current.resolvedPratyantar?.lord
    ).joinToString(" – ")
}

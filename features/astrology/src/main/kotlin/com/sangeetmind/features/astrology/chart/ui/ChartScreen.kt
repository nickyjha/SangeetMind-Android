package com.sangeetmind.features.astrology.chart.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.chart.ChartViewModel
import com.sangeetmind.libs.models.BhuktiPeriod
import com.sangeetmind.libs.models.ChartSummaryResponse
import com.sangeetmind.libs.models.DIVISIONAL_CHART_META
import com.sangeetmind.libs.models.DashaPeriod
import com.sangeetmind.libs.models.DivisionalChart
import com.sangeetmind.libs.models.LagnaInfo
import com.sangeetmind.libs.models.MahadashaPeriod
import com.sangeetmind.libs.models.PlanetInfo
import com.sangeetmind.libs.models.displayName
import com.sangeetmind.libs.models.toTitleCase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.shani.copy(alpha = 0.14f),
                    titleContentColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.shani,
                    navigationIconContentColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.shani,
                    actionIconContentColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.shani
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
                        personName = uiState.kundli?.fullName?.toTitleCase(),
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
    val availableCharts = chart.availableCharts
    var selectedKey by remember(chart) { mutableStateOf("D1") }
    val selected = availableCharts.firstOrNull { it.first == selectedKey }
        ?: availableCharts.first()
    val meta = DIVISIONAL_CHART_META[selected.first]

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
        if (availableCharts.size > 1) {
            item {
                ScrollableTabRow(
                    selectedTabIndex = availableCharts.indexOfFirst { it.first == selected.first }.coerceAtLeast(0),
                    edgePadding = 0.dp
                ) {
                    availableCharts.forEach { (key, _) ->
                        Tab(
                            selected = key == selected.first,
                            onClick = { selectedKey = key },
                            text = { Text(chartTabLabel(key)) }
                        )
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                NorthIndianHouseChart(
                    lagna = selected.second.lagna,
                    planets = selected.second.planets,
                    title = meta?.first ?: selected.first,
                    subtitle = meta?.second ?: "North Indian · whole-sign houses",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        item {
            PlanetListCard(selected.second.lagna, selected.second.planets)
        }
        if (selected.first == "D1") {
            item {
                CurrentDashaCard(chart)
            }
            item {
                TextButton(onClick = onToggleTimeline) {
                    Text(if (showFullTimeline) "Hide full timeline" else "View full Vimshottari timeline")
                }
            }
        }
        if (selected.first == "D1" && showFullTimeline) {
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

/** "D2" -> "Wealth" — most people don't know varga numbers by heart, so the tab itself
 * shows what the chart is actually for (the D-number + full name still shows once you're
 * on the tab, as the card title/subtitle below). */
private fun chartTabLabel(key: String): String =
    DIVISIONAL_CHART_META[key]?.second?.substringBefore(',')?.trim() ?: key

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
            SummaryChip("Nakshatra", "$nakshatra · $nakshatraDetail", highlight = true)
            if (currentDasha.isNotBlank()) {
                SummaryChip("Current dasha", currentDasha)
            }
        }
    }
}

@Composable
private fun SummaryChip(label: String, value: String, highlight: Boolean = false) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

/** A small colored pill — used for dignity flags (Retrograde/Combust/…) and dasha status tags. */
@Composable
private fun Chip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

/** Dignity flags as (label, color) pairs — colors reuse the graha accents so a planet's
 * strength/weakness reads at a glance instead of via the old `*^↑↓□` symbol suffix. */
@Composable
private fun dignityChips(planet: PlanetInfo): List<Pair<String, Color>> {
    val graha = LocalGrahaColors.current
    val neutral = MaterialTheme.colorScheme.onSurfaceVariant
    return buildList {
        if (planet.retrograde) add("Retrograde" to neutral)
        if (planet.combust) add("Combust" to graha.mangala)
        if (planet.exalted) add("Exalted" to graha.budha)
        if (planet.debilitated) add("Debilitated" to graha.guru)
        if (planet.vargottama) add("Vargottama" to graha.shani)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanetListCard(lagna: LagnaInfo, planets: Map<String, PlanetInfo>) {
    val order = listOf(
        "Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu"
    )
    // Sorted by house (H1 first) rather than the fixed Sun→Ketu order, so the list reads
    // the same way the North Indian chart above it does. No section headers — the house
    // number rides along inline instead, to keep this a single clean list.
    val sorted = order.mapNotNull { name -> planets[name]?.let { name to it } }
        .sortedBy { (_, info) -> info.house ?: signToHouseNumber(lagna.sign, info.sign) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Planetary positions", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            sorted.forEach { (name, planet) ->
                val house = planet.house ?: signToHouseNumber(lagna.sign, planet.sign)
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "H$house",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(name, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            text = buildString {
                                append(planet.sign)
                                degreeDisplay(planet)?.let { append(" $it") }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val chips = dignityChips(planet)
                    if (chips.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp, start = 28.dp)
                        ) {
                            chips.forEach { (label, color) -> Chip(label, color) }
                        }
                    }
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
            text = "${fmtIso(period.start)} → ${fmtIso(period.end)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun parseIso(value: String): Instant? = runCatching { Instant.parse(value) }.getOrNull()

private val DASHA_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy").withZone(ZoneId.systemDefault())

private fun fmtIso(value: String): String = parseIso(value)?.let { DASHA_DATE_FORMAT.format(it) } ?: value

private fun yearsBetween(start: String, end: String): Long? {
    val s = parseIso(start) ?: return null
    val e = parseIso(end) ?: return null
    return Math.round(ChronoUnit.DAYS.between(s, e) / 365.25)
}

/** Mahadasha card, colored by where "now" falls: the current one (Guru amber), a past
 * one (neutral), or an upcoming one (Shani indigo) — mirrors the amber/grey/blue
 * treatment on the website's timeline instead of a flat, undifferentiated list. */
@Composable
private fun MahadashaCard(md: MahadashaPeriod) {
    val now = Instant.now()
    val start = parseIso(md.start)
    val end = parseIso(md.end)
    val isCurrent = start != null && end != null && !now.isBefore(start) && !now.isAfter(end)
    val isPast = end != null && now.isAfter(end)
    val graha = LocalGrahaColors.current

    val accent = when {
        isCurrent -> graha.guru
        isPast -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> graha.shani
    }
    val containerColor = when {
        isCurrent -> graha.guru.copy(alpha = 0.12f)
        isPast -> MaterialTheme.colorScheme.surfaceVariant
        else -> graha.shani.copy(alpha = 0.1f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${md.lord} Mahadasha",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPast) MaterialTheme.colorScheme.onSurface else accent
                )
                if (isCurrent) Chip("Current", accent)
                if (md.partial) Chip("Partial", MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = buildString {
                    append(fmtIso(md.start)); append(" → "); append(fmtIso(md.end))
                    yearsBetween(md.start, md.end)?.let { append(" · $it yrs") }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (md.bhuktis.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                md.bhuktis.forEach { bhukti -> BhuktiRow(bhukti, isMahadashaCurrent = isCurrent, accent = accent) }
            }
        }
    }
}

@Composable
private fun BhuktiRow(bhukti: BhuktiPeriod, isMahadashaCurrent: Boolean, accent: Color) {
    val now = Instant.now()
    val start = parseIso(bhukti.start)
    val end = parseIso(bhukti.end)
    val isCurrent = isMahadashaCurrent && start != null && end != null && !now.isBefore(start) && !now.isAfter(end)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isCurrent) accent.copy(alpha = 0.14f) else Color.Transparent)
            .padding(vertical = 6.dp, horizontal = if (isCurrent) 8.dp else 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = if (isCurrent) "${bhukti.lord} Antardasha  ●" else "${bhukti.lord} Antardasha",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCurrent) accent else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${fmtIso(bhukti.start)} → ${fmtIso(bhukti.end)}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isCurrent) accent else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (bhukti.pratyantars.isNotEmpty()) {
            Column(modifier = Modifier.padding(start = 12.dp, top = 4.dp)) {
                bhukti.pratyantars.forEach { pr ->
                    val prStart = parseIso(pr.start)
                    val prEnd = parseIso(pr.end)
                    val prCurrent = isCurrent && prStart != null && prEnd != null &&
                        !now.isBefore(prStart) && !now.isAfter(prEnd)
                    Text(
                        text = buildString {
                            append(pr.lord); append(": "); append(fmtIso(pr.start)); append(" → "); append(fmtIso(pr.end))
                            if (prCurrent) append("  ●")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (prCurrent) accent else MaterialTheme.colorScheme.onSurfaceVariant
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

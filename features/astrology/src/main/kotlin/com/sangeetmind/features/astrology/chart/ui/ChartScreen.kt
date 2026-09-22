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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.FilterChip
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
import com.sangeetmind.core.ui.theme.GrahaColors
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.chart.ChartViewModel
import com.sangeetmind.libs.models.BhavabalaHouse
import com.sangeetmind.libs.models.BhuktiPeriod
import com.sangeetmind.libs.models.CharaAntardasha
import com.sangeetmind.libs.models.CharaDashaInfo
import com.sangeetmind.libs.models.CharaMahadasha
import com.sangeetmind.libs.models.ChartAshtakvarga
import com.sangeetmind.libs.models.ChartBhavabala
import com.sangeetmind.libs.models.ChartChalit
import com.sangeetmind.libs.models.ChartDoshas
import com.sangeetmind.libs.models.ChartFriendship
import com.sangeetmind.libs.models.ChartHouses
import com.sangeetmind.libs.models.ChartJaimini
import com.sangeetmind.libs.models.ChartKp
import com.sangeetmind.libs.models.ChartLalKitab
import com.sangeetmind.libs.models.ChartMoonChart
import com.sangeetmind.libs.models.ChartNarratives
import com.sangeetmind.libs.models.ChartShadbala
import com.sangeetmind.libs.models.ChartSummaryResponse
import com.sangeetmind.libs.models.ShadbalaRanking
import com.sangeetmind.libs.models.DIVISIONAL_CHART_META
import com.sangeetmind.libs.models.DashaPeriod
import com.sangeetmind.libs.models.DivisionalChart
import com.sangeetmind.libs.models.LagnaInfo
import com.sangeetmind.libs.models.MahadashaPeriod
import com.sangeetmind.libs.models.PlanetInfo
import com.sangeetmind.libs.models.SadesatiPeriod
import com.sangeetmind.libs.models.YoginiAntardasha
import com.sangeetmind.libs.models.YoginiInfo
import com.sangeetmind.libs.models.YoginiMahadasha
import com.sangeetmind.libs.models.displayName
import com.sangeetmind.libs.models.toTitleCase
import java.time.Instant
import java.time.LocalDate
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
    var dashaSystem by remember(chart) { mutableStateOf(DashaSystem.VIMSHOTTARI) }
    var chartSection by remember(chart) { mutableStateOf(ChartSection.OVERVIEW) }
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
                ChartSectionSelector(chartSection, onSelect = { chartSection = it })
            }
            when (chartSection) {
                ChartSection.OVERVIEW -> {
                    item {
                        DoshaCard(chart.doshas)
                    }
                    item {
                        NarrativesCard(chart.narratives)
                    }
                }
                ChartSection.STRENGTH -> {
                    item {
                        AshtakvargaCard(chart.ashtakvarga)
                    }
                    item {
                        FriendshipCard(chart.friendship)
                    }
                    item {
                        ShadbalaCard(chart.shadbala)
                    }
                    item {
                        BhavabalaCard(chart.bhavabala)
                    }
                }
                ChartSection.DASHA -> {
                    item {
                        DashaSystemSelector(dashaSystem, onSelect = { dashaSystem = it })
                    }
                    item {
                        CurrentDashaCard(chart, dashaSystem)
                    }
                    item {
                        TextButton(onClick = onToggleTimeline) {
                            Text(if (showFullTimeline) "Hide full ${dashaSystem.label} timeline" else "View full ${dashaSystem.label} timeline")
                        }
                    }
                }
                ChartSection.KP_JAIMINI -> {
                    item {
                        KpCard(chart.kp)
                    }
                    item {
                        JaiminiCard(chart.jaimini)
                    }
                }
                ChartSection.LAL_KITAB -> {
                    item {
                        LalKitabCard(chart.lalKitab)
                    }
                }
                ChartSection.HOUSES -> {
                    val chalitPlanets = chart.chalit.toPlanetInfoMap()
                    if (chalitPlanets.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                NorthIndianHouseChart(
                                    lagna = chart.chalit.lagna,
                                    planets = chalitPlanets,
                                    title = "Chalit Chart",
                                    subtitle = "Bhava chart · Placidus house cusps",
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        item {
                            PlanetListCard(chart.chalit.lagna, chalitPlanets)
                        }
                    }
                    val moonChartPlanets = chart.moonChart.toPlanetInfoMap()
                    if (moonChartPlanets.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                NorthIndianHouseChart(
                                    lagna = chart.moonChart.lagna,
                                    planets = moonChartPlanets,
                                    title = "Chandra Kundli",
                                    subtitle = "Moon chart · whole-sign houses from Moon lagna",
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        item {
                            PlanetListCard(chart.moonChart.lagna, moonChartPlanets)
                        }
                    }
                    item {
                        HouseCuspsCard(chart.houses)
                    }
                }
            }
        }
        if (selected.first == "D1" && chartSection == ChartSection.DASHA && showFullTimeline) {
            val isEmpty = when (dashaSystem) {
                DashaSystem.VIMSHOTTARI -> chart.vimshottari.mahadashas.isEmpty()
                DashaSystem.YOGINI -> chart.yogini.mahadashas.isEmpty()
                DashaSystem.CHARA -> chart.charaDasha.mahadashas.isEmpty()
            }
            if (isEmpty) {
                item {
                    Text(
                        text = "Timeline details are not available for this chart response.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else when (dashaSystem) {
                DashaSystem.VIMSHOTTARI -> items(chart.vimshottari.mahadashas, key = { "vim-${it.lord}-${it.start}" }) { md ->
                    MahadashaCard(md)
                }
                DashaSystem.YOGINI -> items(chart.yogini.mahadashas, key = { "yog-${it.yogini}-${it.start}" }) { md ->
                    YoginiMahadashaCard(md)
                }
                DashaSystem.CHARA -> items(chart.charaDasha.mahadashas, key = { "chr-${it.sign}-${it.start}" }) { md ->
                    CharaMahadashaCard(md)
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

/** D1-only content used to stack as one long scroll — Doshas, Ashtakvarga, Friendship,
 * Shadbala, Bhavabala, KP, Jaimini, Lal Kitab, and Dasha all in a row. AstroSage (the
 * deepest competitor per the roadmap research) organizes this same feature set as
 * tabs/icons the user taps into rather than an infinite scroll — this mirrors that. */
private enum class ChartSection(val label: String) {
    OVERVIEW("Overview"), STRENGTH("Strength"), DASHA("Dasha"), KP_JAIMINI("KP · Jaimini"),
    LAL_KITAB("Lal Kitab"), HOUSES("Houses")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChartSectionSelector(selected: ChartSection, onSelect: (ChartSection) -> Unit) {
    ScrollableTabRow(
        selectedTabIndex = ChartSection.entries.indexOf(selected),
        edgePadding = 0.dp
    ) {
        ChartSection.entries.forEach { section ->
            Tab(
                selected = section == selected,
                onClick = { onSelect(section) },
                text = { Text(section.label) }
            )
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

/** Manglik/Kalsarpa/Sade Sati — already computed by the backend on every /v1/chart
 * response, just never surfaced on Android before. Reuses the same [Chip] pill used for
 * planet dignity flags, and the same graha semantic colors (Mangala/vermilion = flagged,
 * Budha/jade = clear) already used there for Combust/Exalted. */
@Composable
private fun DoshaCard(doshas: ChartDoshas) {
    val graha = LocalGrahaColors.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Doshas", style = MaterialTheme.typography.titleMedium)

            val manglik = doshas.manglik
            DoshaRow(
                label = "Manglik",
                isFlagged = manglik.effectivePresent,
                statusText = when {
                    manglik.effectivePresent -> "Present"
                    manglik.cancelled -> "Cancelled"
                    else -> "Not present"
                },
                detail = manglik.summary.ifBlank { null },
                flaggedColor = graha.mangala,
                clearColor = graha.budha
            )

            val kalsarpa = doshas.kalsarpa
            DoshaRow(
                label = "Kalsarpa",
                isFlagged = kalsarpa.present,
                statusText = if (kalsarpa.present) "Present" else "Not present",
                detail = if (kalsarpa.present) {
                    kalsarpa.yogaFullName
                } else {
                    "No Kalsarpa Yoga in this chart."
                },
                flaggedColor = graha.mangala,
                clearColor = graha.budha
            )

            val sadesati = doshas.sadesati
            val active = sadesati.activeOnToday
            val next = if (active == null) {
                sadesati.periods.firstOrNull { it.startDate > todayIsoDate() }
            } else null
            DoshaRow(
                label = "Sade Sati",
                isFlagged = active != null,
                statusText = if (active != null) "Active" else "Not active",
                detail = when {
                    active != null -> "${active.phase ?: active.kind} · until ${fmtLocalDate(active.endDate)}"
                    next != null -> "Next: ${next.kind} starts ${fmtLocalDate(next.startDate)}"
                    else -> "No upcoming Sade Sati or Small Panoti period found."
                },
                flaggedColor = graha.mangala,
                clearColor = graha.budha
            )
        }
    }
}

@Composable
private fun DoshaRow(
    label: String,
    isFlagged: Boolean,
    statusText: String,
    detail: String?,
    flaggedColor: Color,
    clearColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Chip(statusText, if (isFlagged) flaggedColor else clearColor)
        }
        if (!detail.isNullOrBlank()) {
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun todayIsoDate(): String = LocalDate.now().toString()

private fun fmtLocalDate(value: String): String =
    runCatching { DASHA_DATE_FORMAT.format(LocalDate.parse(value)) }.getOrDefault(value)

private val ZODIAC_ORDER = listOf(
    "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
    "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
)

/** Sarva Ashtakvarga — per-sign bindu strength, as a set of bars scaled to this chart's
 * own max (not a fixed theoretical max), so the strongest/weakest signs stand out
 * regardless of scale. >=30 reads as strong, <=24 as weak — a simplified 3-tier read,
 * not a strict classical grading. */
@Composable
private fun AshtakvargaCard(ashtakvarga: ChartAshtakvarga) {
    val sav = ashtakvarga.sav
    if (sav.bindusBySign.isEmpty()) return
    val graha = LocalGrahaColors.current
    val maxBindu = (sav.bindusBySign.values.maxOrNull() ?: 1).coerceAtLeast(1)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ashtakvarga", style = MaterialTheme.typography.titleMedium)
            Text(
                "Sarva bindu strength by sign · total ${sav.totalBindus}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            ZODIAC_ORDER.forEach { sign ->
                val bindus = sav.bindusBySign[sign] ?: return@forEach
                val barColor = when {
                    bindus >= 30 -> graha.budha
                    bindus <= 24 -> graha.mangala
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        sign,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(88.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = (bindus.toFloat() / maxBindu).coerceIn(0.05f, 1f))
                                .clip(RoundedCornerShape(4.dp))
                                .background(barColor)
                        )
                    }
                    Text(
                        "$bindus",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(28.dp).padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

/** Panchadha (five-fold) planetary friendship — pick a graha, see how the other six treat
 * it. Intimate/Friend read as favorable (Budha/jade), Enemy/Bitter Enemy as unfavorable
 * (Mangala/vermilion) — the same pass/fail color language used for doshas and dignity. */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FriendshipCard(friendship: ChartFriendship) {
    if (friendship.relations.isEmpty() || friendship.planets.isEmpty()) return
    var selected by remember(friendship) { mutableStateOf(friendship.planets.first()) }
    val row = friendship.relations[selected].orEmpty()
    val graha = LocalGrahaColors.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Planetary Friendship", style = MaterialTheme.typography.titleMedium)
            Text(
                "How the other grahas treat $selected",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                friendship.planets.forEach { planet ->
                    FilterChip(
                        selected = planet == selected,
                        onClick = { selected = planet },
                        label = { Text(planet) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            row.forEach { (other, relation) ->
                if (other == selected) return@forEach
                val color = when (relation) {
                    "Intimate", "Friend" -> graha.budha
                    "Enemy", "Bitter Enemy" -> graha.mangala
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(other, style = MaterialTheme.typography.bodyMedium)
                    Chip(relation, color)
                }
            }
        }
    }
}

/** Six-fold planetary strength, strongest graha first. Rupas (virupas/60) is the human
 * unit shown; the bar is driven by virupas so components with very different totals still
 * compare cleanly (app/services/shadbala.py — "simplified_parashari", not full BPHS
 * arc-minute tables). */
@Composable
private fun ShadbalaCard(shadbala: ChartShadbala) {
    if (shadbala.planets.isEmpty()) return
    val graha = LocalGrahaColors.current
    val ranked = shadbala.ranking.ifEmpty {
        shadbala.planets.entries
            .map { (planet, data) -> ShadbalaRanking(planet, data.totalVirupas) }
            .sortedByDescending { it.totalVirupas }
    }
    val maxVirupas = (ranked.maxOfOrNull { it.totalVirupas } ?: 1.0).coerceAtLeast(1.0)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Shadbala", style = MaterialTheme.typography.titleMedium)
            Text(
                "Six-fold planetary strength, strongest first",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            ranked.forEach { entry ->
                val planet = shadbala.planets[entry.planet] ?: return@forEach
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        entry.planet,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(72.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(
                                    fraction = (entry.totalVirupas / maxVirupas).toFloat().coerceIn(0.05f, 1f)
                                )
                                .clip(RoundedCornerShape(4.dp))
                                .background(grahaColorFor(entry.planet, graha))
                        )
                    }
                    Text(
                        "${planet.totalRupas} rupas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(72.dp).padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

/** House-by-house strength in house order (1..12), so it reads the same way the North
 * Indian chart and planet list above it do — the strongest house is graha.budha-tinted
 * (app/services/bhavabala.py: SAV-in-house-sign + house lord's Shadbala). */
@Composable
private fun BhavabalaCard(bhavabala: ChartBhavabala) {
    if (bhavabala.houses.isEmpty()) return
    val houses = bhavabala.houses.sortedBy { it.house }
    val maxVirupas = (houses.maxOfOrNull { it.totalVirupas } ?: 1.0).coerceAtLeast(1.0)
    val graha = LocalGrahaColors.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Bhavabala", style = MaterialTheme.typography.titleMedium)
            Text(
                "House strength · strongest is House ${bhavabala.strongestHouse ?: "-"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            houses.forEach { house ->
                val barColor = if (house.house == bhavabala.strongestHouse) {
                    graha.budha
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "H${house.house} ${house.lord}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(88.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(
                                    fraction = (house.totalVirupas / maxVirupas).toFloat().coerceIn(0.05f, 1f)
                                )
                                .clip(RoundedCornerShape(4.dp))
                                .background(barColor)
                        )
                    }
                    Text(
                        house.totalVirupas.toInt().toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(36.dp).padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

private fun grahaColorFor(planet: String, graha: GrahaColors): Color = when (planet) {
    "Sun" -> graha.surya
    "Moon" -> graha.chandra
    "Mars" -> graha.mangala
    "Mercury" -> graha.budha
    "Jupiter" -> graha.guru
    "Venus" -> graha.shukra
    "Saturn" -> graha.shani
    else -> graha.rahu
}

/** KP (Krishnamurti Paddhati) — a toggle between cusp sub-lords and per-planet
 * significator houses, matching AstroSage AI's dedicated KP System tab
 * (app/services/kp_system.py). */
private enum class KpView(val label: String) { CUSPS("Cusps"), SIGNIFICATORS("Significators") }

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun KpCard(kp: ChartKp) {
    if (kp.cusps.isEmpty()) return
    var view by remember(kp) { mutableStateOf(KpView.CUSPS) }
    val planetOrder = listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu")
        .filter { it in kp.significators }
    var selectedPlanet by remember(kp) { mutableStateOf(planetOrder.firstOrNull().orEmpty()) }
    val graha = LocalGrahaColors.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("KP System", style = MaterialTheme.typography.titleMedium)
            Text(
                "${kp.ayanamsaName} ayanamsa · ${"%.2f".format(kp.ayanamsaDeg)}°",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KpView.entries.forEach { v ->
                    FilterChip(selected = v == view, onClick = { view = v }, label = { Text(v.label) })
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            when (view) {
                KpView.CUSPS -> kp.cusps.sortedBy { it.house }.forEach { cusp ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("H${cusp.house} ${cusp.sign}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${cusp.subLord} / ${cusp.subSubLord}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                KpView.SIGNIFICATORS -> {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        planetOrder.forEach { planet ->
                            FilterChip(
                                selected = planet == selectedPlanet,
                                onClick = { selectedPlanet = planet },
                                label = { Text(planet) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val houses = kp.significators[selectedPlanet].orEmpty()
                    if (houses.isEmpty()) {
                        Text(
                            "No significator houses for $selectedPlanet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            houses.forEach { house -> Chip("H$house", grahaColorFor(selectedPlanet, graha)) }
                        }
                    }
                }
            }
        }
    }
}

/** Chara karakas (7 planets ranked by degree-in-sign, Atmakaraka first) plus the
 * Karakamsa/Swamsa lagna summary — matches AstroSage AI's Karakamsa tab
 * (app/services/jaimini_charts.py). */
@Composable
private fun JaiminiCard(jaimini: ChartJaimini) {
    if (jaimini.charaKarakas.isEmpty()) return
    val graha = LocalGrahaColors.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Jaimini", style = MaterialTheme.typography.titleMedium)
            Text(
                "Karakamsa ${jaimini.karakamsa.lagnaSign} · Swamsa ${jaimini.swamsa.lagnaSign}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            jaimini.charaKarakas.forEach { karaka ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            karaka.abbrev,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(32.dp)
                        )
                        Text(karaka.karaka, style = MaterialTheme.typography.bodyMedium)
                    }
                    Chip(
                        "${karaka.planet} ${"%.1f".format(karaka.degreeInSign)}°",
                        grahaColorFor(karaka.planet, graha)
                    )
                }
            }
        }
    }
}

/** Lal Kitab — the backend flags this as its own scoped preview (`system_note`, shown
 * verbatim rather than presented as full classical Lal Kitab): karmic-debt (rin) yoga
 * flags and short remedies from the whole-sign D1 placements
 * (app/services/lal_kitab.py). */
@Composable
private fun LalKitabCard(lalKitab: ChartLalKitab) {
    if (lalKitab.placements.isEmpty()) return
    val graha = LocalGrahaColors.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Lal Kitab", style = MaterialTheme.typography.titleMedium)
            if (lalKitab.systemNote.isNotBlank()) {
                Text(
                    lalKitab.systemNote,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (lalKitab.rinYogas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Karmic debts (Rin)", style = MaterialTheme.typography.titleSmall)
                lalKitab.rinYogas.forEach { yoga ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Chip(yoga.planet, grahaColorFor(yoga.planet, graha))
                        Text(
                            "${yoga.rinType} · H${yoga.house}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (lalKitab.remedies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Remedies", style = MaterialTheme.typography.titleSmall)
                lalKitab.remedies.forEach { remedy ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text(
                            remedy.planet,
                            style = MaterialTheme.typography.bodyMedium,
                            color = grahaColorFor(remedy.planet, graha),
                            modifier = Modifier.width(72.dp)
                        )
                        Text(remedy.remedy, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            if (lalKitab.predictions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Placement notes", style = MaterialTheme.typography.titleSmall)
                lalKitab.predictions.forEach { prediction ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text(
                            "${prediction.planet} H${prediction.house}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = grahaColorFor(prediction.planet, graha),
                            modifier = Modifier.width(96.dp)
                        )
                        Text(prediction.text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            if (lalKitab.rinYogas.isEmpty() && lalKitab.remedies.isEmpty() && lalKitab.predictions.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No debt yogas or remedies flagged for this chart.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Static, non-LLM narrative text — nakshatra/lagna/dasha phal summaries and a
 * per-planet consideration list. Lighter than ChatMind/interpretation; the backend's own
 * `disclaimer` is shown verbatim (app/services/kundli_narratives.py). */
@Composable
private fun NarrativesCard(narratives: ChartNarratives) {
    if (narratives.nakshatraPhal.text.isBlank() && narratives.ascendantSummary.text.isBlank()) return
    val graha = LocalGrahaColors.current
    val dashaPhal = narratives.vimshottariMahadashaPhal

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Reading", style = MaterialTheme.typography.titleMedium)
            if (narratives.nakshatraPhal.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "${narratives.nakshatraPhal.nakshatra} · Pada ${narratives.nakshatraPhal.pada ?: "-"} · " +
                        narratives.nakshatraPhal.lord,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(narratives.nakshatraPhal.text, style = MaterialTheme.typography.bodyMedium)
            }
            if (narratives.ascendantSummary.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("${narratives.ascendantSummary.sign} Ascendant", style = MaterialTheme.typography.titleSmall)
                Text(narratives.ascendantSummary.text, style = MaterialTheme.typography.bodyMedium)
            }
            val mahadashaText = dashaPhal.mahadashaText
            if (!mahadashaText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "${dashaPhal.currentMahadasha} Mahadasha",
                    style = MaterialTheme.typography.titleSmall,
                    color = dashaPhal.currentMahadasha?.let { grahaColorFor(it, graha) }
                        ?: MaterialTheme.colorScheme.onSurface
                )
                Text(mahadashaText, style = MaterialTheme.typography.bodyMedium)
                dashaPhal.antardashaNote?.takeIf { it.isNotBlank() }?.let { note ->
                    Text(
                        note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (narratives.planetConsiderations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Planet placements", style = MaterialTheme.typography.titleSmall)
                narratives.planetConsiderations.forEach { line ->
                    Text(
                        "· $line",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            if (narratives.disclaimer.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    narratives.disclaimer,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Reuses [NorthIndianHouseChart]/[PlanetListCard] — built for D1..D60 [PlanetInfo]
 * maps — for the Chalit chart too, rather than a parallel chart-drawing implementation.
 * Chalit doesn't compute dignity flags (retrograde/combust/exalted/debilitated/
 * vargottama), so those default false here — correctly, since they weren't actually
 * evaluated for this house system, not a bug. */
private fun ChartChalit.toPlanetInfoMap(): Map<String, PlanetInfo> =
    planets.mapValues { (_, p) ->
        PlanetInfo(sign = p.sign, degree = p.degree, absolute = "", absoluteDms = p.absoluteDms, house = p.house)
    }

/** Same [PlanetInfo] reuse as [ChartChalit.toPlanetInfoMap], but the Moon chart does
 * compute retrograde/combust (unlike Chalit), so those carry through. */
private fun ChartMoonChart.toPlanetInfoMap(): Map<String, PlanetInfo> =
    planets.mapValues { (_, p) ->
        PlanetInfo(
            sign = p.sign,
            degree = p.degree,
            absolute = "",
            absoluteDms = p.absoluteDms,
            house = p.house,
            retrograde = p.retrograde,
            combust = p.combust
        )
    }

/** The 12 Placidus house cusps behind `houses.planet_houses` — same computation KP's
 * Cusps view uses, minus sub-lords, at the chart's default (Lahiri) ayanamsa rather than
 * KP's Krishnamurti one (app/services/houses.py: build_houses_block). */
@Composable
private fun HouseCuspsCard(houses: ChartHouses) {
    if (houses.cusps.isEmpty()) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("House Cusps", style = MaterialTheme.typography.titleMedium)
            Text(
                "Placidus cusps · active house system: ${houses.system}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            houses.cusps.sortedBy { it.house }.forEach { cusp ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("H${cusp.house}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${cusp.sign} ${cusp.absoluteDms ?: "${cusp.degree.toInt()}°"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** A second/third dasha system alongside Vimshottari — same underlying birth chart, a
 * different lens on "when." Matches AstroSage AI's dasha-system toggle. */
private enum class DashaSystem(val label: String) {
    VIMSHOTTARI("Vimshottari"), YOGINI("Yogini"), CHARA("Chara")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashaSystemSelector(selected: DashaSystem, onSelect: (DashaSystem) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DashaSystem.entries.forEach { system ->
            FilterChip(
                selected = selected == system,
                onClick = { onSelect(system) },
                label = { Text(system.label) }
            )
        }
    }
}

@Composable
private fun CurrentDashaCard(chart: ChartSummaryResponse, system: DashaSystem) {
    when (system) {
        DashaSystem.VIMSHOTTARI -> {
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
        DashaSystem.YOGINI -> {
            val md = currentYoginiMahadasha(chart.yogini) ?: return
            val ad = currentYoginiAntardasha(md)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Yogini", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("Mahadasha · ${md.yogini} (${md.lord})", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${fmtIso(md.start)} → ${fmtIso(md.end)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (ad != null) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("Antardasha · ${ad.yogini} (${ad.lord})", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "${fmtIso(ad.start)} → ${fmtIso(ad.end)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        DashaSystem.CHARA -> {
            val md = currentCharaMahadasha(chart.charaDasha) ?: return
            val ad = currentCharaAntardasha(md)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Chara", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("Mahadasha · ${md.sign}", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${fmtIso(md.start)} → ${fmtIso(md.end)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (ad != null) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("Antardasha · ${ad.sign}", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "${fmtIso(ad.start)} → ${fmtIso(ad.end)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun currentYoginiMahadasha(info: YoginiInfo): YoginiMahadasha? {
    val now = Instant.now()
    return info.mahadashas.firstOrNull { md ->
        val s = parseIso(md.start); val e = parseIso(md.end)
        s != null && e != null && !now.isBefore(s) && !now.isAfter(e)
    }
}

private fun currentYoginiAntardasha(md: YoginiMahadasha): YoginiAntardasha? {
    val now = Instant.now()
    return md.antardashas.firstOrNull { ad ->
        val s = parseIso(ad.start); val e = parseIso(ad.end)
        s != null && e != null && !now.isBefore(s) && !now.isAfter(e)
    }
}

private fun currentCharaMahadasha(info: CharaDashaInfo): CharaMahadasha? {
    val now = Instant.now()
    return info.mahadashas.firstOrNull { md ->
        val s = parseIso(md.start); val e = parseIso(md.end)
        s != null && e != null && !now.isBefore(s) && !now.isAfter(e)
    }
}

private fun currentCharaAntardasha(md: CharaMahadasha): CharaAntardasha? {
    val now = Instant.now()
    return md.antardashas.firstOrNull { ad ->
        val s = parseIso(ad.start); val e = parseIso(ad.end)
        s != null && e != null && !now.isBefore(s) && !now.isAfter(e)
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

/** Yogini mahadasha card — same current/past/upcoming color language as
 * [MahadashaCard], with its 8 antardashas listed inline like bhuktis. */
@Composable
private fun YoginiMahadashaCard(md: YoginiMahadasha) {
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
                    "${md.yogini} (${md.lord})",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPast) MaterialTheme.colorScheme.onSurface else accent
                )
                if (isCurrent) Chip("Current", accent)
                if (md.partial) Chip("Partial", MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = buildString {
                    append(fmtIso(md.start)); append(" → "); append(fmtIso(md.end))
                    append(" · ${"%.1f".format(md.years)} yrs")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (md.antardashas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                md.antardashas.forEach { ad ->
                    val adStart = parseIso(ad.start)
                    val adEnd = parseIso(ad.end)
                    val adCurrent = isCurrent && adStart != null && adEnd != null &&
                        !now.isBefore(adStart) && !now.isAfter(adEnd)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (adCurrent) accent.copy(alpha = 0.14f) else Color.Transparent)
                            .padding(vertical = 6.dp, horizontal = if (adCurrent) 8.dp else 0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (adCurrent) "${ad.yogini} (${ad.lord})  ●" else "${ad.yogini} (${ad.lord})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (adCurrent) accent else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${fmtIso(ad.start)} → ${fmtIso(ad.end)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (adCurrent) accent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/** Chara mahadasha card — sign-based periods (Jaimini), same visual language as
 * [MahadashaCard]/[YoginiMahadashaCard]. */
@Composable
private fun CharaMahadashaCard(md: CharaMahadasha) {
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
                    md.sign,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPast) MaterialTheme.colorScheme.onSurface else accent
                )
                if (isCurrent) Chip("Current", accent)
                if (md.partial) Chip("Partial", MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = buildString {
                    append(fmtIso(md.start)); append(" → "); append(fmtIso(md.end))
                    append(" · ${"%.1f".format(md.years)} yrs")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (md.antardashas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                md.antardashas.forEach { ad ->
                    val adStart = parseIso(ad.start)
                    val adEnd = parseIso(ad.end)
                    val adCurrent = isCurrent && adStart != null && adEnd != null &&
                        !now.isBefore(adStart) && !now.isAfter(adEnd)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (adCurrent) accent.copy(alpha = 0.14f) else Color.Transparent)
                            .padding(vertical = 6.dp, horizontal = if (adCurrent) 8.dp else 0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (adCurrent) "${ad.sign}  ●" else ad.sign,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (adCurrent) accent else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${fmtIso(ad.start)} → ${fmtIso(ad.end)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (adCurrent) accent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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

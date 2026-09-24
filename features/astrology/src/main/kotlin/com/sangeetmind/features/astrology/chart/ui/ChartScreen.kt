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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.annotation.StringRes
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.core.ui.theme.GrahaColors
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.chart.ChartViewModel
import com.sangeetmind.libs.models.ArudhaPada
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
import com.sangeetmind.libs.models.DashaPeriod
import com.sangeetmind.libs.models.DivisionalChart
import com.sangeetmind.libs.models.LagnaInfo
import com.sangeetmind.libs.models.MahadashaPeriod
import com.sangeetmind.libs.models.MutualAspect
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
                title = { Text(stringResource(R.string.chart_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(CoreR.string.common_refresh))
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
                        text = stringResource(R.string.chart_add_kundli_first),
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
                        TextButton(onClick = viewModel::refresh) { Text(stringResource(CoreR.string.common_retry)) }
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
    val meta = divisionalChartMeta(selected.first)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = personName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.chart_default_heading),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            SummaryStrip(
                lagna = astroTerm(chart.lagna.sign),
                lagnaDegree = chart.lagna.absoluteDms ?: "${chart.lagna.degree.toInt()}°",
                moonSign = astroTerm(moon?.sign.orEmpty()),
                moonDegree = moon?.let { degreeDisplay(it) }.orEmpty(),
                nakshatra = astroTerm(chart.moonNakshatra.displayName()),
                nakshatraDetail = stringResource(
                    R.string.chart_nakshatra_detail,
                    chart.moonNakshatra.pada,
                    astroTerm(chart.moonNakshatra.lord)
                ),
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
                    subtitle = meta?.second ?: stringResource(R.string.chart_subtitle_north_indian),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        item {
            PlanetListCard(selected.second.lagna, selected.second.planets)
        }
        // bhavabala (house sign + lord) is only computed for D1.
        if (selected.first == "D1" && chart.bhavabala.houses.isNotEmpty()) {
            item {
                HouseDetailsCard(chart.planets, chart.bhavabala)
            }
        }
        if (selected.first == "D1" && chart.mutualAspects.isNotEmpty()) {
            item {
                MutualAspectsCard(chart.mutualAspects)
            }
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
                            val systemLabel = stringResource(dashaSystem.labelRes)
                            Text(
                                if (showFullTimeline) stringResource(R.string.chart_hide_timeline, systemLabel)
                                else stringResource(R.string.chart_view_timeline, systemLabel)
                            )
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
                    item {
                        ArudhaPadasCard(chart.jaimini.arudhaPadas)
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
                                    title = stringResource(R.string.chart_chalit_title),
                                    subtitle = stringResource(R.string.chart_chalit_subtitle),
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
                                    title = stringResource(R.string.chart_moon_chart_title),
                                    subtitle = stringResource(R.string.chart_moon_chart_subtitle),
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
                        text = stringResource(R.string.chart_timeline_unavailable),
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
@Composable
private fun chartTabLabel(key: String): String =
    divisionalChartMeta(key)?.second?.substringBefore(',')?.trim() ?: key

/** Localized twin of [com.sangeetmind.libs.models.DIVISIONAL_CHART_META]: (name, purpose)
 * per varga key, resolved through resources so the tab/card labels follow the app language. */
private val DIVISIONAL_CHART_RES: Map<String, Pair<Int, Int>> = mapOf(
    "D1" to (R.string.chart_div_name_d1 to R.string.chart_div_purpose_d1),
    "D2" to (R.string.chart_div_name_d2 to R.string.chart_div_purpose_d2),
    "D3" to (R.string.chart_div_name_d3 to R.string.chart_div_purpose_d3),
    "D4" to (R.string.chart_div_name_d4 to R.string.chart_div_purpose_d4),
    "D7" to (R.string.chart_div_name_d7 to R.string.chart_div_purpose_d7),
    "D9" to (R.string.chart_div_name_d9 to R.string.chart_div_purpose_d9),
    "D10" to (R.string.chart_div_name_d10 to R.string.chart_div_purpose_d10),
    "D12" to (R.string.chart_div_name_d12 to R.string.chart_div_purpose_d12),
    "D16" to (R.string.chart_div_name_d16 to R.string.chart_div_purpose_d16),
    "D20" to (R.string.chart_div_name_d20 to R.string.chart_div_purpose_d20),
    "D24" to (R.string.chart_div_name_d24 to R.string.chart_div_purpose_d24),
    "D27" to (R.string.chart_div_name_d27 to R.string.chart_div_purpose_d27),
    "D30" to (R.string.chart_div_name_d30 to R.string.chart_div_purpose_d30),
    "D40" to (R.string.chart_div_name_d40 to R.string.chart_div_purpose_d40),
    "D45" to (R.string.chart_div_name_d45 to R.string.chart_div_purpose_d45),
    "D60" to (R.string.chart_div_name_d60 to R.string.chart_div_purpose_d60)
)

@Composable
private fun divisionalChartMeta(key: String): Pair<String, String>? {
    val (nameRes, purposeRes) = DIVISIONAL_CHART_RES[key] ?: return null
    return stringResource(nameRes) to stringResource(purposeRes)
}

/** D1-only content used to stack as one long scroll — Doshas, Ashtakvarga, Friendship,
 * Shadbala, Bhavabala, KP, Jaimini, Lal Kitab, and Dasha all in a row. AstroSage (the
 * deepest competitor per the roadmap research) organizes this same feature set as
 * tabs/icons the user taps into rather than an infinite scroll — this mirrors that. */
private enum class ChartSection(@StringRes val labelRes: Int) {
    OVERVIEW(R.string.chart_section_overview),
    STRENGTH(R.string.chart_section_strength),
    DASHA(R.string.chart_section_dasha),
    KP_JAIMINI(R.string.chart_section_kp_jaimini),
    LAL_KITAB(R.string.chart_section_lal_kitab),
    HOUSES(R.string.chart_section_houses)
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
                text = { Text(stringResource(section.labelRes)) }
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
            SummaryChip(stringResource(R.string.chart_lagna_label), "$lagna $lagnaDegree")
            if (moonSign.isNotBlank()) {
                SummaryChip(stringResource(R.string.chart_moon_label), "$moonSign $moonDegree".trim())
            }
            SummaryChip(stringResource(R.string.chart_nakshatra_label), "$nakshatra · $nakshatraDetail", highlight = true)
            if (currentDasha.isNotBlank()) {
                SummaryChip(stringResource(R.string.chart_current_dasha_label), currentDasha)
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
internal fun Chip(text: String, color: Color) {
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
        if (planet.retrograde) add(astroTerm("Retrograde") to neutral)
        if (planet.combust) add(astroTerm("Combust") to graha.mangala)
        if (planet.exalted) add(astroTerm("Exalted") to graha.budha)
        if (planet.debilitated) add(astroTerm("Debilitated") to graha.guru)
        if (planet.vargottama) add(stringResource(R.string.chart_dig_vargottama) to graha.shani)
    }
}

/** Classical navagraha only — Uranus/Neptune/Pluto are in the planets map (chart_service.py
 * computes them for reference) but excluded from Vedic house/aspect UI everywhere else
 * (the wheel, PlanetListCard), so the house/aspect tables exclude them too. */
private val VEDIC_PLANETS = setOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu")

/** Pairs of planets that aspect each other's house (backend `mutual_aspects`, D1 only). */
@Composable
internal fun MutualAspectsCard(pairs: List<MutualAspect>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.chart_mutual_aspects_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.chart_mutual_aspects_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            pairs.forEach { pair ->
                val a = "${astroTerm(pair.planetA)} (${stringResource(CoreR.string.common_house_short, pair.houseA)})"
                val b = "${astroTerm(pair.planetB)} (${stringResource(CoreR.string.common_house_short, pair.houseB)})"
                Text(
                    stringResource(R.string.chart_mutual_aspect_row_fmt, a, b),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * House-by-house table (D1 only): for each house, its sign, lord (bhavabala.houses —
 * already computed by the backend from this chart's own lagna), which planets occupy it,
 * and which planets aspect (drishti) it — the inverse of each planet's aspects — with the
 * drishti-bala strength shown for partial (non-7th) aspects.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun HouseDetailsCard(planets: Map<String, PlanetInfo>, bhavabala: ChartBhavabala) {
    val graha = LocalGrahaColors.current
    val vedicPlanets = planets.filterKeys { it in VEDIC_PLANETS }
    val occupantsByHouse = (1..12).associateWith { house ->
        vedicPlanets.filterValues { it.house == house }.keys.toList()
    }
    // house -> (planet, strength %). Falls back to aspectsHouses / 100% if the detailed list is absent.
    val influencersByHouse = (1..12).associateWith { house ->
        vedicPlanets.mapNotNull { (name, info) ->
            val detailed = info.aspects.firstOrNull { it.house == house }
            when {
                detailed != null -> name to detailed.strength
                house in info.aspectsHouses -> name to 100
                else -> null
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.chart_house_details_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.chart_house_details_legend),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            bhavabala.houses.sortedBy { it.house }.forEachIndexed { index, house ->
                val occupants = occupantsByHouse[house.house].orEmpty()
                val influencers = influencersByHouse[house.house].orEmpty()
                val isQuiet = occupants.isEmpty() && influencers.isEmpty()
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                }
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    // Header: house tag + sign on the left, lord as a chip on the right.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                stringResource(CoreR.string.common_house_short, house.house),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                astroTerm(house.sign),
                                style = MaterialTheme.typography.bodyMedium,
                                // Empty houses read dimmer so the populated ones stand out at a glance.
                                color = if (isQuiet) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Chip(
                            stringResource(R.string.chart_house_lord_chip_fmt, astroTerm(house.lord)),
                            grahaColorFor(house.lord, graha)
                        )
                    }
                    if (occupants.isNotEmpty()) {
                        HouseChipRow(stringResource(R.string.chart_house_occupants_label)) {
                            occupants.forEach { Chip(astroTerm(it), grahaColorFor(it, graha)) }
                        }
                    }
                    if (influencers.isNotEmpty()) {
                        HouseChipRow(stringResource(R.string.chart_house_influencers_label)) {
                            influencers.forEach { (name, strength) ->
                                val label = if (strength < 100) {
                                    stringResource(R.string.chart_house_strength_chip_fmt, astroTerm(name), strength)
                                } else {
                                    astroTerm(name)
                                }
                                Chip(label, grahaColorFor(name, graha))
                            }
                        }
                    }
                }
            }
        }
    }
}

/** A tiny muted label followed by a wrapping row of chips; used for a house's occupants and aspects. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HouseChipRow(label: String, chips: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp, start = 28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(44.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            chips()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PlanetListCard(lagna: LagnaInfo, planets: Map<String, PlanetInfo>) {
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
            Text(stringResource(R.string.chart_planet_positions), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            sorted.forEach { (name, planet) ->
                val house = planet.house ?: signToHouseNumber(lagna.sign, planet.sign)
                val signLabel = astroTerm(planet.sign)
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                stringResource(CoreR.string.common_house_short, house),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(astroTerm(name), style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            text = buildString {
                                append(signLabel)
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
                    // An aspect on an occupied house also falls on the planet sitting there.
                    val aspectedBy = planets
                        .filter { (other, info) -> other != name && other in VEDIC_PLANETS && house in info.aspectsHouses }
                        .keys
                        .map { astroTerm(it) }
                    if (aspectedBy.isNotEmpty()) {
                        Text(
                            stringResource(R.string.chart_planet_aspected_by_fmt, aspectedBy.joinToString(", ")),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, start = 28.dp)
                        )
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
            Text(stringResource(R.string.chart_doshas), style = MaterialTheme.typography.titleMedium)

            val manglik = doshas.manglik
            DoshaRow(
                label = astroTerm("Manglik"),
                isFlagged = manglik.effectivePresent,
                statusText = when {
                    manglik.effectivePresent -> stringResource(R.string.chart_status_present)
                    manglik.cancelled -> stringResource(R.string.chart_status_cancelled)
                    else -> stringResource(R.string.chart_status_not_present)
                },
                detail = manglik.summary.ifBlank { null },
                flaggedColor = graha.mangala,
                clearColor = graha.budha
            )

            val kalsarpa = doshas.kalsarpa
            DoshaRow(
                label = astroTerm("Kalsarpa"),
                isFlagged = kalsarpa.present,
                statusText = if (kalsarpa.present) stringResource(R.string.chart_status_present) else stringResource(R.string.chart_status_not_present),
                detail = if (kalsarpa.present) {
                    kalsarpa.yogaFullName
                } else {
                    stringResource(R.string.chart_no_kalsarpa)
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
                label = astroTerm("Sade Sati"),
                isFlagged = active != null,
                statusText = if (active != null) stringResource(R.string.chart_status_active) else stringResource(R.string.chart_status_not_active),
                detail = when {
                    active != null -> stringResource(
                        R.string.chart_sadesati_until,
                        astroTerm(active.phase ?: active.kind),
                        fmtLocalDate(active.endDate)
                    )
                    next != null -> stringResource(
                        R.string.chart_sadesati_next,
                        astroTerm(next.kind),
                        fmtLocalDate(next.startDate)
                    )
                    else -> stringResource(R.string.chart_sadesati_none)
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
            Text(stringResource(R.string.chart_ashtakvarga), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.chart_ashtakvarga_subtitle, sav.totalBindus),
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
                        astroTerm(sign),
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
            Text(stringResource(R.string.chart_friendship), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.chart_friendship_subtitle, astroTerm(selected)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                friendship.planets.forEach { planet ->
                    FilterChip(
                        selected = planet == selected,
                        onClick = { selected = planet },
                        label = { Text(astroTerm(planet)) }
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
                    Text(astroTerm(other), style = MaterialTheme.typography.bodyMedium)
                    Chip(friendshipRelationLabel(relation), color)
                }
            }
        }
    }
}

/** Backend friendship relation label -> display language. Friend/Neutral/Enemy are shared
 * astro vocabulary; the two panchadha-only extremes live in this module's strings. */
@Composable
private fun friendshipRelationLabel(relation: String): String = when (relation.trim().lowercase()) {
    "intimate" -> stringResource(R.string.chart_rel_intimate)
    "bitter enemy" -> stringResource(R.string.chart_rel_bitter_enemy)
    else -> astroTerm(relation)
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
            Text(stringResource(R.string.chart_shadbala), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.chart_shadbala_subtitle),
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
                        astroTerm(entry.planet),
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
                        stringResource(R.string.chart_rupas_fmt, planet.totalRupas.toString()),
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
            Text(stringResource(R.string.chart_bhavabala), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.chart_bhavabala_subtitle, bhavabala.strongestHouse?.toString() ?: "-"),
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
                        "${stringResource(CoreR.string.common_house_short, house.house)} ${astroTerm(house.lord)}",
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

internal fun grahaColorFor(planet: String, graha: GrahaColors): Color = when (planet) {
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
private enum class KpView(@StringRes val labelRes: Int) {
    CUSPS(R.string.chart_kp_cusps), SIGNIFICATORS(R.string.chart_kp_significators)
}

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
            Text(stringResource(R.string.chart_kp_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.chart_kp_ayanamsa_fmt, kp.ayanamsaName, "%.2f".format(kp.ayanamsaDeg)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KpView.entries.forEach { v ->
                    FilterChip(selected = v == view, onClick = { view = v }, label = { Text(stringResource(v.labelRes)) })
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
                        Text(
                            "${stringResource(CoreR.string.common_house_short, cusp.house)} ${astroTerm(cusp.sign)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "${astroTerm(cusp.subLord)} / ${astroTerm(cusp.subSubLord)}",
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
                                label = { Text(astroTerm(planet)) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val houses = kp.significators[selectedPlanet].orEmpty()
                    if (houses.isEmpty()) {
                        Text(
                            stringResource(R.string.chart_kp_no_significators, astroTerm(selectedPlanet)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            houses.forEach { house ->
                                Chip(stringResource(CoreR.string.common_house_short, house), grahaColorFor(selectedPlanet, graha))
                            }
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
            Text(stringResource(R.string.chart_jaimini), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(
                    R.string.chart_jaimini_subtitle,
                    astroTerm(jaimini.karakamsa.lagnaSign),
                    astroTerm(jaimini.swamsa.lagnaSign)
                ),
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
                        "${astroTerm(karaka.planet)} ${"%.1f".format(karaka.degreeInSign)}°",
                        grahaColorFor(karaka.planet, graha)
                    )
                }
            }
        }
    }
}

/** Arudha padas: Arudha Lagna (how others see you) and Upapada (marriage, spouse) first,
 * each with its meaning and occupants, then the other ten padas as compact rows
 * (app/services/jaimini_charts.py). */
@Composable
private fun ArudhaPadasCard(padas: List<ArudhaPada>) {
    if (padas.isEmpty()) return
    val graha = LocalGrahaColors.current
    val byHouse = padas.associateBy { it.ofHouse }
    val headline = listOfNotNull(
        byHouse[1]?.let { it to R.string.chart_arudha_al_meaning },
        byHouse[12]?.let { it to R.string.chart_arudha_ul_meaning }
    )
    val others = padas.filter { it.ofHouse in 2..11 }.sortedBy { it.ofHouse }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.chart_arudha_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.chart_arudha_legend),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            headline.forEach { (pada, meaningRes) ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    PadaHeader(pada, highlight = true)
                    Text(
                        stringResource(meaningRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (pada.occupants.isNotEmpty()) {
                        HouseChipRow(stringResource(R.string.chart_house_occupants_label)) {
                            pada.occupants.forEach { Chip(astroTerm(it), grahaColorFor(it, graha)) }
                        }
                    }
                }
            }
            others.forEach { pada ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    PadaHeader(pada, highlight = false)
                    if (pada.occupants.isNotEmpty()) {
                        HouseChipRow(stringResource(R.string.chart_house_occupants_label)) {
                            pada.occupants.forEach { Chip(astroTerm(it), grahaColorFor(it, graha)) }
                        }
                    }
                }
            }
        }
    }
}

/** "A2  Dhana pada · wealth" on the left, "Virgo · H10" chip (in the sign lord's colour) on the right. */
@Composable
private fun PadaHeader(pada: ArudhaPada, highlight: Boolean) {
    val graha = LocalGrahaColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                pada.pada,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(28.dp)
            )
            Text(
                stringResource(padaLabelRes(pada.ofHouse)),
                style = if (highlight) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
        Chip(
            "${astroTerm(pada.sign)} · ${stringResource(CoreR.string.common_house_short, pada.house)}",
            grahaColorFor(pada.signLord, graha)
        )
    }
}

private fun padaLabelRes(ofHouse: Int): Int = when (ofHouse) {
    1 -> R.string.chart_pada_1
    2 -> R.string.chart_pada_2
    3 -> R.string.chart_pada_3
    4 -> R.string.chart_pada_4
    5 -> R.string.chart_pada_5
    6 -> R.string.chart_pada_6
    7 -> R.string.chart_pada_7
    8 -> R.string.chart_pada_8
    9 -> R.string.chart_pada_9
    10 -> R.string.chart_pada_10
    11 -> R.string.chart_pada_11
    else -> R.string.chart_pada_12
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
            Text(stringResource(R.string.chart_lal_kitab), style = MaterialTheme.typography.titleMedium)
            if (lalKitab.systemNote.isNotBlank()) {
                Text(
                    lalKitab.systemNote,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (lalKitab.rinYogas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.chart_lk_rin), style = MaterialTheme.typography.titleSmall)
                lalKitab.rinYogas.forEach { yoga ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Chip(astroTerm(yoga.planet), grahaColorFor(yoga.planet, graha))
                        Text(
                            stringResource(R.string.chart_lk_rin_row, yoga.rinType, yoga.house),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (lalKitab.remedies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.chart_lk_remedies), style = MaterialTheme.typography.titleSmall)
                lalKitab.remedies.forEach { remedy ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text(
                            astroTerm(remedy.planet),
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
                Text(stringResource(R.string.chart_lk_placement_notes), style = MaterialTheme.typography.titleSmall)
                lalKitab.predictions.forEach { prediction ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text(
                            stringResource(R.string.chart_lk_prediction_row, astroTerm(prediction.planet), prediction.house),
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
                    stringResource(R.string.chart_lk_empty),
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
            Text(stringResource(R.string.chart_reading), style = MaterialTheme.typography.titleMedium)
            if (narratives.nakshatraPhal.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(
                        R.string.chart_nakshatra_line,
                        astroTerm(narratives.nakshatraPhal.nakshatra),
                        narratives.nakshatraPhal.pada?.toString() ?: "-",
                        astroTerm(narratives.nakshatraPhal.lord)
                    ),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(narratives.nakshatraPhal.text, style = MaterialTheme.typography.bodyMedium)
            }
            if (narratives.ascendantSummary.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(R.string.chart_ascendant_fmt, astroTerm(narratives.ascendantSummary.sign)),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(narratives.ascendantSummary.text, style = MaterialTheme.typography.bodyMedium)
            }
            val mahadashaText = dashaPhal.mahadashaText
            if (!mahadashaText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(R.string.chart_mahadasha_fmt, astroTerm(dashaPhal.currentMahadasha)),
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
                Text(stringResource(R.string.chart_planet_placements), style = MaterialTheme.typography.titleSmall)
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
            Text(stringResource(R.string.chart_house_cusps), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.chart_house_cusps_subtitle, houses.system),
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
                    Text(stringResource(CoreR.string.common_house_short, cusp.house), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${astroTerm(cusp.sign)} ${cusp.absoluteDms ?: "${cusp.degree.toInt()}°"}",
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
private enum class DashaSystem(@StringRes val labelRes: Int) {
    VIMSHOTTARI(R.string.chart_dasha_vimshottari),
    YOGINI(R.string.chart_dasha_yogini),
    CHARA(R.string.chart_dasha_chara)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashaSystemSelector(selected: DashaSystem, onSelect: (DashaSystem) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DashaSystem.entries.forEach { system ->
            FilterChip(
                selected = selected == system,
                onClick = { onSelect(system) },
                label = { Text(stringResource(system.labelRes)) }
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
                    Text(stringResource(R.string.chart_current_vimshottari), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    DashaRow(stringResource(R.string.chart_mahadasha), current.mahadasha)
                    DashaRow(stringResource(R.string.chart_antardasha), current.antardasha)
                    DashaRow(stringResource(R.string.chart_pratyantar), current.resolvedPratyantar)
                }
            }
        }
        DashaSystem.YOGINI -> {
            val md = currentYoginiMahadasha(chart.yogini) ?: return
            val ad = currentYoginiAntardasha(md)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.chart_current_yogini), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            stringResource(
                                R.string.chart_dasha_row_fmt,
                                stringResource(R.string.chart_mahadasha),
                                yoginiName(md.yogini, md.lord)
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "${fmtIso(md.start)} → ${fmtIso(md.end)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (ad != null) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                stringResource(
                                    R.string.chart_dasha_row_fmt,
                                    stringResource(R.string.chart_antardasha),
                                    yoginiName(ad.yogini, ad.lord)
                                ),
                                style = MaterialTheme.typography.bodyLarge
                            )
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
                    Text(stringResource(R.string.chart_current_chara), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            stringResource(R.string.chart_dasha_row_fmt, stringResource(R.string.chart_mahadasha), astroTerm(md.sign)),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "${fmtIso(md.start)} → ${fmtIso(md.end)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (ad != null) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                stringResource(R.string.chart_dasha_row_fmt, stringResource(R.string.chart_antardasha), astroTerm(ad.sign)),
                                style = MaterialTheme.typography.bodyLarge
                            )
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
        Text(
            stringResource(R.string.chart_dasha_row_fmt, label, astroTerm(period.lord)),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "${fmtIso(period.start)} → ${fmtIso(period.end)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** "Mangala (Moon)" — Yogini name plus its ruling planet, both localized. */
@Composable
private fun yoginiName(yogini: String, lord: String): String =
    stringResource(R.string.chart_yogini_name_fmt, astroTerm(yogini), astroTerm(lord))

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
                    stringResource(R.string.chart_mahadasha_fmt, astroTerm(md.lord)),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPast) MaterialTheme.colorScheme.onSurface else accent
                )
                if (isCurrent) Chip(stringResource(R.string.chart_chip_current), accent)
                if (md.partial) Chip(stringResource(R.string.chart_chip_partial), MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val yearsLabel = yearsBetween(md.start, md.end)?.let { stringResource(R.string.chart_years_fmt, it.toString()) }
            Text(
                text = buildString {
                    append(fmtIso(md.start)); append(" → "); append(fmtIso(md.end))
                    yearsLabel?.let { append(" · $it") }
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
            val antardashaLabel = stringResource(R.string.chart_antardasha_fmt, astroTerm(bhukti.lord))
            Text(
                text = if (isCurrent) "$antardashaLabel  ●" else antardashaLabel,
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
                    val prLord = astroTerm(pr.lord)
                    Text(
                        text = buildString {
                            append(prLord); append(": "); append(fmtIso(pr.start)); append(" → "); append(fmtIso(pr.end))
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
                    yoginiName(md.yogini, md.lord),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPast) MaterialTheme.colorScheme.onSurface else accent
                )
                if (isCurrent) Chip(stringResource(R.string.chart_chip_current), accent)
                if (md.partial) Chip(stringResource(R.string.chart_chip_partial), MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val yearsLabel = stringResource(R.string.chart_years_fmt, "%.1f".format(md.years))
            Text(
                text = buildString {
                    append(fmtIso(md.start)); append(" → "); append(fmtIso(md.end))
                    append(" · $yearsLabel")
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
                        val adLabel = yoginiName(ad.yogini, ad.lord)
                        Text(
                            text = if (adCurrent) "$adLabel  ●" else adLabel,
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
                    astroTerm(md.sign),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPast) MaterialTheme.colorScheme.onSurface else accent
                )
                if (isCurrent) Chip(stringResource(R.string.chart_chip_current), accent)
                if (md.partial) Chip(stringResource(R.string.chart_chip_partial), MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val yearsLabel = stringResource(R.string.chart_years_fmt, "%.1f".format(md.years))
            Text(
                text = buildString {
                    append(fmtIso(md.start)); append(" → "); append(fmtIso(md.end))
                    append(" · $yearsLabel")
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
                        val adSign = astroTerm(ad.sign)
                        Text(
                            text = if (adCurrent) "$adSign  ●" else adSign,
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

@Composable
private fun formatCurrentDasha(chart: ChartSummaryResponse): String {
    val current = chart.vimshottari.current ?: return ""
    return listOfNotNull(
        current.mahadasha?.lord,
        current.antardasha?.lord,
        current.resolvedPratyantar?.lord
    ).map { astroTerm(it) }.joinToString(" – ")
}

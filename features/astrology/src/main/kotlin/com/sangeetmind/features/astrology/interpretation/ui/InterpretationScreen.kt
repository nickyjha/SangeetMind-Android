package com.sangeetmind.features.astrology.interpretation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.components.AstroTopBar
import com.sangeetmind.core.ui.components.ErrorCard
import com.sangeetmind.core.ui.language.LocalAppLanguage
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.core.ui.theme.GrahaColors
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.accuracy.AccuracySurveyCard
import com.sangeetmind.features.astrology.accuracy.AccuracySurveyDialog
import com.sangeetmind.features.astrology.chart.ui.Chip
import com.sangeetmind.features.astrology.interpretation.InterpretationViewModel
import com.sangeetmind.libs.models.ChartSummaryResponse
import com.sangeetmind.libs.models.PhalHouse
import com.sangeetmind.libs.models.PhalPeriod
import com.sangeetmind.libs.models.PhalPlanet
import com.sangeetmind.libs.models.PhalRemedy
import com.sangeetmind.libs.models.PhalSadeSati
import com.sangeetmind.libs.models.PhalSummary
import com.sangeetmind.libs.models.PhalTopic
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Full Reading: the phal engine's deterministic verdicts (POST /v1/phal/summary) for the
 * eight life areas, the running dasha periods, today's transits and every planet and house,
 * each with the reasons behind it. The older rules-engine text sits collapsed at the end.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterpretationScreen(
    onNavigateBack: () -> Unit,
    viewModel: InterpretationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AstroTopBar(
                title = stringResource(R.string.interpretation_title),
                onBack = onNavigateBack,
                accent = LocalGrahaColors.current.shukra
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
                    ErrorCard(
                        message = uiState.error,
                        onRetry = viewModel::refresh,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                uiState.data != null -> {
                    val data = uiState.data!!
                    InterpretationContent(
                        chart = data.chart,
                        phal = data.phal,
                        married = uiState.married,
                        onMarriedChange = viewModel::setMarried
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InterpretationContent(
    chart: ChartSummaryResponse,
    phal: PhalSummary?,
    married: Boolean,
    onMarriedChange: (Boolean) -> Unit
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ChartSummaryCard(chart) }
        item { DashaCard(chart) }

        if (phal != null) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = married,
                        onClick = { onMarriedChange(!married) },
                        label = { Text(stringResource(R.string.interpretation_married_toggle)) }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        stringResource(R.string.interpretation_married_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = muted,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Column {
                    SectionHeader(stringResource(R.string.interpretation_life_areas))
                    Text(
                        stringResource(R.string.interpretation_life_areas_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = muted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            items(phal.topics, key = { it.topic }) { TopicCard(it) }

            if (phal.dasha.isNotEmpty() || phal.sadesati != null) {
                item { SectionHeader(stringResource(R.string.interpretation_running_periods)) }
                item { PeriodsCard(phal.dasha, phal.sadesati) }
            }
            if (phal.planets.isNotEmpty()) {
                item {
                    Column {
                        SectionHeader(stringResource(R.string.interpretation_planets))
                        Text(
                            stringResource(R.string.interpretation_planets_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = muted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                items(phal.planets.sortedByDescending { it.score }, key = { it.planet }) { PlanetCard(it) }
            }
            if (phal.houses.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.interpretation_house_strengths)) }
                item { HouseStrengthsCard(phal.houses) }
            }
        }

        val remedies = phal?.remedies.orEmpty()
        if (remedies.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.interpretation_section_remedies)) }
            items(remedies) { RemedyCard(it) }
        }

        item {
            var surveyOpen by rememberSaveable { mutableStateOf(false) }
            AccuracySurveyCard(onOpen = { surveyOpen = true })
            if (surveyOpen) AccuracySurveyDialog(onDismiss = { surveyOpen = false })
        }
    }
}

// ---- Phal engine cards -------------------------------------------------------------------

@Composable
private fun TopicCard(topic: PhalTopic) {
    val code = LocalAppLanguage.current.code
    val graha = LocalGrahaColors.current
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    topic.name(code),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Chip(verdictText(topic.label.en), verdictColor(topic.label.en, graha))
            }
            ScoreBar(topic.score, verdictColor(topic.label.en, graha))
            val house = topic.mainHouse
            val lord = topic.lord
            if (house != null && lord != null) {
                Text(
                    stringResource(R.string.interpretation_house_lord_fmt, ordinal(house, code), astroTerm(lord)),
                    style = MaterialTheme.typography.labelSmall,
                    color = muted
                )
            }
            if (topic.agreement == "mixed") {
                Text(
                    stringResource(R.string.interpretation_agreement_conflict),
                    style = MaterialTheme.typography.labelSmall,
                    color = muted
                )
            }
            if (topic.strengths.isNotEmpty() || topic.cautions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
            }
            topic.strengths.forEach { ReasonLine("+", it.forLanguage(code), graha.budha) }
            topic.cautions.forEach { ReasonLine("−", it.forLanguage(code), graha.mangala) }
            if (topic.dasha.isNotEmpty() || topic.gochar != null) {
                Spacer(modifier = Modifier.height(8.dp))
            }
            topic.dasha.forEach { tone ->
                Text(
                    stringResource(
                        R.string.interpretation_dasha_tone_fmt,
                        astroTerm(tone.lord),
                        levelText(tone.level),
                        shortDate(tone.end),
                        verdictText(tone.label.en)
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            topic.gochar?.let { g ->
                val why = (g.cautions + g.strengths).firstOrNull()?.forLanguage(code)
                val base = stringResource(R.string.interpretation_transit_tone_fmt, verdictText(g.label.en))
                Text(
                    if (why.isNullOrBlank()) base else "$base · $why",
                    style = MaterialTheme.typography.bodySmall,
                    color = muted
                )
            }
        }
    }
}

@Composable
private fun ReasonLine(sign: String, text: String, color: Color) {
    Row(modifier = Modifier.padding(top = 2.dp)) {
        Text(
            sign,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.width(14.dp)
        )
        Text(text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
    }
}

/** -3..+3 shown as a bar with the midpoint at the centre. */
@Composable
private fun ScoreBar(score: Double, color: Color) {
    val fraction = ((score + 3.0) / 6.0).coerceIn(0.0, 1.0).toFloat()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp)
    ) {
        LinearProgressIndicator(
            progress = fraction,
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.weight(1f).height(6.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "%+.1f".format(Locale.US, score),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PeriodsCard(periods: List<PhalPeriod>, sadesati: PhalSadeSati?) {
    val graha = LocalGrahaColors.current
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            periods.forEach { p ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(
                            R.string.interpretation_period_fmt,
                            astroTerm(p.lord),
                            levelText(p.level),
                            shortDate(p.start),
                            shortDate(p.end)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Chip(verdictText(p.label.en), verdictColor(p.label.en, graha))
                }
            }
            sadesati?.takeIf { it.phase.isNotBlank() }?.let { s ->
                Text(
                    stringResource(R.string.interpretation_sadesati_fmt, s.phase, shortDate(s.endDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = muted
                )
            }
        }
    }
}

/** One planet, both sides: Shadbala strength (capacity) and placement (what it delivers),
 * then the engine's two-sentence reading that puts them together. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanetCard(p: PhalPlanet) {
    val code = LocalAppLanguage.current.code
    val graha = LocalGrahaColors.current
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(astroTerm(p.planet), style = MaterialTheme.typography.titleMedium)
                p.strength?.let { s ->
                    Chip(
                        stringResource(R.string.interpretation_shadbala_fmt, strengthText(s.en)),
                        strengthColor(s.en, graha)
                    )
                }
                val placement = p.placement?.forLanguage(code)
                if (!placement.isNullOrBlank()) {
                    Chip(placement, verdictColor(p.label.en, graha))
                }
            }
            val sign = p.sign
            val house = p.house
            if (sign != null && house != null) {
                Text(
                    "${astroTerm(sign)} · ${ordinal(house, code)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = muted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            val reading = p.reading?.forLanguage(code)
            if (!reading.isNullOrBlank()) {
                Text(
                    reading,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                val reason = (p.strengths + p.cautions).firstOrNull()?.forLanguage(code)
                if (!reason.isNullOrBlank()) {
                    Text(reason, style = MaterialTheme.typography.bodySmall, color = muted, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun strengthText(label: String): String = when (label) {
    "very_strong" -> stringResource(R.string.interpretation_strength_very_strong)
    "strong" -> stringResource(R.string.interpretation_strength_strong)
    "adequate" -> stringResource(R.string.interpretation_strength_adequate)
    "weak" -> stringResource(R.string.interpretation_strength_weak)
    "very_weak" -> stringResource(R.string.interpretation_strength_very_weak)
    else -> label
}

private fun strengthColor(label: String, graha: GrahaColors): Color = when (label) {
    "very_strong", "strong" -> graha.budha
    "adequate" -> graha.guru
    else -> graha.mangala
}

@Composable
private fun HouseStrengthsCard(houses: List<PhalHouse>) {
    val code = LocalAppLanguage.current.code
    val graha = LocalGrahaColors.current
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            houses.forEach { h ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        h.name(code),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    val lord = h.lord
                    if (lord != null) {
                        Text(
                            astroTerm(lord),
                            style = MaterialTheme.typography.labelSmall,
                            color = muted,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Chip(verdictText(h.label.en), verdictColor(h.label.en, graha))
                }
            }
        }
    }
}

/** A remedy for a debilitated, combust or weak planet, from the phal engine. */
@Composable
private fun RemedyCard(remedy: PhalRemedy) {
    val code = LocalAppLanguage.current.code
    val text = if (code == "hi" && remedy.hi.isNotBlank()) remedy.hi else remedy.en
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(astroTerm(remedy.planet), style = MaterialTheme.typography.titleMedium)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ---- helpers -----------------------------------------------------------------------------

@Composable
private fun verdictText(label: String): String = when (label) {
    "very_strong" -> stringResource(R.string.interpretation_label_very_strong)
    "strong" -> stringResource(R.string.interpretation_label_strong)
    "mixed" -> stringResource(R.string.interpretation_label_mixed)
    "weak" -> stringResource(R.string.interpretation_label_weak)
    "very_weak" -> stringResource(R.string.interpretation_label_very_weak)
    else -> label
}

private fun verdictColor(label: String, graha: GrahaColors): Color = when (label) {
    "very_strong", "strong" -> graha.budha
    "mixed" -> graha.guru
    "weak" -> graha.surya
    else -> graha.mangala
}

@Composable
private fun levelText(level: String): String = when (level) {
    "mahadasha" -> stringResource(R.string.interpretation_level_mahadasha)
    "antardasha" -> stringResource(R.string.interpretation_level_antardasha)
    "pratyantardasha" -> stringResource(R.string.interpretation_level_pratyantardasha)
    else -> level
}

private fun ordinal(n: Int, code: String): String {
    if (code == "hi") return n.toString()
    val suffix = if (n % 100 in 11..13) "th" else when (n % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
    return "$n$suffix"
}

/** "2027-04-20T09:05:53Z" (or a bare date) -> "20 Apr 2027"; blank when absent. */
private fun shortDate(iso: String?): String {
    val day = iso?.take(10) ?: return ""
    return runCatching {
        LocalDate.parse(day).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))
    }.getOrDefault(day)
}

// ---- existing cards ----------------------------------------------------------------------

@Composable
private fun ChartSummaryCard(chart: ChartSummaryResponse) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.interpretation_lagna_fmt, astroTerm(chart.lagna.sign)),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(
                    R.string.interpretation_moon_nakshatra_fmt,
                    chart.moonNakshatra.pada,
                    astroTerm(chart.moonNakshatra.lord)
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.interpretation_planetary_positions), style = MaterialTheme.typography.labelLarge)
            chart.planets.forEach { (name, planet) ->
                val showRetro = planet.retrograde == true
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(astroTerm(name), style = MaterialTheme.typography.bodySmall)
                    Text(
                        if (showRetro) stringResource(R.string.interpretation_sign_retrograde_fmt, astroTerm(planet.sign))
                        else astroTerm(planet.sign),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun DashaCard(chart: ChartSummaryResponse) {
    val current = chart.vimshottari.current
    if (current != null) {
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
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp)
    )
}

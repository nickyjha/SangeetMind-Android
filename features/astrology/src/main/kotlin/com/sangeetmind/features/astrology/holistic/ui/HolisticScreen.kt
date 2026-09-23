package com.sangeetmind.features.astrology.holistic.ui

import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.core.ui.theme.GrahaColors
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.holistic.HolisticViewModel
import com.sangeetmind.libs.models.AstroProfileSummary
import com.sangeetmind.libs.models.CombinedInsights
import com.sangeetmind.libs.models.HolisticCombinedResponse
import com.sangeetmind.libs.models.HolisticHarmony
import com.sangeetmind.libs.models.HolisticInsight
import com.sangeetmind.libs.models.HolisticNumerology
import com.sangeetmind.libs.models.LlmNarrative
import com.sangeetmind.libs.models.NumerologyEnhancedAnalysis
import com.sangeetmind.libs.models.SangeetMindRecommendations
import com.sangeetmind.libs.models.toTitleCase

private enum class HolisticTab(@StringRes val labelRes: Int) {
    READING(R.string.holistic_tab_reading),
    INSIGHTS(R.string.holistic_tab_insights),
    SANGEET(R.string.holistic_tab_sangeet),
    NUMEROLOGY(R.string.holistic_tab_numerology)
}

/** Astro × Numerology synthesis (POST /holistic/combined). Its own screen, not a Birth Chart
 * or Numerology sub-tab, because it is the one place both systems are read together — the
 * cross-system harmonies, the unified Sangeet recommendation, and the Gemini narrative have
 * no home in either single-system screen. Sub-tabs keep it from becoming one long scroll. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolisticScreen(
    onNavigateBack: () -> Unit,
    viewModel: HolisticViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val graha = LocalGrahaColors.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.holistic_title)) },
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = graha.guru.copy(alpha = 0.14f),
                    titleContentColor = graha.guru,
                    navigationIconContentColor = graha.guru,
                    actionIconContentColor = graha.guru
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        // The Gemini call is the slow part; say so rather than spin silently.
                        Text(
                            stringResource(R.string.holistic_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                uiState.hasNoKundli -> {
                    Text(
                        text = stringResource(R.string.holistic_no_kundli),
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                uiState.needsName -> {
                    Text(
                        text = stringResource(R.string.holistic_needs_name),
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
                uiState.analysis != null -> {
                    HolisticContent(
                        personName = uiState.kundli?.fullName?.toTitleCase(),
                        analysis = uiState.analysis!!,
                        onRegenerate = viewModel::refresh
                    )
                }
            }
        }
    }
}

@Composable
private fun HolisticContent(
    personName: String?,
    analysis: HolisticCombinedResponse,
    onRegenerate: () -> Unit
) {
    val graha = LocalGrahaColors.current
    var selectedTab by rememberSaveable { mutableStateOf(HolisticTab.READING) }
    val numerology = analysis.analyses.numerology
    val astro = analysis.analyses.astrology?.vedicProfile

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = personName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.holistic_your_reading_default),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                stringResource(R.string.holistic_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item { AtAGlanceChips(numerology, astro, graha) }
        item {
            ScrollableTabRow(selectedTabIndex = selectedTab.ordinal, edgePadding = 0.dp) {
                HolisticTab.entries.forEach { tab ->
                    Tab(
                        selected = tab == selectedTab,
                        onClick = { selectedTab = tab },
                        text = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        }
        when (selectedTab) {
            HolisticTab.READING -> readingTab(analysis, numerology, astro, onRegenerate, graha)
            HolisticTab.INSIGHTS -> insightsTab(analysis.combinedInsights, graha)
            HolisticTab.SANGEET -> sangeetTab(analysis.sangeetmindRecommendations, graha)
            HolisticTab.NUMEROLOGY -> numerologyTab(numerology?.enhancedAnalysis, graha)
        }
    }
}

/** One-line summary of the two systems' anchors: Life Path from numerology, Moon/Lagna/dasha
 * from astrology. The rest of the screen elaborates on these four facts. */
@Composable
private fun AtAGlanceChips(numerology: HolisticNumerology?, astro: AstroProfileSummary?, graha: GrahaColors) {
    val chips = buildList {
        numerology?.coreNumbers?.lifePath?.let { add(stringResource(R.string.holistic_chip_life_path, it.number) to graha.budha) }
        astro?.let {
            add(stringResource(R.string.holistic_chip_moon_in, astroTerm(it.moonSign)) to graha.chandra)
            add(stringResource(R.string.holistic_chip_lagna, astroTerm(it.lagna)) to graha.shani)
            if (it.currentMahadasha.isNotBlank()) {
                add(stringResource(R.string.holistic_chip_mahadasha, astroTerm(it.currentMahadasha)) to grahaColorFor(it.currentMahadasha, graha))
            }
        }
    }
    if (chips.isEmpty()) return
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(chips.size) { i ->
            val (label, color) = chips[i]
            AssistChip(
                onClick = {},
                label = { Text(label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = color.copy(alpha = 0.12f),
                    labelColor = color
                )
            )
        }
    }
}

// ---------------- Reading tab ----------------

private fun LazyListScope.readingTab(
    analysis: HolisticCombinedResponse,
    numerology: HolisticNumerology?,
    astro: AstroProfileSummary?,
    onRegenerate: () -> Unit,
    graha: GrahaColors
) {
    item {
        NarrativeCard(analysis.llmEnhancedNarrative, onRegenerate, graha)
    }
    numerology?.coreNumbers?.let { core ->
        item {
            SectionCard(stringResource(R.string.holistic_section_numbers)) {
                // (interpretations map key, label, number) — the key is the backend's snake_case id.
                val rows = listOf(
                    Triple("life_path", R.string.holistic_num_life_path, core.lifePath.number),
                    Triple("destiny", R.string.holistic_num_destiny, core.destiny.number),
                    Triple("soul_urge", R.string.holistic_num_soul_urge, core.soulUrge.number),
                    Triple("personality", R.string.holistic_num_personality, core.personality.number),
                    Triple("maturity", R.string.holistic_num_maturity, core.maturity.number),
                    Triple("birth_day", R.string.holistic_num_birth_day, core.birthDay.number),
                    Triple("attitude", R.string.holistic_num_attitude, core.attitude.number)
                )
                rows.forEach { (key, labelRes, number) ->
                    val name = numerology.interpretations[key]?.name?.let { astroTerm(it) }
                    LabelValueRow(stringResource(labelRes), if (name != null) "$number · $name" else "$number")
                }
            }
        }
    }
    astro?.let {
        item {
            SectionCard(stringResource(R.string.holistic_section_chart)) {
                LabelValueRow(stringResource(R.string.holistic_label_moon_sign), astroTerm(it.moonSign))
                LabelValueRow(stringResource(R.string.holistic_label_lagna), astroTerm(it.lagna))
                if (it.nakshatra.isNotBlank()) {
                    LabelValueRow(
                        stringResource(R.string.holistic_label_nakshatra),
                        astroTerm(it.nakshatra) + (it.nakshatraRuler.takeIf { r -> r.isNotBlank() }?.let { r -> " · ${astroTerm(r)}" } ?: "")
                    )
                }
                if (it.currentMahadasha.isNotBlank()) {
                    LabelValueRow(
                        stringResource(R.string.holistic_label_dasha),
                        astroTerm(it.currentMahadasha) + (it.currentAntardasha.takeIf { a -> a.isNotBlank() }?.let { a -> " / ${astroTerm(a)}" } ?: ""),
                        color = grahaColorFor(it.currentMahadasha, graha)
                    )
                }
                if (it.astroMood.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_todays_mood), astroTerm(it.astroMood))
                if (it.suggestedRaag.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_suggested_raag), astroTerm(it.suggestedRaag))
            }
        }
    }
    analysis.analyses.astrology?.note?.let { note ->
        item { NoteText(note) }
    }
}

/** The narrative is the feature's headline. Its provenance matters: Gemini-written and
 * template-assembled readings look alike but are not — the badge says which one this is. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NarrativeCard(
    narrative: LlmNarrative?,
    onRegenerate: () -> Unit,
    graha: GrahaColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = graha.guru.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = graha.guru)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.holistic_narrative_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                val badge = when {
                    narrative == null -> null
                    narrative.isLlm -> stringResource(R.string.holistic_badge_ai)
                    else -> stringResource(R.string.holistic_badge_template)
                }
                badge?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (narrative?.isLlm == true) graha.guru else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // The narrative follows the app-wide language (Settings / globe action), so there is
            // no per-card language toggle here; Regenerate re-asks Gemini in that language.
            Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onRegenerate) { Text(stringResource(R.string.holistic_regenerate)) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val text = narrative?.text.orEmpty()
            if (text.isBlank()) {
                Text(
                    stringResource(R.string.holistic_no_narrative),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
            if (narrative != null && !narrative.isLlm) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.holistic_template_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ---------------- Insights tab ----------------

private fun LazyListScope.insightsTab(insights: CombinedInsights, graha: GrahaColors) {
    if (insights.personalitySynthesis.isNotEmpty()) {
        item { InsightListCard(stringResource(R.string.holistic_section_personality_synthesis), insights.personalitySynthesis, graha) }
    }
    if (insights.timingInsights.isNotEmpty()) {
        item { InsightListCard(stringResource(R.string.holistic_section_timing), insights.timingInsights, graha) }
    }
    if (insights.lifePathAlignment.isNotEmpty()) {
        item { InsightListCard(stringResource(R.string.holistic_section_life_path_alignment), insights.lifePathAlignment, graha) }
    }
    if (insights.spiritualGuidance.isNotEmpty()) {
        item { InsightListCard(stringResource(R.string.holistic_section_spiritual_guidance), insights.spiritualGuidance, graha) }
    }
    // Harmonies are the genuinely cross-system part, so an empty list is itself a finding
    // worth stating, unlike the sections above.
    item { HarmonyCard(stringResource(R.string.holistic_section_harmonies), insights.harmonies, emptyText = stringResource(R.string.holistic_harmonies_empty), graha) }
    if (insights.potentialConflicts.isNotEmpty()) {
        item { HarmonyCard(stringResource(R.string.holistic_section_conflicts), insights.potentialConflicts, emptyText = "", graha) }
    }
}

@Composable
private fun InsightListCard(title: String, insights: List<HolisticInsight>, graha: GrahaColors) {
    SectionCard(title) {
        insights.forEachIndexed { i, insight ->
            if (i > 0) Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SourceTag(insight.source, graha)
                Spacer(modifier = Modifier.width(8.dp))
                Text(insight.label, style = MaterialTheme.typography.labelLarge)
            }
            Text(insight.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun HarmonyCard(title: String, items: List<HolisticHarmony>, emptyText: String, graha: GrahaColors) {
    SectionCard(title) {
        if (items.isEmpty()) {
            Text(emptyText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items.forEachIndexed { i, h ->
            if (i > 0) Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    h.type.replace('_', ' ').replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
                h.significance?.let {
                    Text(
                        when (it.lowercase()) {
                            "high" -> stringResource(R.string.holistic_significance_high)
                            "medium" -> stringResource(R.string.holistic_significance_medium)
                            "low" -> stringResource(R.string.holistic_significance_low)
                            else -> it.replaceFirstChar { c -> c.uppercase() }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (it == "high") graha.guru else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(h.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SourceTag(source: String, graha: GrahaColors) {
    val (label, color) = when (source) {
        "astrology" -> stringResource(R.string.holistic_source_astro) to graha.shani
        "numerology" -> stringResource(R.string.holistic_source_numero) to graha.budha
        else -> source to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.SemiBold
    )
}

// ---------------- Sangeet tab ----------------

private fun LazyListScope.sangeetTab(recs: SangeetMindRecommendations, graha: GrahaColors) {
    recs.combinedRecommendation?.let { combined ->
        item {
            SectionCard(stringResource(R.string.holistic_section_combined_recommendation)) {
                combined.practiceTime?.let { pt ->
                    LabelValueRow(
                        stringResource(R.string.holistic_label_practice_time),
                        astroTerm(pt.recommended).replaceFirstChar { it.uppercase() } +
                            (pt.alternative?.let { stringResource(R.string.holistic_practice_alternative, astroTerm(it)) } ?: "") +
                            (pt.confidence.takeIf { it.isNotBlank() }?.let { stringResource(R.string.holistic_practice_confidence, it) } ?: "")
                    )
                }
                combined.specificRaag?.let { LabelValueRow(stringResource(R.string.holistic_label_raag), astroTerm(it), color = graha.chandra) }
                if (combined.raagMoods.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.holistic_label_raag_moods), style = MaterialTheme.typography.labelLarge)
                    ChipRow(combined.raagMoods.map { astroTerm(it).replaceFirstChar { c -> c.uppercase() } }, graha.chandra)
                }
            }
        }
    }
    if (recs.raagRecommendations.isNotEmpty()) {
        item {
            SectionCard(stringResource(R.string.holistic_section_recommendation_sources)) {
                recs.raagRecommendations.forEachIndexed { i, r ->
                    if (i > 0) Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SourceTag(r.source, graha)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            r.raag?.let { astroTerm(it) } ?: r.mood?.let { astroTerm(it).replaceFirstChar { c -> c.uppercase() } } ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        val mood = r.mood
                        if (r.raag != null && !mood.isNullOrBlank()) {
                            Text(astroTerm(mood), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    r.reason?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
    recs.practiceTime?.let { pt ->
        if (pt.numerology != null || pt.astrology != null) {
            item {
                SectionCard(stringResource(R.string.holistic_section_practice_time_by_system)) {
                    pt.numerology?.let {
                        LabelValueRow(stringResource(R.string.holistic_label_numerology), astroTerm(it.preferred).replaceFirstChar { c -> c.uppercase() })
                        if (it.reasoning.isNotBlank()) NoteText(it.reasoning)
                    }
                    pt.astrology?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        LabelValueRow(stringResource(R.string.holistic_label_astrology), astroTerm(it.preferred).replaceFirstChar { c -> c.uppercase() })
                        if (it.basedOn.isNotBlank()) NoteText(it.basedOn)
                    }
                }
            }
        }
    }
    recs.emotionalGuidance?.numerology?.let { e ->
        item {
            SectionCard(stringResource(R.string.holistic_section_emotional_guidance)) {
                LabelValueRow(stringResource(R.string.holistic_label_nature), listOf(e.primary, e.secondary).filter { it.isNotBlank() }.joinToString(" · ") { it.replaceFirstChar { c -> c.uppercase() } })
                if (e.description.isNotBlank()) Text(e.description, style = MaterialTheme.typography.bodyMedium)
                BulletList(stringResource(R.string.holistic_label_strengths), e.emotionalStrengths)
                BulletList(stringResource(R.string.holistic_label_challenges), e.emotionalChallenges)
                if (e.balancePractice.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LabelValueRow(stringResource(R.string.holistic_label_balance_practice), e.balancePractice)
                }
            }
        }
    }
    recs.habitStyle?.numerology?.let { h ->
        item {
            SectionCard(stringResource(R.string.holistic_section_habit_style)) {
                LabelValueRow(stringResource(R.string.holistic_label_style), h.primaryStyle.replace('_', ' ').replaceFirstChar { it.uppercase() })
                if (h.approach.isNotBlank()) Text(h.approach, style = MaterialTheme.typography.bodyMedium)
                BulletList(stringResource(R.string.holistic_label_practice_tips), h.practiceTips)
            }
        }
    }
    if (recs.dailyPractices.isNotEmpty()) {
        item { SectionCard(stringResource(R.string.holistic_section_daily_practices)) { BulletList(null, recs.dailyPractices) } }
    }
}

// ---------------- Numerology+ tab ----------------

private fun LazyListScope.numerologyTab(enhanced: NumerologyEnhancedAnalysis?, graha: GrahaColors) {
    if (enhanced == null) {
        item { NoteText(stringResource(R.string.holistic_numerology_missing)) }
        return
    }
    enhanced.planetaryRuler?.let { r ->
        item {
            val color = grahaColorFor(r.planet, graha)
            SectionCard(stringResource(R.string.holistic_section_ruling_planet), titleColor = color) {
                Text(
                    "${r.symbol} ${astroTerm(r.planet)}" + (r.planetSanskrit.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.titleLarge,
                    color = color
                )
                if (r.nature.isNotBlank()) Text(r.nature, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (r.day.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_day), astroTerm(r.day))
                if (r.color.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_colours), r.color)
                if (r.gemstone.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_gemstone), r.gemstone)
                if (r.metal.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_metal), r.metal)
                if (r.deity.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_deity), r.deity)
                if (r.mantra.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_mantra), r.mantra, color = color)
                BulletList(stringResource(R.string.holistic_label_positive_influence), r.positiveInfluence)
                BulletList(stringResource(R.string.holistic_label_watch_for), r.negativeInfluence)
            }
        }
    }
    enhanced.yogas?.let { y ->
        if (y.detected.isNotEmpty()) {
            item {
                SectionCard(stringResource(R.string.holistic_section_numerology_yogas)) {
                    if (y.summary.isNotBlank()) NoteText(y.summary)
                    y.detected.forEach { yoga ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(yoga.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            Text(yoga.rarity, style = MaterialTheme.typography.labelSmall, color = graha.guru)
                        }
                        Text(
                            yoga.sanskrit + (yoga.numbersInvolved.takeIf { it.isNotEmpty() }?.let { " · ${it.joinToString(" + ")}" } ?: ""),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (yoga.meaning.isNotBlank()) Text(yoga.meaning, style = MaterialTheme.typography.bodyMedium)
                        if (yoga.effect.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_effect), yoga.effect)
                        if (yoga.advice.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_advice), yoga.advice)
                    }
                }
            }
        }
    }
    enhanced.lifePredictions?.let { lp ->
        item {
            SectionCard(stringResource(R.string.holistic_section_life_predictions)) {
                lp.health?.let { h ->
                    ExpandableSection("health", stringResource(R.string.holistic_label_health), h.general) {
                        BulletList(stringResource(R.string.holistic_label_strengths), h.strengths)
                        BulletList(stringResource(R.string.holistic_label_vulnerabilities), h.vulnerabilities)
                        BulletList(stringResource(R.string.holistic_label_best_practices), h.bestPractices)
                        if (h.advice.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_advice), h.advice)
                    }
                }
                lp.wealth?.let { w ->
                    ExpandableSection("wealth", stringResource(R.string.holistic_label_wealth), w.general) {
                        if (w.pattern.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_pattern), w.pattern)
                        if (w.peakYears.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_peak_years), w.peakYears)
                        BulletList(stringResource(R.string.holistic_label_strengths), w.strengths)
                        BulletList(stringResource(R.string.holistic_label_challenges), w.challenges)
                        if (w.advice.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_advice), w.advice)
                    }
                }
                lp.relationships?.let { r ->
                    ExpandableSection("relationships", stringResource(R.string.holistic_label_relationships), r.general) {
                        if (r.lovePattern.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_love_pattern), r.lovePattern)
                        if (r.compatibleNumbers.isNotEmpty()) LabelValueRow(stringResource(R.string.holistic_label_compatible_numbers), r.compatibleNumbers.joinToString(", "))
                        if (r.challengingNumbers.isNotEmpty()) LabelValueRow(stringResource(R.string.holistic_label_challenging_numbers), r.challengingNumbers.joinToString(", "))
                        BulletList(stringResource(R.string.holistic_label_strengths), r.strengths)
                        BulletList(stringResource(R.string.holistic_label_challenges), r.challenges)
                        if (r.advice.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_advice), r.advice)
                    }
                }
                lp.success?.let { s ->
                    ExpandableSection("success", stringResource(R.string.holistic_label_success), s.general) {
                        BulletList(stringResource(R.string.holistic_label_career_paths), s.careerPaths)
                        if (s.successPattern.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_pattern), s.successPattern)
                        if (s.peakSuccessAge.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_peak_age), s.peakSuccessAge)
                        if (s.advice.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_advice), s.advice)
                    }
                }
            }
        }
    }
    enhanced.cautionsAndExcellence?.let { ce ->
        ce.cautions?.let { c ->
            item {
                SectionCard(stringResource(R.string.holistic_section_cautions), titleColor = graha.mangala) {
                    if (c.primaryWarning.isNotBlank()) Text(c.primaryWarning, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    BulletList(stringResource(R.string.holistic_label_watch_out_for), c.watchOutFor)
                    Spacer(modifier = Modifier.height(6.dp))
                    if (c.karmicLesson.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_karmic_lesson), c.karmicLesson)
                    if (c.healthCaution.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_health), c.healthCaution)
                    if (c.relationshipCaution.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_relationships), c.relationshipCaution)
                    if (c.financialCaution.isNotBlank()) LabelValueRow(stringResource(R.string.holistic_label_finances), c.financialCaution)
                }
            }
        }
        ce.excellence?.let { e ->
            item {
                SectionCard(stringResource(R.string.holistic_section_how_to_excel), titleColor = graha.surya) {
                    if (e.successFormula.isNotBlank()) Text(e.successFormula, style = MaterialTheme.typography.titleSmall, color = graha.surya)
                    BulletList(stringResource(R.string.holistic_label_how_to_excel), e.howToExcel)
                    BulletList(stringResource(R.string.holistic_label_leverage), e.leverageStrengths)
                    BulletList(stringResource(R.string.holistic_label_daily_practice), e.dailyPractice)
                    if (e.affirmation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LabelValueRow(stringResource(R.string.holistic_label_affirmation), "“${e.affirmation}”")
                    }
                }
            }
        }
    }
    enhanced.famousPersonalities?.let { f ->
        if (f.indian.isNotEmpty() || f.global.isNotEmpty()) {
            item {
                SectionCard(stringResource(R.string.holistic_section_shares_life_path)) {
                    if (f.commonTraits.isNotBlank()) NoteText(f.commonTraits)
                    (f.indian + f.global).forEach { p ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(p.name + (p.field.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""), style = MaterialTheme.typography.labelLarge)
                        if (p.note.isNotBlank()) Text(p.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ---------------- shared building blocks ----------------

@Composable
private fun SectionCard(
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = titleColor)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun LabelValueRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, color = color, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun BulletList(title: String?, items: List<String>) {
    if (items.isEmpty()) return
    Spacer(modifier = Modifier.height(6.dp))
    title?.let { Text(it, style = MaterialTheme.typography.labelLarge) }
    items.forEach {
        Text("•  $it", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
    }
}

@Composable
private fun NoteText(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ChipRow(labels: List<String>, color: Color) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
        items(labels.size) { i ->
            AssistChip(
                onClick = {},
                label = { Text(labels[i]) },
                colors = AssistChipDefaults.assistChipColors(containerColor = color.copy(alpha = 0.12f), labelColor = color)
            )
        }
    }
}

/** Collapsed by default: four life-prediction domains × ~5 bullets each would otherwise
 * dominate the tab. The one-line `summary` stays visible so the collapsed state is useful. */
@Composable
private fun ExpandableSection(key: String, title: String, summary: String, content: @Composable () -> Unit) {
    // Keyed on a stable id, not the (localized) title, so a language switch keeps the state.
    var expanded by rememberSaveable(key) { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 6.dp)
            .animateContentSize()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = stringResource(if (expanded) R.string.holistic_cd_collapse else R.string.holistic_cd_expand)
            )
        }
        if (summary.isNotBlank()) Text(summary, style = MaterialTheme.typography.bodyMedium)
        if (expanded) {
            Spacer(modifier = Modifier.height(4.dp))
            content()
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

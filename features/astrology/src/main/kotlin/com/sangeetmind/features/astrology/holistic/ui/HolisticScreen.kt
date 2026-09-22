package com.sangeetmind.features.astrology.holistic.ui

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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.theme.GrahaColors
import com.sangeetmind.core.ui.theme.LocalGrahaColors
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

private enum class HolisticTab(val label: String) {
    READING("Reading"),
    INSIGHTS("Insights"),
    SANGEET("Sangeet"),
    NUMEROLOGY("Numerology+")
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
                title = { Text("Holistic Reading") },
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
                            "Weaving your chart and numbers together…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                uiState.hasNoKundli -> {
                    Text(
                        text = "Add a kundli first to get your holistic reading.",
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                uiState.needsName -> {
                    Text(
                        text = "Your kundli has no name. Numerology needs your birth name — add one to the kundli to unlock this reading.",
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
                uiState.analysis != null -> {
                    HolisticContent(
                        personName = uiState.kundli?.fullName?.toTitleCase(),
                        analysis = uiState.analysis!!,
                        language = uiState.language,
                        onLanguageChange = viewModel::selectLanguage,
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
    language: String,
    onLanguageChange: (String) -> Unit,
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
                text = personName?.takeIf { it.isNotBlank() } ?: "Your Holistic Reading",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                "Vedic astrology × numerology, read together",
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
                        text = { Text(tab.label) }
                    )
                }
            }
        }
        when (selectedTab) {
            HolisticTab.READING -> readingTab(analysis, numerology, astro, language, onLanguageChange, onRegenerate, graha)
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
        numerology?.coreNumbers?.lifePath?.let { add("Life Path ${it.number}" to graha.budha) }
        astro?.let {
            add("Moon in ${it.moonSign}" to graha.chandra)
            add("${it.lagna} Lagna" to graha.shani)
            if (it.currentMahadasha.isNotBlank()) {
                add("${it.currentMahadasha} Mahadasha" to grahaColorFor(it.currentMahadasha, graha))
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
    language: String,
    onLanguageChange: (String) -> Unit,
    onRegenerate: () -> Unit,
    graha: GrahaColors
) {
    item {
        NarrativeCard(analysis.llmEnhancedNarrative, language, onLanguageChange, onRegenerate, graha)
    }
    numerology?.coreNumbers?.let { core ->
        item {
            SectionCard("Your Numbers") {
                val rows = listOf(
                    "Life Path" to core.lifePath.number,
                    "Destiny" to core.destiny.number,
                    "Soul Urge" to core.soulUrge.number,
                    "Personality" to core.personality.number,
                    "Maturity" to core.maturity.number,
                    "Birth Day" to core.birthDay.number,
                    "Attitude" to core.attitude.number
                )
                rows.forEach { (label, number) ->
                    val key = label.lowercase().replace(' ', '_')
                    val name = numerology.interpretations[key]?.name
                    LabelValueRow(label, if (name != null) "$number · $name" else "$number")
                }
            }
        }
    }
    astro?.let {
        item {
            SectionCard("Your Chart") {
                LabelValueRow("Moon sign", it.moonSign)
                LabelValueRow("Lagna", it.lagna)
                if (it.nakshatra.isNotBlank()) {
                    LabelValueRow("Nakshatra", it.nakshatra + (it.nakshatraRuler.takeIf { r -> r.isNotBlank() }?.let { r -> " · $r" } ?: ""))
                }
                if (it.currentMahadasha.isNotBlank()) {
                    LabelValueRow(
                        "Dasha",
                        it.currentMahadasha + (it.currentAntardasha.takeIf { a -> a.isNotBlank() }?.let { a -> " / $a" } ?: ""),
                        color = grahaColorFor(it.currentMahadasha, graha)
                    )
                }
                if (it.astroMood.isNotBlank()) LabelValueRow("Today's mood", it.astroMood)
                if (it.suggestedRaag.isNotBlank()) LabelValueRow("Suggested raag", it.suggestedRaag)
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
    language: String,
    onLanguageChange: (String) -> Unit,
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
                Text("Your Reading", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                val badge = when {
                    narrative == null -> null
                    narrative.isLlm -> "AI-crafted"
                    else -> "Template"
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                FilterChip(selected = language == "en", onClick = { onLanguageChange("en") }, label = { Text("English") })
                FilterChip(selected = language == "hi", onClick = { onLanguageChange("hi") }, label = { Text("हिंदी") })
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onRegenerate) { Text("Regenerate") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val text = narrative?.text.orEmpty()
            if (text.isBlank()) {
                Text(
                    "No narrative was returned.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
            if (narrative != null && !narrative.isLlm) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This reading was assembled from your numbers and chart without AI. Tap Regenerate to try the AI-crafted version.",
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
        item { InsightListCard("Personality Synthesis", insights.personalitySynthesis, graha) }
    }
    if (insights.timingInsights.isNotEmpty()) {
        item { InsightListCard("Timing", insights.timingInsights, graha) }
    }
    if (insights.lifePathAlignment.isNotEmpty()) {
        item { InsightListCard("Life Path Alignment", insights.lifePathAlignment, graha) }
    }
    if (insights.spiritualGuidance.isNotEmpty()) {
        item { InsightListCard("Spiritual Guidance", insights.spiritualGuidance, graha) }
    }
    // Harmonies are the genuinely cross-system part, so an empty list is itself a finding
    // worth stating, unlike the sections above.
    item { HarmonyCard("Harmonies Between Systems", insights.harmonies, emptyText = "No direct harmony between your Life Path and current Mahadasha lord right now — the two systems are speaking with separate voices in this period.", graha) }
    if (insights.potentialConflicts.isNotEmpty()) {
        item { HarmonyCard("Potential Conflicts", insights.potentialConflicts, emptyText = "", graha) }
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
                        it.replaceFirstChar { c -> c.uppercase() },
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
        "astrology" -> "Astro" to graha.shani
        "numerology" -> "Numero" to graha.budha
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
            SectionCard("Combined Recommendation") {
                combined.practiceTime?.let { pt ->
                    LabelValueRow(
                        "Practice time",
                        pt.recommended.replaceFirstChar { it.uppercase() } +
                            (pt.alternative?.let { " (or $it)" } ?: "") +
                            (pt.confidence.takeIf { it.isNotBlank() }?.let { " · $it confidence" } ?: "")
                    )
                }
                combined.specificRaag?.let { LabelValueRow("Raag", it, color = graha.chandra) }
                if (combined.raagMoods.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Raag moods", style = MaterialTheme.typography.labelLarge)
                    ChipRow(combined.raagMoods.map { it.replaceFirstChar { c -> c.uppercase() } }, graha.chandra)
                }
            }
        }
    }
    if (recs.raagRecommendations.isNotEmpty()) {
        item {
            SectionCard("Where each recommendation comes from") {
                recs.raagRecommendations.forEachIndexed { i, r ->
                    if (i > 0) Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SourceTag(r.source, graha)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            r.raag ?: r.mood?.replaceFirstChar { it.uppercase() } ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        val mood = r.mood
                        if (r.raag != null && !mood.isNullOrBlank()) {
                            Text(mood, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                SectionCard("Practice Time, by system") {
                    pt.numerology?.let {
                        LabelValueRow("Numerology", it.preferred.replaceFirstChar { c -> c.uppercase() })
                        if (it.reasoning.isNotBlank()) NoteText(it.reasoning)
                    }
                    pt.astrology?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        LabelValueRow("Astrology", it.preferred.replaceFirstChar { c -> c.uppercase() })
                        if (it.basedOn.isNotBlank()) NoteText(it.basedOn)
                    }
                }
            }
        }
    }
    recs.emotionalGuidance?.numerology?.let { e ->
        item {
            SectionCard("Emotional Guidance") {
                LabelValueRow("Nature", listOf(e.primary, e.secondary).filter { it.isNotBlank() }.joinToString(" · ") { it.replaceFirstChar { c -> c.uppercase() } })
                if (e.description.isNotBlank()) Text(e.description, style = MaterialTheme.typography.bodyMedium)
                BulletList("Strengths", e.emotionalStrengths)
                BulletList("Challenges", e.emotionalChallenges)
                if (e.balancePractice.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LabelValueRow("Balance practice", e.balancePractice)
                }
            }
        }
    }
    recs.habitStyle?.numerology?.let { h ->
        item {
            SectionCard("Habit Style") {
                LabelValueRow("Style", h.primaryStyle.replace('_', ' ').replaceFirstChar { it.uppercase() })
                if (h.approach.isNotBlank()) Text(h.approach, style = MaterialTheme.typography.bodyMedium)
                BulletList("Practice tips", h.practiceTips)
            }
        }
    }
    if (recs.dailyPractices.isNotEmpty()) {
        item { SectionCard("Daily Practices") { BulletList(null, recs.dailyPractices) } }
    }
}

// ---------------- Numerology+ tab ----------------

private fun LazyListScope.numerologyTab(enhanced: NumerologyEnhancedAnalysis?, graha: GrahaColors) {
    if (enhanced == null) {
        item { NoteText("Extended numerology was not returned for this kundli.") }
        return
    }
    enhanced.planetaryRuler?.let { r ->
        item {
            val color = grahaColorFor(r.planet, graha)
            SectionCard("Ruling Planet", titleColor = color) {
                Text(
                    "${r.symbol} ${r.planet}" + (r.planetSanskrit.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.titleLarge,
                    color = color
                )
                if (r.nature.isNotBlank()) Text(r.nature, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (r.day.isNotBlank()) LabelValueRow("Day", r.day)
                if (r.color.isNotBlank()) LabelValueRow("Colours", r.color)
                if (r.gemstone.isNotBlank()) LabelValueRow("Gemstone", r.gemstone)
                if (r.metal.isNotBlank()) LabelValueRow("Metal", r.metal)
                if (r.deity.isNotBlank()) LabelValueRow("Deity", r.deity)
                if (r.mantra.isNotBlank()) LabelValueRow("Mantra", r.mantra, color = color)
                BulletList("Positive influence", r.positiveInfluence)
                BulletList("Watch for", r.negativeInfluence)
            }
        }
    }
    enhanced.yogas?.let { y ->
        if (y.detected.isNotEmpty()) {
            item {
                SectionCard("Numerology Yogas") {
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
                        if (yoga.effect.isNotBlank()) LabelValueRow("Effect", yoga.effect)
                        if (yoga.advice.isNotBlank()) LabelValueRow("Advice", yoga.advice)
                    }
                }
            }
        }
    }
    enhanced.lifePredictions?.let { lp ->
        item {
            SectionCard("Life Predictions") {
                lp.health?.let { h ->
                    ExpandableSection("Health", h.general) {
                        BulletList("Strengths", h.strengths)
                        BulletList("Vulnerabilities", h.vulnerabilities)
                        BulletList("Best practices", h.bestPractices)
                        if (h.advice.isNotBlank()) LabelValueRow("Advice", h.advice)
                    }
                }
                lp.wealth?.let { w ->
                    ExpandableSection("Wealth", w.general) {
                        if (w.pattern.isNotBlank()) LabelValueRow("Pattern", w.pattern)
                        if (w.peakYears.isNotBlank()) LabelValueRow("Peak years", w.peakYears)
                        BulletList("Strengths", w.strengths)
                        BulletList("Challenges", w.challenges)
                        if (w.advice.isNotBlank()) LabelValueRow("Advice", w.advice)
                    }
                }
                lp.relationships?.let { r ->
                    ExpandableSection("Relationships", r.general) {
                        if (r.lovePattern.isNotBlank()) LabelValueRow("Love pattern", r.lovePattern)
                        if (r.compatibleNumbers.isNotEmpty()) LabelValueRow("Compatible numbers", r.compatibleNumbers.joinToString(", "))
                        if (r.challengingNumbers.isNotEmpty()) LabelValueRow("Challenging numbers", r.challengingNumbers.joinToString(", "))
                        BulletList("Strengths", r.strengths)
                        BulletList("Challenges", r.challenges)
                        if (r.advice.isNotBlank()) LabelValueRow("Advice", r.advice)
                    }
                }
                lp.success?.let { s ->
                    ExpandableSection("Success", s.general) {
                        BulletList("Career paths", s.careerPaths)
                        if (s.successPattern.isNotBlank()) LabelValueRow("Pattern", s.successPattern)
                        if (s.peakSuccessAge.isNotBlank()) LabelValueRow("Peak age", s.peakSuccessAge)
                        if (s.advice.isNotBlank()) LabelValueRow("Advice", s.advice)
                    }
                }
            }
        }
    }
    enhanced.cautionsAndExcellence?.let { ce ->
        ce.cautions?.let { c ->
            item {
                SectionCard("Cautions", titleColor = graha.mangala) {
                    if (c.primaryWarning.isNotBlank()) Text(c.primaryWarning, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    BulletList("Watch out for", c.watchOutFor)
                    Spacer(modifier = Modifier.height(6.dp))
                    if (c.karmicLesson.isNotBlank()) LabelValueRow("Karmic lesson", c.karmicLesson)
                    if (c.healthCaution.isNotBlank()) LabelValueRow("Health", c.healthCaution)
                    if (c.relationshipCaution.isNotBlank()) LabelValueRow("Relationships", c.relationshipCaution)
                    if (c.financialCaution.isNotBlank()) LabelValueRow("Finances", c.financialCaution)
                }
            }
        }
        ce.excellence?.let { e ->
            item {
                SectionCard("How to Excel", titleColor = graha.surya) {
                    if (e.successFormula.isNotBlank()) Text(e.successFormula, style = MaterialTheme.typography.titleSmall, color = graha.surya)
                    BulletList("How to excel", e.howToExcel)
                    BulletList("Leverage", e.leverageStrengths)
                    BulletList("Daily practice", e.dailyPractice)
                    if (e.affirmation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LabelValueRow("Affirmation", "“${e.affirmation}”")
                    }
                }
            }
        }
    }
    enhanced.famousPersonalities?.let { f ->
        if (f.indian.isNotEmpty() || f.global.isNotEmpty()) {
            item {
                SectionCard("Shares Your Life Path") {
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
private fun ExpandableSection(title: String, summary: String, content: @Composable () -> Unit) {
    var expanded by rememberSaveable(title) { mutableStateOf(false) }
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
                contentDescription = if (expanded) "Collapse" else "Expand"
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

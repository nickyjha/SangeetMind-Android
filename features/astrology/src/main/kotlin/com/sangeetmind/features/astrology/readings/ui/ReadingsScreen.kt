package com.sangeetmind.features.astrology.readings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.flattenToReadableText
import com.sangeetmind.features.astrology.readings.ReadingTab
import com.sangeetmind.features.astrology.readings.ReadingsViewModel
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.platform.LocalConfiguration
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.chart.ui.Chip
import com.sangeetmind.features.astrology.chart.ui.grahaColorFor
import androidx.annotation.StringRes
import com.sangeetmind.libs.models.MarriageRemedy
import com.sangeetmind.libs.models.MarriageReading
import com.sangeetmind.libs.models.MarriageTiming
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReadingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.readings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val tabLabels = mapOf(
                ReadingTab.CAREER to R.string.readings_tab_career,
                ReadingTab.STRENGTHS to R.string.readings_tab_strengths,
                ReadingTab.MARRIAGE to R.string.readings_tab_marriage,
                ReadingTab.CHILDREN to R.string.readings_tab_children,
                ReadingTab.FOREIGN to R.string.readings_tab_foreign
            )
            ScrollableTabRow(selectedTabIndex = uiState.tab.ordinal, edgePadding = 8.dp) {
                ReadingTab.values().forEach { tab ->
                    Tab(
                        selected = uiState.tab == tab,
                        onClick = { viewModel.setTab(tab) },
                        text = { Text(stringResource(tabLabels.getValue(tab)), maxLines = 1) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (uiState.error != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(uiState.error!!, modifier = Modifier.padding(12.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                when (uiState.tab) {
                    ReadingTab.CAREER -> CareerTab(
                        isLoading = uiState.isLoading,
                        text = uiState.career?.let { it.analysis?.flattenToReadableText() ?: it.rawModelText },
                        onGenerate = viewModel::generateCareerReading
                    )
                    ReadingTab.STRENGTHS -> StrengthsTab(
                        isLoading = uiState.isLoading,
                        strengths = uiState.strengths,
                        onGenerate = viewModel::generateStrengthsReading
                    )
                    ReadingTab.MARRIAGE -> MarriageTab(
                        isLoading = uiState.isLoading,
                        married = uiState.married,
                        reading = uiState.marriage?.reading,
                        readingIsForMarried = uiState.marriage?.maritalStatus == "married",
                        onMarriedChange = viewModel::setMarried,
                        onGenerate = viewModel::generateMarriageReading
                    )
                    ReadingTab.CHILDREN -> LifeReadingTab(
                        descRes = R.string.readings_children_desc,
                        firstOptionRes = R.string.readings_children_planning,
                        secondOptionRes = R.string.readings_children_parent,
                        secondSelected = uiState.isParent,
                        onSecondSelectedChange = viewModel::setParent,
                        generateRes = R.string.readings_children_generate,
                        isLoading = uiState.isLoading,
                        onGenerate = viewModel::generateChildrenReading,
                        content = uiState.children?.reading?.let { r ->
                            LifeReadingContent(
                                summary = r.summary,
                                natureTitle = stringResource(R.string.readings_children_nature),
                                nature = r.childrenNature,
                                timingTitle = stringResource(
                                    if (uiState.children?.status == "parent") R.string.readings_marriage_timing_married
                                    else R.string.readings_children_timing_planning
                                ),
                                timing = r.timing,
                                strengthsTitle = stringResource(R.string.readings_children_strengths),
                                strengths = r.strengths,
                                careTitle = stringResource(R.string.readings_children_care),
                                care = r.carePoints,
                                advice = r.advice,
                                remedies = r.remedies,
                                disclaimer = stringResource(R.string.readings_children_disclaimer)
                            )
                        }
                    )
                    ReadingTab.FOREIGN -> LifeReadingTab(
                        descRes = R.string.readings_foreign_desc,
                        firstOptionRes = R.string.readings_foreign_planning,
                        secondOptionRes = R.string.readings_foreign_abroad,
                        secondSelected = uiState.livesAbroad,
                        onSecondSelectedChange = viewModel::setLivesAbroad,
                        generateRes = R.string.readings_foreign_generate,
                        isLoading = uiState.isLoading,
                        onGenerate = viewModel::generateForeignReading,
                        content = uiState.foreign?.reading?.let { r ->
                            LifeReadingContent(
                                summary = r.summary,
                                natureTitle = stringResource(R.string.readings_foreign_outlook),
                                nature = r.abroadOutlook,
                                timingTitle = stringResource(
                                    if (uiState.foreign?.status == "abroad") R.string.readings_marriage_timing_married
                                    else R.string.readings_foreign_timing_planning
                                ),
                                timing = r.timing,
                                strengthsTitle = stringResource(R.string.readings_children_strengths),
                                strengths = r.strengths,
                                careTitle = stringResource(R.string.readings_children_care),
                                care = r.carePoints,
                                advice = r.advice,
                                remedies = r.remedies,
                                disclaimer = stringResource(R.string.readings_foreign_disclaimer)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CareerTab(isLoading: Boolean, text: String?, onGenerate: () -> Unit) {
    Text(
        stringResource(R.string.readings_career_desc),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onGenerate, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        else Text(stringResource(R.string.readings_career_generate))
    }
    if (text != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(text, modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
private fun StrengthsTab(
    isLoading: Boolean,
    strengths: com.sangeetmind.libs.models.StrengthsReadingResponse?,
    onGenerate: () -> Unit
) {
    Text(
        stringResource(R.string.readings_strengths_desc),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onGenerate, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        else Text(stringResource(R.string.readings_strengths_generate))
    }
    if (strengths != null) {
        Spacer(modifier = Modifier.height(16.dp))
        if (strengths.strengths.isNotEmpty()) {
            SectionCard(stringResource(R.string.readings_section_strengths), strengths.strengths)
        }
        if (strengths.weaknesses.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            SectionCard(stringResource(R.string.readings_section_growth_areas), strengths.weaknesses)
        }
        strengths.remedies.forEach { remedy ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(remedy.mantraTitle, style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.readings_raag_fmt, remedy.raag), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    remedy.mantraText.forEach { line -> Text(line) }
                    remedy.why?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, items: List<String>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { item ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("• ")
                    Text(item)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarriageTab(
    isLoading: Boolean,
    married: Boolean,
    reading: MarriageReading?,
    readingIsForMarried: Boolean,
    onMarriedChange: (Boolean) -> Unit,
    onGenerate: () -> Unit
) {
    Text(
        stringResource(R.string.readings_marriage_desc),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = !married,
            onClick = { onMarriedChange(false) },
            label = { Text(stringResource(R.string.readings_marriage_single)) }
        )
        FilterChip(
            selected = married,
            onClick = { onMarriedChange(true) },
            label = { Text(stringResource(R.string.readings_marriage_married)) }
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    Button(onClick = onGenerate, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        else Text(stringResource(R.string.readings_marriage_generate))
    }
    if (reading == null) return

    Spacer(modifier = Modifier.height(16.dp))
    TextCard(stringResource(R.string.readings_marriage_summary), reading.summary)
    Spacer(modifier = Modifier.height(12.dp))
    TextCard(stringResource(R.string.readings_marriage_spouse), reading.spouseNature)
    if (reading.timing.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        TimingCard(
            title = stringResource(
                if (readingIsForMarried) R.string.readings_marriage_timing_married
                else R.string.readings_marriage_timing_single
            ),
            timing = reading.timing
        )
    }
    if (reading.relationshipStrengths.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        SectionCard(stringResource(R.string.readings_marriage_strengths), reading.relationshipStrengths)
    }
    if (reading.challenges.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        SectionCard(stringResource(R.string.readings_marriage_challenges), reading.challenges)
    }
    if (reading.manglikNote.isNotBlank()) {
        Spacer(modifier = Modifier.height(12.dp))
        TextCard(stringResource(R.string.readings_marriage_manglik), reading.manglikNote)
    }
    if (reading.advice.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        SectionCard(stringResource(R.string.readings_marriage_advice), reading.advice)
    }
    if (reading.remedies.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        SectionCard(
            stringResource(R.string.readings_marriage_remedies),
            reading.remedies.map { r ->
                if (r.forPlanet.isBlank()) r.remedy
                else stringResource(R.string.readings_marriage_remedy_fmt, r.remedy, astroTerm(r.forPlanet))
            }
        )
    }
}

/** Display-ready slice of a status-based life reading (children, foreign, ...). */
private class LifeReadingContent(
    val summary: String,
    val natureTitle: String,
    val nature: String,
    val timingTitle: String,
    val timing: List<MarriageTiming>,
    val strengthsTitle: String,
    val strengths: List<String>,
    val careTitle: String,
    val care: List<String>,
    val advice: List<String>,
    val remedies: List<MarriageRemedy>,
    val disclaimer: String?
)

/** Two status chips, a generate button, then the reading cards. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LifeReadingTab(
    @StringRes descRes: Int,
    @StringRes firstOptionRes: Int,
    @StringRes secondOptionRes: Int,
    secondSelected: Boolean,
    onSecondSelectedChange: (Boolean) -> Unit,
    @StringRes generateRes: Int,
    isLoading: Boolean,
    onGenerate: () -> Unit,
    content: LifeReadingContent?
) {
    Text(
        stringResource(descRes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = !secondSelected,
            onClick = { onSecondSelectedChange(false) },
            label = { Text(stringResource(firstOptionRes)) }
        )
        FilterChip(
            selected = secondSelected,
            onClick = { onSecondSelectedChange(true) },
            label = { Text(stringResource(secondOptionRes)) }
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    Button(onClick = onGenerate, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        else Text(stringResource(generateRes))
    }
    val r = content ?: return

    Spacer(modifier = Modifier.height(16.dp))
    TextCard(stringResource(R.string.readings_marriage_summary), r.summary)
    Spacer(modifier = Modifier.height(12.dp))
    TextCard(r.natureTitle, r.nature)
    if (r.timing.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        TimingCard(title = r.timingTitle, timing = r.timing)
    }
    if (r.strengths.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        SectionCard(r.strengthsTitle, r.strengths)
    }
    if (r.care.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        SectionCard(r.careTitle, r.care)
    }
    if (r.advice.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        SectionCard(stringResource(R.string.readings_marriage_advice), r.advice)
    }
    if (r.remedies.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        SectionCard(
            stringResource(R.string.readings_marriage_remedies),
            r.remedies.map { rem ->
                if (rem.forPlanet.isBlank()) rem.remedy
                else stringResource(R.string.readings_marriage_remedy_fmt, rem.remedy, astroTerm(rem.forPlanet))
            }
        )
    }
    if (r.disclaimer != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            r.disclaimer,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TextCard(title: String, body: String) {
    if (body.isBlank()) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(body)
        }
    }
}

/** One row per window: date range, kind/strength/dasha chips, then why it matters. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimingCard(title: String, timing: List<MarriageTiming>) {
    val graha = LocalGrahaColors.current
    val locale = LocalConfiguration.current.locales[0]
    val fmt = DateTimeFormatter.ofPattern("MMM yyyy", locale)
    fun month(iso: String) = runCatching { LocalDate.parse(iso).format(fmt) }.getOrDefault(iso)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.readings_marriage_timing_legend),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            timing.forEachIndexed { index, w ->
                if (index > 0) Divider(modifier = Modifier.padding(top = 8.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text(month(w.start) + " – " + month(w.end), style = MaterialTheme.typography.titleSmall)
                    FlowRow(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        when (w.kind) {
                            "marriage" -> Chip(stringResource(R.string.readings_marriage_kind_marriage), graha.shukra)
                            "children" -> Chip(stringResource(R.string.readings_children_kind_children), graha.guru)
                            "abroad" -> Chip(stringResource(R.string.readings_foreign_kind_abroad), graha.rahu)
                            "supportive" -> Chip(stringResource(R.string.readings_marriage_kind_supportive), graha.budha)
                            "sensitive" -> Chip(stringResource(R.string.readings_marriage_kind_sensitive), graha.mangala)
                        }
                        if (w.strength == "strong") {
                            Chip(stringResource(R.string.readings_marriage_strong), graha.surya)
                        }
                        Chip(
                            stringResource(R.string.readings_marriage_dasha_fmt, astroTerm(w.mahadasha), astroTerm(w.antardasha)),
                            grahaColorFor(w.antardasha, graha)
                        )
                    }
                    if (w.why.isNotBlank()) {
                        Text(w.why, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
}

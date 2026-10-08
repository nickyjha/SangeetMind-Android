package com.sangeetmind.features.astrology.readings.ui

import com.sangeetmind.core.ui.components.AstroTopBar
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.components.ErrorCard
import com.sangeetmind.core.ui.components.isoDatesInText
import com.sangeetmind.core.ui.text.MarkdownText
import com.sangeetmind.core.ui.text.markdownInline
import com.sangeetmind.features.astrology.R
import com.sangeetmind.libs.models.flattenToReadableText
import com.sangeetmind.features.astrology.readings.CAREER_QUESTIONS
import com.sangeetmind.features.astrology.readings.SMALL_TOPICS
import com.sangeetmind.libs.models.SmallReading
import com.sangeetmind.features.astrology.readings.ReadingTab
import com.sangeetmind.features.astrology.readings.ReadingsViewModel
import com.sangeetmind.core.ui.language.LocalAppLanguage
import com.sangeetmind.libs.models.ReadingPreview
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.platform.LocalConfiguration
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.chart.ui.Chip
import com.sangeetmind.features.astrology.chart.ui.grahaColorFor
import androidx.annotation.StringRes
import com.sangeetmind.libs.models.CareerQuestionResponse
import com.sangeetmind.libs.models.DashaStoryReading
import com.sangeetmind.libs.models.MarriageRemedy
import com.sangeetmind.libs.models.MarriageReading
import com.sangeetmind.libs.models.MarriageTiming
import com.sangeetmind.libs.models.ReadingSection
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
            AstroTopBar(
                title = stringResource(R.string.readings_title),
                onBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val tabLabels = mapOf(
                ReadingTab.CAREER to R.string.readings_tab_career,
                ReadingTab.STRENGTHS to R.string.readings_tab_strengths,
                ReadingTab.MARRIAGE to R.string.readings_tab_marriage,
                ReadingTab.SMALL to R.string.readings_tab_small,
                ReadingTab.CHILDREN to R.string.readings_tab_children,
                ReadingTab.FOREIGN to R.string.readings_tab_foreign,
                ReadingTab.WEALTH to R.string.readings_tab_wealth,
                ReadingTab.PROPERTY to R.string.readings_tab_property,
                ReadingTab.EDUCATION to R.string.readings_tab_education,
                ReadingTab.DEBT to R.string.readings_tab_debt,
                ReadingTab.RELATIONSHIP to R.string.readings_tab_relationship,
                ReadingTab.HEALTH to R.string.readings_tab_health,
                ReadingTab.DASHA to R.string.readings_tab_dasha
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
                    ErrorCard(message = uiState.error, onRetry = viewModel::retry)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (!uiState.tabHasReading && uiState.previewKey != null) {
                    PreviewCard(preview = uiState.previews[uiState.previewKey])
                    Spacer(modifier = Modifier.height(16.dp))
                }

                when (uiState.tab) {
                    ReadingTab.CAREER -> CareerTab(
                        isLoading = uiState.isLoading,
                        question = uiState.careerQuestion,
                        onQuestionChange = viewModel::setCareerQuestion,
                        text = uiState.career?.let { it.analysis?.flattenToReadableText() ?: it.rawModelText },
                        answer = uiState.careerAnswer?.takeIf { it.question == uiState.careerQuestion },
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
                    ReadingTab.SMALL -> SmallTab(
                        topic = uiState.smallTopic,
                        onTopicChange = viewModel::setSmallTopic,
                        isLoading = uiState.isLoading,
                        reading = uiState.small[uiState.smallTopic]?.reading,
                        onGenerate = viewModel::generateSmallReading
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
                                disclaimer = stringResource(R.string.readings_children_disclaimer),
                                sections = r.sections
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
                                disclaimer = stringResource(R.string.readings_foreign_disclaimer),
                                sections = r.sections
                            )
                        }
                    )
                    ReadingTab.WEALTH -> LifeReadingTab(
                        descRes = R.string.readings_wealth_desc,
                        firstOptionRes = R.string.readings_wealth_job,
                        secondOptionRes = R.string.readings_wealth_business,
                        secondSelected = uiState.ownsBusiness,
                        onSecondSelectedChange = viewModel::setOwnsBusiness,
                        generateRes = R.string.readings_wealth_generate,
                        isLoading = uiState.isLoading,
                        onGenerate = viewModel::generateWealthReading,
                        content = uiState.wealth?.reading?.let { r ->
                            LifeReadingContent(
                                summary = r.summary,
                                natureTitle = stringResource(R.string.readings_wealth_nature),
                                nature = r.moneyNature,
                                timingTitle = stringResource(R.string.readings_wealth_timing),
                                timing = r.timing,
                                strengthsTitle = stringResource(R.string.readings_children_strengths),
                                strengths = r.strengths,
                                careTitle = stringResource(R.string.readings_children_care),
                                care = r.carePoints,
                                advice = r.advice,
                                remedies = r.remedies,
                                disclaimer = stringResource(R.string.readings_wealth_disclaimer),
                                sections = r.sections
                            )
                        }
                    )
                    ReadingTab.PROPERTY -> LifeReadingTab(
                        descRes = R.string.readings_property_desc,
                        firstOptionRes = R.string.readings_property_home,
                        secondOptionRes = R.string.readings_property_vehicle,
                        secondSelected = uiState.aboutVehicle,
                        onSecondSelectedChange = viewModel::setAboutVehicle,
                        generateRes = R.string.readings_property_generate,
                        isLoading = uiState.isLoading,
                        onGenerate = viewModel::generatePropertyReading,
                        content = uiState.property?.reading?.let { r ->
                            val vehicle = uiState.property?.status == "vehicle"
                            LifeReadingContent(
                                summary = r.summary,
                                natureTitle = stringResource(
                                    if (vehicle) R.string.readings_property_outlook_vehicle
                                    else R.string.readings_property_outlook_home
                                ),
                                nature = r.outlook,
                                timingTitle = stringResource(
                                    if (vehicle) R.string.readings_property_timing_vehicle
                                    else R.string.readings_property_timing_home
                                ),
                                timing = r.timing,
                                strengthsTitle = stringResource(R.string.readings_children_strengths),
                                strengths = r.strengths,
                                careTitle = stringResource(R.string.readings_children_care),
                                care = r.carePoints,
                                advice = r.advice,
                                remedies = r.remedies,
                                disclaimer = stringResource(R.string.readings_property_disclaimer),
                                sections = r.sections
                            )
                        }
                    )
                    ReadingTab.DASHA -> DashaStoryTab(
                        isLoading = uiState.isLoading,
                        onGenerate = viewModel::generateDashaStoryReading,
                        reading = uiState.dashaStory?.reading
                    )
                    ReadingTab.HEALTH -> LifeReadingTab(
                        descRes = R.string.readings_health_desc,
                        firstOptionRes = R.string.readings_health_body,
                        secondOptionRes = R.string.readings_health_mind,
                        secondSelected = uiState.aboutMind,
                        onSecondSelectedChange = viewModel::setAboutMind,
                        generateRes = R.string.readings_health_generate,
                        isLoading = uiState.isLoading,
                        onGenerate = viewModel::generateHealthReading,
                        content = uiState.health?.reading?.let { r ->
                            LifeReadingContent(
                                summary = r.summary,
                                natureTitle = stringResource(R.string.readings_health_outlook),
                                nature = r.outlook,
                                timingTitle = stringResource(R.string.readings_health_timing),
                                timing = r.timing,
                                strengthsTitle = stringResource(R.string.readings_children_strengths),
                                strengths = r.strengths,
                                careTitle = stringResource(R.string.readings_children_care),
                                care = r.carePoints,
                                advice = r.advice,
                                remedies = r.remedies,
                                disclaimer = stringResource(R.string.readings_health_disclaimer),
                                sections = r.sections
                            )
                        }
                    )
                    ReadingTab.EDUCATION -> LifeReadingTab(
                        descRes = R.string.readings_education_desc,
                        firstOptionRes = R.string.readings_education_student,
                        secondOptionRes = R.string.readings_education_higher,
                        secondSelected = uiState.higherStudies,
                        onSecondSelectedChange = viewModel::setHigherStudies,
                        generateRes = R.string.readings_education_generate,
                        isLoading = uiState.isLoading,
                        onGenerate = viewModel::generateEducationReading,
                        content = uiState.education?.reading?.let { r ->
                            LifeReadingContent(
                                summary = r.summary,
                                natureTitle = stringResource(R.string.readings_education_outlook),
                                nature = r.outlook,
                                timingTitle = stringResource(R.string.readings_education_timing),
                                timing = r.timing,
                                strengthsTitle = stringResource(R.string.readings_children_strengths),
                                strengths = r.strengths,
                                careTitle = stringResource(R.string.readings_children_care),
                                care = r.carePoints,
                                advice = r.advice,
                                remedies = r.remedies,
                                disclaimer = stringResource(R.string.readings_education_disclaimer),
                                sections = r.sections
                            )
                        }
                    )
                    ReadingTab.RELATIONSHIP -> LifeReadingTab(
                        descRes = R.string.readings_rel_desc,
                        firstOptionRes = R.string.readings_rel_strain,
                        secondOptionRes = R.string.readings_rel_remarriage,
                        secondSelected = uiState.aboutRemarriage,
                        onSecondSelectedChange = viewModel::setAboutRemarriage,
                        generateRes = R.string.readings_rel_generate,
                        isLoading = uiState.isLoading,
                        onGenerate = viewModel::generateRelationshipReading,
                        content = uiState.relationship?.reading?.let { r ->
                            LifeReadingContent(
                                summary = r.summary,
                                natureTitle = stringResource(R.string.readings_rel_outlook),
                                nature = r.outlook,
                                timingTitle = stringResource(R.string.readings_rel_timing),
                                timing = r.timing,
                                strengthsTitle = stringResource(R.string.readings_children_strengths),
                                strengths = r.strengths,
                                careTitle = stringResource(R.string.readings_children_care),
                                care = r.carePoints,
                                advice = r.advice,
                                remedies = r.remedies,
                                disclaimer = stringResource(R.string.readings_rel_disclaimer),
                                sections = r.sections
                            )
                        }
                    )
                    ReadingTab.DEBT -> LifeReadingTab(
                        descRes = R.string.readings_debt_desc,
                        firstOptionRes = R.string.readings_debt_debt,
                        secondOptionRes = R.string.readings_debt_dispute,
                        secondSelected = uiState.aboutDispute,
                        onSecondSelectedChange = viewModel::setAboutDispute,
                        generateRes = R.string.readings_debt_generate,
                        isLoading = uiState.isLoading,
                        onGenerate = viewModel::generateDebtReading,
                        content = uiState.debt?.reading?.let { r ->
                            LifeReadingContent(
                                summary = r.summary,
                                natureTitle = stringResource(R.string.readings_debt_outlook),
                                nature = r.outlook,
                                timingTitle = stringResource(R.string.readings_debt_timing),
                                timing = r.timing,
                                strengthsTitle = stringResource(R.string.readings_children_strengths),
                                strengths = r.strengths,
                                careTitle = stringResource(R.string.readings_children_care),
                                care = r.carePoints,
                                advice = r.advice,
                                remedies = r.remedies,
                                disclaimer = stringResource(R.string.readings_debt_disclaimer),
                                sections = r.sections
                            )
                        }
                    )
                }
            }
        }
    }
}

/** The full career reading, or one career question with the chart's computed answer. */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CareerTab(
    isLoading: Boolean,
    question: String?,
    onQuestionChange: (String?) -> Unit,
    text: String?,
    answer: CareerQuestionResponse?,
    onGenerate: () -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FilterChip(
            selected = question == null,
            onClick = { onQuestionChange(null) },
            label = { Text(stringResource(R.string.readings_career_q_full)) }
        )
        CAREER_QUESTIONS.forEach { q ->
            FilterChip(
                selected = question == q,
                onClick = { onQuestionChange(q) },
                label = { Text(stringResource(careerQuestionLabel(q))) }
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        stringResource(if (question == null) R.string.readings_career_desc else R.string.readings_career_q_desc),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onGenerate, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        else Text(stringResource(if (question == null) R.string.readings_career_generate else R.string.readings_career_q_generate))
    }
    if (question == null) {
        if (text != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                MarkdownText(text, modifier = Modifier.padding(16.dp))
            }
        }
    } else {
        val r = answer?.reading
        if (answer != null && r != null) {
            careerVerdictLabel(answer.facts.verdict)?.let { label ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.readings_career_answer), style = MaterialTheme.typography.labelLarge)
                        Text(
                            stringResource(label),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            LifeReadingCards(
                LifeReadingContent(
                    summary = r.summary,
                    natureTitle = stringResource(R.string.readings_career_outlook),
                    nature = r.outlook,
                    timingTitle = stringResource(R.string.readings_career_timing),
                    timing = r.timing,
                    strengthsTitle = stringResource(R.string.readings_children_strengths),
                    strengths = r.strengths,
                    careTitle = stringResource(R.string.readings_children_care),
                    care = r.carePoints,
                    advice = r.advice,
                    remedies = r.remedies,
                    disclaimer = stringResource(R.string.readings_career_disclaimer),
                    sections = r.sections
                )
            )
        }
    }
}

@StringRes
private fun careerQuestionLabel(question: String): Int = when (question) {
    "promotion" -> R.string.readings_career_q_promotion
    "govt_private" -> R.string.readings_career_q_govt_private
    "job_business" -> R.string.readings_career_q_job_business
    else -> R.string.readings_career_q_job_change
}

@StringRes
private fun careerVerdictLabel(verdict: String): Int? = when (verdict) {
    "frequent_change" -> R.string.readings_career_v_frequent_change
    "some_change" -> R.string.readings_career_v_some_change
    "stable" -> R.string.readings_career_v_stable
    "strong" -> R.string.readings_career_v_strong
    "moderate" -> R.string.readings_career_v_moderate
    "mild" -> R.string.readings_career_v_mild
    "government" -> R.string.readings_career_v_government
    "private" -> R.string.readings_career_v_private
    "business" -> R.string.readings_career_v_business
    "job" -> R.string.readings_career_v_job
    "either" -> R.string.readings_career_v_either
    else -> null
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
                        MarkdownText(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text(markdownInline(item))
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
    if (reading != null) {
        Spacer(modifier = Modifier.height(16.dp))
        if (reading.sections.isNotEmpty()) {
            SectionCards(reading.sections)
            Spacer(modifier = Modifier.height(12.dp))
            MoreDetails(key = "marriage") { MarriageLegacyCards(reading, readingIsForMarried) }
        } else {
            MarriageLegacyCards(reading, readingIsForMarried)
        }
    }
}

/** The pre-sections marriage layout: summary, spouse, timing, strengths, challenges, manglik, advice, remedies. */
@Composable
private fun MarriageLegacyCards(reading: MarriageReading, readingIsForMarried: Boolean) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextCard(stringResource(R.string.readings_marriage_summary), reading.summary)
        TextCard(stringResource(R.string.readings_marriage_spouse), reading.spouseNature)
        if (reading.timing.isNotEmpty()) {
            TimingCard(
                title = stringResource(
                    if (readingIsForMarried) R.string.readings_marriage_timing_married
                    else R.string.readings_marriage_timing_single
                ),
                timing = reading.timing
            )
        }
        if (reading.relationshipStrengths.isNotEmpty()) {
            SectionCard(stringResource(R.string.readings_marriage_strengths), reading.relationshipStrengths)
        }
        if (reading.challenges.isNotEmpty()) {
            SectionCard(stringResource(R.string.readings_marriage_challenges), reading.challenges)
        }
        if (reading.manglikNote.isNotBlank()) {
            TextCard(stringResource(R.string.readings_marriage_manglik), reading.manglikNote)
        }
        if (reading.advice.isNotEmpty()) {
            SectionCard(stringResource(R.string.readings_marriage_advice), reading.advice)
        }
        if (reading.remedies.isNotEmpty()) {
            SectionCard(
                stringResource(R.string.readings_marriage_remedies),
                reading.remedies.map { r ->
                    if (r.forPlanet.isBlank()) r.remedy
                    else stringResource(R.string.readings_marriage_remedy_fmt, r.remedy, astroTerm(r.forPlanet))
                }
            )
        }
    }
}

/** The six structured sections, one card each, in the order the backend sends them. */
@Composable
private fun SectionCards(sections: List<ReadingSection>) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sections.forEach { section ->
            TextCard(section.heading, isoDatesInText(section.body), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Collapsed by default: with the six sections as the reading, the legacy blocks (timing,
 * strengths, care points, advice, remedies) stay one tap away so nothing is lost. */
@Composable
private fun MoreDetails(key: String, content: @Composable () -> Unit) {
    var expanded by rememberSaveable(key) { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.readings_more_details),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = stringResource(if (expanded) R.string.holistic_cd_collapse else R.string.holistic_cd_expand)
            )
        }
        if (expanded) content()
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
    val disclaimer: String?,
    /** The six structured sections; empty for readings made before the backend added them. */
    val sections: List<ReadingSection> = emptyList()
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
    content?.let { LifeReadingCards(it) }
}

/** The six sections first (when the backend sent them) with the legacy blocks under
 * "More details"; otherwise the legacy blocks as before. Then the disclaimer. */
@Composable
private fun LifeReadingCards(r: LifeReadingContent) {
    Spacer(modifier = Modifier.height(16.dp))
    if (r.sections.isNotEmpty()) {
        SectionCards(r.sections)
        Spacer(modifier = Modifier.height(12.dp))
        MoreDetails(key = "life") { LifeLegacyCards(r) }
    } else {
        LifeLegacyCards(r)
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

/** Summary, nature, timing, strengths, care points, advice and remedies. */
@Composable
private fun LifeLegacyCards(r: LifeReadingContent) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextCard(stringResource(R.string.readings_marriage_summary), r.summary)
        TextCard(r.natureTitle, r.nature)
        if (r.timing.isNotEmpty()) {
            TimingCard(title = r.timingTitle, timing = r.timing)
        }
        if (r.strengths.isNotEmpty()) {
            SectionCard(r.strengthsTitle, r.strengths)
        }
        if (r.care.isNotEmpty()) {
            SectionCard(r.careTitle, r.care)
        }
        if (r.advice.isNotEmpty()) {
            SectionCard(stringResource(R.string.readings_marriage_advice), r.advice)
        }
        if (r.remedies.isNotEmpty()) {
            SectionCard(
                stringResource(R.string.readings_marriage_remedies),
                r.remedies.map { rem ->
                    if (rem.forPlanet.isBlank()) rem.remedy
                    else stringResource(R.string.readings_marriage_remedy_fmt, rem.remedy, astroTerm(rem.forPlanet))
                }
            )
        }
    }
}

@Composable
private fun TextCard(title: String, body: String, style: TextStyle = MaterialTheme.typography.bodyLarge) {
    if (body.isNotBlank()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                MarkdownText(body, style = style)
            }
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
                            "wealth" -> Chip(stringResource(R.string.readings_wealth_kind), graha.chandra)
                            "career" -> Chip(stringResource(R.string.readings_career_kind), graha.shani)
                            "property" -> Chip(stringResource(R.string.readings_property_kind_home), graha.mangala)
                            "vehicle" -> Chip(stringResource(R.string.readings_property_kind_vehicle), graha.shukra)
                            "education" -> Chip(stringResource(R.string.readings_education_kind), graha.guru)
                            "vitality" -> Chip(stringResource(R.string.readings_health_kind_vitality), graha.surya)
                            "calm" -> Chip(stringResource(R.string.readings_health_kind_calm), graha.chandra)
                            "harmony" -> Chip(stringResource(R.string.readings_rel_kind_harmony), graha.shukra)
                            "remarriage" -> Chip(stringResource(R.string.readings_rel_kind_remarriage), graha.guru)
                            "debt" -> Chip(stringResource(R.string.readings_debt_kind_debt), graha.shani)
                            "dispute" -> Chip(stringResource(R.string.readings_debt_kind_dispute), graha.mangala)
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
                        MarkdownText(w.why, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
}

/** Before a paid reading: the free chart preview (key planets, how many good periods lie
 * ahead) and what the full reading covers. The price is in each tab's description. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreviewCard(preview: ReadingPreview?) {
    val graha = LocalGrahaColors.current
    val languageCode = LocalAppLanguage.current.code
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.readings_preview_title), style = MaterialTheme.typography.titleMedium)
            if (preview == null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
            } else {
                FlowRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    preview.keyPlanets.forEach { p ->
                        val sign = p.sign
                        val house = p.house
                        val text = if (sign != null && house != null) {
                            stringResource(R.string.readings_preview_planet_fmt, astroTerm(p.planet), astroTerm(sign), house)
                        } else astroTerm(p.planet)
                        Chip(text, grahaColorFor(p.planet, graha))
                    }
                }
                Text(
                    markdownInline(preview.teaser.forLanguage(languageCode)),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                stringResource(R.string.readings_preview_includes),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    R.string.readings_preview_inc_timing,
                    R.string.readings_preview_inc_strengths,
                    R.string.readings_preview_inc_care,
                    R.string.readings_preview_inc_advice,
                    R.string.readings_preview_inc_remedies
                ).forEach { Chip(stringResource(it), graha.guru) }
            }
        }
    }
}

@StringRes
private fun smallTopicLabel(topic: String): Int = when (topic) {
    "love_style" -> R.string.readings_small_love_style
    "ideal_partner" -> R.string.readings_small_ideal_partner
    else -> R.string.readings_small_in_laws
}

/** Short ₹49 readings on one question each: love style, ideal partner, in-laws. */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SmallTab(
    topic: String,
    onTopicChange: (String) -> Unit,
    isLoading: Boolean,
    reading: SmallReading?,
    onGenerate: () -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SMALL_TOPICS.forEach { t ->
            FilterChip(
                selected = topic == t,
                onClick = { onTopicChange(t) },
                label = { Text(stringResource(smallTopicLabel(t))) }
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        stringResource(
            when (topic) {
                "love_style" -> R.string.readings_small_love_style_desc
                "ideal_partner" -> R.string.readings_small_ideal_partner_desc
                else -> R.string.readings_small_in_laws_desc
            }
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onGenerate, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        else Text(stringResource(R.string.readings_small_generate_fmt, stringResource(smallTopicLabel(topic))))
    }
    if (reading != null) {

        Spacer(modifier = Modifier.height(16.dp))
        TextCard(stringResource(R.string.readings_marriage_summary), reading.summary)
        reading.points.forEach { p ->
            Spacer(modifier = Modifier.height(12.dp))
            TextCard(p.title, p.text)
        }
        if (reading.tip.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            TextCard(stringResource(R.string.readings_small_tip), reading.tip)
        }
        if (reading.remedies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            SectionCard(
                stringResource(R.string.readings_marriage_remedies),
                reading.remedies.map { rem ->
                    if (rem.forPlanet.isBlank()) rem.remedy
                    else stringResource(R.string.readings_marriage_remedy_fmt, rem.remedy, astroTerm(rem.forPlanet))
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.readings_small_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Lifetime dasha story: description, generate button, then summary, "right now" and one card
 * per mahadasha chapter with its years and ages; the running period is highlighted. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DashaStoryTab(isLoading: Boolean, onGenerate: () -> Unit, reading: DashaStoryReading?) {
    Text(
        stringResource(R.string.readings_dasha_desc),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(12.dp))
    Button(onClick = onGenerate, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        else Text(stringResource(R.string.readings_dasha_generate))
    }
    if (reading != null) {
        val graha = LocalGrahaColors.current
        val locale = LocalConfiguration.current.locales[0]
        val fmt = DateTimeFormatter.ofPattern("MMM yyyy", locale)
        fun month(iso: String) = runCatching { LocalDate.parse(iso).format(fmt) }.getOrDefault(iso)

        Spacer(modifier = Modifier.height(16.dp))
        TextCard(stringResource(R.string.readings_marriage_summary), reading.summary)
        Spacer(modifier = Modifier.height(12.dp))
        TextCard(stringResource(R.string.readings_dasha_now), reading.now)
        if (reading.timing.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.readings_dasha_chapters), style = MaterialTheme.typography.titleMedium)
        }
        reading.timing.forEach { ch ->
            Spacer(modifier = Modifier.height(12.dp))
            val current = ch.phase == "current"
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = if (current) {
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                } else {
                    CardDefaults.cardColors()
                }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.readings_dasha_chapter_title, astroTerm(ch.mahadasha), ch.title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        stringResource(R.string.readings_dasha_chapter_span, month(ch.start), month(ch.end), ch.ageFrom, ch.ageTo),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        when (ch.phase) {
                            "current" -> Chip(stringResource(R.string.readings_dasha_when_current), graha.surya)
                            "past" -> Chip(stringResource(R.string.readings_dasha_when_past), MaterialTheme.colorScheme.onSurfaceVariant)
                            "future" -> Chip(stringResource(R.string.readings_dasha_when_future), graha.budha)
                        }
                        when (ch.kind) {
                            "growth" -> Chip(stringResource(R.string.readings_dasha_kind_growth), graha.guru)
                            "steady" -> Chip(stringResource(R.string.readings_dasha_kind_steady), graha.chandra)
                            "sensitive" -> Chip(stringResource(R.string.readings_dasha_kind_sensitive), graha.mangala)
                        }
                    }
                    if (ch.why.isNotBlank()) {
                        MarkdownText(ch.why, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
        if (reading.advice.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            SectionCard(stringResource(R.string.readings_marriage_advice), reading.advice)
        }
        if (reading.remedies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            SectionCard(
                stringResource(R.string.readings_marriage_remedies),
                reading.remedies.map { rem ->
                    if (rem.forPlanet.isBlank()) rem.remedy
                    else stringResource(R.string.readings_marriage_remedy_fmt, rem.remedy, astroTerm(rem.forPlanet))
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.readings_dasha_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

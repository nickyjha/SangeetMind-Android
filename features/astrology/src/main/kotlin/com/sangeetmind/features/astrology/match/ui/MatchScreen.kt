package com.sangeetmind.features.astrology.match.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.components.ErrorCard
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.LocalAppLanguage
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.match.MATCH_RELATIONS
import com.sangeetmind.features.astrology.match.MatchViewModel
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.KundliMatchResult
import com.sangeetmind.libs.models.MatchDosha
import com.sangeetmind.libs.models.RelationMatchResult
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.libs.models.toTitleCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchScreen(
    onNavigateBack: () -> Unit,
    viewModel: MatchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.match_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.mangala.copy(alpha = 0.14f),
                    titleContentColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.mangala,
                    navigationIconContentColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.mangala
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Marriage keeps the classical Ashtakoot; the other relations use /v1/match/relation.
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MATCH_RELATIONS.forEach { relation ->
                    FilterChip(
                        selected = uiState.relation == relation,
                        onClick = { viewModel.setRelation(relation) },
                        label = { Text(stringResource(relationLabelRes(relation))) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            val marriage = uiState.relation == "marriage"
            Text(
                text = stringResource(if (marriage) R.string.match_intro else R.string.match_intro_relation),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
            val (labelA, labelB) = roleLabelRes(uiState.relation)

            if (uiState.isLoadingKundlis) {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.kundlis.size < 2) {
                Text(
                    text = stringResource(R.string.match_need_two),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                KundliDropdown(
                    label = stringResource(labelA),
                    selected = uiState.personA,
                    options = uiState.kundlis,
                    onSelect = viewModel::selectPersonA
                )
                Spacer(modifier = Modifier.height(16.dp))
                KundliDropdown(
                    label = stringResource(labelB),
                    selected = uiState.personB,
                    options = uiState.kundlis,
                    onSelect = viewModel::selectPersonB
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = viewModel::checkCompatibility,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !uiState.isMatching && uiState.personA != null && uiState.personB != null
                ) {
                    if (uiState.isMatching) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(stringResource(R.string.match_check))
                    }
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                ErrorCard(message = uiState.error, onRetry = { viewModel.retry() })
            }

            uiState.result?.let { result ->
                Spacer(modifier = Modifier.height(24.dp))
                MatchResultCard(result)
            }
            uiState.relationResult?.let { result ->
                Spacer(modifier = Modifier.height(24.dp))
                RelationResultCard(result)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KundliDropdown(
    label: String,
    selected: Kundli?,
    options: List<Kundli>,
    onSelect: (Kundli) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.let { it.fullName?.takeIf { n -> n.isNotBlank() }?.toTitleCase() ?: it.birthPlace } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { kundli ->
                DropdownMenuItem(
                    text = {
                        Text(kundli.fullName?.takeIf { it.isNotBlank() }?.toTitleCase() ?: "${kundli.birthDate} · ${kundli.birthPlace}")
                    },
                    onClick = {
                        onSelect(kundli)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MatchResultCard(result: KundliMatchResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.match_gunas_fmt, result.totalGunas.toInt(), result.maxGunas.toInt()),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = astroTerm(result.verdict).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            KootaRow(stringResource(R.string.match_koota_varna), result.ashtakoot.varna)
            KootaRow(stringResource(R.string.match_koota_vashya), result.ashtakoot.vashya)
            KootaRow(stringResource(R.string.match_koota_tara), result.ashtakoot.tara)
            KootaRow(stringResource(R.string.match_koota_yoni), result.ashtakoot.yoni)
            KootaRow(stringResource(R.string.match_koota_graha_maitri), result.ashtakoot.grahaMaitri)
            KootaRow(stringResource(R.string.match_koota_gana), result.ashtakoot.gana)
            KootaRow(stringResource(R.string.match_koota_bhakoot), result.ashtakoot.bhakoot)
            KootaRow(stringResource(R.string.match_koota_nadi), result.ashtakoot.nadi)

            Spacer(modifier = Modifier.height(12.dp))
            Text(stringResource(R.string.match_status_fmt, astroTerm("Manglik")), style = MaterialTheme.typography.titleSmall)
            val languageCode = LocalAppLanguage.current.code
            Text(stringResource(R.string.match_person_a_fmt, result.manglik.personA.summaryText.forLanguage(languageCode)), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.match_person_b_fmt, result.manglik.personB.summaryText.forLanguage(languageCode)), style = MaterialTheme.typography.bodySmall)

            result.doshaExceptions?.let { exceptions ->
                val shown = listOf(
                    R.string.match_dosha_nadi to exceptions.nadi,
                    R.string.match_dosha_bhakoot to exceptions.bhakoot
                ).filter { it.second.present }
                if (shown.isNotEmpty()) {
                    shown.forEach { (label, dosha) -> MatchDoshaRow(stringResource(label), dosha, languageCode) }
                    Text(
                        stringResource(R.string.match_dosha_points_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (result.remedyHint != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.match_remedy_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/** A Nadi/Bhakoot dosha that is present: its status, then the exceptions that cancel it. */
@Composable
private fun MatchDoshaRow(label: String, dosha: MatchDosha, languageCode: String) {
    Spacer(modifier = Modifier.height(12.dp))
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
        Text(
            if (dosha.cancelled) stringResource(R.string.match_dosha_cancelled) else stringResource(R.string.match_dosha_present),
            style = MaterialTheme.typography.labelLarge,
            color = if (dosha.cancelled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
    if (dosha.reasons.isEmpty()) {
        Text(stringResource(R.string.match_dosha_no_exception), style = MaterialTheme.typography.bodySmall)
    }
    dosha.reasons.forEach {
        Text(it.forLanguage(languageCode), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun KootaRow(label: String, koota: com.sangeetmind.libs.models.Koota) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text("${koota.points} / ${koota.max}", style = MaterialTheme.typography.bodyMedium)
    }
}

private fun relationLabelRes(relation: String): Int = when (relation) {
    "parent_child" -> R.string.match_rel_parent_child
    "siblings" -> R.string.match_rel_siblings
    "business" -> R.string.match_rel_business
    "friends" -> R.string.match_rel_friends
    else -> R.string.match_rel_marriage
}

/** Dropdown labels: Boy/Girl for marriage, the elder first for parent–child and siblings. */
private fun roleLabelRes(relation: String): Pair<Int, Int> = when (relation) {
    "parent_child" -> R.string.match_role_parent to R.string.match_role_child
    "siblings" -> R.string.match_role_elder to R.string.match_role_younger
    "business", "friends" -> R.string.match_role_person1 to R.string.match_role_person2
    else -> R.string.match_person_a to R.string.match_person_b
}

/** Score out of 36, level, summary, one row per factor (points, status colour, reason), tips. */
@Composable
private fun RelationResultCard(result: RelationMatchResult) {
    val languageCode = LocalAppLanguage.current.code
    val graha = LocalGrahaColors.current
    val total = if (result.total % 1.0 == 0.0) result.total.toInt().toString() else result.total.toString()
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.match_points_fmt, total, result.max.toInt()),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = stringResource(
                    when (result.level) {
                        "strong" -> R.string.match_level_strong
                        "good" -> R.string.match_level_good
                        "mixed" -> R.string.match_level_mixed
                        else -> R.string.match_level_careful
                    }
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(result.summary.forLanguage(languageCode), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            result.factors.forEach { f ->
                val color = when (f.status) {
                    "good" -> graha.budha
                    "caution" -> graha.mangala
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(f.label.forLanguage(languageCode), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    val pts = if (f.points % 1.0 == 0.0) f.points.toInt().toString() else f.points.toString()
                    Text("$pts / ${f.max.toInt()}", style = MaterialTheme.typography.titleSmall, color = color)
                }
                Text(
                    f.reason.forLanguage(languageCode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (result.tips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.match_tips), style = MaterialTheme.typography.titleSmall)
                result.tips.forEach { tip ->
                    Text("\u2022 " + tip.forLanguage(languageCode), style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.match_relation_note),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

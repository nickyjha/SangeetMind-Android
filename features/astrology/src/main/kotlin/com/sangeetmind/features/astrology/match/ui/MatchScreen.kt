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
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.match.MatchViewModel
import com.sangeetmind.libs.models.Kundli
import com.sangeetmind.libs.models.KundliMatchResult
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
            Text(
                text = stringResource(R.string.match_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

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
                    label = stringResource(R.string.match_person_a),
                    selected = uiState.personA,
                    options = uiState.kundlis,
                    onSelect = viewModel::selectPersonA
                )
                Spacer(modifier = Modifier.height(16.dp))
                KundliDropdown(
                    label = stringResource(R.string.match_person_b),
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
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            uiState.result?.let { result ->
                Spacer(modifier = Modifier.height(24.dp))
                MatchResultCard(result)
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
                text = result.verdict.replaceFirstChar { it.uppercase() },
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
            Text(stringResource(R.string.match_person_a_fmt, result.manglik.personA.summary), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.match_person_b_fmt, result.manglik.personB.summary), style = MaterialTheme.typography.bodySmall)

            result.remedyHint?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun KootaRow(label: String, koota: com.sangeetmind.libs.models.Koota) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text("${koota.points} / ${koota.max}", style = MaterialTheme.typography.bodyMedium)
    }
}

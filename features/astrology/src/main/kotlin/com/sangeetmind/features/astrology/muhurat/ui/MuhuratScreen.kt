package com.sangeetmind.features.astrology.muhurat.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.muhurat.MUHURAT_INTENTS
import com.sangeetmind.features.astrology.muhurat.MuhuratViewModel
import com.sangeetmind.libs.models.MuhuratSlot
import com.sangeetmind.libs.models.VivahResponse
import com.sangeetmind.libs.models.toTitleCase
import com.sangeetmind.core.ui.language.LocalAppLanguage
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.chart.ui.Chip
import androidx.compose.ui.platform.LocalConfiguration
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MuhuratScreen(
    onNavigateBack: () -> Unit,
    viewModel: MuhuratViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.muhurat_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.guru.copy(alpha = 0.14f),
                    titleContentColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.guru,
                    navigationIconContentColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.guru
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
            Text(stringResource(R.string.muhurat_purpose), style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MUHURAT_INTENTS.forEach { intent ->
                    FilterChip(
                        selected = uiState.intent == intent,
                        onClick = { viewModel.onIntentChange(intent) },
                        label = { Text(intentLabel(intent)) }
                    )
                }
            }

            if (uiState.isMarriage && uiState.kundlis.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.muhurat_vivah_couple), style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.muhurat_vivah_couple_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.kundlis.forEach { k ->
                        FilterChip(
                            selected = k.id in uiState.coupleIds,
                            onClick = { viewModel.toggleCouple(k.id) },
                            label = { Text(k.fullName?.takeIf { it.isNotBlank() }?.toTitleCase() ?: k.birthDate) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.windowStart,
                onValueChange = viewModel::onWindowStartChange,
                label = { Text(stringResource(R.string.muhurat_start_date)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.windowEnd,
                onValueChange = viewModel::onWindowEndChange,
                label = { Text(stringResource(R.string.muhurat_end_date)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            )

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = viewModel::findMuhurat,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !uiState.isSearching
            ) {
                if (uiState.isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.muhurat_find))
                }
            }

            val vivah = uiState.vivah
            if (uiState.isMarriage && vivah != null) {
                Spacer(modifier = Modifier.height(24.dp))
                VivahResults(vivah)
            } else if (uiState.results.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(stringResource(R.string.muhurat_results), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.results) { slot -> MuhuratSlotCard(slot) }
                }
            }
        }
    }
}

@Composable
private fun MuhuratSlotCard(slot: MuhuratSlot) {
    val verdictColor = when (slot.verdict) {
        "auspicious" -> MaterialTheme.colorScheme.primary
        "avoid" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(slot.date, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${astroTerm(slot.vara)} · ${astroTerm(slot.tithi)} · ${astroTerm(slot.nakshatra)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = verdictLabel(slot.verdict),
                    color = verdictColor,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(stringResource(R.string.muhurat_score_fmt, slot.score), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/** Vivah muhurat: a card per day with its windows (time, nakshatras, tithis), then the
 * periods with no muhurat and why. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VivahResults(vivah: VivahResponse) {
    val graha = LocalGrahaColors.current
    val languageCode = LocalAppLanguage.current.code
    val locale = LocalConfiguration.current.locales[0]
    val dayFmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", locale)
    val timeFmt = DateTimeFormatter.ofPattern("d MMM, HH:mm", locale)
    val shortDate = DateTimeFormatter.ofPattern("d MMM", locale)
    fun time(iso: String) = runCatching { OffsetDateTime.parse(iso).format(timeFmt) }.getOrDefault(iso)
    fun date(iso: String, fmt: DateTimeFormatter) = runCatching { LocalDate.parse(iso).format(fmt) }.getOrDefault(iso)

    Text(stringResource(R.string.muhurat_vivah_results_fmt, vivah.days.size), style = MaterialTheme.typography.titleMedium)
    if (vivah.coupleChecked) {
        Text(
            stringResource(R.string.muhurat_vivah_couple_checked),
            style = MaterialTheme.typography.bodySmall,
            color = graha.budha
        )
    }
    vivah.days.forEach { day ->
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(date(day.date, dayFmt), style = MaterialTheme.typography.titleMedium)
                day.windows.forEach { w ->
                    Text(
                        stringResource(R.string.muhurat_vivah_window_fmt, time(w.start), time(w.end)),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    FlowRow(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        w.nakshatras.forEach { Chip(astroTerm(it), graha.shukra) }
                        w.tithis.forEach { Chip(it.forLanguage(languageCode), graha.chandra) }
                    }
                }
            }
        }
    }
    if (vivah.blocked.isNotEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.muhurat_vivah_blocked), style = MaterialTheme.typography.titleMedium)
        vivah.blocked.forEach { b ->
            val range = if (b.start == b.end) date(b.start, shortDate)
            else "${date(b.start, shortDate)} – ${date(b.end, shortDate)}"
            Text(
                "$range: ${b.text.forLanguage(languageCode)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        stringResource(R.string.muhurat_vivah_note),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Display label for an app-defined [MUHURAT_INTENTS] key (the key itself is what the backend receives). */
@Composable
private fun intentLabel(intent: String): String = when (intent) {
    "general" -> stringResource(R.string.muhurat_intent_general)
    "marriage" -> stringResource(R.string.muhurat_intent_marriage)
    "business" -> stringResource(R.string.muhurat_intent_business)
    "travel" -> stringResource(R.string.muhurat_intent_travel)
    "griha pravesh" -> stringResource(R.string.muhurat_intent_griha_pravesh)
    else -> intent.replaceFirstChar { it.uppercase() }
}

@Composable
private fun verdictLabel(verdict: String): String = when (verdict) {
    "auspicious" -> stringResource(R.string.muhurat_verdict_auspicious)
    "neutral" -> stringResource(R.string.muhurat_verdict_neutral)
    "avoid" -> stringResource(R.string.muhurat_verdict_avoid)
    else -> verdict.replaceFirstChar { it.uppercase() }
}

package com.sangeetmind.features.astrology.numerology.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.numerology.NumerologySystemUi
import com.sangeetmind.features.astrology.numerology.NumerologyViewModel
import com.sangeetmind.libs.models.ChaldeanNumberDetail
import com.sangeetmind.libs.models.ChaldeanNumbers
import com.sangeetmind.libs.models.CoreNumbers
import com.sangeetmind.libs.models.KarmicDebtDetail
import com.sangeetmind.libs.models.LoShuGrid
import com.sangeetmind.libs.models.MoolankBhagyankCompatibility
import com.sangeetmind.libs.models.NumberDetail
import com.sangeetmind.libs.models.SangeetMindPersonalization
import com.sangeetmind.libs.models.VedicNumberDetail
import com.sangeetmind.libs.models.VedicNumbers

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NumerologyScreen(
    onNavigateBack: () -> Unit,
    onOpenGlossary: () -> Unit,
    viewModel: NumerologyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val budha = LocalGrahaColors.current.budha

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.numerology_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = onOpenGlossary) {
                        Icon(Icons.Default.MenuBook, contentDescription = stringResource(R.string.numerology_number_meanings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = budha.copy(alpha = 0.14f),
                    titleContentColor = budha,
                    navigationIconContentColor = budha,
                    actionIconContentColor = budha
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
            Text(stringResource(R.string.numerology_system_label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumerologySystemUi.entries.forEach { system ->
                    FilterChip(
                        selected = uiState.system == system,
                        onClick = { viewModel.onSystemChange(system) },
                        label = { Text(stringResource(system.labelRes)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = { Text(stringResource(R.string.numerology_field_full_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.dateOfBirth,
                onValueChange = viewModel::onDateOfBirthChange,
                label = { Text(stringResource(R.string.numerology_field_dob)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            )

            if (uiState.system == NumerologySystemUi.CHALDEAN) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.currentName,
                    onValueChange = viewModel::onCurrentNameChange,
                    label = { Text(stringResource(R.string.numerology_field_current_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done)
                )
            }

            if (uiState.system == NumerologySystemUi.VEDIC) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.numerology_gender_label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.gender == "male",
                        onClick = { viewModel.onGenderChange("male") },
                        label = { Text(stringResource(R.string.numerology_gender_male)) }
                    )
                    FilterChip(
                        selected = uiState.gender == "female",
                        onClick = { viewModel.onGenderChange("female") },
                        label = { Text(stringResource(R.string.numerology_gender_female)) }
                    )
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = viewModel::analyze,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.numerology_analyze))
                }
            }

            when (uiState.system) {
                NumerologySystemUi.PYTHAGOREAN -> uiState.pythagoreanResult?.let { result ->
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(stringResource(R.string.numerology_core_numbers_title), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    CoreNumbersCard(result.coreNumbers)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.numerology_personalization_title), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    PersonalizationCard(result.sangeetmindPersonalization)
                }
                NumerologySystemUi.CHALDEAN -> uiState.chaldeanResult?.let { result ->
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        if (result.isCurrentName) stringResource(R.string.numerology_calculated_from_current_name_fmt, result.nameUsed)
                        else stringResource(R.string.numerology_calculated_from_birth_name),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ChaldeanNumbersCard(result.numbers)

                    result.sangeetmindPersonalization?.let { personalization ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.numerology_personalization_title), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        PersonalizationCard(personalization)
                    }
                }
                NumerologySystemUi.VEDIC -> uiState.vedicResult?.let { result ->
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(stringResource(R.string.numerology_vedic_numbers_title), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    VedicNumbersCard(result.numbers)

                    Spacer(modifier = Modifier.height(16.dp))
                    LoShuGridCard(result.loShuGrid)

                    if (result.karmicDebtDetails.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        KarmicDebtsCard(result.karmicDebtDetails)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    DriverConductorCard(result.moolankBhagyankCompatibility)

                    result.sangeetmindPersonalization?.let { personalization ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.numerology_personalization_title), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        PersonalizationCard(personalization)
                    }
                }
            }
        }
    }
}

@Composable
private fun CoreNumbersCard(coreNumbers: CoreNumbers) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            NumberRow(stringResource(R.string.numerology_life_path), coreNumbers.lifePath)
            NumberRow(stringResource(R.string.numerology_destiny), coreNumbers.destiny)
            NumberRow(stringResource(R.string.numerology_soul_urge), coreNumbers.soulUrge)
            NumberRow(stringResource(R.string.numerology_personality), coreNumbers.personality)
            NumberRow(stringResource(R.string.numerology_maturity), coreNumbers.maturity)
            NumberRow(stringResource(R.string.numerology_birth_day), coreNumbers.birthDay)
            NumberRow(stringResource(R.string.numerology_attitude), coreNumbers.attitude)
        }
    }
}

@Composable
private fun NumberRow(label: String, detail: NumberDetail) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = if (detail.isMasterNumber) "${detail.number} ✦" else "${detail.number}",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ChaldeanNumbersCard(numbers: ChaldeanNumbers) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.numerology_chaldean_numbers), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ChaldeanNumberRow(stringResource(R.string.numerology_name_number), numbers.nameNumber)
            ChaldeanNumberRow(stringResource(R.string.numerology_birth_number), numbers.birthNumber)
            ChaldeanNumberRow(stringResource(R.string.numerology_destiny_number), numbers.destinyNumber)
            ChaldeanNumberRow(stringResource(R.string.numerology_soul_number), numbers.soulNumber)
            ChaldeanNumberRow(stringResource(R.string.numerology_personality_number), numbers.personalityNumber)
        }
    }
}

@Composable
private fun ChaldeanNumberRow(label: String, detail: ChaldeanNumberDetail) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${detail.number}", style = MaterialTheme.typography.bodyLarge)
        }
        detail.compoundMeaning?.let { cm ->
            Text(
                "${cm.number} · ${cm.name} — ${cm.meaning}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun VedicNumbersCard(numbers: VedicNumbers) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.numerology_vedic_numbers), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            VedicNumberRow(stringResource(R.string.numerology_moolank), numbers.moolank)
            VedicNumberRow(stringResource(R.string.numerology_bhagyank), numbers.bhagyank)
            VedicNumberRow(stringResource(R.string.numerology_namank), numbers.namank)
            VedicNumberRow(stringResource(R.string.numerology_soul_number), numbers.soulNumber)
            VedicNumberRow(stringResource(R.string.numerology_personality_number), numbers.personalityNumber)
            VedicNumberRow(stringResource(R.string.numerology_kua_number), numbers.kuaNumber)
        }
    }
}

@Composable
private fun VedicNumberRow(label: String, detail: VedicNumberDetail) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val rulingPlanet = detail.rulingPlanet
            if (!rulingPlanet.isNullOrBlank()) {
                Text(astroTerm(rulingPlanet), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text("${detail.number}", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun LoShuGridCard(grid: LoShuGrid) {
    if (grid.gridDisplay.isEmpty()) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.numerology_lo_shu_grid), style = MaterialTheme.typography.titleMedium)
            if (grid.interpretationSummary.isNotBlank()) {
                Text(
                    grid.interpretationSummary,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            grid.gridDisplay.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { cell ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cell.ifBlank { "–" }, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
            if (grid.missingNumbers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.numerology_missing_fmt, grid.missingNumbers.joinToString(", ")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (grid.repeatedNumbers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.numerology_repeated_fmt, grid.repeatedNumbers.joinToString(", ")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun KarmicDebtsCard(debts: List<KarmicDebtDetail>) {
    val graha = LocalGrahaColors.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.numerology_karmic_debts), style = MaterialTheme.typography.titleMedium)
            debts.forEach { debt ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (debt.name.isNotBlank()) stringResource(R.string.numerology_debt_named_fmt, debt.code, debt.name)
                            else stringResource(R.string.numerology_debt_fmt, debt.code),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Chip(stringResource(R.string.numerology_karmic_chip), graha.mangala)
                    }
                    if (debt.meaning.isNotBlank()) {
                        Text(
                            debt.meaning,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (debt.remedy.isNotBlank()) {
                        Text(
                            stringResource(R.string.numerology_remedy_fmt, debt.remedy),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (debt.mantra.isNotBlank()) {
                        Text(
                            stringResource(R.string.numerology_mantra_fmt, debt.mantra),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverConductorCard(compat: MoolankBhagyankCompatibility) {
    val graha = LocalGrahaColors.current
    val relationColor = when {
        compat.relationship.contains("Harmon", ignoreCase = true) -> graha.budha
        compat.relationship.contains("Challeng", ignoreCase = true) -> graha.mangala
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.numerology_driver_conductor_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.numerology_moolank_bhagyank_fmt, compat.moolank, compat.bhagyank),
                    style = MaterialTheme.typography.bodyMedium
                )
                Chip(compat.relationship, relationColor)
            }
            if (compat.description.isNotBlank()) {
                Text(
                    compat.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun Chip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun PersonalizationCard(personalization: SangeetMindPersonalization) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(personalization.personalizationSummary, style = MaterialTheme.typography.bodyMedium)
            if (personalization.raagMoods.recommended.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val translatedMoods = personalization.raagMoods.recommended.map { astroTerm(it) }
                Text(
                    stringResource(
                        R.string.numerology_recommended_raag_moods_fmt,
                        translatedMoods.joinToString(", ")
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (personalization.practiceTime.preferred.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.numerology_best_practice_time_fmt, astroTerm(personalization.practiceTime.preferred)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

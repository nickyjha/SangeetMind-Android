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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.theme.LocalGrahaColors
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
                title = { Text("Numerology") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenGlossary) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Number meanings")
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
            Text("System", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumerologySystemUi.entries.forEach { system ->
                    FilterChip(
                        selected = uiState.system == system,
                        onClick = { viewModel.onSystemChange(system) },
                        label = { Text(system.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = { Text("Full name (as given at birth)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.dateOfBirth,
                onValueChange = viewModel::onDateOfBirthChange,
                label = { Text("Date of birth (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            )

            if (uiState.system == NumerologySystemUi.CHALDEAN) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.currentName,
                    onValueChange = viewModel::onCurrentNameChange,
                    label = { Text("Current/known name (optional, if different)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done)
                )
            }

            if (uiState.system == NumerologySystemUi.VEDIC) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Gender (for Kua number)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.gender == "male",
                        onClick = { viewModel.onGenderChange("male") },
                        label = { Text("Male") }
                    )
                    FilterChip(
                        selected = uiState.gender == "female",
                        onClick = { viewModel.onGenderChange("female") },
                        label = { Text("Female") }
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
                    Text("Analyze")
                }
            }

            when (uiState.system) {
                NumerologySystemUi.PYTHAGOREAN -> uiState.pythagoreanResult?.let { result ->
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Your Core Numbers", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    CoreNumbersCard(result.coreNumbers)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("SangeetMind Personalization", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    PersonalizationCard(result.sangeetmindPersonalization)
                }
                NumerologySystemUi.CHALDEAN -> uiState.chaldeanResult?.let { result ->
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        if (result.isCurrentName) "Calculated from current name: ${result.nameUsed}"
                        else "Calculated from birth name",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ChaldeanNumbersCard(result.numbers)

                    result.sangeetmindPersonalization?.let { personalization ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("SangeetMind Personalization", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        PersonalizationCard(personalization)
                    }
                }
                NumerologySystemUi.VEDIC -> uiState.vedicResult?.let { result ->
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Your Vedic Numbers", style = MaterialTheme.typography.titleMedium)
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
                        Text("SangeetMind Personalization", style = MaterialTheme.typography.titleMedium)
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
            NumberRow("Life Path", coreNumbers.lifePath)
            NumberRow("Destiny", coreNumbers.destiny)
            NumberRow("Soul Urge", coreNumbers.soulUrge)
            NumberRow("Personality", coreNumbers.personality)
            NumberRow("Maturity", coreNumbers.maturity)
            NumberRow("Birth Day", coreNumbers.birthDay)
            NumberRow("Attitude", coreNumbers.attitude)
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
            Text("Chaldean Numbers", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ChaldeanNumberRow("Name Number", numbers.nameNumber)
            ChaldeanNumberRow("Birth Number", numbers.birthNumber)
            ChaldeanNumberRow("Destiny Number", numbers.destinyNumber)
            ChaldeanNumberRow("Soul Number", numbers.soulNumber)
            ChaldeanNumberRow("Personality Number", numbers.personalityNumber)
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
            Text("Vedic Numbers", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            VedicNumberRow("Moolank (Driver)", numbers.moolank)
            VedicNumberRow("Bhagyank (Conductor)", numbers.bhagyank)
            VedicNumberRow("Namank", numbers.namank)
            VedicNumberRow("Soul Number", numbers.soulNumber)
            VedicNumberRow("Personality Number", numbers.personalityNumber)
            VedicNumberRow("Kua Number", numbers.kuaNumber)
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
                Text(rulingPlanet, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Text("Lo Shu Grid", style = MaterialTheme.typography.titleMedium)
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
                    "Missing: ${grid.missingNumbers.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (grid.repeatedNumbers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Repeated: ${grid.repeatedNumbers.joinToString(", ")}",
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
            Text("Karmic Debts", style = MaterialTheme.typography.titleMedium)
            debts.forEach { debt ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (debt.name.isNotBlank()) "Debt ${debt.code} · ${debt.name}" else "Debt ${debt.code}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Chip("Karmic", graha.mangala)
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
                            "Remedy: ${debt.remedy}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (debt.mantra.isNotBlank()) {
                        Text(
                            "Mantra: ${debt.mantra}",
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
            Text("Driver–Conductor Compatibility", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Moolank ${compat.moolank} × Bhagyank ${compat.bhagyank}",
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
                Text(
                    "Recommended raag moods: ${personalization.raagMoods.recommended.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (personalization.practiceTime.preferred.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Best practice time: ${personalization.practiceTime.preferred}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

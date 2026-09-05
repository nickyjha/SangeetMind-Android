package com.sangeetmind.features.astrology.numerology.ui

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.features.astrology.numerology.NumerologyViewModel
import com.sangeetmind.libs.models.CoreNumbers
import com.sangeetmind.libs.models.NumberDetail
import com.sangeetmind.libs.models.SangeetMindPersonalization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumerologyScreen(
    onNavigateBack: () -> Unit,
    viewModel: NumerologyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Numerology") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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

            uiState.result?.let { result ->
                Spacer(modifier = Modifier.height(24.dp))
                Text("Your Core Numbers", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                CoreNumbersCard(result.coreNumbers)

                Spacer(modifier = Modifier.height(16.dp))
                Text("SangeetMind Personalization", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                PersonalizationCard(result.sangeetmindPersonalization)
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

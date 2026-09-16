package com.sangeetmind.features.astrology.sangeet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.features.astrology.sangeet.SangeetTab
import com.sangeetmind.features.astrology.sangeet.SangeetViewModel
import com.sangeetmind.libs.models.SoundHealingSession

private val ZODIAC_SIGNS = listOf(
    "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
    "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SangeetScreen(
    onNavigateBack: () -> Unit,
    viewModel: SangeetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sangeet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = uiState.tab.ordinal) {
                Tab(
                    selected = uiState.tab == SangeetTab.RAAG,
                    onClick = { viewModel.setTab(SangeetTab.RAAG) },
                    text = { Text("Daily Raag") }
                )
                Tab(
                    selected = uiState.tab == SangeetTab.JAPA,
                    onClick = { viewModel.setTab(SangeetTab.JAPA) },
                    text = { Text("Japa") }
                )
                Tab(
                    selected = uiState.tab == SangeetTab.VOICE_HOROSCOPE,
                    onClick = { viewModel.setTab(SangeetTab.VOICE_HOROSCOPE) },
                    text = { Text("Voice Horoscope") }
                )
                Tab(
                    selected = uiState.tab == SangeetTab.SOUND_HEALING,
                    onClick = { viewModel.setTab(SangeetTab.SOUND_HEALING) },
                    text = { Text("Sound Healing") }
                )
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

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                when (uiState.tab) {
                    SangeetTab.RAAG -> DailyRaagTab(uiState.dailyRaag)
                    SangeetTab.JAPA -> JapaTab(
                        count = uiState.japaCount,
                        logged = uiState.japaLogged,
                        totalJapa = uiState.japaStats?.totalJapa ?: 0,
                        streakDays = uiState.japaStats?.streakDays ?: 0,
                        onTap = viewModel::incrementJapa,
                        onReset = viewModel::resetJapaCount,
                        onLog = viewModel::logJapaSession
                    )
                    SangeetTab.VOICE_HOROSCOPE -> VoiceHoroscopeTab(
                        script = uiState.voiceHoroscope?.script,
                        onGenerate = viewModel::getVoiceHoroscope
                    )
                    SangeetTab.SOUND_HEALING -> SoundHealingTab(
                        sessions = uiState.soundHealingSessions,
                        premiumRequired = uiState.soundHealingPremiumRequired
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyRaagTab(playlist: com.sangeetmind.libs.models.RaagPlaylist?) {
    if (playlist == null) return
    Text("Personalized for today", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        "${playlist.tradition} • ${playlist.timeOfDay} • dasha: ${playlist.mahadashaLord}" +
            (playlist.antardashaLord?.let { " / $it" } ?: ""),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))
    if (playlist.previewOnly) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Text("Preview only — unlock the full playlist with Premium.", modifier = Modifier.padding(12.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
    playlist.tracks.forEach { track ->
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(track.raag, style = MaterialTheme.typography.titleMedium)
                Text(track.purpose, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun JapaTab(
    count: Int,
    logged: Boolean,
    totalJapa: Int,
    streakDays: Int,
    onTap: () -> Unit,
    onReset: () -> Unit,
    onLog: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("Streak: $streakDays days • Lifetime japa: $totalJapa", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Text("$count", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onTap, modifier = Modifier.size(140.dp), shape = MaterialTheme.shapes.extraLarge) {
            Text("Tap")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onReset, enabled = count > 0) { Text("Reset") }
            Button(onClick = onLog, enabled = count > 0) { Text("Log session") }
        }
        if (logged) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Session logged", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoiceHoroscopeTab(script: String?, onGenerate: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedSign by remember { mutableStateOf(ZODIAC_SIGNS.first()) }

    Text(
        "Premium — a spoken daily horoscope script for your sign.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedSign,
            onValueChange = {},
            readOnly = true,
            label = { Text("Sign") },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ZODIAC_SIGNS.forEach { sign ->
                DropdownMenuItem(text = { Text(sign) }, onClick = {
                    selectedSign = sign
                    expanded = false
                })
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = { onGenerate(selectedSign) }, modifier = Modifier.fillMaxWidth()) {
        Text("Generate voice horoscope")
    }
    if (script != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(script, modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
private fun SoundHealingTab(sessions: List<SoundHealingSession>, premiumRequired: Boolean) {
    if (premiumRequired) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Text(
                "Showing a preview session. Unlock the full sound healing library with Premium.",
                modifier = Modifier.padding(12.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
    sessions.forEach { session ->
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(session.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${session.durationMin} min • ${session.theme}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

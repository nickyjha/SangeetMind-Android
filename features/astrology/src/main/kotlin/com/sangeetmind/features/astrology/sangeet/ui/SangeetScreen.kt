package com.sangeetmind.features.astrology.sangeet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.sangeet.SangeetTab
import com.sangeetmind.features.astrology.sangeet.SangeetViewModel
import com.sangeetmind.libs.models.MantraTally
import com.sangeetmind.libs.models.SoundHealingSession

private val ZODIAC_SIGNS = listOf(
    "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
    "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
)

/** Every raag/sound-healing item ties back to a graha (planet) by name — from the
 * playlist's own dasha lord, or a sound-healing session's `theme` field (already Hindi
 * graha names like "Shani"/"Mangal"). Reuses the same Navagraha palette as the rest of
 * the app so Sangeet stops being the one screen still in default Material3. Ketu has no
 * defined graha tone in this app (see Color.kt) so it falls back like any unknown name. */
@Composable
private fun grahaToneFor(planetName: String): Color {
    val local = LocalGrahaColors.current
    return when (planetName.trim().lowercase()) {
        "sun", "surya" -> local.surya
        "moon", "chandra" -> local.chandra
        "mars", "mangal", "mangala" -> local.mangala
        "mercury", "budh", "budha" -> local.budha
        "jupiter", "guru" -> local.guru
        "venus", "shukra" -> local.shukra
        "saturn", "shani" -> local.shani
        "rahu" -> local.rahu
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SangeetScreen(
    onNavigateBack: () -> Unit,
    viewModel: SangeetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chandra = LocalGrahaColors.current.chandra

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sangeet_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = chandra.copy(alpha = 0.14f),
                    titleContentColor = chandra,
                    navigationIconContentColor = chandra
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = uiState.tab.ordinal) {
                Tab(
                    selected = uiState.tab == SangeetTab.RAAG,
                    onClick = { viewModel.setTab(SangeetTab.RAAG) },
                    text = { Text(stringResource(R.string.sangeet_tab_daily_raag)) }
                )
                Tab(
                    selected = uiState.tab == SangeetTab.JAPA,
                    onClick = { viewModel.setTab(SangeetTab.JAPA) },
                    text = { Text(stringResource(R.string.sangeet_tab_japa)) }
                )
                Tab(
                    selected = uiState.tab == SangeetTab.VOICE_HOROSCOPE,
                    onClick = { viewModel.setTab(SangeetTab.VOICE_HOROSCOPE) },
                    text = { Text(stringResource(R.string.sangeet_tab_voice_horoscope)) }
                )
                Tab(
                    selected = uiState.tab == SangeetTab.SOUND_HEALING,
                    onClick = { viewModel.setTab(SangeetTab.SOUND_HEALING) },
                    text = { Text(stringResource(R.string.sangeet_tab_sound_healing)) }
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
                        byMantra = uiState.japaStats?.byMantra ?: emptyList(),
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
    val tone = grahaToneFor(playlist.mahadashaLord)
    Text(stringResource(R.string.sangeet_raag_personalized), style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(4.dp))
    val mahadasha = astroTerm(playlist.mahadashaLord)
    val dashaLabel = playlist.antardashaLord
        ?.let { stringResource(R.string.sangeet_raag_dasha_pair_fmt, mahadasha, astroTerm(it)) }
        ?: mahadasha
    Text(
        stringResource(R.string.sangeet_raag_meta_fmt, playlist.tradition, playlist.timeOfDay, dashaLabel),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))
    if (playlist.previewOnly) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Text(stringResource(R.string.sangeet_raag_preview_only), modifier = Modifier.padding(12.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
    playlist.tracks.forEach { track ->
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(tone.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = tone, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(track.raag, style = MaterialTheme.typography.titleMedium)
                    Text(track.purpose, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
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
    byMantra: List<MantraTally>,
    onTap: () -> Unit,
    onReset: () -> Unit,
    onLog: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.sangeet_japa_stats_fmt, streakDays, totalJapa), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Text("$count", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onTap, modifier = Modifier.size(140.dp), shape = MaterialTheme.shapes.extraLarge) {
            Text(stringResource(R.string.sangeet_japa_tap))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onReset, enabled = count > 0) { Text(stringResource(R.string.sangeet_japa_reset)) }
            Button(onClick = onLog, enabled = count > 0) { Text(stringResource(R.string.sangeet_japa_log_session)) }
        }
        if (logged) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(stringResource(R.string.sangeet_japa_session_logged), color = MaterialTheme.colorScheme.primary)
        }

        if (byMantra.isNotEmpty()) {
            Spacer(modifier = Modifier.height(28.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.sangeet_japa_by_mantra), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    byMantra.forEach { tally ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(tally.mantraId, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    if (tally.sessions == 1) stringResource(R.string.sangeet_japa_session_one)
                                    else stringResource(R.string.sangeet_japa_sessions_fmt, tally.sessions),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text("${tally.total}", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoiceHoroscopeTab(script: String?, onGenerate: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedSign by remember { mutableStateOf(ZODIAC_SIGNS.first()) }

    Text(
        stringResource(R.string.sangeet_voice_premium_desc),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = astroTerm(selectedSign),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.sangeet_voice_sign_label)) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ZODIAC_SIGNS.forEach { sign ->
                DropdownMenuItem(text = { Text(astroTerm(sign)) }, onClick = {
                    selectedSign = sign
                    expanded = false
                })
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = { onGenerate(selectedSign) }, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.sangeet_voice_generate))
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
                stringResource(R.string.sangeet_sound_preview),
                modifier = Modifier.padding(12.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
    sessions.forEach { session ->
        val tone = grahaToneFor(session.theme)
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(tone.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = tone, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(session.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.sangeet_sound_session_meta_fmt, session.durationMin, astroTerm(session.theme)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

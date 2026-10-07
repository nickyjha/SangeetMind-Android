package com.sangeetmind.features.astrology.sangeet.ui

import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import com.sangeetmind.features.astrology.sangeet.ChantState
import com.sangeetmind.features.astrology.sangeet.MantraAudio
import com.sangeetmind.core.ui.components.AstroTopBar
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.sangeet.JapaMantras
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

/** Raag (lowercased) -> its traditional time + mood. The backend's per-track `purpose`
 * is the same "Balance <lord> dasha" placeholder for every row, so show this instead. */
private val RAAG_MOODS: Map<String, Int> = mapOf(
    "bhairavi" to R.string.sangeet_raag_mood_bhairavi,
    "ramkali" to R.string.sangeet_raag_mood_ramkali,
    "yaman" to R.string.sangeet_raag_mood_yaman,
    "darbari" to R.string.sangeet_raag_mood_darbari,
    "darbari kanada" to R.string.sangeet_raag_mood_darbari_kanada,
    "bhairav" to R.string.sangeet_raag_mood_bhairav,
    "hindol" to R.string.sangeet_raag_mood_hindol,
    "desh" to R.string.sangeet_raag_mood_desh,
    "bageshri" to R.string.sangeet_raag_mood_bageshri,
    "hansadhwani" to R.string.sangeet_raag_mood_hansadhwani,
    "bihag" to R.string.sangeet_raag_mood_bihag,
    "miyan ki malhar" to R.string.sangeet_raag_mood_miyan_ki_malhar,
    "charukeshi" to R.string.sangeet_raag_mood_charukeshi,
    "malkauns" to R.string.sangeet_raag_mood_malkauns,
    "todi" to R.string.sangeet_raag_mood_todi,
    "marwa" to R.string.sangeet_raag_mood_marwa,
    "multani" to R.string.sangeet_raag_mood_multani,
    "puriya dhanashri" to R.string.sangeet_raag_mood_puriya_dhanashri,
    "shanmukhapriya" to R.string.sangeet_raag_mood_shanmukhapriya,
    "natabhairavi" to R.string.sangeet_raag_mood_natabhairavi,
    "kalyani" to R.string.sangeet_raag_mood_kalyani,
    "hamsadhwani" to R.string.sangeet_raag_mood_hamsadhwani,
    "mohanam" to R.string.sangeet_raag_mood_mohanam,
    "abheri" to R.string.sangeet_raag_mood_abheri,
    "hindolam" to R.string.sangeet_raag_mood_hindolam,
    "suddha saveri" to R.string.sangeet_raag_mood_suddha_saveri,
    "shankarabharanam" to R.string.sangeet_raag_mood_shankarabharanam,
    "kambhoji" to R.string.sangeet_raag_mood_kambhoji
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SangeetScreen(
    onNavigateBack: () -> Unit,
    initialMantraId: String? = null,
    viewModel: SangeetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(initialMantraId) {
        if (initialMantraId != null) viewModel.openJapa(initialMantraId)
    }
    val chandra = LocalGrahaColors.current.chandra

    Scaffold(
        topBar = {
            AstroTopBar(
                title = stringResource(R.string.sangeet_title),
                onBack = onNavigateBack,
                accent = chandra
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Fixed (not scrollable) so all four tabs are always visible; labels may wrap
            // to two lines instead of "Sound Healing" being cut off at the screen edge.
            TabRow(selectedTabIndex = uiState.tab.ordinal) {
                Tab(
                    selected = uiState.tab == SangeetTab.RAAG,
                    onClick = { viewModel.setTab(SangeetTab.RAAG) },
                    text = { Text(stringResource(R.string.sangeet_tab_daily_raag), maxLines = 2, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium) }
                )
                Tab(
                    selected = uiState.tab == SangeetTab.JAPA,
                    onClick = { viewModel.setTab(SangeetTab.JAPA) },
                    text = { Text(stringResource(R.string.sangeet_tab_japa), maxLines = 2, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium) }
                )
                Tab(
                    selected = uiState.tab == SangeetTab.VOICE_HOROSCOPE,
                    onClick = { viewModel.setTab(SangeetTab.VOICE_HOROSCOPE) },
                    text = { Text(stringResource(R.string.sangeet_tab_voice_horoscope), maxLines = 2, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium) }
                )
                Tab(
                    selected = uiState.tab == SangeetTab.SOUND_HEALING,
                    onClick = { viewModel.setTab(SangeetTab.SOUND_HEALING) },
                    text = { Text(stringResource(R.string.sangeet_tab_sound_healing), maxLines = 2, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium) }
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
                        selectedMantraId = uiState.selectedMantraId,
                        recommendedMantraId = uiState.recommendedMantraId,
                        recommendedForLord = uiState.recommendedForLord,
                        supportMantraIds = uiState.supportMantraIds,
                        onSelectMantra = viewModel::selectMantra,
                        onTap = viewModel::incrementJapa,
                        onReset = viewModel::resetJapaCount,
                        onLog = viewModel::logJapaSession,
                        hasAudio = MantraAudio.forMantra(uiState.selectedMantraId) != null,
                        chantState = uiState.chantState,
                        malaCompleted = uiState.malaCompleted,
                        onPlayChant = viewModel::playChant,
                        onPauseChant = viewModel::pauseChant,
                        onStopChant = viewModel::stopChant
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
        stringResource(R.string.sangeet_raag_meta_fmt, astroTerm(playlist.tradition), astroTerm(playlist.timeOfDay), dashaLabel),
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
                    Text(astroTerm(track.raag), style = MaterialTheme.typography.titleMedium)
                    // Traditional time + mood per raag; the generic dasha line (already in
                    // the header) is only a fallback for a raag we don't know.
                    val moodRes = RAAG_MOODS[track.raag.trim().lowercase()]
                    Text(
                        if (moodRes != null) stringResource(moodRes)
                        else stringResource(R.string.sangeet_raag_track_purpose_fmt, mahadasha),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
    selectedMantraId: String,
    recommendedMantraId: String?,
    recommendedForLord: String?,
    supportMantraIds: Set<String> = emptySet(),
    onSelectMantra: (String) -> Unit,
    onTap: () -> Unit,
    onReset: () -> Unit,
    onLog: () -> Unit,
    hasAudio: Boolean = false,
    chantState: ChantState = ChantState.IDLE,
    malaCompleted: Int? = null,
    onPlayChant: () -> Unit = {},
    onPauseChant: () -> Unit = {},
    onStopChant: () -> Unit = {}
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        MantraPicker(
            selectedMantraId = selectedMantraId,
            recommendedMantraId = recommendedMantraId,
            recommendedForLord = recommendedForLord,
            supportMantraIds = supportMantraIds,
            onSelect = onSelectMantra
        )
        if (hasAudio) {
            Spacer(modifier = Modifier.height(12.dp))
            ChantAlongCard(
                count = count,
                chantState = chantState,
                malaCompleted = malaCompleted,
                onPlay = onPlayChant,
                onPause = onPauseChant,
                onStop = onStopChant
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
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
                                Text(mantraDisplayName(tally.mantraId), style = MaterialTheme.typography.bodyMedium)
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

/**
 * Dropdown of [JapaMantras.all]. Personal support mantras are tagged "Recommended for you";
 * the beej mantra for the running dasha keeps its own tag otherwise.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MantraPicker(
    selectedMantraId: String,
    recommendedMantraId: String?,
    recommendedForLord: String?,
    supportMantraIds: Set<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedIsSupport = selectedMantraId in supportMantraIds
    val selectedIsRecommended = recommendedMantraId != null && selectedMantraId == recommendedMantraId
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = mantraDisplayName(selectedMantraId),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.sangeet_japa_choose_mantra)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            supportingText = if (selectedIsSupport) {
                {
                    Text(
                        stringResource(R.string.sangeet_japa_recommended_for_you),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (selectedIsRecommended && recommendedForLord != null) {
                {
                    Text(
                        stringResource(R.string.sangeet_japa_recommended_fmt, astroTerm(recommendedForLord)),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                null
            },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            JapaMantras.all.forEach { mantra ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                stringResource(mantra.nameRes) +
                                    if (MantraAudio.forMantra(mantra.id) != null) "  ♪" else ""
                            )
                            val tagRes = when {
                                mantra.id in supportMantraIds -> R.string.sangeet_japa_recommended_for_you
                                mantra.id == recommendedMantraId -> R.string.sangeet_japa_recommended
                                else -> null
                            }
                            if (tagRes != null) {
                                Text(
                                    stringResource(tagRes),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    onClick = {
                        onSelect(mantra.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** Server mantra ids are slugs ("generic", "shani_beej") — show the readable name;
 * an id this app version doesn't know falls back to title case. */
@Composable
private fun mantraDisplayName(mantraId: String): String {
    val known = JapaMantras.byId(mantraId)
    return if (known != null) stringResource(known.nameRes) else astroTerm(JapaMantras.titleCase(mantraId))
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

/** Plays the recorded chant on a loop; the mala advances by itself up to 108. */
@Composable
private fun ChantAlongCard(
    count: Int,
    chantState: ChantState,
    malaCompleted: Int?,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit
) {
    val target = MantraAudio.MALA
    // A full mala runs 15-30 minutes: keep the screen awake while the chant plays.
    val view = LocalView.current
    DisposableEffect(chantState) {
        view.keepScreenOn = chantState == ChantState.PLAYING
        onDispose { view.keepScreenOn = false }
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.sangeet_japa_audio_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(R.string.sangeet_japa_audio_desc, target),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = (count.coerceAtMost(target)) / target.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                stringResource(R.string.sangeet_japa_audio_progress_fmt, count.coerceAtMost(target), target),
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                when (chantState) {
                    ChantState.PLAYING -> Button(onClick = onPause) {
                        Icon(Icons.Default.Pause, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.sangeet_japa_audio_pause))
                    }
                    ChantState.PAUSED -> Button(onClick = onPlay) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.sangeet_japa_audio_resume))
                    }
                    ChantState.IDLE -> Button(onClick = onPlay) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.sangeet_japa_audio_play))
                    }
                }
                if (chantState != ChantState.IDLE) {
                    OutlinedButton(onClick = onStop) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.sangeet_japa_audio_stop))
                    }
                }
            }
            if (malaCompleted != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    stringResource(R.string.sangeet_japa_mala_complete, malaCompleted),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


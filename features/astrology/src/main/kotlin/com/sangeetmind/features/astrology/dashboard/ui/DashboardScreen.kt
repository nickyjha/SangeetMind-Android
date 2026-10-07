package com.sangeetmind.features.astrology.dashboard.ui

import com.sangeetmind.features.astrology.sangeet.MantraAudio
import com.sangeetmind.features.astrology.sangeet.JapaMantras
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.WindowInsets
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.common.language.findActivity
import com.sangeetmind.core.network.SangeetMindMessagingService
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.components.ErrorCard
import com.sangeetmind.core.ui.language.LanguagePickerAction
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.dashboard.DashboardViewModel
import com.sangeetmind.features.astrology.dashboard.NotificationNudge
import com.sangeetmind.features.astrology.home.TabRootTopBar
import com.sangeetmind.features.astrology.home.themedTone
import com.sangeetmind.core.ui.components.DateFieldFormat
import com.sangeetmind.core.ui.language.LocalAppLanguage
import com.sangeetmind.features.astrology.dashboard.PeriodCountdown
import com.sangeetmind.features.astrology.dashboard.RelativeSpan
import com.sangeetmind.libs.models.CurrentPeriod
import com.sangeetmind.libs.models.SupportMantra
import com.sangeetmind.features.astrology.sangeet.SupportMantras
import com.sangeetmind.libs.models.DailyHoroscope
import com.sangeetmind.libs.models.PanchangResponse
import com.sangeetmind.libs.models.toTitleCase

/** Which graha (if any) a destination is tinted with — see home/MainTabs.kt themedTone(). */
enum class Graha { SURYA, CHANDRA, MANGALA, BUDHA, GURU, SHUKRA, SHANI, RAHU }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenKundliList: () -> Unit,
    onOpenKundliOnboarding: () -> Unit,
    onOpenHoroscope: () -> Unit,
    onOpenChart: () -> Unit,
    /** The Vimshottari view (chart screen, Dasha section). */
    onOpenDasha: () -> Unit = onOpenChart,
    onOpenReadings: () -> Unit,
    onOpenMatch: () -> Unit,
    onOpenPanchang: () -> Unit,
    onOpenChatMind: () -> Unit,
    onOpenGeet: (String?) -> Unit = {},
    onAskChatMind: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Daily push: ask for POST_NOTIFICATIONS once, after the first successful load (never on
    // the login screen), then register the device so the server knows the timezone/hour.
    val context = LocalContext.current
    val canAskAgain = {
        context.findActivity()?.let {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.POST_NOTIFICATIONS)
        } ?: false
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.onNotificationsAllowed() else viewModel.onNotificationsDenied(canAskAgain())
    }
    val requestPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            viewModel.onSystemNotificationDialogShown()
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val loaded = uiState.profile != null
    LaunchedEffect(loaded) {
        if (!loaded) return@LaunchedEffect
        when {
            SangeetMindMessagingService.notificationsAllowed(context) -> viewModel.onNotificationsAllowed()
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> viewModel.onNotificationsDenied(canAskAgain = false)
            viewModel.shouldShowSystemNotificationDialog() -> requestPermission()
            else -> viewModel.onNotificationsDenied(canAskAgain())
        }
    }

    // Resolved here (composable scope) rather than inside the LazyColumn builder, which
    // is a LazyListScope lambda where stringResource() can't be called.
    val quickActions = listOf(
        QuickAction(stringResource(R.string.dashboard_home_quick_kundli), Icons.Default.DonutLarge, Graha.SHANI, onOpenChart),
        QuickAction(stringResource(R.string.dashboard_home_quick_readings), Icons.Default.Psychology, Graha.SHUKRA, onOpenReadings),
        QuickAction(stringResource(R.string.dashboard_home_quick_match), Icons.Default.Favorite, Graha.MANGALA, onOpenMatch),
        QuickAction(stringResource(R.string.dashboard_home_quick_geet), Icons.Default.MusicNote, Graha.SURYA) { onOpenGeet(null) }
    )

    Scaffold(
        // Tab roots sit above the bottom bar, which already covers the system nav bar.
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            // Settings moved to the Me tab; the quick kundli switcher stays here.
            TabRootTopBar(
                title = stringResource(CoreR.string.common_app_name),
                actions = {
                    LanguagePickerAction()
                    IconButton(onClick = onOpenKundliList) {
                        Icon(Icons.Default.People, contentDescription = stringResource(R.string.dashboard_my_kundlis))
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                when {
                    uiState.isLoading -> {
                        Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.profile != null && uiState.primaryKundli != null -> {
                        Column {
                            if (uiState.notificationNudge != NotificationNudge.NONE) {
                                NotificationNudgeCard(
                                    nudge = uiState.notificationNudge,
                                    onTurnOn = requestPermission,
                                    onOpenSettings = {
                                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        } else {
                                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                                        }
                                        runCatching { context.startActivity(intent) }
                                    },
                                    onDismiss = viewModel::dismissNotificationNudge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            ProfileSummaryCard(
                                name = uiState.primaryKundli!!.fullName?.toTitleCase(),
                                moonSign = uiState.profile!!.moonSign,
                                lagna = uiState.profile!!.lagna,
                                nakshatra = uiState.profile!!.nakshatra,
                                currentMahadasha = uiState.profile!!.currentMahadasha,
                                astroMood = uiState.profile!!.astroMood
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            GeetCard(
                                dashaLord = uiState.profile!!.currentMahadasha,
                                supportMantras = uiState.profile!!.supportMantras,
                                onChant = onOpenGeet
                            )
                            // Hidden until the backend sends current_period (older deploys don't).
                            uiState.profile!!.currentPeriod?.takeIf { it.mahadasha != null }?.let { period ->
                                Spacer(modifier = Modifier.height(16.dp))
                                CurrentPeriodCard(period, onClick = onOpenDasha)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            TodayCard(
                                horoscope = uiState.todayHoroscope,
                                panchang = uiState.todayPanchang,
                                onOpenHoroscope = onOpenHoroscope
                            )
                            uiState.dailyScores?.let {
                                Spacer(modifier = Modifier.height(16.dp))
                                DailyScoresCard(it, onAsk = onAskChatMind)
                                it.days.firstOrNull { d -> d.label == "today" }?.action?.let { action ->
                                    Spacer(modifier = Modifier.height(16.dp))
                                    ActionCard(
                                        action = action,
                                        done = uiState.actionDoneToday,
                                        streak = uiState.actionStreak,
                                        onDone = viewModel::markActionDone
                                    )
                                }
                            }
                        }
                    }
                    uiState.hasNoKundlis -> {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(stringResource(R.string.dashboard_create_kundli_title), style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.dashboard_create_kundli_body),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = onOpenKundliOnboarding) {
                                    Text(stringResource(R.string.dashboard_get_started))
                                }
                            }
                        }
                    }
                    uiState.error != null -> {
                        ErrorCard(message = uiState.error, onRetry = viewModel::refresh)
                    }
                }
            }

            if (!uiState.hasNoKundlis) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(stringResource(R.string.dashboard_home_quick_actions), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(10.dp))
                    QuickActionsRow(quickActions)
                    Spacer(modifier = Modifier.height(16.dp))
                    AskPromptCard(onClick = onOpenChatMind)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileSummaryCard(
    name: String?,
    moonSign: String,
    lagna: String,
    nakshatra: String,
    currentMahadasha: String,
    astroMood: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                text = name?.takeIf { it.isNotBlank() } ?: stringResource(R.string.dashboard_your_kundli),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            SummaryRow(stringResource(R.string.dashboard_moon_sign), astroTerm(moonSign))
            SummaryRow(stringResource(R.string.dashboard_lagna), astroTerm(lagna))
            if (nakshatra.isNotBlank()) SummaryRow(stringResource(R.string.dashboard_nakshatra), astroTerm(nakshatra), highlight = true)
            if (currentMahadasha.isNotBlank()) SummaryRow(stringResource(R.string.dashboard_current_dasha), astroTerm(currentMahadasha))
            if (astroMood.isNotBlank()) SummaryRow(stringResource(R.string.dashboard_todays_mood), astroTerm(astroMood))
        }
    }
}

/** Best-effort English color name → swatch, for the LLM's free-text `lucky_color`
 * (see HoroscopeScreen.kt's identical map — kept local here to avoid a cross-package
 * export for 15 color constants). */
private val TODAY_COLOR_MAP: Map<String, Color> = mapOf(
    "red" to Color(0xFFD64545),
    "yellow" to Color(0xFFE0B23C),
    "green" to Color(0xFF3FBF8F),
    "white" to Color(0xFFE8E6F5),
    "orange" to Color(0xFFE08A3C),
    "blue" to Color(0xFF5B7FE0),
    "pink" to Color(0xFFE07FA8),
    "purple" to Color(0xFF9B6FE0),
    "violet" to Color(0xFF9B6FE0),
    "gold" to Color(0xFFD4AF37),
    "silver" to Color(0xFFB8B8C4),
    "brown" to Color(0xFF9E6B4A),
    "black" to Color(0xFF3A3550),
    "maroon" to Color(0xFF8B3A4A),
    "cream" to Color(0xFFE8DCC0)
)

private val COLOR_ALIASES: Map<String, String> = mapOf(
    "saffron" to "orange", "grey" to "silver", "gray" to "silver", "golden" to "gold",
    "navy" to "blue", "indigo" to "purple", "lavender" to "purple", "magenta" to "pink"
)

/**
 * Free-text colour ("Light Blue", "Saffron", "dark green") -> swatch, matched by the
 * colour word it contains (the last one wins: in "sky blue" the noun comes last).
 * Returns null when no known colour word appears, so the caller can hide the dot.
 */
internal fun luckySwatch(name: String): Color? {
    val words = name.lowercase().split(Regex("[^a-z]+")).filter { it.isNotEmpty() }
    val key = words.asReversed().firstNotNullOfOrNull { w ->
        when {
            TODAY_COLOR_MAP.containsKey(w) -> w
            else -> COLOR_ALIASES[w]
        }
    }
    return key?.let { TODAY_COLOR_MAP[it] }
}

/** The dashboard's daily-engagement hub: mood (from ProfileSummaryCard above), lucky
 * color/auspicious window (from the daily horoscope's LLM enhancement), and a Panchang
 * snapshot — all auto-loaded with the profile, no extra tap needed. Tapping through opens
 * the full Horoscope screen for the mantra/daan/what-to-avoid detail. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayCard(
    horoscope: DailyHoroscope?,
    panchang: PanchangResponse?,
    onOpenHoroscope: () -> Unit
) {
    val surya = LocalGrahaColors.current.surya
    val enhanced = horoscope?.enhanced
    val luckyColor = enhanced?.luckyColor?.takeIf { it.isNotBlank() }
    val interpretation = enhanced?.interpretation?.takeIf { it.isNotBlank() }
        ?: horoscope?.theme?.longText?.takeIf { it.isNotBlank() }

    // No early return mid-composition (crashed the app before) — just skip the card.
    if (horoscope != null || panchang != null) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onOpenHoroscope) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WbSunny, contentDescription = null, tint = surya, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(CoreR.string.common_today), style = MaterialTheme.typography.titleMedium)
            }

            if (interpretation != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    interpretation,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (luckyColor != null || panchang != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    if (luckyColor != null) {
                        val swatch = luckySwatch(luckyColor)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Unknown colour name -> no dot (better than a wrong one).
                            if (swatch != null) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(swatch)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Column {
                                Text(stringResource(R.string.dashboard_lucky_color), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(luckyColor, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    panchang?.let { p ->
                        Column {
                            Text(stringResource(R.string.dashboard_panchang), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${astroTerm(p.tithi.name)} · ${astroTerm(p.nakshatra.name)}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                stringResource(R.string.dashboard_view_full_guidance),
                style = MaterialTheme.typography.labelLarge,
                color = surya
            )
        }
    }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

private data class QuickAction(val label: String, val icon: ImageVector, val graha: Graha, val onClick: () -> Unit)

/** Four compact shortcuts (tinted circle + label) instead of the old 19-tile grid;
 * everything else lives in the Kundli / Calendar / Me tabs. */
@Composable
private fun QuickActionsRow(actions: List<QuickAction>) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        actions.forEach { action ->
            val tone = action.graha.themedTone()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(onClick = action.onClick)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(tone.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(action.icon, contentDescription = null, tint = tone, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    action.label,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Nudge toward the core AI feature (also the raised centre tab). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AskPromptCard(onClick: () -> Unit) {
    val rahu = LocalGrahaColors.current.rahu
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(rahu.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.dashboard_home_ask_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    stringResource(R.string.dashboard_home_ask_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Short rationale shown after the notification permission was denied. [NotificationNudge.ASK_AGAIN]
 * re-opens the system dialog; [NotificationNudge.OPEN_SETTINGS] deep-links to the app's
 * notification settings (the system won't show the dialog again).
 */
@Composable
private fun NotificationNudgeCard(
    nudge: NotificationNudge,
    onTurnOn: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.dashboard_notif_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.dashboard_notif_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dashboard_notif_not_now))
                }
                Spacer(modifier = Modifier.width(4.dp))
                if (nudge == NotificationNudge.OPEN_SETTINGS) {
                    Button(onClick = onOpenSettings) {
                        Text(stringResource(R.string.dashboard_notif_open_settings))
                    }
                } else {
                    Button(onClick = onTurnOn) {
                        Text(stringResource(R.string.dashboard_notif_turn_on))
                    }
                }
            }
        }
    }
}

/**
 * Geet up front. With the backend's support mantras, today's pick rotates daily through
 * them, with a gentle reason line and a chip row of the others. Without them (older
 * backend), the beej mantra for the running mahadasha; Gayatri when the lord is unknown.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GeetCard(
    dashaLord: String,
    supportMantras: List<SupportMantra>,
    onChant: (String?) -> Unit
) {
    val usable = remember(supportMantras) { SupportMantras.usable(supportMantras) }
    val dayOfYear = remember { java.time.LocalDate.now().dayOfYear }
    val todays = SupportMantras.forDay(usable, dayOfYear)
    val beej = JapaMantras.beejForLord(dashaLord)
    val mantra = todays?.let { JapaMantras.byId(it.mantraId) } ?: beej ?: JapaMantras.byId("gayatri")
    val hasAudio = mantra != null && MantraAudio.forMantra(mantra.id) != null
    val accent = LocalGrahaColors.current.surya
    val locale = LocalAppLanguage.current.locale

    val reasonRes = todays?.let { SupportMantras.reasonRes(it.reason) }
    val reasonLine: String? = when {
        todays == null || reasonRes == null -> null
        todays.reason == SupportMantra.REASON_UPCOMING -> {
            val from = todays.periodLordFrom?.let { DateFieldFormat.parseIso(it) }
            if (from != null) {
                stringResource(
                    reasonRes,
                    astroTerm(todays.planet),
                    DateFieldFormat.display(DateFieldFormat.toIso(from), locale)
                )
            } else {
                stringResource(R.string.dashboard_geet_reason_running, astroTerm(todays.planet))
            }
        }
        todays.reason == SupportMantra.REASON_PROTECTION -> stringResource(reasonRes)
        else -> stringResource(reasonRes, astroTerm(todays.planet))
    }
    val others = usable.filter { it.mantraId != todays?.mantraId }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = accent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.dashboard_geet_title), style = MaterialTheme.typography.titleMedium)
            }
            if (todays == null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    if (beej != null) stringResource(R.string.dashboard_geet_for_dasha_fmt, astroTerm(dashaLord))
                    else stringResource(R.string.dashboard_geet_general),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (mantra != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(mantra.nameRes), style = MaterialTheme.typography.titleMedium)
            }
            if (reasonLine != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    reasonLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(R.string.dashboard_geet_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(onClick = { onChant(mantra?.id) }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(
                        if (hasAudio) R.string.dashboard_geet_chant else R.string.dashboard_geet_open
                    )
                )
            }
            if (others.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(R.string.dashboard_geet_support_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    others.forEach { item ->
                        val label = astroTerm(item.planet) +
                            if (MantraAudio.forMantra(item.mantraId) != null) " ♪" else ""
                        AssistChip(
                            onClick = { onChant(item.mantraId) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun relativeText(span: RelativeSpan): String = when (span) {
    RelativeSpan.Today -> stringResource(R.string.dashboard_period_today)
    RelativeSpan.Tomorrow -> stringResource(R.string.dashboard_period_tomorrow)
    is RelativeSpan.Days -> stringResource(R.string.dashboard_period_in_days, span.count.toInt())
    is RelativeSpan.Weeks -> stringResource(R.string.dashboard_period_in_weeks, span.count.toInt())
    is RelativeSpan.Months -> stringResource(R.string.dashboard_period_in_months, span.count.toInt())
    is RelativeSpan.Years -> stringResource(R.string.dashboard_period_in_years, span.count.toInt())
}

/**
 * "Your current period": maha › antar › pratyantar, a countdown to the next sub-period
 * change and to the next mahadasha. Tapping opens the Vimshottari view.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrentPeriodCard(period: CurrentPeriod, onClick: () -> Unit) {
    val locale = LocalAppLanguage.current.locale
    val today = remember { java.time.LocalDate.now() }
    val accent = LocalGrahaColors.current.shani
    val chain = listOfNotNull(period.mahadasha, period.antardasha, period.pratyantardasha)
        .map { astroTerm(it.lord) }
        .joinToString(stringResource(R.string.dashboard_period_chain_sep))

    val change = period.nextChange
    val changeDate = PeriodCountdown.localDate(change?.at)
    val changeLine = if (change != null && changeDate != null) {
        val endingLord = when (change.level) {
            "pratyantardasha" -> period.pratyantardasha?.lord
            "antardasha" -> period.antardasha?.lord
            else -> null
        }
        val res = when (change.level) {
            "pratyantardasha" -> R.string.dashboard_period_praty_ends
            "antardasha" -> R.string.dashboard_period_antar_ends
            else -> null
        }
        // A mahadasha change is already the next line.
        if (endingLord != null && res != null) {
            stringResource(
                res,
                astroTerm(endingLord),
                relativeText(PeriodCountdown.humanize(today, changeDate)),
                PeriodCountdown.shortDate(changeDate, locale)
            )
        } else null
    } else null

    val next = period.nextMahadasha
    val nextDate = PeriodCountdown.localDate(next?.start)
    val nextLine = if (next != null && next.lord.isNotBlank() && nextDate != null) {
        stringResource(
            R.string.dashboard_period_next_maha,
            astroTerm(next.lord),
            relativeText(PeriodCountdown.humanize(today, nextDate)),
            DateFieldFormat.display(DateFieldFormat.toIso(nextDate), locale)
        )
    } else null

    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.dashboard_period_title), style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    chain,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = accent
                )
                if (changeLine != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(changeLine, style = MaterialTheme.typography.bodyMedium)
                }
                if (nextLine != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        nextLine,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

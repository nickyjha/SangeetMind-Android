package com.sangeetmind.features.astrology.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.theme.GrahaBudha
import com.sangeetmind.core.ui.theme.GrahaBudhaDeep
import com.sangeetmind.core.ui.theme.GrahaChandra
import com.sangeetmind.core.ui.theme.GrahaChandraDeep
import com.sangeetmind.core.ui.theme.GrahaGuru
import com.sangeetmind.core.ui.theme.GrahaGuruDeep
import com.sangeetmind.core.ui.theme.GrahaMangala
import com.sangeetmind.core.ui.theme.GrahaMangalaDeep
import com.sangeetmind.core.ui.theme.GrahaRahu
import com.sangeetmind.core.ui.theme.GrahaRahuDeep
import com.sangeetmind.core.ui.theme.GrahaShani
import com.sangeetmind.core.ui.theme.GrahaShaniDeep
import com.sangeetmind.core.ui.theme.GrahaShukra
import com.sangeetmind.core.ui.theme.GrahaShukraDeep
import com.sangeetmind.core.ui.theme.GrahaSurya
import com.sangeetmind.core.ui.theme.GrahaSuryaDeep
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.dashboard.DashboardViewModel
import com.sangeetmind.libs.models.DailyHoroscope
import com.sangeetmind.libs.models.PanchangResponse
import com.sangeetmind.libs.models.toTitleCase

/** Which graha (if any) a destination is tinted with — see [Graha.tones]. */
enum class Graha { SURYA, CHANDRA, MANGALA, BUDHA, GURU, SHUKRA, SHANI, RAHU }

/**
 * A graha's two fixed tones (bright/deep, from Color.kt) plus [themed] — whichever of
 * the two is correct for the theme active right now. [themed] is what a plain icon or
 * top-bar tint should use; [bright]/[deep] together are for a featured tile's gradient,
 * which deliberately uses both ends in every theme (see [DestinationCard]).
 */
private data class GrahaTones(val bright: Color, val deep: Color, val themed: Color)

@Composable
private fun Graha.tones(): GrahaTones {
    val local = LocalGrahaColors.current
    return when (this) {
        Graha.SURYA -> GrahaTones(GrahaSurya, GrahaSuryaDeep, local.surya)
        Graha.CHANDRA -> GrahaTones(GrahaChandra, GrahaChandraDeep, local.chandra)
        Graha.MANGALA -> GrahaTones(GrahaMangala, GrahaMangalaDeep, local.mangala)
        Graha.BUDHA -> GrahaTones(GrahaBudha, GrahaBudhaDeep, local.budha)
        Graha.GURU -> GrahaTones(GrahaGuru, GrahaGuruDeep, local.guru)
        Graha.SHUKRA -> GrahaTones(GrahaShukra, GrahaShukraDeep, local.shukra)
        Graha.SHANI -> GrahaTones(GrahaShani, GrahaShaniDeep, local.shani)
        Graha.RAHU -> GrahaTones(GrahaRahu, GrahaRahuDeep, local.rahu)
    }
}

/**
 * [graha] is the destination's Navagraha association, if any — the Navagraha system
 * (see the design pitch): each core astrology feature is tinted with the planet that
 * traditionally governs it, so a returning user recognizes tiles by color before
 * reading the label. Secondary/monetization destinations (no graha association)
 * pass null and render as calm, neutral tiles so the core features keep the eye.
 */
data class DashboardDestination(
    val label: String,
    val icon: ImageVector,
    val graha: Graha? = null,
    val featured: Boolean = false,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenKundliList: () -> Unit,
    onOpenKundliOnboarding: () -> Unit,
    onOpenHoroscope: () -> Unit,
    onOpenPanchang: () -> Unit,
    onOpenMuhurat: () -> Unit,
    onOpenMatch: () -> Unit,
    onOpenNumerology: () -> Unit,
    onOpenInterpretation: () -> Unit,
    onOpenChart: () -> Unit,
    onOpenChatMind: () -> Unit,
    onOpenPayments: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenReadings: () -> Unit,
    onOpenMarketplace: () -> Unit,
    onOpenReferrals: () -> Unit,
    onOpenSangeet: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SangeetMind") },
                actions = {
                    IconButton(onClick = onOpenKundliList) {
                        Icon(Icons.Default.People, contentDescription = "My kundlis")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.profile != null && uiState.primaryKundli != null -> {
                    ProfileSummaryCard(
                        name = uiState.primaryKundli!!.fullName?.toTitleCase(),
                        moonSign = uiState.profile!!.moonSign,
                        lagna = uiState.profile!!.lagna,
                        nakshatra = uiState.profile!!.nakshatra,
                        currentMahadasha = uiState.profile!!.currentMahadasha,
                        astroMood = uiState.profile!!.astroMood
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TodayCard(
                        horoscope = uiState.todayHoroscope,
                        panchang = uiState.todayPanchang,
                        onOpenHoroscope = onOpenHoroscope
                    )
                }
                uiState.hasNoKundlis -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Create your kundli", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add your birth details to unlock your horoscope, panchang, and full reading.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onOpenKundliOnboarding) {
                                Text("Get started")
                            }
                        }
                    }
                }
                uiState.error != null -> {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(
                            text = uiState.error!!,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            if (!uiState.hasNoKundlis) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Explore", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                val destinations = listOf(
                    DashboardDestination("Birth Chart", Icons.Default.DonutLarge, Graha.SHANI, featured = true, onClick = onOpenChart),
                    DashboardDestination("Horoscope", Icons.Default.Insights, Graha.SURYA, featured = true, onClick = onOpenHoroscope),
                    DashboardDestination("Panchang", Icons.Default.CalendarMonth, Graha.CHANDRA, onClick = onOpenPanchang),
                    DashboardDestination("Muhurat", Icons.Default.Schedule, Graha.GURU, onClick = onOpenMuhurat),
                    DashboardDestination("Match", Icons.Default.Favorite, Graha.MANGALA, onClick = onOpenMatch),
                    DashboardDestination("Numerology", Icons.Default.Tag, Graha.BUDHA, onClick = onOpenNumerology),
                    DashboardDestination("Full Reading", Icons.Default.AutoStories, Graha.SHUKRA, onClick = onOpenInterpretation),
                    DashboardDestination("ChatMind", Icons.Default.Chat, Graha.RAHU, onClick = onOpenChatMind),
                    DashboardDestination("Payments", Icons.Default.AccountBalanceWallet, onClick = onOpenPayments),
                    DashboardDestination("Reports", Icons.Default.PictureAsPdf, onClick = onOpenReports),
                    DashboardDestination("Readings", Icons.Default.Psychology, onClick = onOpenReadings),
                    DashboardDestination("Astrologers", Icons.Default.SupportAgent, onClick = onOpenMarketplace),
                    DashboardDestination("Referrals", Icons.Default.CardGiftcard, onClick = onOpenReferrals),
                    DashboardDestination("Sangeet", Icons.Default.MusicNote, onClick = onOpenSangeet)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(destinations) { destination ->
                        DestinationCard(destination)
                    }
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
                text = name?.takeIf { it.isNotBlank() } ?: "Your kundli",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            SummaryRow("Moon sign", moonSign)
            SummaryRow("Lagna", lagna)
            if (nakshatra.isNotBlank()) SummaryRow("Nakshatra", nakshatra, highlight = true)
            if (currentMahadasha.isNotBlank()) SummaryRow("Current dasha", currentMahadasha)
            if (astroMood.isNotBlank()) SummaryRow("Today's mood", astroMood)
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

    if (horoscope == null && panchang == null) return

    Card(modifier = Modifier.fillMaxWidth(), onClick = onOpenHoroscope) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WbSunny, contentDescription = null, tint = surya, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Today", style = MaterialTheme.typography.titleMedium)
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
                        val swatch = TODAY_COLOR_MAP[luckyColor.trim().lowercase()] ?: surya
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(swatch)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Lucky color", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(luckyColor, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    panchang?.let { p ->
                        Column {
                            Text("Panchang", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${p.tithi.name} · ${p.nakshatra.name}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                "View today's full guidance →",
                style = MaterialTheme.typography.labelLarge,
                color = surya
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DestinationCard(destination: DashboardDestination) {
    val tones = destination.graha?.tones()
    when {
        destination.featured && tones != null -> {
            // Deep-to-bright gradient, deep end under the text (left, where the label
            // sits) so white text stays legible in both themes; the bright end still
            // lets the tile pop against the page. See the GrahaXDeep contrast notes
            // in Color.kt — this is why it's not just a flat `accent` fill.
            Card(
                modifier = Modifier.fillMaxWidth().height(96.dp),
                onClick = destination.onClick,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(colors = listOf(tones.deep, tones.bright)))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Icon(destination.icon, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        destination.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
        }
        else -> {
            val tint = tones?.themed ?: MaterialTheme.colorScheme.onSurfaceVariant
            Card(
                modifier = Modifier.fillMaxWidth().height(96.dp),
                onClick = destination.onClick
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(tint.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            destination.icon,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(destination.label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

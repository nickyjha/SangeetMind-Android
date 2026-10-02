package com.sangeetmind.features.astrology.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sangeetmind.core.common.Constants
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.LanguagePickerDialog
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.dashboard.ui.Graha

/** Shared body for the three list-style tab roots. */
@Composable
private fun HubScaffold(title: String, content: LazyListScope.() -> Unit) {
    Scaffold(
        topBar = { TabRootTopBar(title) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp)
        ) {
            content()
        }
    }
}

private fun LazyListScope.section(header: String, items: List<HubItem>) {
    item(key = header) {
        HubSectionHeader(header)
        HubGroup(items)
    }
}

/**
 * Kundli tab: everything computed from the user's own birth chart. The three "reading"
 * entry points (topic Readings, Full reading, Holistic) are grouped under one Readings
 * section; PDF reports stand alone since they're downloadable files, not screens.
 */
@Composable
fun KundliHubScreen(
    onOpenChart: () -> Unit,
    onOpenGochar: () -> Unit,
    onOpenVarshaphal: () -> Unit,
    onOpenNumerology: () -> Unit,
    onOpenReadings: () -> Unit,
    onOpenInterpretation: () -> Unit,
    onOpenHolistic: () -> Unit,
    onOpenMatch: () -> Unit,
    onOpenReports: () -> Unit
) {
    val chart = listOf(
        HubItem(Icons.Default.DonutLarge, Graha.SHANI, stringResource(R.string.dashboard_dest_birth_chart), stringResource(R.string.dashboard_hub_chart_desc), onOpenChart),
        HubItem(Icons.Default.Public, Graha.RAHU, stringResource(R.string.dashboard_dest_gochar), stringResource(R.string.dashboard_hub_transits_desc), onOpenGochar),
        HubItem(Icons.Default.Autorenew, Graha.SURYA, stringResource(R.string.dashboard_hub_varshaphal_title), stringResource(R.string.dashboard_hub_varshaphal_desc), onOpenVarshaphal),
        HubItem(Icons.Default.Tag, Graha.BUDHA, stringResource(R.string.dashboard_dest_numerology), stringResource(R.string.dashboard_hub_numerology_desc), onOpenNumerology)
    )
    val readings = listOf(
        HubItem(Icons.Default.Psychology, Graha.SHUKRA, stringResource(R.string.dashboard_hub_readings_title), stringResource(R.string.dashboard_hub_readings_desc), onOpenReadings),
        HubItem(Icons.Default.AutoStories, Graha.GURU, stringResource(R.string.dashboard_hub_full_reading_title), stringResource(R.string.dashboard_hub_full_reading_desc), onOpenInterpretation),
        HubItem(Icons.Default.AutoAwesome, Graha.CHANDRA, stringResource(R.string.dashboard_hub_holistic_title), stringResource(R.string.dashboard_hub_holistic_desc), onOpenHolistic)
    )
    val match = listOf(
        HubItem(Icons.Default.Favorite, Graha.MANGALA, stringResource(R.string.dashboard_hub_match_title), stringResource(R.string.dashboard_hub_match_desc), onOpenMatch)
    )
    val reports = listOf(
        HubItem(Icons.Default.PictureAsPdf, Graha.SHANI, stringResource(R.string.dashboard_hub_reports_title), stringResource(R.string.dashboard_hub_reports_desc), onOpenReports)
    )
    val hChart = stringResource(R.string.dashboard_hub_section_chart)
    val hReadings = stringResource(R.string.dashboard_hub_section_readings)
    val hMatch = stringResource(R.string.dashboard_hub_section_match)
    val hReports = stringResource(R.string.dashboard_hub_section_reports)
    HubScaffold(stringResource(R.string.dashboard_hub_kundli_title)) {
        section(hChart, chart)
        section(hReadings, readings)
        section(hMatch, match)
        section(hReports, reports)
    }
}

/** Calendar tab: day-and-date tools (Panchang, festivals, eclipses, muhurat, prashna). */
@Composable
fun CalendarHubScreen(
    onOpenHoroscope: () -> Unit,
    onOpenPanchang: () -> Unit,
    onOpenFestivals: () -> Unit,
    onOpenEclipses: () -> Unit,
    onOpenMuhurat: () -> Unit,
    onOpenPrashna: () -> Unit
) {
    val today = listOf(
        HubItem(Icons.Default.Insights, Graha.SURYA, stringResource(R.string.dashboard_hub_horoscope_title), stringResource(R.string.dashboard_hub_horoscope_desc), onOpenHoroscope),
        HubItem(Icons.Default.CalendarMonth, Graha.CHANDRA, stringResource(R.string.dashboard_dest_panchang), stringResource(R.string.dashboard_hub_panchang_desc), onOpenPanchang)
    )
    val dates = listOf(
        HubItem(Icons.Default.Celebration, Graha.SHUKRA, stringResource(R.string.dashboard_dest_festivals), stringResource(R.string.dashboard_hub_festivals_desc), onOpenFestivals),
        HubItem(Icons.Default.DarkMode, Graha.RAHU, stringResource(R.string.dashboard_dest_eclipses), stringResource(R.string.dashboard_hub_eclipses_desc), onOpenEclipses),
        HubItem(Icons.Default.Schedule, Graha.GURU, stringResource(R.string.dashboard_dest_muhurat), stringResource(R.string.dashboard_hub_muhurat_desc), onOpenMuhurat),
        // Shani (not Budha): Numerology already owns Budha's jade.
        HubItem(Icons.Default.HelpOutline, Graha.SHANI, stringResource(R.string.dashboard_dest_prashna), stringResource(R.string.dashboard_hub_prashna_desc), onOpenPrashna)
    )
    val hToday = stringResource(R.string.dashboard_hub_section_today)
    val hDates = stringResource(R.string.dashboard_hub_section_dates)
    HubScaffold(stringResource(R.string.dashboard_hub_calendar_title)) {
        section(hToday, today)
        section(hDates, dates)
    }
}

/** Me tab: profile/kundli switcher, money, app settings, help and legal. */
@Composable
fun MeHubScreen(
    onOpenKundliList: () -> Unit,
    onOpenPayments: () -> Unit,
    onOpenReferrals: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSangeet: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    var languageOpen by rememberSaveable { mutableStateOf(false) }
    val profile = listOf(
        HubItem(Icons.Default.People, Graha.SHANI, stringResource(R.string.dashboard_my_kundlis), stringResource(R.string.dashboard_hub_kundlis_desc), onOpenKundliList),
        HubItem(Icons.Default.AccountBalanceWallet, Graha.GURU, stringResource(R.string.dashboard_dest_payments), stringResource(R.string.dashboard_hub_premium_desc), onOpenPayments),
        HubItem(Icons.Default.CardGiftcard, Graha.SHUKRA, stringResource(R.string.dashboard_dest_referrals), stringResource(R.string.dashboard_hub_referrals_desc), onOpenReferrals)
    )
    val app = listOf(
        HubItem(Icons.Default.Language, Graha.CHANDRA, stringResource(CoreR.string.common_language), stringResource(R.string.dashboard_hub_language_desc)) { languageOpen = true },
        HubItem(Icons.Default.Settings, Graha.RAHU, stringResource(R.string.dashboard_settings), stringResource(R.string.dashboard_hub_settings_desc), onOpenSettings),
        HubItem(Icons.Default.MusicNote, Graha.BUDHA, stringResource(R.string.dashboard_hub_sangeet_title), stringResource(R.string.dashboard_hub_sangeet_desc), onOpenSangeet)
    )
    val help = listOf(
        HubItem(Icons.Default.Help, Graha.SURYA, stringResource(R.string.dashboard_hub_help_title), stringResource(R.string.dashboard_hub_help_desc)) {
            runCatching { uriHandler.openUri(Constants.WEBSITE_BASE_URL) }
        },
        HubItem(Icons.Default.Policy, Graha.MANGALA, stringResource(R.string.dashboard_hub_privacy_title), null) {
            runCatching { uriHandler.openUri(Constants.PRIVACY_URL) }
        },
        HubItem(Icons.Default.Description, Graha.SHANI, stringResource(R.string.dashboard_hub_terms_title), null) {
            runCatching { uriHandler.openUri(Constants.TERMS_URL) }
        }
    )
    val hProfile = stringResource(R.string.dashboard_hub_section_profile)
    val hApp = stringResource(R.string.dashboard_hub_section_app)
    val hHelp = stringResource(R.string.dashboard_hub_section_help)
    HubScaffold(stringResource(R.string.dashboard_hub_me_title)) {
        section(hProfile, profile)
        section(hApp, app)
        section(hHelp, help)
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
    if (languageOpen) {
        LanguagePickerDialog(onDismiss = { languageOpen = false })
    }
}

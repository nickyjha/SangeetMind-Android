package com.sangeetmind.features.astrology.horoscope.ui

import com.sangeetmind.core.ui.components.AstroTopBar
import com.sangeetmind.features.astrology.dashboard.ui.luckySwatch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.components.ErrorCard
import com.sangeetmind.core.ui.text.MarkdownText
import com.sangeetmind.core.ui.text.markdownInline
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.horoscope.HoroscopeTab
import com.sangeetmind.features.astrology.horoscope.HoroscopeViewModel
import com.sangeetmind.libs.models.AuspiciousTime
import com.sangeetmind.libs.models.DaanDonation
import com.sangeetmind.libs.models.DailyHoroscope
import com.sangeetmind.libs.models.EnhancedMantra
import com.sangeetmind.libs.models.PeriodHoroscope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoroscopeScreen(
    onNavigateBack: () -> Unit,
    viewModel: HoroscopeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = HoroscopeTab.entries.toList()

    Scaffold(
        topBar = {
            AstroTopBar(
                title = stringResource(R.string.horoscope_title),
                onBack = onNavigateBack,
                accent = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.surya
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tabs.indexOf(uiState.selectedTab)) {
                tabs.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tabLabel(tab)) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    uiState.error != null -> ErrorCard(
                        message = uiState.error,
                        onRetry = { viewModel.load() },
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    )
                    else -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        when (uiState.selectedTab) {
                            HoroscopeTab.DAILY -> uiState.daily?.let { DailyContent(it) }
                            HoroscopeTab.WEEKLY -> uiState.weekly?.let { PeriodContent(it) }
                            HoroscopeTab.MONTHLY -> uiState.monthly?.let { PeriodContent(it) }
                            HoroscopeTab.YEARLY -> uiState.yearly?.let { PeriodContent(it) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun tabLabel(tab: HoroscopeTab): String = stringResource(
    when (tab) {
        HoroscopeTab.DAILY -> R.string.horoscope_tab_daily
        HoroscopeTab.WEEKLY -> R.string.horoscope_tab_weekly
        HoroscopeTab.MONTHLY -> R.string.horoscope_tab_monthly
        HoroscopeTab.YEARLY -> R.string.horoscope_tab_yearly
    }
)

@Composable
private fun DailyContent(horoscope: DailyHoroscope) {
    Text(horoscope.theme?.shortLabel ?: stringResource(CoreR.string.common_today), style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(8.dp))
    Text(horoscope.theme?.longText ?: "", style = MaterialTheme.typography.bodyLarge)

    val enhanced = horoscope.enhanced

    enhanced?.interpretation?.let { interpretation ->
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.horoscope_todays_insight), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        MarkdownText(interpretation, style = MaterialTheme.typography.bodyMedium)
    }

    enhanced?.whatToDoToday?.takeIf { it.isNotEmpty() }?.let { items ->
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.horoscope_what_to_do), style = MaterialTheme.typography.titleMedium)
        items.forEach { item ->
            Text(
                text = markdownInline("• $item"),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    enhanced?.whatToAvoid?.takeIf { it.isNotEmpty() }?.let { items ->
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.horoscope_what_to_avoid), style = MaterialTheme.typography.titleMedium)
        items.forEach { item ->
            Text(
                text = markdownInline("• $item"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    if (!enhanced?.luckyColor.isNullOrBlank() || enhanced?.auspiciousTime != null) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            enhanced?.luckyColor?.takeIf { it.isNotBlank() }?.let { color ->
                LuckyColorCard(color, enhanced.colorReason, modifier = Modifier.weight(1f))
            }
            enhanced?.auspiciousTime?.let { window ->
                AuspiciousTimeCard(window, modifier = Modifier.weight(1f))
            }
        }
    }

    enhanced?.mantra?.takeIf { it.name.isNotBlank() }?.let { mantra ->
        Spacer(modifier = Modifier.height(12.dp))
        MantraCard(mantra)
    } ?: horoscope.recommendedMantras.firstOrNull()?.name?.let { mantra ->
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.horoscope_recommended_mantra_fmt, astroTerm(mantra)), style = MaterialTheme.typography.bodyMedium)
    }

    enhanced?.daanDonation?.takeIf { it.item.isNotBlank() }?.let { daan ->
        Spacer(modifier = Modifier.height(12.dp))
        DaanCard(daan)
    }

    enhanced?.generalAdvice?.takeIf { it.isNotBlank() }?.let { advice ->
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            advice,
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun LuckyColorCard(color: String, reason: String?, modifier: Modifier = Modifier) {
    // Same matching as Home: "Light Blue" -> blue; unknown colours get no dot.
    val swatch = luckySwatch(color)
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (swatch != null) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(swatch)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(stringResource(R.string.horoscope_lucky_color), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(color, style = MaterialTheme.typography.titleMedium)
            if (!reason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(reason, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AuspiciousTimeCard(window: AuspiciousTime, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(stringResource(R.string.horoscope_auspicious_time), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text("${window.start} – ${window.end}", style = MaterialTheme.typography.titleMedium)
            if (window.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(window.reason, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MantraCard(mantra: EnhancedMantra) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.horoscope_recommended_mantra), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(astroTerm(mantra.name), style = MaterialTheme.typography.titleMedium)
            val details = listOfNotNull(
                mantra.count.takeIf { it.isNotBlank() },
                mantra.bestTime.takeIf { it.isNotBlank() }
            ).joinToString(" · ")
            if (details.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (mantra.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(markdownInline(mantra.reason), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun DaanCard(daan: DaanDonation) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.horoscope_todays_daan), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(daan.item, style = MaterialTheme.typography.titleMedium)
            if (daan.toWhom.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(stringResource(R.string.horoscope_daan_to_fmt, daan.toWhom), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (daan.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(markdownInline(daan.reason), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun PeriodContent(period: PeriodHoroscope) {
    Text(
        text = "${period.start} – ${period.end}",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    DailyContent(period.summary)
}

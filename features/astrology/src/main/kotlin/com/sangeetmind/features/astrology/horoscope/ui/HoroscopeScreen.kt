package com.sangeetmind.features.astrology.horoscope.ui

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
import com.sangeetmind.features.astrology.horoscope.HoroscopeTab
import com.sangeetmind.features.astrology.horoscope.HoroscopeViewModel
import com.sangeetmind.libs.models.DailyHoroscope
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
            TopAppBar(
                title = { Text("Horoscope") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tabs.indexOf(uiState.selectedTab)) {
                tabs.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tab.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    uiState.error != null -> Text(
                        text = uiState.error!!,
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
private fun DailyContent(horoscope: DailyHoroscope) {
    Text(horoscope.theme?.shortLabel ?: "Today", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(8.dp))
    Text(horoscope.theme?.longText ?: "", style = MaterialTheme.typography.bodyLarge)

    horoscope.enhanced?.interpretation?.let { interpretation ->
        Spacer(modifier = Modifier.height(16.dp))
        Text("Today's insight", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(interpretation, style = MaterialTheme.typography.bodyMedium)
    }

    horoscope.enhanced?.whatToDoToday?.takeIf { it.isNotEmpty() }?.let { items ->
        Spacer(modifier = Modifier.height(16.dp))
        Text("What to do today", style = MaterialTheme.typography.titleMedium)
        items.forEach { item ->
            Text(
                text = "• $item",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    horoscope.recommendedMantras.firstOrNull()?.name?.let { mantra ->
        Spacer(modifier = Modifier.height(16.dp))
        Text("Recommended mantra: $mantra", style = MaterialTheme.typography.bodyMedium)
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

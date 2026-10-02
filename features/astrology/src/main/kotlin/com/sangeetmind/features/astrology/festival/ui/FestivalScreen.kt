package com.sangeetmind.features.astrology.festival.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.components.ErrorCard
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.LocalAppLanguage
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.chart.ui.Chip
import com.sangeetmind.features.astrology.festival.FestivalViewModel
import com.sangeetmind.libs.models.Festival
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/** A year's major Hindu festivals (backend festival_service.py), computed for the primary
 * kundli's place (Delhi without one). Past ones are dimmed; the coming ones show a countdown. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalScreen(
    onNavigateBack: () -> Unit,
    viewModel: FestivalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val graha = LocalGrahaColors.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.festival_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(CoreR.string.common_refresh))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = graha.shukra.copy(alpha = 0.14f),
                    titleContentColor = graha.shukra,
                    navigationIconContentColor = graha.shukra,
                    actionIconContentColor = graha.shukra
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.error != null -> ErrorCard(
                    message = uiState.error,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
                else -> {
                    val today = LocalDate.now()
                    val festivals = uiState.calendar?.festivals.orEmpty()
                    val listState = rememberLazyListState()
                    // Open at the next festival (item 0 is the year header), keeping one past
                    // festival above it for context.
                    LaunchedEffect(festivals) {
                        val next = festivals.indexOfFirst { it.date >= today.toString() }
                        if (next > 0) listState.scrollToItem(next)
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { viewModel.selectYear(uiState.year - 1) }) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.varshaphal_cd_previous_year))
                                }
                                Text(
                                    "${uiState.year}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                IconButton(onClick = { viewModel.selectYear(uiState.year + 1) }) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.varshaphal_cd_next_year))
                                }
                            }
                            Text(
                                uiState.kundli?.let { stringResource(R.string.festival_place_fmt, it.birthPlace) }
                                    ?: stringResource(R.string.festival_place_default),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            )
                        }
                        if (festivals.isEmpty()) {
                            item { Text(stringResource(R.string.festival_none), style = MaterialTheme.typography.bodyMedium) }
                        }
                        items(festivals, key = { it.id.ifBlank { it.name } }) { FestivalRow(it, today) }
                        item {
                            Text(
                                stringResource(R.string.festival_note),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FestivalRow(festival: Festival, today: LocalDate) {
    val graha = LocalGrahaColors.current
    val languageCode = LocalAppLanguage.current.code
    val locale = LocalConfiguration.current.locales[0]
    val date = runCatching { LocalDate.parse(festival.date) }.getOrNull()
    if (date != null) {
        val days = ChronoUnit.DAYS.between(today, date)

        Card(modifier = Modifier.fillMaxWidth().alpha(if (days < 0) 0.55f else 1f)) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(festival.nameFor(languageCode), style = MaterialTheme.typography.titleMedium)
                    Text(
                        date.format(DateTimeFormatter.ofPattern("d MMM, EEEE", locale)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                when (days) {
                    0L -> Chip(stringResource(R.string.festival_today), graha.budha)
                    1L -> Chip(stringResource(R.string.festival_tomorrow), graha.shukra)
                    in 2L..60L -> Chip(stringResource(R.string.festival_in_days_fmt, days.toInt()), graha.shukra)
                }
            }
        }
    }
}

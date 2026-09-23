package com.sangeetmind.features.astrology.varshaphal.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.core.ui.theme.GrahaColors
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.chart.ui.NorthIndianHouseChart
import com.sangeetmind.features.astrology.chart.ui.PlanetListCard
import com.sangeetmind.features.astrology.varshaphal.VarshaphalViewModel
import com.sangeetmind.libs.models.VarshaphalResponse
import com.sangeetmind.libs.models.toTitleCase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Tajika annual/solar-return chart — a full new chart for the year (Muntha, Varshaphal
 * lagna/Moon, year lord), deliberately its own screen rather than a Birth Chart sub-tab
 * since it's a distinct chart for a distinct instant, not another read of the D1 moment
 * (app/services/varshaphal.py, app/services/tajika_varshaphal.py). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VarshaphalScreen(
    onNavigateBack: () -> Unit,
    viewModel: VarshaphalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val graha = LocalGrahaColors.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.varshaphal_title)) },
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
                    containerColor = graha.surya.copy(alpha = 0.14f),
                    titleContentColor = graha.surya,
                    navigationIconContentColor = graha.surya,
                    actionIconContentColor = graha.surya
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.hasNoKundli -> {
                    Text(
                        text = stringResource(R.string.varshaphal_no_kundli),
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = viewModel::refresh) { Text(stringResource(CoreR.string.common_retry)) }
                    }
                }
                else -> {
                    VarshaphalContent(
                        personName = uiState.kundli?.fullName?.toTitleCase(),
                        year = uiState.year,
                        varshaphal = uiState.varshaphal,
                        onYearChange = viewModel::selectYear
                    )
                }
            }
        }
    }
}

@Composable
private fun VarshaphalContent(
    personName: String?,
    year: Int,
    varshaphal: VarshaphalResponse?,
    onYearChange: (Int) -> Unit
) {
    val graha = LocalGrahaColors.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = personName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.varshaphal_your_default),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onYearChange(year - 1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.varshaphal_cd_previous_year))
                }
                Text(
                    "$year",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(onClick = { onYearChange(year + 1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.varshaphal_cd_next_year))
                }
            }
        }
        if (varshaphal != null) {
            item {
                TajikaCard(varshaphal, graha)
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    NorthIndianHouseChart(
                        lagna = varshaphal.chart.lagna,
                        planets = varshaphal.chart.planets,
                        title = stringResource(R.string.varshaphal_chart_title, year),
                        subtitle = stringResource(R.string.varshaphal_chart_subtitle),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            item {
                PlanetListCard(varshaphal.chart.lagna, varshaphal.chart.planets)
            }
        }
    }
}

private fun fmtSolarReturn(isoLocal: String): String =
    runCatching {
        val instant = Instant.parse(if (isoLocal.endsWith("Z")) isoLocal else "${isoLocal}Z")
        DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a").withZone(ZoneId.systemDefault()).format(instant)
    }.getOrDefault(isoLocal)

@Composable
private fun TajikaCard(varshaphal: VarshaphalResponse, graha: GrahaColors) {
    val tajika = varshaphal.tajika
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.varshaphal_tajika_summary), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(
                    R.string.varshaphal_solar_return_fmt,
                    fmtSolarReturn(varshaphal.solarReturnLocal),
                    tajika.ageCompleted
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            val munthaHouse = tajika.muntha.houseFromVarshaphalLagna
                ?.let { stringResource(CoreR.string.common_house_short, it) } ?: "-"
            TajikaRow(stringResource(R.string.varshaphal_muntha), "${astroTerm(tajika.muntha.sign)} · $munthaHouse")
            TajikaRow(
                stringResource(R.string.varshaphal_lagna),
                astroTerm(tajika.varshaphalLagna.sign) +
                    (tajika.varshaphalLagna.lord?.let { stringResource(R.string.varshaphal_lord_suffix_fmt, astroTerm(it)) } ?: "")
            )
            TajikaRow(
                stringResource(R.string.varshaphal_moon),
                astroTerm(tajika.varshaphalMoon.sign) +
                    (tajika.varshaphalMoon.lord?.let { stringResource(R.string.varshaphal_lord_suffix_fmt, astroTerm(it)) } ?: "")
            )
            tajika.yearLord?.let { lord ->
                TajikaRow(stringResource(R.string.varshaphal_year_lord), astroTerm(lord), color = grahaColorFor(lord, graha))
            }
        }
    }
}

@Composable
private fun TajikaRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

private fun grahaColorFor(planet: String, graha: GrahaColors): Color = when (planet) {
    "Sun" -> graha.surya
    "Moon" -> graha.chandra
    "Mars" -> graha.mangala
    "Mercury" -> graha.budha
    "Jupiter" -> graha.guru
    "Venus" -> graha.shukra
    "Saturn" -> graha.shani
    else -> graha.rahu
}

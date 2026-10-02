package com.sangeetmind.features.astrology.eclipse.ui

import com.sangeetmind.core.ui.components.AstroTopBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.components.ErrorCard
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.LocalAppLanguage
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.chart.ui.Chip
import com.sangeetmind.features.astrology.eclipse.EclipseViewModel
import com.sangeetmind.libs.models.Eclipse
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/** A year's solar and lunar eclipses for the primary kundli's place (backend
 * eclipse_service.py): whether each is visible there and when, sutak, and whether it is
 * favourable for the person's Moon sign. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EclipseScreen(
    onNavigateBack: () -> Unit,
    viewModel: EclipseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val graha = LocalGrahaColors.current

    Scaffold(
        topBar = {
            AstroTopBar(
                title = stringResource(R.string.eclipse_title),
                onBack = onNavigateBack,
                accent = graha.rahu,
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(CoreR.string.common_refresh))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.hasNoKundli -> Text(
                    stringResource(R.string.eclipse_no_kundli),
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                uiState.error != null -> ErrorCard(
                    message = uiState.error,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        uiState.kundli?.let {
                            Text(
                                stringResource(R.string.eclipse_place_fmt, it.birthPlace),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            )
                        }
                    }
                    val eclipses = uiState.calendar?.eclipses.orEmpty()
                    if (eclipses.isEmpty()) {
                        item { Text(stringResource(R.string.eclipse_none), style = MaterialTheme.typography.bodyMedium) }
                    }
                    items(eclipses) { EclipseCard(it, uiState.moonSign) }
                    item {
                        Text(
                            stringResource(R.string.eclipse_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun parse(iso: String): OffsetDateTime? = runCatching { OffsetDateTime.parse(iso) }.getOrNull()

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EclipseCard(eclipse: Eclipse, moonSign: String?) {
    val graha = LocalGrahaColors.current
    val languageCode = LocalAppLanguage.current.code
    val locale = LocalConfiguration.current.locales[0]
    val dateFmt = DateTimeFormatter.ofPattern("d MMM yyyy", locale)
    val dayTimeFmt = DateTimeFormatter.ofPattern("d MMM, HH:mm", locale)
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm", locale)
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val kindColor = if (eclipse.isSolar) graha.surya else graha.chandra
    val peak = parse(eclipse.peak)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(eclipse.name.forLanguage(languageCode), style = MaterialTheme.typography.titleMedium, color = kindColor)
            if (peak != null) {
                Text(
                    stringResource(R.string.eclipse_peak_fmt, peak.format(dateFmt), peak.format(timeFmt)),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            FlowRow(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Chip(astroTerm(eclipse.sign), graha.rahu)
                Chip(astroTerm(eclipse.nakshatra), muted)
                if (eclipse.visible) Chip(stringResource(R.string.eclipse_visible), graha.budha)
                else Chip(stringResource(R.string.eclipse_not_visible), muted)
            }

            eclipse.local?.let { local ->
                val begin = parse(local.begin)
                val end = parse(local.end)
                if (begin != null && end != null) {
                    Text(
                        stringResource(R.string.eclipse_local_fmt, begin.format(timeFmt), end.format(timeFmt)),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            val sutak = eclipse.sutak
            Text(
                when {
                    sutak != null -> {
                        val b = parse(sutak.begin)
                        val e = parse(sutak.end)
                        if (b != null && e != null) {
                            stringResource(R.string.eclipse_sutak_fmt, b.format(dayTimeFmt), e.format(dayTimeFmt))
                        } else ""
                    }
                    eclipse.visible -> stringResource(R.string.eclipse_no_sutak_penumbral)
                    else -> stringResource(R.string.eclipse_no_sutak_not_visible)
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (sutak != null) graha.mangala else muted,
                modifier = Modifier.padding(top = 4.dp)
            )

            val effect = moonSign?.let { eclipse.rashiEffects.effectFor(it) }
            if (moonSign != null && effect != null) {
                val (label, color) = effectLabel(effect, graha.budha, graha.surya, graha.mangala)
                FlowRow(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        stringResource(R.string.eclipse_your_sign_fmt, astroTerm(moonSign)),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Chip(label, color)
                }
            }
            RashiRow(stringResource(R.string.eclipse_favourable), eclipse.rashiEffects.favourable, graha.budha)
            RashiRow(stringResource(R.string.eclipse_mixed), eclipse.rashiEffects.mixed, graha.surya)
            RashiRow(stringResource(R.string.eclipse_careful), eclipse.rashiEffects.careful, graha.mangala)
        }
    }
}

@Composable
private fun effectLabel(effect: String, good: Color, mixed: Color, careful: Color): Pair<String, Color> =
    when (effect) {
        "favourable" -> stringResource(R.string.eclipse_favourable) to good
        "mixed" -> stringResource(R.string.eclipse_mixed) to mixed
        else -> stringResource(R.string.eclipse_careful) to careful
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RashiRow(label: String, signs: List<String>, color: Color) {
    if (signs.isNotEmpty()) {
        FlowRow(
            modifier = Modifier.padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            signs.forEach { Chip(astroTerm(it), color) }
        }
    }
}

package com.sangeetmind.features.astrology.panchang.ui

import com.sangeetmind.core.ui.components.ErrorCard
import com.sangeetmind.libs.models.Choghadiya
import com.sangeetmind.libs.models.PanchangSpan
import com.sangeetmind.core.ui.theme.LocalGrahaColors
import com.sangeetmind.features.astrology.chart.ui.Chip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.astroTerm
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.panchang.PanchangViewModel
import com.sangeetmind.libs.models.PanchangResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanchangScreen(
    onNavigateBack: () -> Unit,
    viewModel: PanchangViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.panchang_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.chandra.copy(alpha = 0.14f),
                    titleContentColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.chandra,
                    navigationIconContentColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.chandra
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            OutlinedTextField(
                value = uiState.date,
                onValueChange = viewModel::onDateChange,
                label = { Text(stringResource(R.string.panchang_date_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.placeQuery,
                onValueChange = viewModel::onPlaceQueryChange,
                label = { Text(stringResource(R.string.panchang_place_label)) },
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                trailingIcon = {
                    if (uiState.isSearchingPlace) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done)
            )

            if (uiState.placeSuggestions.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                        items(uiState.placeSuggestions) { suggestion ->
                            ListItem(
                                headlineContent = { Text(suggestion.label) },
                                modifier = Modifier.clickable { viewModel.onPlaceSelected(suggestion) }
                            )
                            Divider()
                        }
                    }
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                ErrorCard(message = uiState.error, onRetry = { viewModel.fetchPanchang() })
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::fetchPanchang,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.panchang_get))
                }
            }

            uiState.result?.let { result ->
                Spacer(modifier = Modifier.height(24.dp))
                PanchangResultCard(result)
            }
        }
    }
}

@Composable
private fun PanchangResultCard(result: PanchangResponse) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = result.date, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            PanchangRow(
                stringResource(R.string.panchang_tithi),
                listOfNotNull(result.tithi.paksha?.let { astroTerm(it) }, astroTerm(result.tithi.name)).joinToString(" ")
            )
            PanchangRow(stringResource(R.string.panchang_nakshatra), astroTerm(result.nakshatra.name))
            PanchangRow(stringResource(R.string.panchang_yoga), astroTerm(result.yoga.name))
            PanchangRow(stringResource(R.string.panchang_karana), astroTerm(result.karana.name))
            PanchangRow(stringResource(R.string.panchang_vara), astroTerm(result.vara.name))
            PanchangRow(stringResource(R.string.panchang_moon_sign), astroTerm(result.moonSign))
            PanchangRow(stringResource(R.string.panchang_sun_sign), astroTerm(result.sunSign))
            result.sunrise?.let { PanchangRow(stringResource(R.string.panchang_sunrise), it) }
            result.sunset?.let { PanchangRow(stringResource(R.string.panchang_sunset), it) }
            result.rahuKalam?.span()?.let { PanchangRow(stringResource(R.string.panchang_rahu_kalam), it) }
            result.abhijitMuhurat?.span()?.let { PanchangRow(stringResource(R.string.panchang_abhijit), it) }
            if (result.choghadiya.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.panchang_choghadiya), style = MaterialTheme.typography.titleSmall)
                ChoghadiyaChips(result.choghadiya)
            }
        }
    }
}

private fun PanchangSpan.span(): String? = if (start != null && end != null) "$start – $end" else null

/** Day choghadiya as chips, green for auspicious, red for inauspicious. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChoghadiyaChips(slots: List<Choghadiya>) {
    val graha = LocalGrahaColors.current
    FlowRow(
        modifier = Modifier.padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        slots.forEach { c ->
            val color = when (c.quality) {
                "auspicious" -> graha.budha
                "inauspicious" -> graha.mangala
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Chip("${astroTerm(c.name)} ${c.start.orEmpty()}–${c.end.orEmpty()}", color)
        }
    }
}

@Composable
private fun PanchangRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}


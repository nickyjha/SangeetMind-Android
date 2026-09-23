package com.sangeetmind.features.astrology.marketplace.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.marketplace.MarketplaceListViewModel
import com.sangeetmind.libs.models.Astrologer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceListScreen(
    onNavigateBack: () -> Unit,
    onOpenAstrologer: (String) -> Unit,
    viewModel: MarketplaceListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.marketplace_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.marketplace_online_only), modifier = Modifier.weight(1f))
                Switch(checked = uiState.onlineOnly, onCheckedChange = viewModel::setOnlineOnly)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.astrologers.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    uiState.astrologers.isEmpty() -> {
                        Text(
                            stringResource(R.string.marketplace_empty),
                            modifier = Modifier.align(Alignment.Center).padding(24.dp)
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.astrologers, key = { it.astrologerId }) { astrologer ->
                                AstrologerCard(astrologer, onClick = { onOpenAstrologer(astrologer.astrologerId) })
                            }
                        }
                    }
                }

                if (uiState.error != null) {
                    Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
                        Text(uiState.error!!)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AstrologerCard(astrologer: Astrologer, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(astrologer.displayName, style = MaterialTheme.typography.titleMedium)
                    if (astrologer.online) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Circle,
                            contentDescription = stringResource(R.string.marketplace_online_cd),
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
                Text(
                    text = astrologer.skills.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("%.1f".format(astrologer.ratingAvg), style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(
                text = stringResource(
                    R.string.marketplace_rate_per_min_fmt,
                    "%.2f".format(astrologer.ratePaisePerMin / 100.0)
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

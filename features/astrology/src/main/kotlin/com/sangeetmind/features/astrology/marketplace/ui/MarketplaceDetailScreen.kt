package com.sangeetmind.features.astrology.marketplace.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.marketplace.MarketplaceDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceDetailScreen(
    onNavigateBack: () -> Unit,
    onStartChat: (String) -> Unit,
    viewModel: MarketplaceDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.astrologer?.displayName ?: stringResource(R.string.marketplace_astrologer)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading && uiState.astrologer == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                return@Box
            }
            val astrologer = uiState.astrologer ?: return@Box

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(astrologer.displayName, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("%.1f".format(astrologer.ratingAvg))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        stringResource(
                            if (astrologer.online) R.string.marketplace_online else R.string.marketplace_offline
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.marketplace_languages_fmt, astrologer.languages.joinToString(", ")))
                Text(stringResource(R.string.marketplace_skills_fmt, astrologer.skills.joinToString(", ")))
                if (!astrologer.bio.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(astrologer.bio.orEmpty())
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(
                        R.string.marketplace_rate_per_minute_fmt,
                        "%.2f".format(astrologer.ratePaisePerMin / 100.0)
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onStartChat(astrologer.astrologerId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.marketplace_start_chat))
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.marketplace_leave_review), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.reviewSubmitted) {
                    Text(stringResource(R.string.marketplace_review_thanks))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        for (i in 1..5) {
                            IconButton(onClick = { rating = i }) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = stringResource(R.string.marketplace_stars_cd, i),
                                    tint = if (i <= rating) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text(stringResource(R.string.marketplace_comment)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.submitReview(rating, comment) }) {
                        Text(stringResource(R.string.marketplace_submit_review))
                    }
                }

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

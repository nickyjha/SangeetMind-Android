package com.sangeetmind.features.astrology.marketplace.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.marketplace.ChatMessage
import com.sangeetmind.features.astrology.marketplace.MarketplaceChatViewModel

/**
 * NOTE: there is no real-time chat transport on the backend yet (see
 * MarketplaceChatViewModel's doc comment) — messages sent here are local-only. Only the
 * per-minute wallet debit while a session is active is real.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: MarketplaceChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.astrologer?.displayName ?: stringResource(R.string.marketplace_chat)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.sessionActive) viewModel.endSession()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                }
            )
        },
        bottomBar = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.draft,
                        onValueChange = viewModel::onDraftChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.marketplace_message_placeholder)) }
                    )
                    IconButton(onClick = viewModel::sendDraft) {
                        Icon(Icons.Default.Send, contentDescription = stringResource(R.string.marketplace_send_cd))
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            stringResource(
                                if (uiState.sessionActive) R.string.marketplace_session_active
                                else R.string.marketplace_session_not_started
                            ),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            stringResource(
                                R.string.marketplace_billed_fmt,
                                uiState.minutesBilled,
                                "%.2f".format(uiState.totalPaise / 100.0)
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (uiState.sessionActive) {
                        Button(onClick = viewModel::endSession) { Text(stringResource(R.string.marketplace_end)) }
                    } else {
                        Button(onClick = viewModel::startSession) { Text(stringResource(R.string.marketplace_start)) }
                    }
                }
            }

            if (uiState.error != null) {
                Text(
                    uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages) { message -> MessageBubble(message) }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.fromMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (message.fromMe) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(message.text, modifier = Modifier.padding(12.dp))
        }
    }
}

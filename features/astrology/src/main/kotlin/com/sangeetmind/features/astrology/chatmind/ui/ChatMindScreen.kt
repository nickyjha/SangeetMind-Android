package com.sangeetmind.features.astrology.chatmind.ui

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.sangeetmind.core.ui.components.ErrorCard
import com.sangeetmind.core.ui.text.MarkdownText
import com.sangeetmind.features.astrology.R
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.sangeetmind.features.astrology.chatmind.CHAT_PERSONAS
import com.sangeetmind.features.astrology.chatmind.ChatMessage
import com.sangeetmind.features.astrology.chatmind.ChatMindViewModel
import com.sangeetmind.features.astrology.chatmind.ChatRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMindScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChatMindViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.chatmind_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.rahu.copy(alpha = 0.14f),
                    titleContentColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.rahu,
                    navigationIconContentColor = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.rahu
                )
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                // Keep the input above the system navigation bar and the keyboard.
                Column(modifier = Modifier.navigationBarsPadding().imePadding()) {
                    // Who answers: the persona changes the tone, the tier changes the model.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.chatmind_persona_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        CHAT_PERSONAS.forEach { persona ->
                            FilterChip(
                                selected = uiState.persona == persona,
                                onClick = { viewModel.setPersona(persona) },
                                enabled = !uiState.isSending,
                                label = { Text(stringResource(personaNameRes(persona))) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.chatmind_advanced_title), style = MaterialTheme.typography.labelLarge)
                            Text(
                                stringResource(R.string.chatmind_advanced_desc),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.advancedAnalysis,
                            onCheckedChange = viewModel::setAdvancedAnalysis,
                            enabled = !uiState.isSending
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.input,
                            onValueChange = viewModel::onInputChange,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(stringResource(R.string.chatmind_input_placeholder)) },
                            singleLine = true,
                            enabled = !uiState.isSending
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = viewModel::send, enabled = !uiState.isSending) {
                            if (uiState.isSending) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Send, contentDescription = stringResource(R.string.chatmind_send))
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.error != null) {
                ErrorCard(
                    message = uiState.error,
                    onRetry = if (uiState.failedQuestion != null) viewModel::retry else null,
                    modifier = Modifier.padding(12.dp)
                )
            }
            if (uiState.messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.chatmind_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.messages) { message -> ChatBubble(message) }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == ChatRole.USER
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                val badge = listOfNotNull(
                    message.persona?.let { stringResource(personaNameRes(it)) },
                    if (message.tier == "advanced") stringResource(R.string.chatmind_tier_badge_advanced) else null
                ).joinToString(" · ")
                if (badge.isNotEmpty()) {
                    Text(
                        badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = com.sangeetmind.core.ui.theme.LocalGrahaColors.current.rahu,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                if (isUser) {
                    Text(message.text)
                } else {
                    MarkdownText(message.text, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

private fun personaNameRes(persona: String): Int = when (persona) {
    "counsellor" -> R.string.chatmind_persona_counsellor
    "analyst" -> R.string.chatmind_persona_analyst
    "friend" -> R.string.chatmind_persona_friend
    else -> R.string.chatmind_persona_pandit
}

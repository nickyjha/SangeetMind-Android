package com.sangeetmind.features.astrology.kundli.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.features.astrology.kundli.KundliListViewModel
import com.sangeetmind.libs.models.Kundli

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KundliListScreen(
    onNavigateBack: () -> Unit,
    onAddKundli: () -> Unit,
    viewModel: KundliListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Kundlis") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddKundli) {
                Icon(Icons.Default.Add, contentDescription = "Add kundli")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading && uiState.kundlis.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.kundlis.isEmpty() -> {
                    Text(
                        text = "No kundlis yet. Tap + to add one.",
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.kundlis, key = { it.id }) { kundli ->
                            KundliCard(
                                kundli = kundli,
                                onSetPrimary = { viewModel.setPrimary(kundli.id) },
                                onDelete = { viewModel.delete(kundli.id) }
                            )
                        }
                    }
                }
            }

            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                ) { Text(uiState.error!!) }
            }
        }
    }
}

@Composable
private fun KundliCard(
    kundli: Kundli,
    onSetPrimary: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (kundli.isPrimary) Icons.Default.Star else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (kundli.isPrimary) "Primary" else "Not primary",
                tint = if (kundli.isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = kundli.fullName?.takeIf { it.isNotBlank() } ?: "Untitled kundli",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${kundli.birthDate} · ${kundli.birthTime} · ${kundli.birthPlace}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!kundli.isPrimary) {
                IconButton(onClick = onSetPrimary) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Set as primary")
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

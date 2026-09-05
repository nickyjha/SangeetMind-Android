package com.sangeetmind.features.astrology.dashboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.features.astrology.dashboard.DashboardViewModel

data class DashboardDestination(val label: String, val icon: ImageVector, val onClick: () -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenKundliList: () -> Unit,
    onOpenKundliOnboarding: () -> Unit,
    onOpenHoroscope: () -> Unit,
    onOpenPanchang: () -> Unit,
    onOpenMuhurat: () -> Unit,
    onOpenMatch: () -> Unit,
    onOpenNumerology: () -> Unit,
    onOpenInterpretation: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SangeetMind") },
                actions = {
                    IconButton(onClick = onOpenKundliList) {
                        Icon(Icons.Default.People, contentDescription = "My kundlis")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.profile != null && uiState.primaryKundli != null -> {
                    ProfileSummaryCard(
                        name = uiState.primaryKundli!!.fullName,
                        moonSign = uiState.profile!!.moonSign,
                        lagna = uiState.profile!!.lagna,
                        nakshatra = uiState.profile!!.nakshatra,
                        currentMahadasha = uiState.profile!!.currentMahadasha,
                        astroMood = uiState.profile!!.astroMood
                    )
                }
                uiState.hasNoKundlis -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Create your kundli", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add your birth details to unlock your horoscope, panchang, and full reading.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onOpenKundliOnboarding) {
                                Text("Get started")
                            }
                        }
                    }
                }
                uiState.error != null -> {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(
                            text = uiState.error!!,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            if (!uiState.hasNoKundlis) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Explore", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                val destinations = listOf(
                    DashboardDestination("Horoscope", Icons.Default.Insights, onOpenHoroscope),
                    DashboardDestination("Panchang", Icons.Default.CalendarMonth, onOpenPanchang),
                    DashboardDestination("Muhurat", Icons.Default.Schedule, onOpenMuhurat),
                    DashboardDestination("Match", Icons.Default.Favorite, onOpenMatch),
                    DashboardDestination("Numerology", Icons.Default.Tag, onOpenNumerology),
                    DashboardDestination("Full Reading", Icons.Default.AutoStories, onOpenInterpretation)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(destinations) { destination ->
                        DestinationCard(destination)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSummaryCard(
    name: String?,
    moonSign: String,
    lagna: String,
    nakshatra: String,
    currentMahadasha: String,
    astroMood: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = name?.takeIf { it.isNotBlank() } ?: "Your kundli",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow("Moon sign", moonSign)
            SummaryRow("Lagna", lagna)
            if (nakshatra.isNotBlank()) SummaryRow("Nakshatra", nakshatra)
            if (currentMahadasha.isNotBlank()) SummaryRow("Current dasha", currentMahadasha)
            if (astroMood.isNotBlank()) SummaryRow("Today's mood", astroMood)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DestinationCard(destination: DashboardDestination) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        onClick = destination.onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(destination.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(destination.label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

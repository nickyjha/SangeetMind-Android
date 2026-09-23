package com.sangeetmind.features.raaglibrary.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.features.raaglibrary.R
import com.sangeetmind.features.raaglibrary.RaagListViewModel
import com.sangeetmind.libs.models.Mood
import com.sangeetmind.libs.models.Raag
import com.sangeetmind.libs.models.TimeOfDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaagListScreen(
    viewModel: RaagListViewModel = hiltViewModel(),
    onRaagClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.raaglib_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onClearClick = viewModel::clearSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    ErrorView(
                        message = uiState.error ?: stringResource(CoreR.string.common_error_generic),
                        onRetry = viewModel::loadRaags
                    )
                }
                uiState.filteredRaags.isEmpty() -> {
                    EmptyView(
                        message = if (uiState.searchQuery.isNotEmpty()) {
                            stringResource(R.string.raaglib_no_results_fmt, uiState.searchQuery)
                        } else {
                            stringResource(R.string.raaglib_no_raags)
                        }
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.filteredRaags,
                            key = { it.id }
                        ) { raag ->
                            RaagListItem(
                                raag = raag,
                                onClick = { onRaagClick(raag.id) },
                                onFavoriteClick = {
                                    viewModel.toggleFavorite(raag.id, !raag.isFavorite)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(stringResource(R.string.raaglib_search_hint)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(CoreR.string.common_search)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.raaglib_clear_search)
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
fun RaagListItem(
    raag: Raag,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = raag.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val nameHindi = raag.nameHindi
                if (nameHindi != null) {
                    Text(
                        text = nameHindi,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = raag.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    raag.timeOfDay?.let { timeOfDay ->
                        AssistChip(
                            onClick = {},
                            label = { Text(timeOfDayLabel(timeOfDay), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                    raag.mood?.let { mood ->
                        AssistChip(
                            onClick = {},
                            label = { Text(moodLabel(mood), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (raag.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(
                            if (raag.isFavorite) R.string.raaglib_remove_favorite else R.string.raaglib_add_favorite
                        ),
                        tint = if (raag.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { /* TODO: Play raag */ }) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.raaglib_play)
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(CoreR.string.common_retry))
        }
    }
}

@Composable
private fun timeOfDayLabel(timeOfDay: TimeOfDay): String = stringResource(
    when (timeOfDay) {
        TimeOfDay.MORNING -> R.string.raaglib_time_morning
        TimeOfDay.AFTERNOON -> R.string.raaglib_time_afternoon
        TimeOfDay.EVENING -> R.string.raaglib_time_evening
        TimeOfDay.NIGHT -> R.string.raaglib_time_night
        TimeOfDay.ANYTIME -> R.string.raaglib_time_anytime
    }
)

@Composable
private fun moodLabel(mood: Mood): String = stringResource(
    when (mood) {
        Mood.PEACEFUL -> R.string.raaglib_mood_peaceful
        Mood.ENERGETIC -> R.string.raaglib_mood_energetic
        Mood.DEVOTIONAL -> R.string.raaglib_mood_devotional
        Mood.ROMANTIC -> R.string.raaglib_mood_romantic
        Mood.MELANCHOLIC -> R.string.raaglib_mood_melancholic
        Mood.JOYFUL -> R.string.raaglib_mood_joyful
    }
)

@Composable
fun EmptyView(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


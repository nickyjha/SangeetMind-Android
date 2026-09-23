package com.sangeetmind.features.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.LanguagePickerDialog
import com.sangeetmind.core.ui.language.LocalAppLanguage
import com.sangeetmind.features.settings.DarkModePreference
import com.sangeetmind.features.settings.R
import com.sangeetmind.features.settings.SettingsViewModel
import com.sangeetmind.libs.models.PlaybackQuality

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
                .verticalScroll(rememberScrollState())
        ) {
            // General Section
            SettingsSection(title = stringResource(R.string.settings_section_general)) {
                LanguageSettingItem()

                DarkModeSettingItem(
                    currentPreference = uiState.darkMode,
                    onPreferenceChange = viewModel::setDarkMode
                )
            }

            Divider()

            // Playback Section
            SettingsSection(title = stringResource(R.string.settings_section_playback)) {
                PlaybackQualitySettingItem(
                    currentQuality = uiState.playbackQuality,
                    onQualityChange = viewModel::setPlaybackQuality
                )

                SwitchSettingItem(
                    title = stringResource(R.string.settings_autoplay_title),
                    description = stringResource(R.string.settings_autoplay_desc),
                    icon = Icons.Default.PlayArrow,
                    checked = uiState.autoPlayNext,
                    onCheckedChange = viewModel::setAutoPlayNext
                )

                SwitchSettingItem(
                    title = stringResource(R.string.settings_lyrics_title),
                    description = stringResource(R.string.settings_lyrics_desc),
                    icon = Icons.Default.Subtitles,
                    checked = uiState.showLyrics,
                    onCheckedChange = viewModel::setShowLyrics
                )
            }

            Divider()

            // Downloads Section
            SettingsSection(title = stringResource(R.string.settings_section_downloads)) {
                SwitchSettingItem(
                    title = stringResource(R.string.settings_wifi_only_title),
                    description = stringResource(R.string.settings_wifi_only_desc),
                    icon = Icons.Default.Wifi,
                    checked = uiState.downloadOnWifiOnly,
                    onCheckedChange = viewModel::setDownloadOnWifiOnly
                )

                ActionSettingItem(
                    title = stringResource(R.string.settings_clear_downloads_title),
                    description = stringResource(R.string.settings_clear_downloads_desc),
                    icon = Icons.Default.Delete,
                    onClick = viewModel::clearDownloads
                )
            }

            Divider()

            // Notifications Section
            SettingsSection(title = stringResource(R.string.settings_section_notifications)) {
                SwitchSettingItem(
                    title = stringResource(R.string.settings_notifications_title),
                    description = stringResource(R.string.settings_notifications_desc),
                    icon = Icons.Default.Notifications,
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = viewModel::setNotificationsEnabled
                )
            }

            Divider()

            // Storage Section
            SettingsSection(title = stringResource(R.string.settings_section_storage)) {
                ActionSettingItem(
                    title = stringResource(R.string.settings_clear_cache_title),
                    description = stringResource(R.string.settings_clear_cache_desc),
                    icon = Icons.Default.CleaningServices,
                    onClick = viewModel::clearCache
                )
            }

            Divider()

            // About Section
            SettingsSection(title = stringResource(R.string.settings_section_about)) {
                ActionSettingItem(
                    title = stringResource(R.string.settings_version_title),
                    description = "1.0.0",
                    icon = Icons.Default.Info,
                    onClick = {}
                )

                ActionSettingItem(
                    title = stringResource(R.string.settings_privacy_title),
                    description = stringResource(R.string.settings_privacy_desc),
                    icon = Icons.Default.PrivacyTip,
                    onClick = { /* TODO: Open privacy policy */ }
                )

                ActionSettingItem(
                    title = stringResource(R.string.settings_terms_title),
                    description = stringResource(R.string.settings_terms_desc),
                    icon = Icons.Default.Description,
                    onClick = { /* TODO: Open terms */ }
                )

                ActionSettingItem(
                    title = stringResource(R.string.settings_licenses_title),
                    description = stringResource(R.string.settings_licenses_desc),
                    icon = Icons.Default.Code,
                    onClick = { /* TODO: Show licenses */ }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        content()
    }
}

/**
 * Shows the app's current display language and opens the shared language chooser.
 * Backed by the real language layer ([LocalAppLanguage] / `LocalLanguageSwitcher`),
 * so the choice persists and re-renders the whole app.
 */
@Composable
fun LanguageSettingItem() {
    var showDialog by remember { mutableStateOf(false) }
    val language = LocalAppLanguage.current

    ActionSettingItem(
        title = stringResource(CoreR.string.common_language),
        description = language.nativeName,
        icon = Icons.Default.Language,
        onClick = { showDialog = true }
    )

    if (showDialog) {
        LanguagePickerDialog(onDismiss = { showDialog = false })
    }
}

@Composable
fun DarkModeSettingItem(
    currentPreference: DarkModePreference,
    onPreferenceChange: (DarkModePreference) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    ActionSettingItem(
        title = stringResource(R.string.settings_dark_mode),
        description = stringResource(
            when (currentPreference) {
                DarkModePreference.LIGHT -> R.string.settings_theme_light
                DarkModePreference.DARK -> R.string.settings_theme_dark
                DarkModePreference.SYSTEM -> R.string.settings_theme_system
            }
        ),
        icon = Icons.Default.DarkMode,
        onClick = { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.settings_dark_mode)) },
            text = {
                Column {
                    RadioButtonItem(
                        text = stringResource(R.string.settings_theme_light),
                        selected = currentPreference == DarkModePreference.LIGHT,
                        onClick = {
                            onPreferenceChange(DarkModePreference.LIGHT)
                            showDialog = false
                        }
                    )
                    RadioButtonItem(
                        text = stringResource(R.string.settings_theme_dark),
                        selected = currentPreference == DarkModePreference.DARK,
                        onClick = {
                            onPreferenceChange(DarkModePreference.DARK)
                            showDialog = false
                        }
                    )
                    RadioButtonItem(
                        text = stringResource(R.string.settings_theme_system),
                        selected = currentPreference == DarkModePreference.SYSTEM,
                        onClick = {
                            onPreferenceChange(DarkModePreference.SYSTEM)
                            showDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(CoreR.string.common_cancel))
                }
            }
        )
    }
}

@Composable
fun PlaybackQualitySettingItem(
    currentQuality: PlaybackQuality,
    onQualityChange: (PlaybackQuality) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    ActionSettingItem(
        title = stringResource(R.string.settings_playback_quality),
        description = stringResource(
            when (currentQuality) {
                PlaybackQuality.LOW -> R.string.settings_quality_low
                PlaybackQuality.MEDIUM -> R.string.settings_quality_medium
                PlaybackQuality.HIGH -> R.string.settings_quality_high
            }
        ),
        icon = Icons.Default.HighQuality,
        onClick = { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.settings_playback_quality)) },
            text = {
                Column {
                    RadioButtonItem(
                        text = stringResource(R.string.settings_quality_low),
                        selected = currentQuality == PlaybackQuality.LOW,
                        onClick = {
                            onQualityChange(PlaybackQuality.LOW)
                            showDialog = false
                        }
                    )
                    RadioButtonItem(
                        text = stringResource(R.string.settings_quality_medium),
                        selected = currentQuality == PlaybackQuality.MEDIUM,
                        onClick = {
                            onQualityChange(PlaybackQuality.MEDIUM)
                            showDialog = false
                        }
                    )
                    RadioButtonItem(
                        text = stringResource(R.string.settings_quality_high),
                        selected = currentQuality == PlaybackQuality.HIGH,
                        onClick = {
                            onQualityChange(PlaybackQuality.HIGH)
                            showDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(CoreR.string.common_cancel))
                }
            }
        )
    }
}

@Composable
fun SwitchSettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun ActionSettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RadioButtonItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}


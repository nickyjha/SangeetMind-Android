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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.features.settings.DarkModePreference
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
                title = { Text("Settings") },
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
            SettingsSection(title = "General") {
                LanguageSettingItem(
                    currentLanguage = uiState.language,
                    onLanguageChange = viewModel::setLanguage
                )
                
                DarkModeSettingItem(
                    currentPreference = uiState.darkMode,
                    onPreferenceChange = viewModel::setDarkMode
                )
            }

            Divider()

            // Playback Section
            SettingsSection(title = "Playback") {
                PlaybackQualitySettingItem(
                    currentQuality = uiState.playbackQuality,
                    onQualityChange = viewModel::setPlaybackQuality
                )
                
                SwitchSettingItem(
                    title = "Auto-play next",
                    description = "Automatically play next track in queue",
                    icon = Icons.Default.PlayArrow,
                    checked = uiState.autoPlayNext,
                    onCheckedChange = viewModel::setAutoPlayNext
                )
                
                SwitchSettingItem(
                    title = "Show lyrics",
                    description = "Display lyrics when available",
                    icon = Icons.Default.Subtitles,
                    checked = uiState.showLyrics,
                    onCheckedChange = viewModel::setShowLyrics
                )
            }

            Divider()

            // Downloads Section
            SettingsSection(title = "Downloads") {
                SwitchSettingItem(
                    title = "Download on Wi-Fi only",
                    description = "Only download content when connected to Wi-Fi",
                    icon = Icons.Default.Wifi,
                    checked = uiState.downloadOnWifiOnly,
                    onCheckedChange = viewModel::setDownloadOnWifiOnly
                )
                
                ActionSettingItem(
                    title = "Clear downloads",
                    description = "Remove all downloaded content",
                    icon = Icons.Default.Delete,
                    onClick = viewModel::clearDownloads
                )
            }

            Divider()

            // Notifications Section
            SettingsSection(title = "Notifications") {
                SwitchSettingItem(
                    title = "Enable notifications",
                    description = "Receive updates and reminders",
                    icon = Icons.Default.Notifications,
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = viewModel::setNotificationsEnabled
                )
            }

            Divider()

            // Storage Section
            SettingsSection(title = "Storage") {
                ActionSettingItem(
                    title = "Clear cache",
                    description = "Free up storage space",
                    icon = Icons.Default.CleaningServices,
                    onClick = viewModel::clearCache
                )
            }

            Divider()

            // About Section
            SettingsSection(title = "About") {
                ActionSettingItem(
                    title = "Version",
                    description = "1.0.0",
                    icon = Icons.Default.Info,
                    onClick = {}
                )
                
                ActionSettingItem(
                    title = "Privacy Policy",
                    description = "View our privacy policy",
                    icon = Icons.Default.PrivacyTip,
                    onClick = { /* TODO: Open privacy policy */ }
                )
                
                ActionSettingItem(
                    title = "Terms of Service",
                    description = "View terms and conditions",
                    icon = Icons.Default.Description,
                    onClick = { /* TODO: Open terms */ }
                )
                
                ActionSettingItem(
                    title = "Open Source Licenses",
                    description = "View third-party licenses",
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

@Composable
fun LanguageSettingItem(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    ActionSettingItem(
        title = "Language",
        description = when (currentLanguage) {
            "en" -> "English"
            "hi" -> "हिंदी (Hindi)"
            else -> currentLanguage
        },
        icon = Icons.Default.Language,
        onClick = { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Language") },
            text = {
                Column {
                    RadioButtonItem(
                        text = "English",
                        selected = currentLanguage == "en",
                        onClick = {
                            onLanguageChange("en")
                            showDialog = false
                        }
                    )
                    RadioButtonItem(
                        text = "हिंदी (Hindi)",
                        selected = currentLanguage == "hi",
                        onClick = {
                            onLanguageChange("hi")
                            showDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DarkModeSettingItem(
    currentPreference: DarkModePreference,
    onPreferenceChange: (DarkModePreference) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    ActionSettingItem(
        title = "Dark mode",
        description = when (currentPreference) {
            DarkModePreference.LIGHT -> "Light"
            DarkModePreference.DARK -> "Dark"
            DarkModePreference.SYSTEM -> "System default"
        },
        icon = Icons.Default.DarkMode,
        onClick = { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Dark Mode") },
            text = {
                Column {
                    RadioButtonItem(
                        text = "Light",
                        selected = currentPreference == DarkModePreference.LIGHT,
                        onClick = {
                            onPreferenceChange(DarkModePreference.LIGHT)
                            showDialog = false
                        }
                    )
                    RadioButtonItem(
                        text = "Dark",
                        selected = currentPreference == DarkModePreference.DARK,
                        onClick = {
                            onPreferenceChange(DarkModePreference.DARK)
                            showDialog = false
                        }
                    )
                    RadioButtonItem(
                        text = "System default",
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
                    Text("Cancel")
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
        title = "Playback quality",
        description = when (currentQuality) {
            PlaybackQuality.LOW -> "Low (Save data)"
            PlaybackQuality.MEDIUM -> "Medium"
            PlaybackQuality.HIGH -> "High (Best quality)"
        },
        icon = Icons.Default.HighQuality,
        onClick = { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Playback Quality") },
            text = {
                Column {
                    RadioButtonItem(
                        text = "Low (Save data)",
                        selected = currentQuality == PlaybackQuality.LOW,
                        onClick = {
                            onQualityChange(PlaybackQuality.LOW)
                            showDialog = false
                        }
                    )
                    RadioButtonItem(
                        text = "Medium",
                        selected = currentQuality == PlaybackQuality.MEDIUM,
                        onClick = {
                            onQualityChange(PlaybackQuality.MEDIUM)
                            showDialog = false
                        }
                    )
                    RadioButtonItem(
                        text = "High (Best quality)",
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
                    Text("Cancel")
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


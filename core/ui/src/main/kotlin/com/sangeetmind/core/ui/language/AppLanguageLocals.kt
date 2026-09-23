package com.sangeetmind.core.ui.language

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sangeetmind.core.common.language.AppLanguage
import com.sangeetmind.core.ui.R

/** The language the UI is currently displayed in. Installed by MainActivity. */
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.DEFAULT }

/** How a screen asks to switch language. Installed by MainActivity; no DI needed in features. */
val LocalLanguageSwitcher = staticCompositionLocalOf<(AppLanguage) -> Unit> { {} }

/**
 * A top-app-bar action: globe icon that opens a language chooser listing every
 * [AppLanguage]. Drop it into any `TopAppBar(actions = { ... })`.
 */
@Composable
fun LanguagePickerAction(modifier: Modifier = Modifier) {
    var open by rememberSaveable { mutableStateOf(false) }
    IconButton(onClick = { open = true }, modifier = modifier) {
        Icon(Icons.Default.Language, contentDescription = stringResource(R.string.common_language))
    }
    if (open) {
        LanguagePickerDialog(onDismiss = { open = false })
    }
}

@Composable
fun LanguagePickerDialog(onDismiss: () -> Unit) {
    val current = LocalAppLanguage.current
    val switch = LocalLanguageSwitcher.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.common_select_language)) },
        text = {
            Column {
                AppLanguage.entries.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                switch(language)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = language == current, onClick = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(language.nativeName, style = MaterialTheme.typography.bodyLarge)
                            if (language.nativeName != language.englishName) {
                                Text(
                                    language.englishName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_close)) }
        }
    )
}

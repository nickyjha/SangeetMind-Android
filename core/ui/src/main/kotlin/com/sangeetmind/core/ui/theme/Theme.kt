package com.sangeetmind.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * The Navagraha System's per-feature accent colors, resolved for the *current* theme.
 * Each graha has a bright dark-theme tone and a darkened light-theme tone (see the
 * "...Deep" constants in Color.kt) — read [surya], [chandra], etc. from
 * [LocalGrahaColors] rather than the raw Color.kt constants, so a screen never has to
 * know which theme is active to stay legible in both.
 */
data class GrahaColors(
    val surya: Color,
    val chandra: Color,
    val mangala: Color,
    val budha: Color,
    val guru: Color,
    val shukra: Color,
    val shani: Color,
    val rahu: Color
)

private val DarkGrahaColors = GrahaColors(
    surya = GrahaSurya,
    chandra = GrahaChandra,
    mangala = GrahaMangala,
    budha = GrahaBudha,
    guru = GrahaGuru,
    shukra = GrahaShukra,
    shani = GrahaShani,
    rahu = GrahaRahu
)

private val LightGrahaColors = GrahaColors(
    surya = GrahaSuryaDeep,
    chandra = GrahaChandraDeep,
    mangala = GrahaMangalaDeep,
    budha = GrahaBudhaDeep,
    guru = GrahaGuruDeep,
    shukra = GrahaShukraDeep,
    shani = GrahaShaniDeep,
    rahu = GrahaRahuDeep
)

val LocalGrahaColors = staticCompositionLocalOf { DarkGrahaColors }

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

@Composable
fun SangeetMindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalGrahaColors provides if (darkTheme) DarkGrahaColors else LightGrahaColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}


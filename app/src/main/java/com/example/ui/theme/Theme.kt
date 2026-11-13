package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class FreegramPalette(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val accentBlue: Color = FreegramBlue,
    val heartRed: Color = FreegramHeartRed,
    val activeGreen: Color = FreegramActiveGreen,
    val isDark: Boolean,
)

val LocalFreegramPalette = staticCompositionLocalOf {
    FreegramPalette(
        background = LightBg,
        surface = LightSurface,
        surfaceVariant = LightSurfaceVariant,
        textPrimary = LightTextPrimary,
        textSecondary = LightTextSecondary,
        divider = LightDivider,
        isDark = false,
    )
}

private val FreegramDarkColorScheme = darkColorScheme(
    primary = FreegramBlue,
    onPrimary = Color.White,
    secondary = FreegramMagenta,
    onSecondary = Color.White,
    tertiary = FreegramAmber,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkDivider,
    error = FreegramHeartRed,
    onError = Color.White,
)

private val FreegramLightColorScheme = lightColorScheme(
    primary = FreegramBlue,
    onPrimary = Color.White,
    secondary = FreegramMagenta,
    onSecondary = Color.White,
    tertiary = FreegramAmber,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightDivider,
    error = FreegramHeartRed,
    onError = Color.White,
)

@Composable
fun FreegramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) FreegramDarkColorScheme else FreegramLightColorScheme
    val palette = if (darkTheme) {
        FreegramPalette(
            background = DarkBg,
            surface = DarkSurface,
            surfaceVariant = DarkSurfaceVariant,
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextSecondary,
            divider = DarkDivider,
            isDark = true,
        )
    } else {
        FreegramPalette(
            background = LightBg,
            surface = LightSurface,
            surfaceVariant = LightSurfaceVariant,
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            divider = LightDivider,
            isDark = false,
        )
    }

    CompositionLocalProvider(LocalFreegramPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FreegramTypography,
            content = content,
        )
    }
}

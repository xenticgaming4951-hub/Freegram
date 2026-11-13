package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val FreegramBlue = Color(0xFF0095F6)
val FreegramBluePressed = Color(0xFF1877F2)
val FreegramHeartRed = Color(0xFFFF3040)
val FreegramActiveGreen = Color(0xFF1ED760)
val FreegramVerifiedBlue = Color(0xFF0095F6)

val FreegramAmber = Color(0xFFF58529)
val FreegramMagenta = Color(0xFFDD2A7B)
val FreegramPurple = Color(0xFF8134AF)
val FreegramIndigo = Color(0xFF515BD4)

val FreegramBrandGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFF58529),
        Color(0xFFDD2A7B),
        Color(0xFF8134AF),
        Color(0xFF515BD4),
    )
)

val FreegramStoryRingGradient = Brush.sweepGradient(
    colors = listOf(
        Color(0xFFF58529),
        Color(0xFFDD2A7B),
        Color(0xFF8134AF),
        Color(0xFFFEDA77),
        Color(0xFFF58529),
    )
)

val FreegramLiveGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFC913B9),
        Color(0xFFF9373F),
        Color(0xFFFECD00),
    )
)

// Light palette (crisp Instagram light)
val LightBg = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFFAFAFA)
val LightSurfaceVariant = Color(0xFFEFEFEF)
val LightTextPrimary = Color(0xFF0D0D0F)
val LightTextSecondary = Color(0xFF737373)
val LightDivider = Color(0xFFDBDBDB)

// Dark palette (OLED Instagram dark)
val DarkBg = Color(0xFF000000)
val DarkSurface = Color(0xFF121212)
val DarkSurfaceVariant = Color(0xFF262626)
val DarkTextPrimary = Color(0xFFFAFAFA)
val DarkTextSecondary = Color(0xFFA8A8A8)
val DarkDivider = Color(0xFF262626)

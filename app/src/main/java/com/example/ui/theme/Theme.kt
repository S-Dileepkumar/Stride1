package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Color.Black,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = EmeraldContainer,
    secondary = SpeechCoral,
    onSecondary = Color.White,
    secondaryContainer = OnSpeechCoralContainer,
    onSecondaryContainer = SpeechCoralContainer,
    tertiary = AccentIndigo,
    background = HighContrastDarkCanvas,
    surface = HighContrastDarkSurface,
    surfaceVariant = HighContrastDarkCard,
    onBackground = HighContrastDarkText,
    onSurface = HighContrastDarkText,
    onSurfaceVariant = Color(0xFFC1C9BF),
    outline = Color(0xFF414941),
    outlineVariant = Color(0xFF2D322C)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = SpeechCoral,
    onSecondary = Color.White,
    secondaryContainer = SpeechCoralContainer,
    onSecondaryContainer = OnSpeechCoralContainer,
    tertiary = AccentIndigo,
    background = NeutralLightCanvas,
    surface = NeutralLightSurface,
    surfaceVariant = NeutralLightCard,
    onBackground = Color(0xFF191C19),
    onSurface = Color(0xFF191C19),
    onSurfaceVariant = Color(0xFF414941),
    outline = CleanMinimalBorder,
    outlineVariant = CleanMinimalBorder
)

@Composable
fun StrideAndSpeakTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false so custom theme colors stand out consistently
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

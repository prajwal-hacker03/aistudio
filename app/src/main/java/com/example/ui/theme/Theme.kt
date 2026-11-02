package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF22D3EE),          // Cyan #22D3EE
    onPrimary = Color(0xFF0B1120),
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color(0xFFE2E8F0),

    secondary = Color(0xFF8B5CF6),        // Purple #8B5CF6
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF2E1065),
    onSecondaryContainer = Color(0xFFDDD6FE),

    tertiary = Color(0xFF3B82F6),         // Blue #3B82F6
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF1E3A8A),
    onTertiaryContainer = Color(0xFFBFDBFE),

    background = Color(0xFF0B1120),       // Deep Navy #0B1120
    onBackground = Color(0xFFF8FAFC),

    surface = Color(0xFF0F172A),          // Midnight Slate
    onSurface = Color(0xFFF1F5F9),

    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),

    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B),

    error = Color(0xFFF43F5E),
    onError = Color(0xFFFFFFFF)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),          // Deep Sky Blue
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),

    secondary = Color(0xFF7C3AED),        // Rich Purple
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF5B21B6),

    tertiary = Color(0xFF2563EB),         // Royal Blue
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDBEAFE),
    onTertiaryContainer = Color(0xFF1E40AF),

    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),

    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),

    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),

    error = Color(0xFFE11D48),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun GradeTrackTheme(
    darkTheme: Boolean = true, // Default to premium dark theme per spec
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

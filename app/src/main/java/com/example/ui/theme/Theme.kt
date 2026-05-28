package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GeometricColorScheme = lightColorScheme(
    primary = GeometricPrimary,
    onPrimary = GeometricOnPrimary,
    secondary = GeometricMediumBlue,
    onSecondary = GeometricLightBlueAccent,
    tertiary = GeometricDarkBlue,
    background = GeometricBg,
    onBackground = GeometricTextPrimary,
    surface = GeometricSurface,
    onSurface = GeometricTextPrimary,
    surfaceVariant = GeometricMutedBlue,
    onSurfaceVariant = GeometricTextSecondary,
    outline = GeometricBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Force consistency to align perfectly with Geometric Balance design
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = GeometricColorScheme,
        typography = Typography,
        content = content
    )
}

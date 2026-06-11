package com.meminzazo.drawar.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DrawARColorScheme = darkColorScheme(
    primary = Amber400,
    onPrimary = BackgroundDark,
    secondary = Amber200,
    onSecondary = BackgroundDark,
    background = BackgroundDark,
    onBackground = OnSurface,
    surface = SurfaceDark,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceMuted,
    error = ErrorRed,
)

@Composable
fun DrawARTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DrawARColorScheme,
        typography  = DrawARTypography,
        content     = content
    )
}
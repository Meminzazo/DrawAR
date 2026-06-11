package com.meminzazo.drawar.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

enum class WindowSizeClass {
    CompactPortrait,   // teléfono portrait  → barra inferior
    CompactLandscape,  // teléfono landscape → panel lateral compacto
    Expanded           // tablet             → panel lateral expandido
}

@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val config = LocalConfiguration.current
    val widthDp = config.screenWidthDp
    val isLandscape = config.orientation ==
            android.content.res.Configuration.ORIENTATION_LANDSCAPE

    return when {
        widthDp >= 840              -> WindowSizeClass.Expanded
        isLandscape                 -> WindowSizeClass.CompactLandscape
        else                        -> WindowSizeClass.CompactPortrait
    }
}
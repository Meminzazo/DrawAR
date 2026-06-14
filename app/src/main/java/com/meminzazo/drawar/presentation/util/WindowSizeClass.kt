package com.meminzazo.drawar.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration

enum class WindowSizeClass {
    CompactPortrait,
    CompactLandscape,
    Expanded
}

@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val config = LocalConfiguration.current
    val widthDp = config.screenWidthDp
    val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE

    return when {
        widthDp >= 840 -> WindowSizeClass.Expanded
        isLandscape    -> WindowSizeClass.CompactLandscape
        else           -> WindowSizeClass.CompactPortrait
    }
}
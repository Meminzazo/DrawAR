package com.meminzazo.drawar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.systemBars
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.meminzazo.drawar.presentation.navigation.DrawARNavGraph
import com.meminzazo.drawar.presentation.theme.DrawARTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()
        setContent {
            DrawARTheme {
                DrawARNavGraph()
            }
        }
    }

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // Configura el comportamiento: las barras aparecen al deslizar y se ocultan solas
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Oculta tanto la barra de estado (arriba) como la de navegación (abajo)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}
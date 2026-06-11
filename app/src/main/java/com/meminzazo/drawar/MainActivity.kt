package com.meminzazo.drawar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.meminzazo.drawar.presentation.navigation.DrawARNavGraph
import com.meminzazo.drawar.presentation.theme.DrawARTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrawARTheme {
                DrawARNavGraph()
            }
        }
    }
}
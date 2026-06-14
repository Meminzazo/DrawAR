package com.meminzazo.drawar.presentation.screen.simple

import android.graphics.Bitmap
import android.net.Uri

data class SimpleUiState(
    // Imagen
    val imageUri: Uri? = null,

    // Parámetros de overlay
    val opacity: Float = 0.5f,
    val offsetXPercent: Float = 0f,
    val offsetYPercent: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,

    // Control de UI
    val isLocked: Boolean = false,
    val isTorchOn: Boolean = false,
    val controlsVisible: Boolean = true,
    val isLoading: Boolean = true,
    val error: String? = null,

    //Deteccion de bordes
    val edgeBitmap: Bitmap? = null,
    val isEdgeModeActive: Boolean = false,
    val isProcessingEdges: Boolean = false
)
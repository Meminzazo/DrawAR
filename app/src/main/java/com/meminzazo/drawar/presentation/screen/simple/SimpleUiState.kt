package com.meminzazo.drawar.presentation.screen.simple

import android.graphics.Bitmap
import android.net.Uri

data class SimpleUiState(
    val imageUri: Uri? = null,
    val opacity: Float = 0.5f,
    val offsetXPx: Float = 0f,
    val offsetYPx: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val isLocked: Boolean = false,
    val isTorchOn: Boolean = false,
    val controlsVisible: Boolean = true,
    val isLoading: Boolean = true,
    val error: String? = null,
    val edgeBitmap: Bitmap? = null,
    val isEdgeModeActive: Boolean = false,
    val isProcessingEdges: Boolean = false
)
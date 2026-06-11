package com.meminzazo.drawar.domain.model

data class OverlayState(
    val imageUri: String? = null,
    val opacity: Float = 0.5f,
    val offsetXPercent: Float = 0f,   // posición en % del ancho
    val offsetYPercent: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val isLocked: Boolean = false,
    val isTorchOn: Boolean = false
)

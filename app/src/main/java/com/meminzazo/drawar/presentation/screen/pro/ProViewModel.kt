package com.meminzazo.drawar.presentation.screen.pro

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.ar.core.Anchor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel para el Modo Pro Manual.
 * Permite al usuario "forzar" la creación de un lienzo 3D y ajustarlo a mano.
 */
@HiltViewModel
class ProViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ProUiState())
    val uiState: StateFlow<ProUiState> = _uiState.asStateFlow()

    fun onImageSelected(uri: Uri) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    /**
     * Fija la imagen en la posición actual de la cámara.
     */
    fun onSetAnchor(anchor: Anchor) {
        _uiState.value.anchor?.detach()
        _uiState.update { it.copy(anchor = anchor, isFixed = true) }
    }

    /**
     * Libera la imagen para poder re-posicionarla.
     */
    fun onResetAnchor() {
        _uiState.value.anchor?.detach()
        _uiState.update { it.copy(anchor = null, isFixed = false) }
    }

    fun onOpacityChange(opacity: Float) {
        _uiState.update { it.copy(opacity = opacity) }
    }

    fun onScaleChange(scale: Float) {
        _uiState.update { it.copy(scaleFactor = scale) }
    }

    fun onRotationChange(rotation: Float) {
        _uiState.update { it.copy(rotationState = rotation) }
    }
}

data class ProUiState(
    val imageUri: Uri? = null,
    val anchor: Anchor? = null,
    val isFixed: Boolean = false,
    val opacity: Float = 0.5f,
    val rotationState: Float = 0f,
    val scaleFactor: Float = 1f
)

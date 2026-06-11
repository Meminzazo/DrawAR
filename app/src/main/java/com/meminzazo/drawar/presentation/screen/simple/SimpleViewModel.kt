package com.meminzazo.drawar.presentation.screen.simple

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meminzazo.drawar.domain.model.OverlayState
import com.meminzazo.drawar.domain.usecase.GetOverlayStateUseCase
import com.meminzazo.drawar.domain.usecase.ResetOverlayStateUseCase
import com.meminzazo.drawar.domain.usecase.SaveOverlayStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SimpleViewModel @Inject constructor(
    private val getOverlayState: GetOverlayStateUseCase,
    private val saveOverlayState: SaveOverlayStateUseCase,
    private val resetOverlayState: ResetOverlayStateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SimpleUiState())
    val uiState: StateFlow<SimpleUiState> = _uiState.asStateFlow()

    // Job para auto-hide de controles
    private var autoHideJob: Job? = null

    companion object {
        private const val AUTO_HIDE_DELAY_MS = 3000L
        private const val OPACITY_MIN = 0.05f
        private const val OPACITY_MAX = 1.0f
        private const val SCALE_MIN = 0.1f
        private const val SCALE_MAX = 5.0f
    }

    init {
        loadOverlayState()
    }

    // ─────────────────────────────────────────
    // Carga inicial desde DataStore
    // ─────────────────────────────────────────

    private fun loadOverlayState() {
        viewModelScope.launch {
            getOverlayState()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { domainState ->
                    _uiState.update { current ->
                        current.copy(
                            imageUri       = domainState.imageUri?.let { Uri.parse(it) },
                            opacity        = domainState.opacity,
                            offsetXPercent = domainState.offsetXPercent,
                            offsetYPercent = domainState.offsetYPercent,
                            scale          = domainState.scale,
                            rotation       = domainState.rotation,
                            flipHorizontal = domainState.flipHorizontal,
                            flipVertical   = domainState.flipVertical,
                            isLocked       = domainState.isLocked,
                            isTorchOn      = domainState.isTorchOn,
                            isLoading      = false
                        )
                    }
                }
        }
    }

    // ─────────────────────────────────────────
    // Imagen
    // ─────────────────────────────────────────

    fun onImageSelected(uri: Uri) {
        _uiState.update { it.copy(imageUri = uri) }
        persistState()
    }

    // ─────────────────────────────────────────
    // Parámetros de overlay
    // ─────────────────────────────────────────

    fun onOpacityChange(value: Float) {
        _uiState.update { it.copy(opacity = value.coerceIn(OPACITY_MIN, OPACITY_MAX)) }
        persistState()
    }

    fun onOffsetChange(xPercent: Float, yPercent: Float) {
        if (_uiState.value.isLocked) return
        _uiState.update { it.copy(offsetXPercent = xPercent, offsetYPercent = yPercent) }
        persistState()
    }

    fun onScaleChange(scale: Float) {
        if (_uiState.value.isLocked) return
        _uiState.update { it.copy(scale = scale.coerceIn(SCALE_MIN, SCALE_MAX)) }
        persistState()
    }

    fun onRotationChange(rotation: Float) {
        if (_uiState.value.isLocked) return
        _uiState.update { it.copy(rotation = rotation) }
        persistState()
    }

    fun onFlipHorizontal() {
        if (_uiState.value.isLocked) return
        _uiState.update { it.copy(flipHorizontal = !it.flipHorizontal) }
        persistState()
    }

    fun onFlipVertical() {
        if (_uiState.value.isLocked) return
        _uiState.update { it.copy(flipVertical = !it.flipVertical) }
        persistState()
    }

    // ─────────────────────────────────────────
    // Controles de estado
    // ─────────────────────────────────────────

    fun onToggleLock() {
        _uiState.update { it.copy(isLocked = !it.isLocked) }
        persistState()
    }

    fun onToggleTorch() {
        _uiState.update { it.copy(isTorchOn = !it.isTorchOn) }
        persistState()
    }

    fun onReset() {
        viewModelScope.launch {
            resetOverlayState()
            // isTorchOn no se persiste en reset — apagamos también en UI
            _uiState.update { current ->
                SimpleUiState(
                    imageUri  = current.imageUri,  // conservamos la imagen
                    isLoading = false
                )
            }
        }
    }

    // ─────────────────────────────────────────
    // Auto-hide de controles
    // ─────────────────────────────────────────

    fun onUserInteraction() {
        _uiState.update { it.copy(controlsVisible = true) }
        scheduleAutoHide()
    }

    fun onToggleControlsVisibility() {
        val nowVisible = !_uiState.value.controlsVisible
        _uiState.update { it.copy(controlsVisible = nowVisible) }
        if (nowVisible) scheduleAutoHide()
        else autoHideJob?.cancel()
    }

    private fun scheduleAutoHide() {
        autoHideJob?.cancel()
        autoHideJob = viewModelScope.launch {
            delay(AUTO_HIDE_DELAY_MS)
            _uiState.update { it.copy(controlsVisible = false) }
        }
    }

    // ─────────────────────────────────────────
    // Persistencia
    // ─────────────────────────────────────────

    private fun persistState() {
        val current = _uiState.value
        viewModelScope.launch {
            saveOverlayState(
                OverlayState(
                    imageUri       = current.imageUri?.toString(),
                    opacity        = current.opacity,
                    offsetXPercent = current.offsetXPercent,
                    offsetYPercent = current.offsetYPercent,
                    scale          = current.scale,
                    rotation       = current.rotation,
                    flipHorizontal = current.flipHorizontal,
                    flipVertical   = current.flipVertical,
                    isLocked       = current.isLocked,
                    isTorchOn      = current.isTorchOn
                )
            )
        }
    }
}
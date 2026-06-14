package com.meminzazo.drawar.presentation.screen.pro

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.net.Uri
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meminzazo.drawar.domain.model.ScreenPoint
import com.meminzazo.drawar.domain.usecase.ComputeHomographyUseCase
import com.meminzazo.drawar.domain.usecase.DetectSheetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel del Modo Pro.
 *
 * Responsabilidades:
 * - Recibir frames de CameraX y pasarlos al detector OpenCV.
 * - Gestionar la máquina de estados (SCANNING → DETECTED → TRACKING / MANUAL).
 * - Calcular la homografía cuando el usuario confirma la hoja.
 * - Actualizar el bitmap proyectado en cada frame durante TRACKING.
 * - Exponer [uiState] a la UI como StateFlow inmutable.
 */
@HiltViewModel
class ProViewModel @Inject constructor(
    private val detectSheet       : DetectSheetUseCase,
    private val computeHomography : ComputeHomographyUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProUiState())
    val uiState: StateFlow<ProUiState> = _uiState.asStateFlow()

    // Job del análisis de frame actual — lo cancelamos si llega uno nuevo
    // antes de que terminemos de procesar el anterior (backpressure manual)
    private var analysisJob: Job? = null

    // Bitmap de la imagen de referencia cargada en memoria
    // Lo guardamos para no re-decodificarlo en cada frame durante TRACKING
    private var referenceBitmap: Bitmap? = null

    companion object {
        private const val OPACITY_MIN = 0.05f
        private const val OPACITY_MAX = 1.0f
    }

    // ─────────────────────────────────────────
    // Dimensiones de pantalla
    // ─────────────────────────────────────────

    /**
     * La pantalla llama a esto en su primer frame para que el ViewModel
     * sepa el tamaño de salida que necesita para la homografía.
     */
    fun onScreenSizeAvailable(width: Int, height: Int) {
        if (_uiState.value.screenWidth == width &&
            _uiState.value.screenHeight == height) return
        _uiState.update { it.copy(screenWidth = width, screenHeight = height) }
    }

    // ─────────────────────────────────────────
    // Imagen de referencia
    // ─────────────────────────────────────────

    /**
     * Llamado cuando el usuario selecciona una imagen de la galería.
     * Resetea el estado de proyección pero mantiene la detección activa.
     */
    fun onImageSelected(uri: Uri, bitmap: Bitmap) {
        referenceBitmap = bitmap
        _uiState.update {
            it.copy(
                imageUri        = uri,
                projectedBitmap = null,
                // Si estábamos en TRACKING, volvemos a SCANNING para re-confirmar
                detectionState  = if (it.detectionState == ProDetectionState.TRACKING)
                    ProDetectionState.SCANNING else it.detectionState
            )
        }
    }

    // ─────────────────────────────────────────
    // Pipeline de análisis de frames
    // ─────────────────────────────────────────

    /**
     * Llamado por el ImageAnalysis de CameraX en cada frame.
     * Solo analiza en los estados SCANNING, DETECTED y TRACKING.
     *
     * Implementa backpressure simple: si el frame anterior aún no terminó
     * de procesarse, lo cancela y procesa el nuevo (siempre queremos
     * el frame más reciente, no una cola de frames viejos).
     *
     * @param bitmap Frame actual convertido a Bitmap por la cámara.
     */
    fun onFrameAvailable(bitmap: Bitmap) {
        val state = _uiState.value

        // En modo MANUAL no necesitamos analizar frames
        if (state.detectionState == ProDetectionState.MANUAL) return

        // Cancelar análisis anterior si aún estaba corriendo
        analysisJob?.cancel()

        analysisJob = viewModelScope.launch {
            runCatching { detectSheet(bitmap) }
                .onSuccess { result ->
                    if (result == null) {
                        // No se encontró hoja — si estábamos en DETECTED, volver a SCANNING
                        if (state.detectionState == ProDetectionState.DETECTED) {
                            _uiState.update {
                                it.copy(
                                    detectionState     = ProDetectionState.SCANNING,
                                    detectedCorners    = null,
                                    detectionConfidence= 0f
                                )
                            }
                        }
                        return@onSuccess
                    }

                    when (state.detectionState) {
                        ProDetectionState.SCANNING, ProDetectionState.DETECTED -> {
                            // Actualizar vértices y confianza
                            _uiState.update {
                                it.copy(
                                    detectedCorners     = result.corners,
                                    detectionConfidence = result.confidence,
                                    detectionState      = if (result.isReliable)
                                        ProDetectionState.DETECTED
                                    else
                                        ProDetectionState.SCANNING
                                )
                            }
                        }

                        ProDetectionState.TRACKING -> {
                            // En tracking: actualizar vértices Y recalcular homografía
                            _uiState.update { it.copy(detectedCorners = result.corners) }
                            recomputeProjection(result.corners)
                        }

                        ProDetectionState.MANUAL -> { /* no-op */ }
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    // ─────────────────────────────────────────
    // Acciones del usuario
    // ─────────────────────────────────────────

    /**
     * El usuario tocó "Confirmar" — calculamos la homografía inicial
     * y pasamos al estado TRACKING.
     */
    fun onConfirmDetection() {
        val state   = _uiState.value
        val corners = state.detectedCorners ?: return

        viewModelScope.launch {
            recomputeProjection(corners)
            _uiState.update { it.copy(detectionState = ProDetectionState.TRACKING) }
        }
    }

    /**
     * El usuario quiere ajustar los puntos a mano (fallback manual).
     * Pasamos a MANUAL con los últimos vértices conocidos como punto de partida,
     * o con las esquinas de pantalla si no hay detección previa.
     */
    fun onEnterManualMode() {
        val state = _uiState.value
        val fallbackCorners = state.detectedCorners ?: defaultCorners(
            state.screenWidth,
            state.screenHeight
        )
        _uiState.update {
            it.copy(
                detectionState  = ProDetectionState.MANUAL,
                detectedCorners = fallbackCorners
            )
        }
    }

    /**
     * En modo MANUAL, el usuario arrastró una de las 4 esquinas.
     * @param index  Índice del vértice (0=TL, 1=TR, 2=BR, 3=BL).
     * @param point  Nueva posición en coordenadas de pantalla.
     */
    fun onManualCornerMoved(index: Int, point: ScreenPoint) {
        val corners = _uiState.value.detectedCorners?.toMutableList() ?: return
        if (index !in corners.indices) return
        corners[index] = point
        _uiState.update { it.copy(detectedCorners = corners) }
    }

    /**
     * En modo MANUAL, el usuario confirmó los puntos ajustados a mano.
     * Calculamos la homografía y pasamos a TRACKING.
     */
    fun onConfirmManualCorners() {
        val corners = _uiState.value.detectedCorners ?: return
        viewModelScope.launch {
            recomputeProjection(corners)
            _uiState.update { it.copy(detectionState = ProDetectionState.TRACKING) }
        }
    }

    /**
     * Vuelve a SCANNING desde cualquier estado.
     * Limpia la proyección actual.
     */
    fun onResetDetection() {
        _uiState.update {
            it.copy(
                detectionState     = ProDetectionState.SCANNING,
                detectedCorners    = null,
                projectedBitmap    = null,
                detectionConfidence= 0f
            )
        }
    }

    fun onOpacityChange(value: Float) {
        _uiState.update { it.copy(opacity = value.coerceIn(OPACITY_MIN, OPACITY_MAX)) }
    }

    fun onToggleTorch() {
        _uiState.update { it.copy(isTorchOn = !it.isTorchOn) }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(error = null) }
    }

    // ─────────────────────────────────────────
    // Helpers internos
    // ─────────────────────────────────────────

    /**
     * Calcula la homografía con los vértices dados y actualiza [projectedBitmap].
     * Solo actúa si hay imagen de referencia y dimensiones de pantalla disponibles.
     */
    private suspend fun recomputeProjection(corners: List<ScreenPoint>) {
        val ref    = referenceBitmap ?: return
        val state  = _uiState.value
        if (state.screenWidth == 0 || state.screenHeight == 0) return

        runCatching {
            computeHomography(
                referenceImage = ref,
                sheetCorners   = corners,
                outputWidth    = state.screenWidth,
                outputHeight   = state.screenHeight
            )
        }.onSuccess { bitmap ->
            _uiState.update { it.copy(projectedBitmap = bitmap) }
        }.onFailure { e ->
            _uiState.update { it.copy(error = e.message) }
        }
    }

    /**
     * Genera 4 esquinas por defecto centradas en la pantalla (75% del área).
     * Se usan como punto de partida en modo MANUAL si no hay detección previa.
     */
    private fun defaultCorners(screenWidth: Int, screenHeight: Int): List<ScreenPoint> {
        val marginX = screenWidth  * 0.125f  // 12.5% de margen horizontal
        val marginY = screenHeight * 0.125f  // 12.5% de margen vertical
        return listOf(
            ScreenPoint(marginX,               marginY),               // top-left
            ScreenPoint(screenWidth - marginX, marginY),               // top-right
            ScreenPoint(screenWidth - marginX, screenHeight - marginY),// bottom-right
            ScreenPoint(marginX,               screenHeight - marginY) // bottom-left
        )
    }

    override fun onCleared() {
        super.onCleared()
        analysisJob?.cancel()
    }
}

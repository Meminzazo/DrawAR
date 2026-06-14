package com.meminzazo.drawar.presentation.screen.pro

import android.graphics.Bitmap
import android.net.Uri
import com.meminzazo.drawar.domain.model.ScreenPoint

/**
 * Representa en qué fase del flujo está el Modo Pro.
 *
 * SCANNING  → cámara activa, OpenCV buscando la hoja frame a frame.
 * DETECTED  → se encontró una hoja con confianza suficiente,
 *             se muestra el contorno verde y el botón "Confirmar".
 * TRACKING  → usuario confirmó, la imagen se proyecta sobre la hoja
 *             y el detector sigue actualizando los vértices en vivo.
 * MANUAL    → fallback: el usuario arrastra los 4 puntos a mano.
 */
enum class ProDetectionState {
    SCANNING,
    DETECTED,
    TRACKING,
    MANUAL
}

/**
 * Estado completo de la pantalla del Modo Pro.
 * Es inmutable — el ViewModel emite una copia nueva en cada cambio.
 *
 * @param detectionState    Fase actual del flujo (ver [ProDetectionState]).
 * @param imageUri          URI de la imagen de referencia seleccionada por el usuario.
 * @param detectedCorners   Los 4 vértices de la hoja en coordenadas de pantalla.
 *                          Null si todavía no se detectó nada.
 * @param projectedBitmap   Bitmap con la imagen transformada en perspectiva,
 *                          listo para dibujar encima del preview de cámara.
 *                          Null hasta que el usuario confirme la detección.
 * @param opacity           Opacidad de la imagen proyectada (0.05 – 1.0).
 * @param isTorchOn         Estado de la linterna.
 * @param detectionConfidence Confianza del último frame analizado (0f – 1f).
 *                          Se usa para animar el contorno (más verde = más confianza).
 * @param error             Mensaje de error para mostrar en snackbar, null si no hay.
 * @param screenWidth       Ancho de pantalla en px — se necesita para la homografía.
 * @param screenHeight      Alto de pantalla en px — se necesita para la homografía.
 */
data class ProUiState(
    val detectionState    : ProDetectionState = ProDetectionState.SCANNING,
    val imageUri          : Uri?              = null,
    val detectedCorners   : List<ScreenPoint>?= null,
    val projectedBitmap   : Bitmap?           = null,
    val opacity           : Float             = 0.7f,
    val isTorchOn         : Boolean           = false,
    val detectionConfidence: Float            = 0f,
    val error             : String?           = null,
    val screenWidth       : Int               = 0,
    val screenHeight      : Int               = 0
)

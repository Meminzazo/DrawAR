package com.meminzazo.drawar.domain.model

/**
 * Representa el resultado de intentar detectar una hoja/lienzo en un frame de cámara.
 *
 * @param corners Lista de 4 puntos en coordenadas de pantalla (píxeles),
 *                en orden: superior-izquierda, superior-derecha,
 *                inferior-derecha, inferior-izquierda.
 * @param confidence Valor entre 0f y 1f que indica qué tan seguro está
 *                   el detector de que encontró una hoja real.
 *                   Por encima de 0.75f se considera detección válida.
 */
data class SheetDetectionResult(
    val corners: List<ScreenPoint>,
    val confidence: Float
) {
    /**
     * Devuelve true si la confianza supera el umbral mínimo para
     * mostrarle al usuario el botón "Confirmar".
     */
    val isReliable: Boolean get() = confidence >= CONFIDENCE_THRESHOLD

    companion object {
        const val CONFIDENCE_THRESHOLD = 0.75f
    }
}

/**
 * Un punto 2D en coordenadas de pantalla (píxeles).
 * Se usa en lugar de android.graphics.PointF para mantener
 * el dominio libre de dependencias de Android.
 */
data class ScreenPoint(
    val x: Float,
    val y: Float
)

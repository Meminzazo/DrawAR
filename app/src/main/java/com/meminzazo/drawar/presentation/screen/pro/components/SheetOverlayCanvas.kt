package com.meminzazo.drawar.presentation.screen.pro.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.meminzazo.drawar.domain.model.ScreenPoint

/**
 * Dibuja el contorno del rectángulo detectado sobre el preview de cámara.
 *
 * El color del contorno varía según la confianza:
 *   - Baja confianza (< 0.5)  → amarillo
 *   - Alta confianza (>= 0.5) → verde
 *
 * También dibuja un pequeño círculo en cada vértice para que el usuario
 * pueda ver con claridad las 4 esquinas detectadas.
 *
 * @param corners    Los 4 vértices en coordenadas de pantalla.
 *                   Orden: top-left, top-right, bottom-right, bottom-left.
 * @param confidence Confianza de la detección actual (0f – 1f).
 * @param modifier   Modifier — normalmente fillMaxSize para cubrir el preview.
 */
@Composable
fun SheetOverlayCanvas(
    corners: List<ScreenPoint>,
    confidence: Float,
    modifier: Modifier = Modifier
) {
    // El color interpola de amarillo a verde según la confianza
    val strokeColor = if (confidence >= 0.5f) Color(0xFF4CAF50) else Color(0xFFFFC107)

    Canvas(modifier = modifier.fillMaxSize()) {
        if (corners.size != 4) return@Canvas

        // Construir el path del cuadrilátero
        val path = Path().apply {
            moveTo(corners[0].x, corners[0].y)   // top-left
            lineTo(corners[1].x, corners[1].y)   // top-right
            lineTo(corners[2].x, corners[2].y)   // bottom-right
            lineTo(corners[3].x, corners[3].y)   // bottom-left
            close()                               // cierra de bottom-left a top-left
        }

        // Relleno semitransparente para que el usuario vea el área detectada
        drawPath(
            path  = path,
            color = strokeColor.copy(alpha = 0.10f)
        )

        // Contorno del rectángulo
        drawPath(
            path  = path,
            color = strokeColor,
            style = Stroke(
                width     = 3.dp.toPx(),
                cap       = StrokeCap.Round,
                join      = StrokeJoin.Round
            )
        )

        // Círculo en cada vértice
        val cornerRadius = 8.dp.toPx()
        corners.forEach { point ->
            // Relleno blanco del círculo
            drawCircle(
                color  = Color.White,
                radius = cornerRadius,
                center = Offset(point.x, point.y)
            )
            // Borde del color de confianza
            drawCircle(
                color  = strokeColor,
                radius = cornerRadius,
                center = Offset(point.x, point.y),
                style  = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

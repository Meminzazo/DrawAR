package com.meminzazo.drawar.presentation.screen.pro.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.meminzazo.drawar.domain.model.ScreenPoint
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Overlay de ajuste manual: muestra los 4 vértices como círculos arrastrables
 * y el cuadrilátero que los une.
 *
 * El usuario puede arrastrar cada esquina independientemente para ajustar
 * la perspectiva cuando la detección automática no fue precisa.
 *
 * @param corners         Los 4 vértices actuales en coordenadas de pantalla.
 * @param onCornerMoved   Callback cuando el usuario arrastra una esquina.
 *                        Recibe el índice (0-3) y la nueva posición.
 * @param modifier        Modifier — normalmente fillMaxSize.
 */
@Composable
fun ManualCornersOverlay(
    corners: List<ScreenPoint>,
    onCornerMoved: (index: Int, point: ScreenPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    // Radio del área táctil de cada esquina (más grande que el visual para usabilidad)
    val touchRadiusPx = 40.dp

    // Índice de la esquina que el usuario está arrastrando actualmente (-1 = ninguna)
    var draggingIndex by remember { mutableStateOf(-1) }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(corners) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            // Detectar cuál esquina está más cerca del toque inicial
                            draggingIndex = corners.indexOfFirst { corner ->
                                val distance = sqrt(
                                    (corner.x - offset.x).pow(2) +
                                    (corner.y - offset.y).pow(2)
                                )
                                distance <= touchRadiusPx.toPx()
                            }
                        },
                        onDrag = { change, _ ->
                            // Mover la esquina que está siendo arrastrada
                            if (draggingIndex >= 0) {
                                onCornerMoved(
                                    draggingIndex,
                                    ScreenPoint(
                                        x = change.position.x,
                                        y = change.position.y
                                    )
                                )
                            }
                        },
                        onDragEnd = {
                            draggingIndex = -1
                        },
                        onDragCancel = {
                            draggingIndex = -1
                        }
                    )
                }
        ) {
            if (corners.size != 4) return@Canvas

            // Dibujar el cuadrilátero que une los 4 vértices
            val path = Path().apply {
                moveTo(corners[0].x, corners[0].y)
                lineTo(corners[1].x, corners[1].y)
                lineTo(corners[2].x, corners[2].y)
                lineTo(corners[3].x, corners[3].y)
                close()
            }

            // Relleno semitransparente azul (diferente al automático para distinguirlos)
            drawPath(
                path  = path,
                color = Color(0xFF2196F3).copy(alpha = 0.12f)
            )

            // Contorno azul
            drawPath(
                path  = path,
                color = Color(0xFF2196F3),
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap   = StrokeCap.Round,
                    join  = StrokeJoin.Round
                )
            )

            // Dibujar cada esquina como círculo arrastrable
            corners.forEachIndexed { index, point ->
                val isDragging = index == draggingIndex

                // Círculo exterior — más grande si se está arrastrando
                drawCircle(
                    color  = Color(0xFF2196F3),
                    radius = if (isDragging) 18.dp.toPx() else 12.dp.toPx(),
                    center = Offset(point.x, point.y)
                )

                // Círculo interior blanco
                drawCircle(
                    color  = Color.White,
                    radius = if (isDragging) 10.dp.toPx() else 6.dp.toPx(),
                    center = Offset(point.x, point.y)
                )
            }
        }
    }
}

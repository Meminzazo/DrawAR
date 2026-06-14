package com.meminzazo.drawar.presentation.screen.simple.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlin.math.roundToInt

@Composable
fun OverlayImage(
    imageUri: Uri,
    opacity: Float,
    offsetXPercent: Float,
    offsetYPercent: Float,
    scale: Float,
    rotation: Float,
    flipHorizontal: Boolean,
    flipVertical: Boolean,
    isLocked: Boolean,
    edgeBitmap: Bitmap? = null,        // ← nuevo
    isEdgeModeActive: Boolean = false, // ← nuevo
    onOffsetChange: (Float, Float) -> Unit,
    onScaleChange: (Float) -> Unit,
    onRotationChange: (Float) -> Unit,
    onUserInteraction: () -> Unit,
    modifier: Modifier = Modifier
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    // Siempre apuntan al valor más reciente — el gesto nunca captura un valor stale
    val currentOffsetX by rememberUpdatedState(offsetXPercent)
    val currentOffsetY by rememberUpdatedState(offsetYPercent)
    val currentScale   by rememberUpdatedState(scale)
    val currentRotation by rememberUpdatedState(rotation)

    // El modelo que Coil debe mostrar — bitmap procesado o URI original
    val imageModel = if (isEdgeModeActive && edgeBitmap != null) {
        edgeBitmap
    } else {
        imageUri
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
    ) {
        if (containerSize != IntSize.Zero) {
            val offsetX = (currentOffsetX * containerSize.width).roundToInt()
            val offsetY = (currentOffsetY * containerSize.height).roundToInt()

            AsyncImage(
                model = imageModel,
                contentDescription = "Imagen de referencia para calcar",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    // 1. Posicionamos la imagen en el espacio global
                    .offset { IntOffset(offsetX, offsetY) }
                    // 2. Aplicamos rotación y flip en la capa de gráficos (más eficiente)
                    .graphicsLayer {
                        rotationZ = currentRotation
                        scaleX = if (flipHorizontal) -1f else 1f
                        scaleY = if (flipVertical) -1f else 1f
                        // LA CLAVE: Desactivamos el recorte para que al hacer zoom 
                        // no se corte la imagen en los bordes de la pantalla.
                        clip = false
                    }
                    .alpha(opacity)
                    // 3. El truco maestro: wrapContentSize(unbounded = true) permite que el componente
                    // sea más grande que el padre (la pantalla) sin ser recortado.
                    .wrapContentSize(align = Alignment.Center, unbounded = true)
                    // 4. Aplicamos el tamaño escalado directamente al layout del componente.
                    // Al cambiar el tamaño del layout en lugar de solo la escala visual,
                    // evitamos que el motor de renderizado recorte la imagen.
                    .size(
                        (containerSize.width.dp / LocalDensity.current.density) * currentScale,
                        (containerSize.height.dp / LocalDensity.current.density) * currentScale
                    )
                    // 5. Capturamos los gestos sobre el componente ya escalado.
                    .then(
                        if (!isLocked) {
                            Modifier.pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, rotate ->
                                    onUserInteraction()

                                    if (containerSize.width > 0 && containerSize.height > 0) {
                                        // El 'pan' sigue siendo relativo a la pantalla física.
                                        val newX = currentOffsetX + pan.x / containerSize.width
                                        val newY = currentOffsetY + pan.y / containerSize.height
                                        onOffsetChange(newX, newY)
                                    }

                                    onScaleChange(currentScale * zoom)
                                    onRotationChange(currentRotation + rotate)
                                }
                            }
                        } else Modifier
                    )
            )
        }
    }
}
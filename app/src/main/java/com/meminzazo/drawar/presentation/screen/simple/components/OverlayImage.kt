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
    offsetXPx: Float,
    offsetYPx: Float,
    scale: Float,
    rotation: Float,
    flipHorizontal: Boolean,
    flipVertical: Boolean,
    isLocked: Boolean,
    edgeBitmap: Bitmap? = null,
    isEdgeModeActive: Boolean = false,
    onOffsetChange: (Float, Float) -> Unit,
    onScaleChange: (Float) -> Unit,
    onRotationChange: (Float) -> Unit,
    onUserInteraction: () -> Unit,
    modifier: Modifier = Modifier
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val currentOffsetX  by rememberUpdatedState(offsetXPx)
    val currentOffsetY  by rememberUpdatedState(offsetYPx)
    val currentScale    by rememberUpdatedState(scale)
    val currentRotation by rememberUpdatedState(rotation)

    val imageModel = if (isEdgeModeActive && edgeBitmap != null) edgeBitmap else imageUri
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it },
        contentAlignment = Alignment.Center
    ) {
        if (containerSize != IntSize.Zero) {
            AsyncImage(
                model = imageModel,
                contentDescription = "Imagen de referencia para calcar",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    // 1. Capturamos gestos primero en el espacio global (solución al movimiento al rotar)
                    .then(
                        if (!isLocked) {
                            Modifier.pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, rotate ->
                                    onUserInteraction()
                                    onOffsetChange(currentOffsetX + pan.x, currentOffsetY + pan.y)
                                    onScaleChange(currentScale * zoom)
                                    onRotationChange(currentRotation + rotate)
                                }
                            }
                        } else Modifier
                    )
                    // 2. Posicionamiento
                    .offset { IntOffset(currentOffsetX.roundToInt(), currentOffsetY.roundToInt()) }
                    // 3. Tamaño base y permitir desbordamiento (solución al recorte)
                    .wrapContentSize(align = Alignment.Center, unbounded = true)
                    .size(
                        width  = with(density) { containerSize.width.toDp() },
                        height = with(density) { containerSize.height.toDp() }
                    )
                    // 4. Transformaciones visuales (smooth zoom)
                    .graphicsLayer {
                        scaleX = currentScale * if (flipHorizontal) -1f else 1f
                        scaleY = currentScale * if (flipVertical)   -1f else 1f
                        rotationZ = currentRotation
                        clip = false // Evita el recorte
                    }
                    .alpha(opacity)
            )
        }
    }
}

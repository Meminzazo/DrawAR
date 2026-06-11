package com.meminzazo.drawar.presentation.screen.simple.components

import android.net.Uri
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
    onOffsetChange: (Float, Float) -> Unit,
    onScaleChange: (Float) -> Unit,
    onRotationChange: (Float) -> Unit,
    onUserInteraction: () -> Unit,
    modifier: Modifier = Modifier
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
    ) {
        if (containerSize != IntSize.Zero) {
            val offsetX = (offsetXPercent * containerSize.width).roundToInt()
            val offsetY = (offsetYPercent * containerSize.height).roundToInt()

            AsyncImage(
                model = imageUri,
                contentDescription = "Imagen de referencia para calcar",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(offsetX, offsetY) }
                    .alpha(opacity)
                    .graphicsLayer {
                        scaleX = scale * if (flipHorizontal) -1f else 1f
                        scaleY = scale * if (flipVertical) -1f else 1f
                        rotationZ = rotation
                    }
                    .then(
                        if (!isLocked) {
                            Modifier.pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, rotate ->
                                    onUserInteraction()

                                    // Actualizar offset en porcentaje
                                    if (containerSize.width > 0 && containerSize.height > 0) {
                                        val newX = offsetXPercent + pan.x / containerSize.width
                                        val newY = offsetYPercent + pan.y / containerSize.height
                                        onOffsetChange(newX, newY)
                                    }

                                    onScaleChange(scale * zoom)
                                    onRotationChange(rotation + rotate)
                                }
                            }
                        } else Modifier
                    )
            )
        }
    }
}

// Necesario para el remember de IntSize
private fun mutableStateOf(value: IntSize) =
    androidx.compose.runtime.mutableStateOf(value)
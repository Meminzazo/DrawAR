package com.meminzazo.drawar.presentation.screen.simple.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.meminzazo.drawar.presentation.theme.ControlBackground
import com.meminzazo.drawar.presentation.screen.simple.components.ControlAction

@Composable
fun BottomControlBar(
    opacity: Float,
    scale: Float,
    isLocked: Boolean,
    isTorchOn: Boolean,
    isVisible: Boolean,
    isFlippedH: Boolean,
    isFlippedV: Boolean,
    onOpacityChange: (Float) -> Unit,
    onFlipHorizontal: () -> Unit,
    onFlipVertical: () -> Unit,
    onToggleLock: () -> Unit,
    onToggleTorch: () -> Unit,
    onReset: () -> Unit,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit  = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(ControlBackground)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Fila superior — iconos de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                // Cargar imagen

                ControlAction(
                    icon = Icons.Default.AddAPhoto,
                    label = "Cargar imagen",
                    onClick = onPickImage,
                    enabled = !isLocked
                )

                // Voltear horizontal
                ControlAction(
                    icon = Icons.Default.SwapHoriz,
                    label = "Voltear horizontal",
                    onClick = onFlipHorizontal,
                    tint = if (isFlippedH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    enabled = !isLocked
                )

                // Voltear vertical

                ControlAction(
                    icon = Icons.Default.SwapVert,
                    label = "Voltear vertical",
                    onClick = onFlipVertical,
                    tint = if (isFlippedV) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    enabled = !isLocked
                )

                // Bloquear overlay
                ControlAction(
                    icon = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    label = if (isLocked) "Desbloquear" else "Bloquear",
                    onClick = onToggleLock,
                    tint = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )

                // Reset
                ControlAction(
                    icon = Icons.Default.Refresh,
                    label = "Restablecer imagen",
                    onClick = onReset,
                    enabled = !isLocked
                )

            }

            Spacer(modifier = Modifier.height(8.dp))

            // Slider de opacidad
            ControlSlider(
                label = "Opacidad",
                value = opacity,
                valueDisplay = "${(opacity * 100).toInt()}%",
                onValueChange = onOpacityChange
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Indicador de escala (solo lectura)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Escala",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "×${"%.2f".format(scale)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
internal fun ControlSlider(
    label: String,
    value: Float,
    valueDisplay: String,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valueDisplay,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0.05f..1f,
            colors = SliderDefaults.colors(
                thumbColor       = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
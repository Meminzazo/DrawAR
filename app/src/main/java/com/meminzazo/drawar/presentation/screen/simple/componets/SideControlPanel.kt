package com.meminzazo.drawar.presentation.screen.simple.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.meminzazo.drawar.presentation.theme.ControlBackground

@Composable
fun SideControlPanel(
    opacity: Float,
    scale: Float,
    isLocked: Boolean,
    isTorchOn: Boolean,
    isVisible: Boolean,
    isExpanded: Boolean,         // tablet grande vs tablet pequeña
    onOpacityChange: (Float) -> Unit,
    onFlipHorizontal: () -> Unit,
    onFlipVertical: () -> Unit,
    onToggleLock: () -> Unit,
    onToggleTorch: () -> Unit,
    onReset: () -> Unit,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val panelWidth = if (isExpanded) 200.dp else 64.dp

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit  = slideOutHorizontally(targetOffsetX = { it }),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .width(panelWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                .background(ControlBackground)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            IconButton(onClick = onPickImage) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = "Cargar imagen",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onFlipHorizontal, enabled = !isLocked) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Voltear horizontal",
                    tint = if (!isLocked) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onFlipVertical, enabled = !isLocked) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Voltear vertical",
                    tint = if (!isLocked) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onToggleLock) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = if (isLocked) "Desbloquear" else "Bloquear",
                    tint = if (isLocked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = onToggleTorch) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashlightOn
                    else Icons.Default.FlashlightOff,
                    contentDescription = if (isTorchOn) "Apagar linterna" else "Encender linterna",
                    tint = if (isTorchOn) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = onReset) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restablecer",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // En panel expandido mostramos slider y escala con label
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                ControlSlider(
                    label = "Opacidad",
                    value = opacity,
                    valueDisplay = "${(opacity * 100).toInt()}%",
                    onValueChange = onOpacityChange
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Escala ×${"%.2f".format(scale)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
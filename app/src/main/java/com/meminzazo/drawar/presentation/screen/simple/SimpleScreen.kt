package com.meminzazo.drawar.presentation.screen.simple

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.meminzazo.drawar.presentation.screen.simple.components.BottomControlBar
import com.meminzazo.drawar.presentation.screen.simple.components.CameraPreview
import com.meminzazo.drawar.presentation.screen.simple.components.OverlayImage
import com.meminzazo.drawar.presentation.screen.simple.components.SideControlPanel
import com.meminzazo.drawar.presentation.util.rememberWindowSizeClass
import com.meminzazo.drawar.presentation.util.WindowSizeClass

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SimpleScreen(
    onNavigateBack: () -> Unit,
    viewModel: SimpleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Permiso de cámara
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
    }

    // Selector de imagen desde galería
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    // Layout adaptativo
    val windowSize = rememberWindowSizeClass()
    val isLandscapeOrTablet = windowSize != WindowSizeClass.CompactPortrait
    val isLargeTablet = windowSize == WindowSizeClass.Expanded

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { viewModel.onToggleControlsVisibility() }
    ) {
        // 1. Cámara — capa base
        if (cameraPermission.status.isGranted) {
            CameraPreview(
                isTorchOn = uiState.isTorchOn,
                modifier  = Modifier.fillMaxSize()
            )
        } else {
            NoCameraPermissionPlaceholder()
        }

        // 2. Overlay — encima de la cámara
        uiState.imageUri?.let { uri ->
            OverlayImage(
                imageUri       = uri,
                opacity        = uiState.opacity,
                offsetXPercent = uiState.offsetXPercent,
                offsetYPercent = uiState.offsetYPercent,
                scale          = uiState.scale,
                rotation       = uiState.rotation,
                flipHorizontal = uiState.flipHorizontal,
                flipVertical   = uiState.flipVertical,
                isLocked       = uiState.isLocked,
                onOffsetChange     = viewModel::onOffsetChange,
                onScaleChange      = viewModel::onScaleChange,
                onRotationChange   = viewModel::onRotationChange,
                onUserInteraction  = viewModel::onUserInteraction,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 3. Controles — portrait: barra inferior / landscape+tablet: panel lateral
        if (isLandscapeOrTablet) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f))
                SideControlPanel(
                    opacity          = uiState.opacity,
                    scale            = uiState.scale,
                    isLocked         = uiState.isLocked,
                    isTorchOn        = uiState.isTorchOn,
                    isVisible        = uiState.controlsVisible,
                    isExpanded       = isLargeTablet,
                    onOpacityChange  = viewModel::onOpacityChange,
                    onFlipHorizontal = viewModel::onFlipHorizontal,
                    onFlipVertical   = viewModel::onFlipVertical,
                    onToggleLock     = viewModel::onToggleLock,
                    onToggleTorch    = viewModel::onToggleTorch,
                    onReset          = viewModel::onReset,
                    onPickImage      = { imagePicker.launch("image/*") },
                    modifier         = Modifier.align(Alignment.CenterVertically)
                )
            }
        } else {
            BottomControlBar(
                opacity          = uiState.opacity,
                scale            = uiState.scale,
                isLocked         = uiState.isLocked,
                isTorchOn        = uiState.isTorchOn,
                isVisible        = uiState.controlsVisible,
                onOpacityChange  = viewModel::onOpacityChange,
                onFlipHorizontal = viewModel::onFlipHorizontal,
                onFlipVertical   = viewModel::onFlipVertical,
                onToggleLock     = viewModel::onToggleLock,
                onToggleTorch    = viewModel::onToggleTorch,
                onReset          = viewModel::onReset,
                onPickImage      = { imagePicker.launch("image/*") },
                modifier         = Modifier.align(Alignment.BottomCenter)
            )
        }

        // 4. Barra superior — back + linterna rápida
        TopBar(
            isTorchOn     = uiState.isTorchOn,
            isVisible     = uiState.controlsVisible,
            onNavigateBack = onNavigateBack,
            onToggleTorch  = viewModel::onToggleTorch,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // 5. Aviso si no hay imagen cargada
        if (uiState.imageUri == null && !uiState.isLoading) {
            NoImageHint(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun TopBar(
    isTorchOn: Boolean,
    isVisible: Boolean,
    onNavigateBack: () -> Unit,
    onToggleTorch: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.fadeIn(),
        exit  = androidx.compose.animation.fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface
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
        }
    }
}

@Composable
private fun NoImageHint(modifier: Modifier = Modifier) {
    Text(
        text = "Toca el ícono de imagen para comenzar",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
private fun NoCameraPermissionPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Se necesita permiso de cámara",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
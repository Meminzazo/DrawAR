package com.meminzazo.drawar.presentation.screen.simple.components

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraPreview(
    isTorchOn: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    DisposableEffect(isTorchOn) {
        val executor = ContextCompat.getMainExecutor(context)
        var cameraProvider: ProcessCameraProvider? = null

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCamera(
                context        = context,
                cameraProvider = cameraProvider!!,
                previewView    = previewView,
                lifecycleOwner = lifecycleOwner,
                isTorchOn      = isTorchOn
            )
        }, executor)

        onDispose {
            cameraProvider?.unbindAll()
        }
    }

    AndroidView(
        factory  = { previewView },
        modifier = modifier
    )
}

private fun bindCamera(
    context: Context,
    cameraProvider: ProcessCameraProvider,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    isTorchOn: Boolean
) {
    val preview = Preview.Builder().build().also {
        it.surfaceProvider = previewView.surfaceProvider
    }

    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    try {
        cameraProvider.unbindAll()
        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview
        )
        // Linterna — solo si el hardware la soporta
        if (camera.cameraInfo.hasFlashUnit()) {
            camera.cameraControl.enableTorch(isTorchOn)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
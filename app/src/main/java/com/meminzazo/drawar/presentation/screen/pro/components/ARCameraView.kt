package com.meminzazo.drawar.presentation.screen.pro.components

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.node.ImageNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.math.Rotation
import com.meminzazo.drawar.presentation.screen.simple.components.OverlayImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Nueva implementación de AR Manual "Lienzo 3D".
 * El usuario mueve el móvil hasta centrar el dibujo sobre el papel y toca para fijarlo.
 */
@Composable
fun ARCameraView(
    modifier: Modifier = Modifier,
    imageUri: Uri? = null,
    anchor: Anchor? = null,
    opacity: Float = 0.5f,
    onTap: (Anchor) -> Unit
) {
    val context = LocalContext.current
    val engine = rememberEngine()
    val nodes = rememberNodes()
    val materialLoader = rememberMaterialLoader(engine)
    
    var currentAnchorNode by remember { mutableStateOf<AnchorNode?>(null) }
    val bitmapState = remember { mutableStateOf<Bitmap?>(null) }
    
    // Almacenamos el toque pendiente para procesarlo en el hilo de AR
    var pendingTap by remember { mutableStateOf<androidx.compose.ui.geometry.Offset?>(null) }

    // 1. CARGA DE IMAGEN
    LaunchedEffect(imageUri) {
        if (imageUri != null) {
            withContext(Dispatchers.IO) {
                try {
                    val loaded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, imageUri))
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                    }.copy(Bitmap.Config.ARGB_8888, true)
                    bitmapState.value = loaded
                } catch (e: Exception) {
                    bitmapState.value = null
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            childNodes = nodes,
            sessionConfiguration = { _, config ->
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                config.focusMode = Config.FocusMode.AUTO
                config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
            },
            onSessionUpdated = { _, frame ->
                pendingTap?.let { offset ->
                    val hits = frame.hitTest(offset.x, offset.y)
                    if (hits.isNotEmpty()) {
                        onTap(hits.first().createAnchor())
                    }
                    pendingTap = null
                }
            }
        )

        // Guía 2D fija mientras se posiciona
        if (anchor == null && imageUri != null) {
            OverlayImage(
                imageUri = imageUri,
                opacity = opacity,
                offsetXPx = 0f,
                offsetYPx = 0f,
                scale = 1f,
                rotation = 0f,
                flipHorizontal = false,
                flipVertical = false,
                isLocked = true,
                onOffsetChange = { _, _ -> },
                onScaleChange = { },
                onRotationChange = { },
                onUserInteraction = { },
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            pendingTap = offset
                        }
                    }
            )
        }
    }

    // 2. RENDERIZADO ANCLA 3D
    LaunchedEffect(anchor, bitmapState.value) {
        currentAnchorNode?.let { nodes.remove(it); it.destroy() }
        
        if (anchor != null && bitmapState.value != null) {
            val anchorNode = AnchorNode(engine, anchor)
            val imageNode = ImageNode(
                materialLoader = materialLoader,
                bitmap = bitmapState.value!!,
                size = io.github.sceneview.math.Size(x = 0.21f, y = 0.297f)
            ).apply {
                rotation = Rotation(x = -90f, y = 0f, z = 0f)
            }
            anchorNode.addChildNode(imageNode)
            nodes.add(anchorNode)
            currentAnchorNode = anchorNode
        }
    }
}

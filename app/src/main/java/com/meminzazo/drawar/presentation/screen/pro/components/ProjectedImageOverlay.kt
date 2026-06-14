package com.meminzazo.drawar.presentation.screen.pro.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

/**
 * Muestra el bitmap ya transformado en perspectiva sobre el preview de cámara.
 *
 * El bitmap viene del [ComputeHomographyUseCase] y ya tiene el mismo tamaño
 * que la pantalla, con la imagen de referencia proyectada sobre el área
 * de la hoja y transparente fuera de ella.
 *
 * Por eso usamos ContentScale.FillBounds — el bitmap ya está en coordenadas
 * de pantalla exactas, no necesitamos que Compose lo reescale ni reencuadre.
 *
 * @param bitmap  Bitmap proyectado, del tamaño exacto de la pantalla.
 * @param opacity Opacidad de la superposición (0.05 – 1.0).
 * @param modifier Modifier — normalmente fillMaxSize.
 */
@Composable
fun ProjectedImageOverlay(
    bitmap: Bitmap,
    opacity: Float,
    modifier: Modifier = Modifier
) {
    Image(
        bitmap             = bitmap.asImageBitmap(),
        contentDescription = "Imagen de referencia proyectada sobre la hoja",
        contentScale       = ContentScale.FillBounds, // ya está en coordenadas de pantalla
        modifier           = modifier
            .fillMaxSize()
            .alpha(opacity)
    )
}

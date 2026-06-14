package com.meminzazo.drawar.domain.usecase

import android.graphics.Bitmap
import com.meminzazo.drawar.domain.model.ScreenPoint
import com.meminzazo.drawar.domain.repository.SheetDetectionRepository
import javax.inject.Inject

/**
 * Caso de uso que transforma la imagen de referencia en perspectiva
 * para que encaje exactamente sobre los 4 vértices de la hoja detectada.
 *
 * Se invoca una vez cuando el usuario confirma la detección, y luego
 * cada vez que el tracker actualiza los vértices (tracking en vivo).
 *
 * @param repository Implementación inyectada por Hilt.
 */
class ComputeHomographyUseCase @Inject constructor(
    private val repository: SheetDetectionRepository
) {
    /**
     * @param referenceImage Imagen original que el usuario quiere calcar.
     * @param sheetCorners   Los 4 vértices actuales de la hoja en pantalla.
     * @param outputWidth    Ancho de la pantalla en píxeles.
     * @param outputHeight   Alto de la pantalla en píxeles.
     * @return Bitmap del tamaño de la pantalla con la imagen proyectada
     *         sobre el área de la hoja.
     */
    suspend operator fun invoke(
        referenceImage: Bitmap,
        sheetCorners: List<ScreenPoint>,
        outputWidth: Int,
        outputHeight: Int
    ): Bitmap = repository.applyPerspectiveTransform(
        referenceImage = referenceImage,
        sheetCorners   = sheetCorners,
        outputWidth    = outputWidth,
        outputHeight   = outputHeight
    )
}

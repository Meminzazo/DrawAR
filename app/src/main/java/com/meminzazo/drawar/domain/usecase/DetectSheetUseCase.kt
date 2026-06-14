package com.meminzazo.drawar.domain.usecase

import android.graphics.Bitmap
import com.meminzazo.drawar.domain.model.SheetDetectionResult
import com.meminzazo.drawar.domain.repository.SheetDetectionRepository
import javax.inject.Inject

/**
 * Caso de uso que encapsula la detección de hoja en un frame de cámara.
 *
 * Es invocado por el ViewModel cada vez que llega un nuevo frame
 * del ImageAnalysis de CameraX.
 *
 * @param repository Implementación inyectada por Hilt.
 */
class DetectSheetUseCase @Inject constructor(
    private val repository: SheetDetectionRepository
) {
    /**
     * @param frame Bitmap del frame actual de la cámara.
     * @return Resultado de detección, o null si no hay hoja visible.
     */
    suspend operator fun invoke(frame: Bitmap): SheetDetectionResult? =
        repository.detectSheet(frame)
}

package com.meminzazo.drawar.data.repository

import android.graphics.Bitmap
import com.meminzazo.drawar.data.opencv.PerspectiveTransformer
import com.meminzazo.drawar.data.opencv.SheetDetector
import com.meminzazo.drawar.domain.model.ScreenPoint
import com.meminzazo.drawar.domain.model.SheetDetectionResult
import com.meminzazo.drawar.domain.repository.SheetDetectionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementación concreta del [SheetDetectionRepository].
 *
 * Delega la lógica pesada a [SheetDetector] y [PerspectiveTransformer],
 * y asegura que todo corra en el dispatcher de CPU (Dispatchers.Default)
 * para no bloquear el hilo principal.
 */
class SheetDetectionRepositoryImpl @Inject constructor(
    private val detector: SheetDetector,
    private val transformer: PerspectiveTransformer
) : SheetDetectionRepository {

    /**
     * Corre la detección en el dispatcher Default (CPU-bound).
     * El ViewModel puede llamar esto desde un scope de coroutine normal.
     */
    override suspend fun detectSheet(frame: Bitmap): SheetDetectionResult? =
        withContext(Dispatchers.Default) {
            detector.detect(frame)
        }

    /**
     * Corre la transformación de perspectiva en el dispatcher Default.
     * Es una operación costosa — no debe hacerse en el hilo principal.
     */
    override suspend fun applyPerspectiveTransform(
        referenceImage: Bitmap,
        sheetCorners: List<ScreenPoint>,
        outputWidth: Int,
        outputHeight: Int
    ): Bitmap = withContext(Dispatchers.Default) {
        transformer.transform(
            referenceImage = referenceImage,
            sheetCorners   = sheetCorners,
            outputWidth    = outputWidth,
            outputHeight   = outputHeight
        )
    }
}

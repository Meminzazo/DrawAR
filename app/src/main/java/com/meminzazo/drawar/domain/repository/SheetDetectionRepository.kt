package com.meminzazo.drawar.domain.repository

import android.graphics.Bitmap
import com.meminzazo.drawar.domain.model.ScreenPoint
import com.meminzazo.drawar.domain.model.SheetDetectionResult

/**
 * Contrato que define las operaciones de detección y transformación
 * de perspectiva para el Modo Pro.
 *
 * La implementación vive en la capa data (SheetDetectionRepositoryImpl)
 * y usa OpenCV internamente — el dominio no sabe nada de eso.
 */
interface SheetDetectionRepository {

    /**
     * Analiza un frame de cámara (como Bitmap) e intenta detectar
     * el rectángulo de una hoja de papel.
     *
     * @param frame Frame actual de la cámara en formato ARGB_8888.
     * @return SheetDetectionResult con los 4 vértices y la confianza,
     *         o null si no se encontró ningún rectángulo candidato.
     */
    suspend fun detectSheet(frame: Bitmap): SheetDetectionResult?

    /**
     * Dado un Bitmap de referencia (la imagen a calcar) y los 4 vértices
     * de la hoja detectada en coordenadas de pantalla, calcula y devuelve
     * un nuevo Bitmap con la imagen transformada en perspectiva para que
     * encaje exactamente sobre la hoja.
     *
     * @param referenceImage Imagen original que el usuario quiere calcar.
     * @param sheetCorners   Los 4 vértices de la hoja en pantalla,
     *                       en orden: top-left, top-right, bottom-right, bottom-left.
     * @param outputWidth    Ancho del canvas de salida (ancho de pantalla).
     * @param outputHeight   Alto del canvas de salida (alto de pantalla).
     * @return Bitmap del mismo tamaño que la pantalla, con la imagen
     *         proyectada sobre el área de la hoja y transparente fuera de ella.
     */
    suspend fun applyPerspectiveTransform(
        referenceImage: Bitmap,
        sheetCorners: List<ScreenPoint>,
        outputWidth: Int,
        outputHeight: Int
    ): Bitmap
}

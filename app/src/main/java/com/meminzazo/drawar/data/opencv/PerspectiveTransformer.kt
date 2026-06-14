package com.meminzazo.drawar.data.opencv

import android.graphics.Bitmap
import com.meminzazo.drawar.domain.model.ScreenPoint
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Aplica una transformación de perspectiva (homografía) a la imagen de referencia
 * para que encaje exactamente sobre los 4 vértices de la hoja detectada.
 *
 * Pipeline:
 *   1. Definir los 4 puntos de origen (esquinas de la imagen de referencia)
 *   2. Definir los 4 puntos de destino (esquinas de la hoja en pantalla)
 *   3. Calcular la matriz de homografía con getPerspectiveTransform
 *   4. Aplicar warpPerspective al Bitmap de referencia
 *   5. Devolver el Bitmap resultante del tamaño de la pantalla
 */
@Singleton
class PerspectiveTransformer @Inject constructor() {

    /**
     * Transforma [referenceImage] para que se proyecte sobre [sheetCorners].
     *
     * @param referenceImage Imagen original que el usuario quiere calcar.
     * @param sheetCorners   Los 4 vértices de la hoja en coordenadas de pantalla,
     *                       en orden: top-left, top-right, bottom-right, bottom-left.
     * @param outputWidth    Ancho del canvas de salida (ancho de pantalla en px).
     * @param outputHeight   Alto del canvas de salida (alto de pantalla en px).
     * @return Bitmap ARGB_8888 del tamaño de la pantalla, con la imagen proyectada
     *         sobre el área de la hoja y negro-transparente fuera de ella.
     */
    fun transform(
        referenceImage: Bitmap,
        sheetCorners: List<ScreenPoint>,
        outputWidth: Int,
        outputHeight: Int
    ): Bitmap {
        // 1. Convertir la imagen de referencia a Mat
        val srcMat = Mat()
        val srcArgb = referenceImage.copy(Bitmap.Config.ARGB_8888, false)
        Utils.bitmapToMat(srcArgb, srcMat)

        // 2. Puntos de origen = las 4 esquinas de la imagen de referencia completa
        val srcWidth  = referenceImage.width.toDouble()
        val srcHeight = referenceImage.height.toDouble()

        val srcPoints = MatOfPoint2f(
            Point(0.0,       0.0),        // top-left
            Point(srcWidth,  0.0),        // top-right
            Point(srcWidth,  srcHeight),  // bottom-right
            Point(0.0,       srcHeight)   // bottom-left
        )

        // 3. Puntos de destino = los 4 vértices de la hoja en coordenadas de pantalla
        val dstPoints = MatOfPoint2f(
            Point(sheetCorners[0].x.toDouble(), sheetCorners[0].y.toDouble()), // top-left
            Point(sheetCorners[1].x.toDouble(), sheetCorners[1].y.toDouble()), // top-right
            Point(sheetCorners[2].x.toDouble(), sheetCorners[2].y.toDouble()), // bottom-right
            Point(sheetCorners[3].x.toDouble(), sheetCorners[3].y.toDouble())  // bottom-left
        )

        // 4. Calcular la matriz de homografía 3×3
        val homographyMatrix = Imgproc.getPerspectiveTransform(srcPoints, dstPoints)

        // 5. Aplicar la transformación al Mat de origen
        val dstMat = Mat()
        Imgproc.warpPerspective(
            srcMat,
            dstMat,
            homographyMatrix,
            Size(outputWidth.toDouble(), outputHeight.toDouble()),
            Imgproc.INTER_LINEAR,       // interpolación bilineal — buena calidad/velocidad
            org.opencv.core.Core.BORDER_TRANSPARENT  // fuera de la hoja = transparente
        )

        // 6. Convertir Mat resultado a Bitmap
        val resultBitmap = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dstMat, resultBitmap)

        // Liberar Mats locales
        srcMat.release()
        dstMat.release()
        homographyMatrix.release()
        srcPoints.release()
        dstPoints.release()

        return resultBitmap
    }
}

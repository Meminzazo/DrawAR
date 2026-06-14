package com.meminzazo.drawar.data.opencv

import android.graphics.Bitmap
import com.meminzazo.drawar.domain.model.ScreenPoint
import com.meminzazo.drawar.domain.model.SheetDetectionResult
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Encapsula toda la lógica de visión computacional con OpenCV.
 * Es un Singleton para reutilizar los Mat y evitar allocations en cada frame.
 *
 * Pipeline por frame:
 *   1. Bitmap → Mat (RGBA)
 *   2. RGBA → Gris
 *   3. Blur gaussiano (reduce ruido)
 *   4. Canny (detecta bordes)
 *   5. findContours (encuentra contornos)
 *   6. Filtrar el contorno más grande con 4 vértices (la hoja)
 *   7. Ordenar los 4 vértices en orden canónico
 *   8. Calcular confianza según área relativa
 */
@Singleton
class SheetDetector @Inject constructor() {

    companion object {
        // Parámetros de detección - Ajustados para mayor robustez
        private const val CANNY_THRESHOLD_LOW  = 75.0
        private const val CANNY_THRESHOLD_HIGH = 200.0

        // Área mínima del contorno como porcentaje del frame
        private const val MIN_AREA_RATIO = 0.05 // Bajado de 0.10 para detectar hojas más lejanas

        // Épsilon para aproximación de polígono (% del perímetro)
        private const val APPROX_EPSILON_RATIO = 0.02

        // Tamaño del blur gaussiano para eliminar ruido
        private val BLUR_SIZE = Size(7.0, 7.0) // Aumentado ligeramente
    }

    // Reutilizamos Mat para no crear objetos nuevos en cada frame
    private val matRgba  = Mat()
    private val matGray  = Mat()
    private val matBlur  = Mat()
    private val matThresh = Mat()
    private val matEdges = Mat()

    /**
     * Analiza un frame y devuelve el resultado de detección.
     */
    fun detect(frame: Bitmap): SheetDetectionResult? {
        // 1. Bitmap → Mat RGBA
        val bitmapArgb = frame.copy(Bitmap.Config.ARGB_8888, false)
        Utils.bitmapToMat(bitmapArgb, matRgba)

        val frameArea = (matRgba.rows() * matRgba.cols()).toDouble()

        // 2. RGBA → Gris
        Imgproc.cvtColor(matRgba, matGray, Imgproc.COLOR_RGBA2GRAY)

        // 3. Ecualización de histograma limitada (CLAHE) para mejorar contraste local
        // Esto ayuda mucho con sombras o iluminación desigual
        val clahe = Imgproc.createCLAHE(2.0, Size(8.0, 8.0))
        clahe.apply(matGray, matGray)

        // 4. Blur gaussiano para reducir ruido
        Imgproc.GaussianBlur(matGray, matBlur, BLUR_SIZE, 0.0)

        // 5. Umbral adaptativo (ayuda a separar el cuaderno del fondo)
        Imgproc.adaptiveThreshold(
            matBlur,
            matThresh,
            255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY,
            11,
            2.0
        )

        // 6. Canny sobre la imagen con umbral para detectar bordes limpios
        Imgproc.Canny(matThresh, matEdges, CANNY_THRESHOLD_LOW, CANNY_THRESHOLD_HIGH)

        // 7. Dilatar los bordes para cerrar gaps en los contornos
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
        Imgproc.dilate(matEdges, matEdges, kernel)

        // 8. Encontrar contornos externos
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(
            matEdges,
            contours,
            hierarchy,
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        // 9. Buscar el contorno que más se parezca a un rectángulo
        val sheetContour = findBestRectangle(contours, frameArea) ?: return null

        // 10. Calcular confianza
        val contourArea = Imgproc.contourArea(sheetContour)
        val confidence  = calculateConfidence(contourArea, frameArea)

        // 11. Convertir y ordenar puntos
        val points = sheetContour.toArray()
        val ordered = orderCorners(points)

        hierarchy.release()

        return SheetDetectionResult(
            corners    = ordered,
            confidence = confidence
        )
    }

    /**
     * De todos los contornos encontrados, busca el que:
     * - Tenga área suficiente (> MIN_AREA_RATIO del frame)
     * - Al aproximarse como polígono tenga exactamente 4 vértices
     * - Sea convexo (una hoja no tiene concavidades)
     *
     * Devuelve el más grande que cumpla todas las condiciones.
     */
    private fun findBestRectangle(
        contours: List<MatOfPoint>,
        frameArea: Double
    ): MatOfPoint2f? {
        var bestContour: MatOfPoint2f? = null
        var bestArea = 0.0

        for (contour in contours) {
            val contour2f = MatOfPoint2f(*contour.toArray())
            val perimeter = Imgproc.arcLength(contour2f, true)
            val approx    = MatOfPoint2f()
            
            // Intentar aproximación progresiva si el primer intento falla
            Imgproc.approxPolyDP(
                contour2f,
                approx,
                APPROX_EPSILON_RATIO * perimeter,
                true
            )

            val area = Imgproc.contourArea(approx)
            
            // 1. Filtrar por área mínima (5% del frame)
            if (area < frameArea * MIN_AREA_RATIO) continue

            // 2. Debe tener 4 vértices
            if (approx.rows() != 4) continue

            // 3. Debe ser convexo
            if (!Imgproc.isContourConvex(MatOfPoint(*approx.toArray()))) continue

            // 4. Quedarse con el más grande
            if (area > bestArea) {
                bestArea    = area
                bestContour = approx
            }
        }

        return bestContour
    }

    /**
     * Ordena los 4 puntos en orden canónico:
     * [0] top-left, [1] top-right, [2] bottom-right, [3] bottom-left
     *
     * Esto es necesario para que la homografía siempre mapee
     * los mismos puntos de origen a los mismos destinos.
     */
    private fun orderCorners(points: Array<Point>): List<ScreenPoint> {
        // [0] TL, [1] TR, [2] BR, [3] BL
        val sortedBySum  = points.sortedBy { it.x + it.y }
        val topLeft      = sortedBySum.first()
        val bottomRight  = sortedBySum.last()

        val remaining    = points.filter { it != topLeft && it != bottomRight }
        val sortedByDiff = remaining.sortedBy { it.x - it.y }
        
        val bottomLeft   = sortedByDiff.first()
        val topRight     = sortedByDiff.last()

        return listOf(
            ScreenPoint(topLeft.x.toFloat(),     topLeft.y.toFloat()),
            ScreenPoint(topRight.x.toFloat(),    topRight.y.toFloat()),
            ScreenPoint(bottomRight.x.toFloat(), bottomRight.y.toFloat()),
            ScreenPoint(bottomLeft.x.toFloat(),  bottomLeft.y.toFloat())
        )
    }

    /**
     * Calcula una confianza entre 0f y 1f basada en el área relativa del contorno.
     * Un rectángulo que ocupa el 80%+ del frame obtiene confianza ~1.0.
     * Uno que ocupa el 10% (mínimo) obtiene ~0.0.
     */
    private fun calculateConfidence(contourArea: Double, frameArea: Double): Float {
        val ratio = (contourArea / frameArea).toFloat()
        // Normalizar entre MIN_AREA_RATIO y 0.9 (no esperamos que ocupe el 100%)
        return ((ratio - MIN_AREA_RATIO) / (0.9f - MIN_AREA_RATIO))
            .toFloat()
            .coerceIn(0f, 1f)
    }

    /**
     * Libera los Mat reutilizables. Llamar cuando el ViewModel se destruye.
     */
    fun release() {
        matRgba.release()
        matGray.release()
        matBlur.release()
        matEdges.release()
    }
}

package com.meminzazo.drawar.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

class DetectEdgesUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val MAX_SIZE = 1024   // límite para no agotar memoria
        private const val THRESHOLD = 80    // sensibilidad del detector (0-255)
    }

    suspend operator fun invoke(uri: Uri): Bitmap = withContext(Dispatchers.Default) {
        val source = loadScaledBitmap(uri)
        applySobel(source)
    }

    // ─────────────────────────────────────────
    // Carga y escala la imagen para no agotar RAM
    // ─────────────────────────────────────────

    private fun loadScaledBitmap(uri: Uri): Bitmap {
        // Primera pasada — solo leer dimensiones
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        // Calcular subsampling
        val scale = min(options.outWidth, options.outHeight).toFloat() / MAX_SIZE
        val sampleSize = if (scale > 1f) scale.roundToInt() else 1

        // Segunda pasada — decodificar con escala
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inMutable = true
        }

        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOptions)
        } ?: throw IllegalArgumentException("No se pudo leer la imagen")
    }

    // ─────────────────────────────────────────
    // Algoritmo Sobel — detecta bordes por gradiente
    // ─────────────────────────────────────────

    private fun applySobel(source: Bitmap): Bitmap {
        val w = source.width
        val h = source.height

        // Leer todos los píxeles de una sola vez (más eficiente que getPixel en loop)
        val pixels = IntArray(w * h)
        source.getPixels(pixels, 0, w, 0, 0, w, h)

        // Convertir a escala de grises
        val gray = IntArray(w * h) { i ->
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8)  and 0xFF
            val b =  p         and 0xFF
            // Luminancia perceptual
            ((0.299 * r) + (0.587 * g) + (0.114 * b)).toInt()
        }

        // Kernels Sobel
        val output = IntArray(w * h) { 0xFF000000.toInt() } // fondo negro opaco

        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                // Vecinos
                val tl = gray[(y - 1) * w + (x - 1)]
                val tc = gray[(y - 1) * w +  x     ]
                val tr = gray[(y - 1) * w + (x + 1)]
                val ml = gray[ y      * w + (x - 1)]
                val mr = gray[ y      * w + (x + 1)]
                val bl = gray[(y + 1) * w + (x - 1)]
                val bc = gray[(y + 1) * w +  x     ]
                val br = gray[(y + 1) * w + (x + 1)]

                // Gradiente horizontal y vertical
                val gx = -tl + tr - 2 * ml + 2 * mr - bl + br
                val gy = -tl - 2 * tc - tr + bl + 2 * bc + br

                val magnitude = sqrt((gx * gx + gy * gy).toDouble()).toInt()
                    .coerceIn(0, 255)

                if (magnitude > THRESHOLD) {
                    // Borde encontrado — píxel blanco
                    output[y * w + x] = 0xFFFFFFFF.toInt()
                }
                // Si no supera el umbral → permanece negro (ya inicializado)
            }
        }

        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            setPixels(output, 0, w, 0, 0, w, h)
        }
    }
}
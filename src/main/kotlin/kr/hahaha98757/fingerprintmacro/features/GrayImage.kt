package kr.hahaha98757.fingerprintmacro.features

import java.awt.image.BufferedImage
import kotlin.math.abs

class GrayImage(image: BufferedImage) {
    val width = image.width
    val height = image.height
    val pixels = IntArray(width * height)

    init {
        for (y in 0..<height) {
            for (x in 0..<width) {
                val rgb = image.getRGB(x, y)
                val r = (rgb shr 16) and 0xFF
                val g = (rgb shr 8) and 0xFF
                val b = rgb and 0xFF
                val gray = (r + g + b) / 3
                pixels[y * width + x] = gray
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GrayImage) return false
        if (width != other.width || height != other.height) return false
        return pixels.contentEquals(other.pixels)
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + pixels.contentHashCode()
        return result
    }
}

fun getSimilarity(img1: GrayImage, img2: GrayImage, tolerance: Int): Double {
    if (img1.width != img2.width || img1.height != img2.height) return 0.0

    val pixels1 = img1.pixels
    val pixels2 = img2.pixels
    val pixelCount = pixels1.size

    var similar = 0
    for (i in 0..<pixelCount) if (abs(pixels1[i] - pixels2[i]) <= tolerance) similar++
    return similar.toDouble() / pixelCount
}

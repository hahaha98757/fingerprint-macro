package kr.hahaha98757.fingerprintmacro


import java.awt.Color
import java.awt.GraphicsEnvironment
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs

object Feature {
    private val patterns = (1..16).map { ImageIO.read(Feature::class.java.getResourceAsStream("/patterns/$it.png")) }.toTypedArray()

    fun run(test: Boolean = false) {
        if (lock) return
        lock = true
        val screen = getScreenshot()
        if (Setting.saveImages) createPngImage(screen, "images/screenshot.png")

        val pieceWidth = 116 // 조각 크기
        val pieceHeight = 116
        val startX = 476 // 조각 시작 좌표
        val startY = 272
        val gapX = 144 // 조각 간 간격
        val gapY = 144

        val result = mutableListOf<Boolean>()
        var count = 0
        var imageNo = 1

        for (row in 0 until 4) for (col in 0 until 2) {
            val x = startX + col * gapX
            val y = startY + row * gapY
            val piece = screen.getSubimage(x, y, pieceWidth, pieceHeight)
            if (Setting.saveImages) createPngImage(piece, "images/pieces/${imageNo++}.png")
            result += matchesAnyPattern(piece).also { if (it) count++ }
        }

        for ((i, bool) in result.withIndex()) if (i % 2 == 0) print("$bool    ") else println(bool)

        if (count != 4) return

        var enter = 0
        var skip = false
        for ((i, bool) in result.withIndex()) {
            if (skip) {
                skip = false
                continue
            }
            if (bool) {
                inputKey(KeyEvent.VK_ENTER, test)
                enter++
            }
            if (enter == 4) {
                inputKey(KeyEvent.VK_TAB, test)
                break
            }
            if (!result[i+1]) {
                skip = true
                inputKey(KeyEvent.VK_DOWN, test)
            } else inputKey(KeyEvent.VK_RIGHT, test)
        }
        println()
        println("매크로 실행 완료")
        lock = false
    }

    private fun matchesAnyPattern(target: BufferedImage) = patterns.any { template -> imagesAreSimilarHSV(template, target, tolerance = 30, threshold = 0.8f) }

    @Suppress("SameParameterValue")
    private fun imagesAreSimilarHSV(img1: BufferedImage, img2: BufferedImage, tolerance: Int, threshold: Float): Boolean {
        if (img1.width != img2.width || img1.height != img2.height) return false

        val total = img1.width * img1.height
        var similar = 0

        for (y in 0 until img1.height) for (x in 0 until img1.width) {
            val hsv1 = rgbToHSV(img1.getRGB(x, y))
            val hsv2 = rgbToHSV(img2.getRGB(x, y))

            val ds = abs(hsv1[1] - hsv2[1])
            val dv = abs(hsv1[2] - hsv2[2])

            if (dv < tolerance && ds < tolerance) similar++
        }
        val ratio = similar.toFloat() / total
        return ratio >= threshold
    }

    private fun rgbToHSV(rgb: Int): FloatArray {
        val r = (rgb shr 16 and 0xFF) / 255f
        val g = (rgb shr 8 and 0xFF) / 255f
        val b = (rgb and 0xFF) / 255f
        val hsv = FloatArray(3)
        Color.RGBtoHSB((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt(), hsv)
        hsv[0] *= 360f
        hsv[1] *= 100f
        hsv[2] *= 100f
        return hsv
    }

    private fun getScreenshot(): BufferedImage {
        val device = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices[Setting.display - 1]
        val bounds = device.defaultConfiguration.bounds
        return robot.createScreenCapture(bounds)
    }

    private fun createPngImage(image: BufferedImage, path: String) {
        val file = File(root, path)
        if (!file.exists()) file.mkdirs()
        ImageIO.write(image, "png", file)
    }
}
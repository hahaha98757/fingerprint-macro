package kr.hahaha98757.fingerprintmacro

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.HWND
import java.awt.Color
import java.awt.Rectangle
import java.awt.Robot
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.io.path.pathString
import kotlin.io.path.toPath
import kotlin.math.abs

object Feature {
    private lateinit var hWnd: HWND
    private val patterns = (1..16).map { ImageIO.read(Feature::class.java.getResourceAsStream("/patterns/$it.png")) }.toTypedArray()

    fun init(hWnd: HWND) {
        try {
            Feature.hWnd = hWnd
        } catch (e: Exception) {
            throw ExceptionInInitializerError(e)
        }
    }

    fun run(first: Boolean = false) {
        val screen = getScreenshot()
        if (Setting.debugMode && !first) createPngImage(screen, "debug/screenshot.png")

        val pieceWidth = 116 //이미지 크기
        val pieceHeight = 116
        val startX = 476 //시작 좌표
        val startY = 272
        val gapX = 144 //이미지 간 간격
        val gapY = 144

        val result = mutableListOf<Boolean>()
        var imageNo = 1

        for (row in 0 until 4) for (col in 0 until 2) {
            val x = startX + col * gapX
            val y = startY + row * gapY
            val piece = screen.getSubimage(x, y, pieceWidth, pieceHeight)
            if (Setting.debugMode && !first) createPngImage(piece, "debug/pieces/${imageNo++}.png")
            result.add(matchesAnyPattern(piece))
        }

        if (Setting.debugMode && !first) for ((i, b) in result.withIndex()) if (i % 2 == 0) print("$b    ") else println(b)

        var t = 0
        for (b in result) if (b) t++
        if (t != 4) return

        if (first) return
        var enter = 0
        val robot = Robot()
        var skip = false
        for ((i, b) in result.withIndex()) {
            if (skip) {
                skip = false
                continue
            }
            if (b) {
                robot.inputKey(KeyEvent.VK_ENTER)
                enter++
            }
            if (i == 15 || enter == 4) {
                robot.inputKey(KeyEvent.VK_TAB)
                break
            }
            if (!result[i+1]) {
                skip = true
                robot.inputKey(KeyEvent.VK_DOWN)
            } else robot.inputKey(KeyEvent.VK_RIGHT)
        }
        if (Setting.debugMode) println()
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
        val rect = WinDef.RECT()
        User32.INSTANCE.GetWindowRect(hWnd, rect)
        val width = rect.right - rect.left
        val height = rect.bottom - rect.top

        val robot = Robot()
        return robot.createScreenCapture(Rectangle(rect.left, rect.top, width, height))
    }

    private fun createPngImage(image: BufferedImage, path: String) {
        val realFile = File(Feature::class.java.protectionDomain.codeSource.location.toURI().toPath().parent.pathString, path)
        if (!realFile.exists()) realFile.mkdirs()
        ImageIO.write(image, "png", realFile)
    }
}
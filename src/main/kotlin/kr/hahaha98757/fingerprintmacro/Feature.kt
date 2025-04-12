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
    private var debug = false

    fun toggleDebug() {
        debug = !debug
        println("디버그 모드를 ${if (debug) "켰" else "껐"}습니다.")
    }

    fun init(hWnd: HWND) {
        try {
            Feature.hWnd = hWnd
        } catch (e: Exception) {
            throw ExceptionInInitializerError(e)
        }
    }

    fun run() {
        val screen = getScreenshot()
        if (debug) createPngImage(screen, File("debug/screenshot.png"))

        val patternWidth = 116 //이미지 크기
        val patternHeight = 116
        val startX = 476 //시작 좌표
        val startY = 272
        val gapX = 144 //이미지 간 간격
        val gapY = 144

        val result = mutableListOf<Boolean>()

        var imgNum = 1

        for (row in 0 until 4) for (col in 0 until 2) {
            val x = startX + col * gapX
            val y = startY + row * gapY
            val subImage = screen.getSubimage(x, y, patternWidth, patternHeight)
            if (debug) createPngImage(subImage, File("debug/patterns/${imgNum++}.png"))
            result.add(matchesAnyPattern(subImage))
        }

        if (debug) for ((i, b) in result.withIndex()) if (i % 2 == 0) print("$b    ") else println(b)

        var t = 0
        for (b in result) if (b) t++
        if (t != 4) return

        var enter = 0
        val robot = Robot()
        var skip = false
        for ((i, b) in result.withIndex()) {
            if (skip) {
                skip = false
                continue
            }
            if (b) {
                robot.keyPress(KeyEvent.VK_ENTER)
                robot.keyRelease(KeyEvent.VK_ENTER)
                if (debug) print("enter ")
                enter++
            }
            if (i == 15 || enter == 4) {
                robot.keyPress(KeyEvent.VK_TAB)
                robot.keyRelease(KeyEvent.VK_TAB)
                if (debug) print("tab ")
                break
            }
            if (!result[i+1]) {
                skip = true
                robot.keyPress(KeyEvent.VK_DOWN)
                robot.keyRelease(KeyEvent.VK_DOWN)
                if (debug) print("down ")
            } else {
                robot.keyPress(KeyEvent.VK_RIGHT)
                robot.keyRelease(KeyEvent.VK_RIGHT)
                if (debug) print("right ")
            }
        }
        if (debug) println()
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

    private fun createPngImage(image: BufferedImage, file: File) {
        val realFile = File(Feature::class.java.protectionDomain.codeSource.location.toURI().toPath().parent.pathString, file.path)
        if (!realFile.exists()) realFile.mkdirs()
        ImageIO.write(image, "png", realFile)
    }
}
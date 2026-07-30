package kr.hahaha98757.fingerprintmacro.features

import kr.hahaha98757.fingerprintmacro.Setting
import kr.hahaha98757.fingerprintmacro.printErr
import kr.hahaha98757.fingerprintmacro.robot
import kr.hahaha98757.fingerprintmacro.root
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.roundToInt

object Capture {
    private const val BASE_WIDTH = 1920
    private const val BASE_HEIGHT = 1080

    private const val PIECE_WIDTH = 116
    private const val PIECE_HEIGHT = 116
    private const val START_X = 476
    private const val START_Y = 272
    private const val GAP_X = 144
    private const val GAP_Y = 144

    private val scaleX get() = bounds.width.toDouble() / BASE_WIDTH
    private val scaleY get() = bounds.height.toDouble() / BASE_HEIGHT

    val pieceWidth get() = (PIECE_WIDTH * scaleX).roundToInt()
    val pieceHeight get() = (PIECE_HEIGHT * scaleY).roundToInt()
    val startX get() = (START_X * scaleX).roundToInt()
    val startY get() = (START_Y * scaleY).roundToInt()
    val gapX get() = (GAP_X * scaleX).roundToInt()
    val gapY get() = (GAP_Y * scaleY).roundToInt()

    private lateinit var bounds: Rectangle

    fun getPieces(saveImage: Boolean = false): List<GrayImage> {
        val screen = robot.createScreenCapture(bounds)
//        val screen = ImageIO.read(Capture::class.java.getResourceAsStream("/screen.png"))!! // 테스트용
        if (saveImage) saveImage(screen, "images/screenshot.png")
        val pieces = mutableListOf<GrayImage>()
        var imageNo = 1

        for (row in 0 until 4) for (col in 0 until 2) {
            val x = startX + col * gapX
            val y = startY + row * gapY
            val piece = screen.getSubimage(x, y, pieceWidth, pieceHeight)
            if (saveImage) saveImage(piece, "images/pieces/${imageNo++}.png")
            pieces += GrayImage(piece)
        }
        return pieces
    }

    private fun saveImage(image: BufferedImage, path: String) {
        val file = File(root, path)
        file.parentFile.mkdirs()
        ImageIO.write(image, "png", file)
    }

    fun initOrReload() {
        val device = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices
        val index = if (Setting.display <= device.size) Setting.display - 1 else {
            printErr("설정된 디스플레이 번호가 잘못되었습니다. 1번 디스플레이를 사용합니다.")
            0
        }
        bounds = device[index].defaultConfiguration.bounds
        println("설정된 디스플레이: ${index + 1}번, 해상도: ${bounds.width}x${bounds.height}")
    }
}
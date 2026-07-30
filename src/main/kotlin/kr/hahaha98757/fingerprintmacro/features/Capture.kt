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

    private const val PIECE_X = 116
    private const val PIECE_Y = 116
    private const val START_X = 476
    private const val START_Y = 272
    private const val GAP_X = 144
    private const val GAP_Y = 144

    private val scaleX get() = bounds.width.toDouble() / BASE_WIDTH
    private val scaleY get() = bounds.height.toDouble() / BASE_HEIGHT

    val pieceX get() = (PIECE_X * scaleX).roundToInt()
    val pieceY get() = (PIECE_Y * scaleY).roundToInt()
    val startX get() = (START_X * scaleX).roundToInt()
    val startY get() = (START_Y * scaleY).roundToInt()
    val gapX get() = (GAP_X * scaleX).roundToInt()
    val gapY get() = (GAP_Y * scaleY).roundToInt()

    private lateinit var bounds: Rectangle

    fun getPieces(saveImage: Boolean = false): List<GrayImage> {
        val pieceW = pieceX
        val pieceH = pieceY
        val gapW = gapX
        val gapH = gapY
        val captureRect = Rectangle(
            bounds.x + startX,
            bounds.y + startY,
            gapW + pieceW,
            3 * gapH + pieceH
        )
        val screen = robot.createScreenCapture(captureRect)
//        val screen = ImageIO.read(Capture::class.java.getResourceAsStream("/screen.png"))!! // 테스트용
        if (saveImage) saveImage(screen, "images/screenshot.png")
        val pieces = ArrayList<GrayImage>(8)
        var imageNo = 1

        for (row in 0..<4) for (col in 0..<2) {
            val x = col * gapW
            val y = row * gapH
            val piece = screen.getSubimage(x, y, pieceW, pieceH)
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
        val index = if (Setting.display in 1..device.size) Setting.display - 1 else {
            printErr("설정된 디스플레이 번호가 잘못되었습니다. 1번 디스플레이를 사용합니다.")
            0
        }
        bounds = device[index].defaultConfiguration.bounds
        println("설정된 디스플레이: ${index + 1}번, 해상도: ${bounds.width}x${bounds.height}")
    }
}
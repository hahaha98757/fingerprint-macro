package kr.hahaha98757.fingerprintmacro.features

import kr.hahaha98757.fingerprintmacro.Setting
import kr.hahaha98757.fingerprintmacro.lock
import kr.hahaha98757.fingerprintmacro.playTone
import kr.hahaha98757.fingerprintmacro.root
import kr.hahaha98757.fingerprintmacro.tryLock
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object Feature {
    private val patterns = (1..16).map {
        GrayImage(ImageIO.read(Feature::class.java.getResourceAsStream("/patterns/$it.png")))
    }.toTypedArray()

    fun run(test: Boolean = false, init: Boolean = false) {
        if (!tryLock()) return
        if (!init)
            if (!test) println("매크로를 시작합니다.")
            else {
                println("테스트를 시작합니다.")
                playTone(1000.0, 200, 0.1)
            }

        try {
            val screen = Capture.screenshot()
            if (!init && Setting.debug) saveImage(screen, "images/screenshot.png")

            val pieceWidth = 116 // 조각 크기
            val pieceHeight = 116
            val startX = 476 // 조각 시작 좌표
            val startY = 272
            val gapX = 144 // 조각 간 간격
            val gapY = 144

            val pieces = mutableListOf<GrayImage>()
            var imageNo = 1

            for (row in 0 until 4) for (col in 0 until 2) {
                val x = startX + col * gapX
                val y = startY + row * gapY
                val piece = screen.getSubimage(x, y, pieceWidth, pieceHeight)
                if (!init && Setting.debug) saveImage(piece, "images/pieces/${imageNo++}.png")
                pieces += GrayImage(piece)
            }

            val result = mutableListOf<Boolean>()
            var count = 0

            for (piece in pieces) {
                val arr = DoubleArray(16)
                patterns.forEachIndexed { index, image ->
                    arr[index] = getSimilarity(piece, image, Setting.tolerance)
                }
                if (!init && Setting.debug) {
                    val max = arr.maxOrNull() ?: 0.0
                    val secMax = arr.filter { it != max }.maxOrNull() ?: 0.0
                    val min = arr.minOrNull() ?: 0.0
                    println("Max: ${String.format("%.2f", max * 100)}%, Second Max: ${String.format("%.2f", secMax * 100)}%, Min: ${String.format("%.2f", min * 100)}%")
                }
                val maxSimilarity = arr.maxOrNull() ?: 0.0
                if (maxSimilarity >= Setting.threshold) {
                    result += true
                    count++
                } else result += false
            }

            if (init) return

            for ((i, bool) in result.withIndex()) if (i % 2 == 0) print("$bool    ") else println(bool)

            if (count != 4) {
                println()
                println("인식 실패")
                return
            }

            InputHandler.run(result.toBooleanArray(), test)
            println()
            if (!test) println("매크로 실행 완료")
            else println("테스트 완료")
            println()
        } finally {
            lock.set(false)
        }
    }

    private fun saveImage(image: BufferedImage, path: String) {
        val file = File(root, path)
        file.parentFile.mkdirs()
        ImageIO.write(image, "png", file)
    }
}
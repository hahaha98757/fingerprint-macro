package kr.hahaha98757.fingerprintmacro.features

import kr.hahaha98757.fingerprintmacro.Setting
import kr.hahaha98757.fingerprintmacro.lock
import kr.hahaha98757.fingerprintmacro.playTone
import kr.hahaha98757.fingerprintmacro.tryLock
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object Feature {
    private val originalPatterns = (1..16).map { ImageIO.read(Feature::class.java.getResourceAsStream("/patterns/$it.png"))!! }.toTypedArray()
    private lateinit var patterns: Array<GrayImage>

    fun initOrReload() {
        Capture.initOrReload()
        patterns = originalPatterns.map { GrayImage(resizeImage(it, Capture.pieceWidth, Capture.pieceHeight)) }.toTypedArray()
        run(init = true)
    }

    private fun resizeImage(image: BufferedImage, width: Int, height: Int): BufferedImage {
        if (image.width == width && image.height == height) return image
        val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = resized.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g.drawImage(image, 0, 0, width, height, null)
        g.dispose()
        return resized
    }

    fun run(test: Boolean = false, init: Boolean = false) {
        if (!init && !tryLock()) return
        if (!init)
            if (!test) println("매크로를 시작합니다.")
            else {
                println("테스트를 시작합니다.")
                playTone(1000.0, 200, 0.1)
            }

        try {
            val pieces = Capture.getPieces(!init && Setting.saveImage)

            val result = mutableListOf<Boolean>()
            var count = 0

            for (piece in pieces) {
                val arr = DoubleArray(16)
                patterns.forEachIndexed { index, image ->
                    arr[index] = getSimilarity(piece, image, Setting.tolerance)
                }
                if (!init && Setting.similarity) {
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
            if (!init) lock.set(false)
        }
    }
}
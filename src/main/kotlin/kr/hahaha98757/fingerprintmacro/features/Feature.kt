package kr.hahaha98757.fingerprintmacro.features

import kr.hahaha98757.fingerprintmacro.*
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object Feature {
    private val originalPatterns = (1..16).map { ImageIO.read(Feature::class.java.getResourceAsStream("/patterns/$it.png"))!! }.toTypedArray()
    private lateinit var patterns: Array<GrayImage>

    fun initOrReload() {
        patterns = originalPatterns.map { GrayImage(resizeImage(it, Capture.pieceX, Capture.pieceY)) }.toTypedArray()
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
        try {
            val start = System.nanoTime()
            if (!init && !tryLock()) return

            if (!init)
                if (!test) println("매크로를 시작합니다.")
                else {
                    println("테스트를 시작합니다.")
                    playTone(1000.0, 200, 0.1)
                }
            val pieces = Capture.getPieces(!init && Setting.saveImage)
            val tolerance = Setting.tolerance
            val threshold = Setting.threshold
            val similarityDebugEnabled = !init && Setting.similarity
            val result = BooleanArray(pieces.size)
            var count = 0

            for (pieceIndex in pieces.indices) {
                val piece = pieces[pieceIndex]
                var maxSimilarity = 0.0

                if (similarityDebugEnabled) {
                    val scores = DoubleArray(patterns.size)
                    var secondMax = 0.0
                    for (patternIndex in patterns.indices) {
                        val similarity = getSimilarity(piece, patterns[patternIndex], tolerance)
                        scores[patternIndex] = similarity
                        if (similarity > maxSimilarity) {
                            secondMax = maxSimilarity
                            maxSimilarity = similarity
                        } else if (similarity > secondMax) {
                            secondMax = similarity
                        }
                    }
                    val max = maxSimilarity
                    val secMax = secondMax
                    val min = scores.minOrNull() ?: 0.0
                    printDebug("Max: ${String.format("%.2f", max * 100)}%, Second Max: ${String.format("%.2f", secMax * 100)}%, Min: ${String.format("%.2f", min * 100)}%")
                } else {
                    for (pattern in patterns) {
                        val similarity = getSimilarity(piece, pattern, tolerance)
                        if (similarity >= threshold) {
                            maxSimilarity = similarity
                            break
                        }
                        if (similarity > maxSimilarity) maxSimilarity = similarity
                    }
                }

                val matched = maxSimilarity >= threshold
                result[pieceIndex] = matched
                if (matched) count++
            }

            if (init) return

            if (Setting.debug) {
                val debugOutput = buildString {
                    append("조각 인식 결과:\n")
                    for (index in result.indices) {
                        if (index > 0 && index % 2 == 0) append('\n')
                        append(if (result[index]) "O" else "X")
                        if (index % 2 == 0) append("    ")
                    }
                }
                printDebug(debugOutput)
            }

            if (count != 4) {
                printErr("인식 실패: 인식된 조각 수가 4개가 아닙니다. (${count}개)")
                return
            }

            InputHandler.run(result, test)
            val elapsedTime = (System.nanoTime() - start) / 1_000_000.0
            printDebug("소요 시간: ${"%.2f".format(elapsedTime)}ms")
            if (!test) println("매크로 실행 완료")
            else println("테스트 완료")
            println()
        } finally {
            if (!init) lock.set(false)
        }
    }
}
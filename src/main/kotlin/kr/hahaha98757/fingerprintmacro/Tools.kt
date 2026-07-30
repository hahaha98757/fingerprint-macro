package kr.hahaha98757.fingerprintmacro

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import java.awt.Robot
import java.awt.event.KeyEvent
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import kotlin.math.sin

val robot = Robot()

val root = File(object {}::class.java.protectionDomain.codeSource.location.toURI()).parentFile!!

fun inputKey(keyCode: Int, test: Boolean = false): String {
    if (!test) {
        robot.keyPress(keyCode)
        if (Setting.pressingTimes > 0) Thread.sleep(Setting.pressingTimes)
        robot.keyRelease(keyCode)
        if (Setting.inputDelays > 0) Thread.sleep(Setting.inputDelays)
    }
    return "${KeyEvent.getKeyText(keyCode)} "
}

fun Int.getHotKeyText(): String = NativeKeyEvent.getKeyText(this)

fun printDebug(text: String) {
    if (Setting.debug) println("[DEBUG] $text")
}

fun printDebug() {
    if (Setting.debug) println()
}

fun printErr(text: String) {
    println("[ERROR] $text")
}

fun playTone(frequency: Double, durationMs: Int, volume: Double = 1.0) = Thread {
    val sampleRate = 44100f
    val samples = (durationMs / 1000.0 * sampleRate).toInt()
    val buffer = ByteArray(samples)

    for (i in buffer.indices) {
        val angle = 2.0 * Math.PI * i * frequency / sampleRate
        buffer[i] = (sin(angle) * 127 * volume).toInt().toByte()
    }

    val format = AudioFormat(sampleRate, 8, 1, true, false) // 8bit, mono, signed, little endian
    val line = AudioSystem.getSourceDataLine(format)
    line.open(format)
    line.start()
    line.write(buffer, 0, buffer.size)
    line.drain()
    line.stop()
    line.close()
}.start()

fun tryLock() = if (!lock.compareAndSet(false, true)) {
    printErr("이미 매크로가 실행 중이거나, 설정을 불러오는 중입니다.")
    false
} else true
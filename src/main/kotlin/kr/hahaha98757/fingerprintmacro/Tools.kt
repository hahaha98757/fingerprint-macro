package kr.hahaha98757.fingerprintmacro

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import java.awt.Robot
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import kotlin.math.sin

fun Robot.inputKey(keyCode: Int) {
    this.keyPress(keyCode)
    Thread.sleep(Setting.pressingTimes)
    this.keyRelease(keyCode)
    Thread.sleep(Setting.inputDelays)
    if (Setting.debugMode) print(keyCode.getKeyText() + " ")
}

fun getKeyCode(name: String): Int {
    val fieldName = "VC_" + name.uppercase()
    return try {
        val field = NativeKeyEvent::class.java.getField(fieldName)
        field.getInt(null)
    } catch (e: Exception) {
        e.printStackTrace()
        System.err.println("알 수 없는 키")
        0
    }
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
package kr.hahaha98757.fingerprintmacro

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import java.awt.Robot

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
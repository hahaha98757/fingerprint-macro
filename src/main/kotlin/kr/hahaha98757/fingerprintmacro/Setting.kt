package kr.hahaha98757.fingerprintmacro

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import java.io.File

object Setting {
    var legacyMode = false
    var debugMode = false
    var pressingTimes = 8L
    var inputDelays = 8L
    var exit = NativeKeyEvent.VC_F4
    var reload = NativeKeyEvent.VC_F5
    var start = NativeKeyEvent.VC_F6
    var test = NativeKeyEvent.VC_F7

    fun loadSetting() {
        println("설정을 불러오는 중...")
        legacyMode = false
        debugMode = false
        pressingTimes = 8L
        inputDelays = 8L
        exit = NativeKeyEvent.VC_F4
        reload = NativeKeyEvent.VC_F5
        start = NativeKeyEvent.VC_F6
        test = NativeKeyEvent.VC_F7

        val file = File(File(Setting::class.java.protectionDomain.codeSource.location.toURI()).parentFile, "setting.ini")
        if (!file.exists()) {
            file.createNewFile()
            file.writeText("""
                # 키의 이름은 https://javadoc.io/static/com.1stleg/jnativehook/2.1.0/org/jnativehook/keyboard/NativeKeyEvent.html 에서 'VC_' 뒤의 이름을 "있는 그대로" 사용합니다.
                [general]
                legacyMode = false
                debugMode = false
                pressingTimes = 8
                inputDelays = 8

                [hotkeys]
                exit = F4
                reload = F5
                start = F6
                test = F7
            """.trimIndent())
        }
        file.forEachLine {
            try {
                val trimmed = it.trim()
                if (trimmed.startsWith("#") || trimmed.startsWith(";") || trimmed.isBlank()) return@forEachLine

                val parts = trimmed.split("=", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim()
                    val value = parts[1].trim()

                    when (key) {
                        "legacyMode" -> legacyMode = value.toBoolean()
                        "debugMode" -> debugMode = value.toBoolean()
                        "pressingTimes" -> pressingTimes = value.toLong()
                        "inputDelays" -> inputDelays = value.toLong()
                        "exit" -> exit = getKeyCode(value)
                        "reload" -> reload = getKeyCode(value)
                        "start" -> start = getKeyCode(value)
                        "test" -> test = getKeyCode(value)
                    }
                }
            } catch (_: Exception) {}
        }

        println("설정을 불러왔습니다.")
        println("legacyMode: $legacyMode")
        println("debugMode: $debugMode")
        println("pressingTimes: $pressingTimes")
        println("inputDelays: $inputDelays")
        println()
        println("exit: ${exit.getKeyText()}")
        println("reload: ${reload.getKeyText()}")
        println("start: ${start.getKeyText()}")
        println("test: ${test.getKeyText()}")
    }
}

fun Int.getKeyText(): String = NativeKeyEvent.getKeyText(this)
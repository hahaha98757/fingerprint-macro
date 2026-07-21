package kr.hahaha98757.fingerprintmacro

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.system.exitProcess

@Volatile
var lock = false

fun main() {
    println("Copyright (c) 2025 hahaha98757 (MIT License)")
    println("Fingerprint Macro v1.1.3")
    println("공식 사이트: https://github.com/hahaha98757/fingerprint-macro")
    println()
    Thread.sleep(1000)

    Setting.loadSetting()

    println()
    println("'${Setting.exit.getKeyText()}' 키를 눌러 매크로를 종료합니다.")
    println("'${Setting.reload.getKeyText()}' 키를 눌러 설정을 다시 불러옵니다.")
    println("'${Setting.start.getKeyText()}' 키를 눌러 매크로를 시작합니다.")
    println("'${Setting.test.getKeyText()}' 키를 눌러 테스트를 할 수 있습니다.")

    playTone(1000.0, 200, 0.1)

    Thread {
        LogManager.getLogManager().reset()
        Logger.getLogger(GlobalScreen::class.java.packageName).level = Level.OFF

        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(object: NativeKeyListener {
            val pressedKeys = mutableSetOf<Int>()

            override fun nativeKeyPressed(event: NativeKeyEvent) {
                if (pressedKeys.add(event.keyCode)) when (event.keyCode) {
                    Setting.exit -> {
                        if (isLockedAndPrint()) return
                        println("매크로를 종료합니다.")
                        GlobalScreen.unregisterNativeHook()
                        exitProcess(0)
                    }
                    Setting.reload -> {
                        if (isLockedAndPrint()) return
                        Setting.loadSetting()
                    }
                    Setting.start -> {
                        if (isLockedAndPrint()) return
                        println("매크로를 시작합니다.")
                        Thread { Feature.run() }.start()
                    }
                    Setting.test -> {
                        if (isLockedAndPrint()) return
                        println("테스트를 시작합니다.")
                        playTone(1000.0, 200, 0.1)
                        Thread { Feature.run(true) }.start()
                    }
                }
            }
            override fun nativeKeyReleased(event: NativeKeyEvent) {
                pressedKeys -= event.keyCode
            }
        })
    }.start()
    Feature.run(true)
}
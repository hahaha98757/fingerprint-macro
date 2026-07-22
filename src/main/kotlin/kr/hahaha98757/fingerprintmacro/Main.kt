package kr.hahaha98757.fingerprintmacro

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import kr.hahaha98757.fingerprintmacro.features.Feature
import kr.hahaha98757.fingerprintmacro.features.InputHandler
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.system.exitProcess

var lock = AtomicBoolean(false)

fun main() {
    println("Copyright (c) 2025 hahaha98757 (MIT License)")
    println("Fingerprint Macro v1.1.3")
    println("공식 사이트: https://github.com/hahaha98757/fingerprint-macro")
    println()
    Thread.sleep(1000)

    Setting.loadSetting()
    println()
    InputHandler.init()
    println()
    Feature.run(true, init = true)

    Thread {
        LogManager.getLogManager().reset()
        Logger.getLogger(GlobalScreen::class.java.packageName).level = Level.OFF

        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(object: NativeKeyListener {
            val pressedKeys = mutableSetOf<Int>()

            override fun nativeKeyPressed(event: NativeKeyEvent) {
                if (pressedKeys.add(event.keyCode)) when (event.keyCode) {
                    Setting.exit -> {
                        if (!tryLock()) return
                        println("매크로를 종료합니다.")
                        GlobalScreen.unregisterNativeHook()
                        exitProcess(0)
                    }
                    Setting.reload -> Setting.loadSetting()
                    Setting.start -> Thread { Feature.run() }.start()
                    Setting.test -> Thread { Feature.run(true) }.start()
                }
            }
            override fun nativeKeyReleased(event: NativeKeyEvent) {
                pressedKeys -= event.keyCode
            }
        })
    }.start()

    println()
    println("'${Setting.exit.getHotKeyText()}' 키를 눌러 매크로를 종료합니다.")
    println("'${Setting.reload.getHotKeyText()}' 키를 눌러 설정을 다시 불러옵니다.")
    println("'${Setting.start.getHotKeyText()}' 키를 눌러 매크로를 시작합니다.")
    println("'${Setting.test.getHotKeyText()}' 키를 눌러 테스트를 할 수 있습니다.")

    println("매크로 준비 완료")
    playTone(1000.0, 200, 0.1)
}
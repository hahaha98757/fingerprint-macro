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
    println("Fingerprint Macro v1.2.0")
    println("공식 사이트: https://github.com/hahaha98757/fingerprint-macro")
    println()
    Thread.sleep(1000)

    Setting.loadSetting()
    InputHandler.init()

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

    println("종료: ${Setting.exit.getHotKeyText()}, 설정 불러오기: ${Setting.reload.getHotKeyText()}, 매크로 시작: ${Setting.start.getHotKeyText()}, 테스트: ${Setting.test.getHotKeyText()}")
    println()
    println("매크로 준비 완료")
    playTone(1000.0, 200, 0.1)
}
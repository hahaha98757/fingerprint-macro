package kr.hahaha98757.fingerprintmacro

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.W32APIOptions
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.system.exitProcess

fun main() {
    println("Copyright (c) 2025 hahaha98757 (MIT License)")
    println("Fingerprint Macro v1.1.1")
    println("공식 사이트: https://github.com/hahaha98757/fingerprint-macro")
    println()
    Thread.sleep(1000)

    Setting.loadSetting()
    val target = getPid()
    if (target == -1) {
        System.err.println("게임을 찾는데 실패했습니다.")
        Thread.sleep(2000)
        exitProcess(1)
    }

    User32.INSTANCE.EnumWindows({ hWnd, _ ->
        val pidRef = IntByReference()
        User32.INSTANCE.GetWindowThreadProcessId(hWnd, pidRef)
        val pid = pidRef.value

        val processHandle = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION or WinNT.PROCESS_VM_READ, false, pid) ?: return@EnumWindows true
        val exeName = CharArray(512)
        Psapi.INSTANCE.GetModuleBaseNameW(processHandle, null, exeName, 512)
        Kernel32.INSTANCE.CloseHandle(processHandle)

        if (pid == target) {
            println("게임 발견. (PID: $pid)")
            playTone(1000.0, 200, 0.1)
            Feature.init(hWnd)
            return@EnumWindows false
        }
        true
    }, null)

    println()
    println("'${Setting.exit.getKeyText()}' 키를 눌러 매크로를 종료합니다.")
    println("'${Setting.reload.getKeyText()}' 키를 눌러 설정을 다시 불러옵니다.")
    println("'${Setting.start.getKeyText()}' 키를 눌러 매크로를 시작합니다.")
    println("'${Setting.test.getKeyText()}' 키를 눌러 테스트를 할 수 있습니다.")

    Thread {
        LogManager.getLogManager().reset()
        Logger.getLogger(GlobalScreen::class.java.packageName).level = Level.OFF

        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(object: NativeKeyListener {
            val pressedKeys = mutableSetOf<Int>()

            override fun nativeKeyPressed(event: NativeKeyEvent) {
                if (pressedKeys.add(event.keyCode)) when (event.keyCode) {
                    Setting.exit -> {
                        println("매크로를 종료합니다.")
                        GlobalScreen.unregisterNativeHook()
                        exitProcess(0)
                    }
                    Setting.reload -> Setting.loadSetting()
                    Setting.start -> {
                        println("매크로를 시작합니다.")
                        Thread { Feature.run() }.start()
                    }
                    Setting.test -> {
                        println("테스트를 시작합니다.")
                        playTone(1000.0, 200, 0.1)
                    }
                }
            }
            override fun nativeKeyReleased(event: NativeKeyEvent) {
                pressedKeys.remove(event.keyCode)
            }
        })
    }.start()
    Feature.run(true)
}

fun getPid(): Int {
    val processName = if (Setting.legacyMode) "GTA5.exe" else "GTA5_Enhanced.exe"

    println("'$processName'를 찾는 중...")
    val process = ProcessBuilder("tasklist", "/fi", "imagename eq $processName", "/fo", "csv", "/nh").redirectErrorStream(true).start()

    BufferedReader(InputStreamReader(process.inputStream)).use {
        val line = it.readLine() ?: return -1
        if (line.isBlank() || line.contains("No tasks")) return -1

        val parts = line.split(",")
        if (parts.size >= 2) return parts[1].replace("\"", "").toIntOrNull() ?: -1
    }
    return -1
}

interface Psapi: Library {
    @Suppress("FunctionName")
    fun GetModuleBaseNameW(hProcess: WinNT.HANDLE, hModule: Pointer?, lpBaseName: CharArray, nSize: Int): Int

    companion object {
        val INSTANCE: Psapi = Native.load("psapi", Psapi::class.java, W32APIOptions.UNICODE_OPTIONS)
    }
}
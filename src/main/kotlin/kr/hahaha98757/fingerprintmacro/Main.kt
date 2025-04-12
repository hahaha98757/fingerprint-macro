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
import javax.sound.sampled.AudioSystem
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    println("Copyright (c) 2025 hahaha98757 (MIT License)")
    val target = getPid(try { args[0] } catch (e: Exception) { null })
    if (target == -1) {
        System.err.println("게임을 찾는데 실패했습니다.")
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
            Feature.init(hWnd)
            return@EnumWindows false
        }
        true
    }, null)

    Thread {
        while (true) readlnOrNull()?.let {
            if (it.isNotEmpty()) when (it.uppercase()) {
                "CLS" -> cls()
                "EXIT" -> {
                    println("매크로를 종료합니다.")
                    GlobalScreen.unregisterNativeHook()
                    exitProcess(0)
                }
                "HELP" -> help()
                "DEBUG" -> Feature.toggleDebug()
                else -> System.err.println("'$it'은(는) 명령어가 아닙니다.")
            }
        }
    }.apply { isDaemon = true }.start()

    LogManager.getLogManager().reset()
    Logger.getLogger(GlobalScreen::class.java.packageName).level = Level.OFF

    GlobalScreen.registerNativeHook()
    GlobalScreen.addNativeKeyListener(object: NativeKeyListener {
        val pressedKeys = mutableSetOf<Int>()

        override fun nativeKeyPressed(event: NativeKeyEvent) {
            if (pressedKeys.add(event.keyCode)) if (event.keyCode == NativeKeyEvent.VC_F9) {
                println("매크로를 시작하는 중...")
                Thread {
                    Feature.run()
                }.start()
            } else if (event.keyCode == NativeKeyEvent.VC_F10) {
                println("테스트를 시도합니다.")
                Thread {
                    val audioInputStream = AudioSystem.getAudioInputStream(object {}::class.java.classLoader.getResource("beep.wav"))
                    val clip = AudioSystem.getClip()
                    clip.open(audioInputStream)
                    clip.start()
                    Thread.sleep(clip.microsecondLength / 1000)
                    clip.close()
                }.start()
            }
        }

        override fun nativeKeyReleased(event: NativeKeyEvent) {
            pressedKeys.remove(event.keyCode)
        }
    })
}

fun getPid(str: String?): Int {
    if (str == "legacy") println("'GTA5.exe'를 찾는 중...")
    else println("'GTA5_Enhanced.exe'를 찾는 중...")

    val game = if (str == "legacy") "GTA5.exe" else "GTA5_Enhanced.exe"
    val process = ProcessBuilder("tasklist", "/fi", "imagename eq $game", "/fo", "csv", "/nh")
        .redirectErrorStream(true)
        .start()

    BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
        val line = reader.readLine() ?: return -1
        if (line.isBlank() || line.contains("No tasks")) return -1

        val parts = line.split(",")
        if (parts.size >= 2) {
            return parts[1].replace("\"", "").toIntOrNull() ?: -1
        }
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
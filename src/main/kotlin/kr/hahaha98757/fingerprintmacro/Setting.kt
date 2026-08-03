package kr.hahaha98757.fingerprintmacro

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import kr.hahaha98757.fingerprintmacro.features.Capture
import kr.hahaha98757.fingerprintmacro.features.Feature
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object Setting {
    const val VERSION = 2

    var display = 1
    var pressingTimes = 0L
    var inputDelays = 0L
    var stableDelays = 20L
    var tolerance = 30
    var threshold = 0.8

    var exit = NativeKeyEvent.VC_F4
    var reload = NativeKeyEvent.VC_F5
    var start = NativeKeyEvent.VC_F6
    var test = NativeKeyEvent.VC_F7

    var debug = false
    var saveImage = false
        get() = debug && field
    var similarity = false
        get() = debug && field
    var timeTaken = false
        get() = debug && field

    fun loadSetting() {
        try {
            if (!tryLock()) return

            println("설정을 불러오는 중...")
            display = 1
            pressingTimes = 0
            inputDelays = 0
            stableDelays = 20
            tolerance = 30
            threshold = 0.8
            exit = NativeKeyEvent.VC_F4
            reload = NativeKeyEvent.VC_F5
            start = NativeKeyEvent.VC_F6
            test = NativeKeyEvent.VC_F7
            debug = false
            saveImage = false
            similarity = false
            timeTaken = false

            val file = File(root, "setting.ini")
            loadFile(file)

            file.forEachLine {
                try {
                    val line = it.substringBefore("#").substringBefore(";").trim()
                    if (line.isBlank()) return@forEachLine

                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) {
                        val key = parts[0].trim()
                        val value = parts[1].trim()

                        try {
                            when (key) {
                                "display" -> display = value.toInt()
                                "pressingTimes" -> pressingTimes = value.toLong()
                                "inputDelays" -> inputDelays = value.toLong()
                                "stableDelays" -> stableDelays = value.toLong()
                                "tolerance" -> tolerance = value.toInt()
                                "threshold" -> threshold = value.toDouble()
                                "exit" -> exit = getKeyCode(value)
                                "reload" -> reload = getKeyCode(value)
                                "start" -> start = getKeyCode(value)
                                "test" -> test = getKeyCode(value)
                                "debug" -> debug = value.toBoolean()
                                "saveImage" -> saveImage = value.toBoolean()
                                "similarity" -> similarity = value.toBoolean()
                                "timeTaken" -> timeTaken = value.toBoolean()
                            }
                        } catch (_: Exception) {
                            printErr("알 수 없는 설정 값: $key = $value (기본값으로 설정됩니다.)")
                        }
                    }
                } catch (_: Exception) {}
            }

            printDebug("display: $display")
            printDebug("pressingTimes: $pressingTimes")
            printDebug("inputDelays: $inputDelays")
            printDebug("stableDelays: $stableDelays")
            printDebug("tolerance: $tolerance")
            printDebug("threshold: $threshold")
            printDebug()
            printDebug("exit: ${exit.getHotKeyText()}")
            printDebug("reload: ${reload.getHotKeyText()}")
            printDebug("start: ${start.getHotKeyText()}")
            printDebug("test: ${test.getHotKeyText()}")
            printDebug()
            printDebug("debug: $debug")
            printDebug("saveImage: $saveImage")
            printDebug("similarity: $similarity")
            printDebug("timeTaken: $timeTaken")

            actionForDepend()
            println("설정을 불러왔습니다.")
            println()
        } finally {
            lock.set(false)
        }
    }

    private fun actionForDepend() {
        Capture.initOrReload()
        Feature.initOrReload()
    }

    private fun loadFile(file: File) {
        if (!file.exists()) {
            createFile(file)
            return
        }

        for (line in file.readLines()) {
            if (!line.trim().startsWith("version")) continue
            val version = line.substringBefore(";").substringBefore("#").substringAfter("=").trim().toIntOrNull() ?: 0
            if (version != VERSION) {
                println("설정 파일 버전이 맞지 않아 재생성합니다. 기존 설정은 백업되며 모든 설정이 초기화됩니다.")
                Files.move(file.toPath(), File(root, "setting_backup.ini").toPath(), StandardCopyOption.REPLACE_EXISTING)
                createFile(file)
                return
            }
        }
    }

    private fun createFile(file: File) {
        file.createNewFile()
        file.writeText("""
                # # 또는 ; 뒤에 오는 내용은 주석으로 처리되며 공백은 무시됩니다.
                # true는 켜짐, false는 꺼짐을 의미합니다.
                
                # 수정 금지
                [doNotEdit]
                version = 2                ; 버전이 다르면 설정 파일을 재생성합니다.
                
                [general]
                display = 1                ; 감지할 모니터
                pressingTimes = 0          ; 키를 누르고 있는 시간 (밀리초)
                inputDelays = 0            ; 키 입력 간 지연 시간 (밀리초)
                stableDelays = 20           ; 입력 최소 지연 시간 (밀리초). pressingTimes와 inputDelays가 최소 이 시간동안 추가 지연을 가집니다.
                tolerance = 30             ; 이미지의 픽셀 비교 허용 오차 (0~255)
                threshold = 0.8            ; 이미지 유사도 임계값 (0: 0%, 1: 100%)

                # 키의 이름은 다음 사이트에서 'VC_' 뒤의 이름을 "있는 그대로" 사용합니다.
                # https://javadoc.io/static/com.1stleg/jnativehook/2.1.0/org/jnativehook/keyboard/NativeKeyEvent.html
                [hotkeys]
                exit = F4                  ; 매크로 종료
                reload = F5                ; 설정 다시 불러오기
                start = F6                 ; 매크로 시작
                test = F7                  ; 테스트
                
                [debug]
                debug = false              ; 여러 디버그 메시지를 출력합니다. false인 경우 아래 설정은 무시됩니다.
                saveImage = false          ; 감지한 이미지 저장
                similarity = false         ; 유사도 표시
                timeTaken = false          ; 소요 시간 표시
            """.trimIndent())
    }
}

private fun getKeyCode(name: String): Int {
    val fieldName = "VC_" + name.uppercase()
    return try {
        val field = NativeKeyEvent::class.java.getField(fieldName)
        field.getInt(null)
    } catch (_: Exception) {
        printErr("알 수 없는 키: $name")
        0
    }
}
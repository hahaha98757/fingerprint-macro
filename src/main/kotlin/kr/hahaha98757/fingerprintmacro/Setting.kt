package kr.hahaha98757.fingerprintmacro

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import kr.hahaha98757.fingerprintmacro.features.Feature
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object Setting {
    const val VERSION = 1

    var display = 1
    var pressingTimes = 20L
    var inputDelays = 20L
    var tolerance = 30
    var threshold = 0.8

    var width = 1920
    var height = 1080

    var exit = NativeKeyEvent.VC_F4
    var reload = NativeKeyEvent.VC_F5
    var start = NativeKeyEvent.VC_F6
    var test = NativeKeyEvent.VC_F7

    var debug = false
    var saveImage = false
        get() = debug && field
    var similarity = false
        get() = debug && field

    fun loadSetting() {
        if (!tryLock()) return

        try {
            println("설정을 불러오는 중...")
            display = 1
            pressingTimes = 20
            inputDelays = 20
            tolerance = 30
            threshold = 0.8
            width = 1920
            height = 1080
            exit = NativeKeyEvent.VC_F4
            reload = NativeKeyEvent.VC_F5
            start = NativeKeyEvent.VC_F6
            test = NativeKeyEvent.VC_F7
            debug = false
            saveImage = false
            similarity = false

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

                        when (key) {
                            "display" -> display = value.toInt()
                            "pressingTimes" -> pressingTimes = value.toLong()
                            "inputDelays" -> inputDelays = value.toLong()
                            "tolerance" -> tolerance = value.toInt()
                            "threshold" -> threshold = value.toDouble()
                            "width" -> width = value.toInt()
                            "height" -> height = value.toInt()
                            "exit" -> exit = getKeyCode(value)
                            "reload" -> reload = getKeyCode(value)
                            "start" -> start = getKeyCode(value)
                            "test" -> test = getKeyCode(value)
                            "debug" -> debug = value.toBoolean()
                            "saveImage" -> saveImage = value.toBoolean()
                            "similarity" -> similarity = value.toBoolean()
                        }
                    }
                } catch (_: Exception) {}
            }

            println("display: $display")
            println("pressingTimes: $pressingTimes")
            println("inputDelays: $inputDelays")
            println("tolerance: $tolerance")
            println("threshold: $threshold")
            println()
            println("exit: ${exit.getHotKeyText()}")
            println("reload: ${reload.getHotKeyText()}")
            println("start: ${start.getHotKeyText()}")
            println("test: ${test.getHotKeyText()}")
            println("debug: $debug")
            if (debug) {
                println("saveImage: $saveImage")
                println("similarity: $similarity")
            }
            println("설정된 해상도: ${width}x$height")
            println("설정을 불러왔습니다.")

            actionForDepend()
        } finally {
            lock.set(false)
        }
    }

    private fun actionForDepend() {
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
                
                # 수정 금지
                [doNotEdit]
                version = 1            ; 버전이 다르면 설정 파일을 재생성합니다.
                
                [general]
                display = 1            ; 감지할 모니터
                pressingTimes = 10     ; 키를 누르고 있는 시간 (밀리초)
                inputDelays = 10       ; 키 입력 간 지연 시간 (밀리초)
                tolerance = 30         ; 이미지의 픽셀 비교 허용 오차 (0~255)
                threshold = 0.8        ; 이미지 유사도 임계값 (0: 0%, 1: 100%)
                
                # 해상도 예시: 1920x1080(FHD), 2560x1440(QHD), 3840x2160(UHD)
                # FHD에서 가장 정확하게 동작합니다.
                [layout]
                width = 1920
                height = 1080

                # 키의 이름은 "https://javadoc.io/static/com.1stleg/jnativehook/2.1.0/org/jnativehook/keyboard/NativeKeyEvent.html"에서 'VC_' 뒤의 이름을 "있는 그대로" 사용합니다.
                [hotkeys]
                exit = F4              ; 매크로 종료
                reload = F5            ; 설정 다시 불러오기
                start = F6             ; 매크로 시작
                test = F7              ; 테스트
                
                [debug]
                debug = false          ; false인 경우 아래 설정은 무시됩니다.
                saveImage = false      ; 감지한 이미지 저장
                similarity = false     ; 유사도 표시
            """.trimIndent())
    }
}

private fun getKeyCode(name: String): Int {
    val fieldName = "VC_" + name.uppercase()
    return try {
        val field = NativeKeyEvent::class.java.getField(fieldName)
        field.getInt(null)
    } catch (_: Exception) {
        println("알 수 없는 키: $name")
        0
    }
}
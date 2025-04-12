package kr.hahaha98757.fingerprintmacro

fun cls() = repeat(50) { println() }

fun help() = println(
    """
        CLS         출력된 텍스트를 위로 올립니다.
        DEBUG       디버그 모드를 키거나 끕니다.
        EXIT        프로그램을 종료합니다.
        HELP        사용 가능한 명령어 목록을 출력합니다.
    """.trimIndent()
)